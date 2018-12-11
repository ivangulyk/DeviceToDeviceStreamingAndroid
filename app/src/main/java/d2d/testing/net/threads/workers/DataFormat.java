package d2d.testing.net.threads.workers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DataFormat {
    public static final byte[] START_PACKET_CONST = {0x11,0x17,0x16,0x15};
    public static final byte[] END_PREFIX_CONST = {0x11,0x15,0x16,0x17};
    public static final byte[] TYPE_MSG = {0x15,0x00};
    public static final byte[] TYPE_IMAGE = {0x15,0x01};
    public static final byte[] TYPE_FILE = {0x15,0x02};

    public static byte[] createMessagePacket(String message){
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            //output.write(START_PACKET_CONST);     //ALWAYS PREFIX
            output.write(TYPE_MSG);         //SET TYPE

            //TODO: HASH Y CHECKING AL RECIBIR, TIMESTAMP, QUIEN LO HA ENVIADO, NETWORK JUMPTRACE PARA SABER POR QUIEN HA PASADO?
            int len = message.getBytes().length;    //LENGTH OF REST OF THE PACKET NOT COUNTING FINAL AFFIX
            output.write(message.getBytes().length);
            output.write(message.getBytes());

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

            //TODO: HASH Y CHECKING AL RECIBIR, TIMESTAMP, QUIEN LO HA ENVIADO, NETWORK JUMPTRACE PARA SABER POR QUIEN HA PASADO?

            output.write(file.length);
            output.write(file);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return output.toByteArray();
    }
}
