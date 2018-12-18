package d2d.testing.net.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import d2d.testing.helpers.Logger;

public class DataFormatter {
    public static final byte[] START_PACKET_CONST = {0x11,0x17,0x16,0x15};
    public static final byte[] END_PREFIX_CONST = {0x11,0x15,0x16,0x17};

    public static final int TYPE_POSITION       = 0;
    public static final int LEN_POSITION_START  = 1;
    public static final int LEN_POSITION_END    = 5;
    public static final int LENGTH_HEADER       = 5;

    public static final byte[] TYPE_LIST = {0x15,0x16,0x17};
    public static final byte TYPE_MSG = 0x15;
    public static final byte TYPE_FILE = 0x17;

    public static byte[] createMessagePacket(String message){
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] data = message.getBytes();
        try {
            //output.write(START_PACKET_CONST);   //ALWAYS PREFIX
            output.write(TYPE_MSG);             //SET TYPE
            output.write(intToByte(data.length));
            //TODO mandar longitud de mensaje + hash?
            //TODO: HASH Y CHECKING AL RECIBIR, TIMESTAMP, QUIEN LO HA ENVIADO, NETWORK JUMPTRACE PARA SABER POR QUIEN HA PASADO?

            output.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return output.toByteArray();
    }

    public static byte[] createFilePacket(byte[] file){
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            //output.write(START_PACKET_CONST);     //ALWAYS PREFIX
            output.write(TYPE_FILE);              //SET TYPE
            //output.write(intToByte(0));     //SET FILENAME
            output.write(intToByte(file.length));


            output.write(file.length);            //SET TYPE

            //TODO mandar longitud de mensaje + hash?
            //TODO: HASH Y CHECKING AL RECIBIR, TIMESTAMP, QUIEN LO HA ENVIADO, NETWORK JUMPTRACE PARA SABER POR QUIEN HA PASADO?

            output.write(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return output.toByteArray();
    }

    public static List<DataPacket> getPackets(DataPacket openPacket, byte[] data) {
        if(openPacket == null)
            openPacket = new DataPacket();

        List<DataPacket> out = new LinkedList<>();
        switch (openPacket.getStatus())
        {
            case DataPacket.STATUS_UNCOMPLETED_HEADER:
                if (data.length < LENGTH_HEADER) {
                    openPacket.addData(data);
                    out.add(openPacket);
                    return out; //!error no tenemos ni toda la cabecera...
                }

                //todo SUBTIPO DE STATUS PARA DIFERENTES PARTES DE LA CABECERA? LONGITUD, TIPO, ...
                //todo VAMOS A TENER QUE HACER ALGO MAS AHI SEPARADO? CABECERAS PROPIAS... ETC

                //Tenemos la cabecera establecemos los parametros del paquete y hacemos comprobaciones..
                if(!contains(TYPE_LIST,data[TYPE_POSITION])){
                    Logger.d("DataFormatter: Error no hay tipo de mensaje");
                    return out; //retronamos vacios
                }

                openPacket.setBodyLength(byteToInt(Arrays.copyOfRange(data, LEN_POSITION_START, LEN_POSITION_END)));
                openPacket.setType(data[TYPE_POSITION]);
                openPacket.setStatus(DataPacket.STATUS_UNCOMPLETED_BODY);

            case DataPacket.STATUS_UNCOMPLETED_BODY:
                if(data.length < openPacket.getFullRemainingLength()) {
                    //todo futuro: archivos muy grandes se nos peta la memoria o como va la cosa?
                    openPacket.addData(data);
                    out.add(openPacket);
                    return out;
                } else if(data.length > openPacket.getFullRemainingLength()) {
                    int lenRead = openPacket.getFullRemainingLength();
                    openPacket.addData(Arrays.copyOfRange(data,0,lenRead));
                    openPacket.setStatus(DataPacket.STATUS_COMPLETED);
                    out.add(openPacket);
                    //TENEMOS EL MENSAJE COMPLETO Y TODAVIA MAS DATOS
                    data = Arrays.copyOfRange(data,lenRead,data.length);
                    out.addAll(getPackets(null,data));
                    //DEVOLVEMOS TOD
                    return out;
                } else {// ASUMED (bodyLength == packet.getRemainingLength())
                    openPacket.addData(data);
                    openPacket.setStatus(DataPacket.STATUS_COMPLETED);
                    out.add(openPacket);

                    return out;
                }

        }
        return out;
    }

    public static byte[] intToByte(int num){
        return ByteBuffer.allocate(4).putInt(num).array();
    }

    public static int byteToInt(byte[] num){
        return ByteBuffer.wrap(num).getInt();
    }

    public static boolean contains(final byte[] array, final byte search) {

        boolean result = false;

        for(byte b : array){
            if(b == search){
                result = true;
                break;
            }
        }

        return result;
    }
}
