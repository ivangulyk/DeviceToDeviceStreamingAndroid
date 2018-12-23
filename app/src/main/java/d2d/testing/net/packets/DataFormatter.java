package d2d.testing.net.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import d2d.testing.helpers.Logger;
import d2d.testing.net.helpers.IOUtils;

public class DataFormatter {


    /*public static byte[] createMessagePacket(String message){
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] data = message.getBytes();
        try {
            //output.write(START_PACKET_CONST);     //ALWAYS PREFIX
            output.write(TYPE_MSG);                 //SET TYPE
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


           // output.write(file.length);            //SET TYPE

            //TODO mandar longitud de mensaje + hash?
            //TODO: HASH Y CHECKING AL RECIBIR, TIMESTAMP, QUIEN LO HA ENVIADO, NETWORK JUMPTRACE PARA SABER POR QUIEN HA PASADO?

            output.write(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return output.toByteArray();
    }*/

    public static List<DataPacket> getPackets(OpenPacket openPacket, byte[] data) {
        if(openPacket == null)
            openPacket = new OpenPacket();

        List<DataPacket> out = new LinkedList<>();
        switch (openPacket.getStatus())
        {
            case DataPacket.STATUS_NOTHING:
                if (openPacket.getLength() + data.length < DataPacket.HEADER_LENGTH) {
                    openPacket.addData(data);
                    out.add(openPacket);
                    return out; //!error no tenemos ni toda la cabecera...
                }

                //todo SUBTIPO DE STATUS PARA DIFERENTES PARTES DE LA CABECERA? LONGITUD, TIPO, ...
                //todo VAMOS A TENER QUE HACER ALGO MAS AHI SEPARADO? CABECERAS PROPIAS... ETC

                //Tenemos la cabecera establecemos los parametros del paquete y hacemos comprobaciones..
                if(!IOUtils.contains(DataPacket.TYPE_LIST,data[DataPacket.TYPE_POSITION])){
                    Logger.d("DataFormatter: Error no hay tipo de mensaje");
                    return out; //retronamos vacios
                }

            case DataPacket.STATUS_HEADER:
                if(data.length < openPacket.getRemainingLength()) {
                    //todo futuro: archivos muy grandes se nos peta la memoria o como va la cosa?
                    openPacket.addData(data);
                    out.add(openPacket);
                    return out;
                } else if(data.length > openPacket.getRemainingLength()) {
                    int lenRead = openPacket.getRemainingLength();
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
}
