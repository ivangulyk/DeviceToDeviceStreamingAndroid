package d2d.testing.net.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import d2d.testing.helpers.Logger;

public class DataFormat {
    public static final byte[] START_PACKET_CONST = {0x11,0x17,0x16,0x15};
    public static final byte[] END_PREFIX_CONST = {0x11,0x15,0x16,0x17};

    public static final int LENGTH_HEADER = 5;

    public static final int TYPE_POSITION = 4;

    public static final byte[] TYPE_LIST = {0x15,0x16,0x17};
    public static final byte TYPE_MSG = 0x15;
    public static final byte TYPE_IMAGE = 0x16;
    public static final byte TYPE_FILE = 0x17;

    public static byte[] createMessagePacket(String message){
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] data = message.getBytes();
        try {
            //output.write(START_PACKET_CONST);     //ALWAYS PREFIX
            output.write(TYPE_MSG);         //SET TYPE

            //TODO mandar longitud de mensaje + hash?
            //TODO: HASH Y CHECKING AL RECIBIR, TIMESTAMP, QUIEN LO HA ENVIADO, NETWORK JUMPTRACE PARA SABER POR QUIEN HA PASADO?
            output.write(intToByte(data.length));
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
            output.write(TYPE_FILE);                //SET TYPE

            //TODO mandar longitud de mensaje + hash?
            //TODO: HASH Y CHECKING AL RECIBIR, TIMESTAMP, QUIEN LO HA ENVIADO, NETWORK JUMPTRACE PARA SABER POR QUIEN HA PASADO?
            output.write(intToByte(file.length));
            output.write(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return output.toByteArray();
    }

    public static List<DataPacket> getPackets(byte[] data)
    {
        DataPacket packet = new DataPacket();
        List<DataPacket> out = new LinkedList<>();

        if (data.length < LENGTH_HEADER)
        {
            //TODO AÑADIR ALGO EN PACKET PARA SABER EN QUE ESTADO DE RELLENAR ESTA, HEADER, BODY, ETC
            packet.addData(data);
            out.add(packet);
            return out; //!error no tenemos ni toda la cabecera...
        }

        //Tenemos la cabecera establecemos los parametros del paquete
        //Y hacemos comprobaciones, tipo, hash,parametros,de cabecera,timestamp
        if(!Arrays.asList(TYPE_LIST).contains(data[TYPE_POSITION])){
            Logger.d("DataFormatter: Error no hay tipo de mensaje");
            return out; //retronamos vacios
        }

        //TODO AÑADIR ALGO EN PACKET PARA SABER EN QUE ESTADO DE RELLENAR ESTA, HEADER, BODY, ETC
        packet.setBodyLength(byteToInt(Arrays.copyOfRange(data, 0, 3)));
        packet.setType(data[TYPE_POSITION]);
        packet.setStatus(DataPacket.STATUS_BODY);

        //todo VAMOS A TENER QUE HACER ALGO MAS AHI SEPARADO? CABECERAS PROPIAS... ETC

        if(data.length < packet.getFullLength())
        {
            //DEVOLVEMOS UN PAQUETE CON ALGUN FLAG DE INCOMPLETO
            //todo futuro: archivos muy grandes se nos peta la memoria o como va la cosa?
            packet.addData(data);
            out.add(packet);
            return out;
        }
        else if(data.length > packet.getFullLength())
        {
            //TENEMOS EL MENSAJE COMPLETO
            packet.addData(Arrays.copyOfRange(data,0,packet.getFullLength()));
            packet.setStatus(DataPacket.STATUS_COMPLETED);
            out.add(packet);
            //Y TODAVIA MAS DATOS
            data = Arrays.copyOfRange(data,packet.getFullLength(),data.length);
            out.addAll(getPackets(null,data));
            //DEVOLVEMOS TOD
            return out;
        }
        else //if(bodyLength == packet.getRemainingLength()) asumed
        {
            packet.addData(data);
            packet.setStatus(DataPacket.STATUS_COMPLETED);

            return out;
        }
    }


    public static List<DataPacket> getPackets(DataPacket openPacket, byte[] data)
    {
        if(openPacket == null)
            openPacket = new DataPacket();

        List<DataPacket> out = new LinkedList<>();
        switch (openPacket.getStatus())
        {
            case DataPacket.STATUS_HEADER:
                if (data.length < LENGTH_HEADER) {
                    openPacket.addData(data);
                    out.add(openPacket);
                    return out; //!error no tenemos ni toda la cabecera...
                }

                //todo SUBTIPO DE STATUS PARA DIFERENTES PARTES DE LA CABECERA? LONGITUD, TIPO, ...
                //todo VAMOS A TENER QUE HACER ALGO MAS AHI SEPARADO? CABECERAS PROPIAS... ETC

                //Tenemos la cabecera establecemos los parametros del paquete y hacemos comprobaciones..
                if(!Arrays.asList(TYPE_LIST).contains(data[TYPE_POSITION])){
                    Logger.d("DataFormatter: Error no hay tipo de mensaje");
                    return out; //retronamos vacios
                }

                openPacket.setBodyLength(byteToInt(Arrays.copyOfRange(data, 0, 3)));
                openPacket.setType(data[TYPE_POSITION]);
                openPacket.setStatus(DataPacket.STATUS_BODY);

            case DataPacket.STATUS_BODY:
                if(data.length < openPacket.getFullLength())
                {
                    //DEVOLVEMOS UN PAQUETE CON ALGUN FLAG DE INCOMPLETO
                    //todo futuro: archivos muy grandes se nos peta la memoria o como va la cosa?
                    openPacket.addData(data);
                    out.add(openPacket);
                    return out;
                } else if(data.length > openPacket.getFullLength()) {
                    //TENEMOS EL MENSAJE COMPLETO
                    openPacket.addData(Arrays.copyOfRange(data,0,openPacket.getFullLength()));
                    openPacket.setStatus(DataPacket.STATUS_COMPLETED);
                    out.add(openPacket);
                    //Y TODAVIA MAS DATOS
                    data = Arrays.copyOfRange(data,openPacket.getFullLength(),data.length);
                    out.addAll(getPackets(null,data));
                    //DEVOLVEMOS TOD
                    return out;
                } else {// ASUMED (bodyLength == packet.getRemainingLength())
                    openPacket.addData(data);
                    openPacket.setStatus(DataPacket.STATUS_COMPLETED);

                    return out;
                }

        }
        return out;
    }

    public static List<DataPacket> resumePacket(DataPacket packet, byte[] data)
    {
        List<DataPacket> out = new LinkedList<>();
        out.add(packet);
        //TENEMOS UN MENSAJE INCOMPLETO Y SEGUIMOS RELLENANDOLO
        switch (packet.getStatus())
        {
            case DataPacket.STATUS_HEADER:
                //SUBTIPO DE STATUS PARA DIFERENTES PARTES DE LA CABECERA? LONGITUD, TIPO, ...

            case DataPacket.STATUS_BODY:
                break;

            case DataPacket.STATUS_POST_BODY:
                break;
        }
        return out;
    }

    public static byte[] intToByte(int num)
    {
        //ByteBuffer dbuf = ByteBuffer.allocate(2);
        //dbuf.putInt(num);
        //return dbuf.array();
        return ByteBuffer.allocate(2).putInt(num).array();
    }

    public static int byteToInt(byte[] num)
    {
        //ByteBuffer dbuf = ByteBuffer.wrap(num);
        //dbuf.putInt(num);
        //return dbuf.array();
        return ByteBuffer.wrap(num).getInt();
    }
}
