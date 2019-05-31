package d2d.testing.net.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import d2d.testing.utils.IOUtils;

public class DataPacketBuilder {
    public static DataPacket buildMsgPacket(String msg){
        DataPacket packet = new DataPacket();
        packet.setType(DataPacket.TYPE_MSG);

        try {
            //CREATE THE MSG WITH JUST THE MSG
            packet.createPacket(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            packet = null;
        }

        return packet;
    }

    public static DataPacket buildStreamNotifier(boolean on_off, String ip, String name){
        DataPacket packet = new DataPacket();
        if (on_off)
            packet.setType(DataPacket.STREAM_ON);
        else packet.setType(DataPacket.STREAM_OFF);

        try {
            //CREATE THE MSG WITH JUST THE MSG
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            output.write(IOUtils.intToByteArray(ip.length()));
            output.write(ip.getBytes());

            output.write(IOUtils.intToByteArray(name.length()));
            output.write(name.getBytes());

            packet.createPacket(output.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            packet = null;
        }

        return packet;
    }

    public static DataPacket buildFilePacket(byte[] file, String fileName){
        DataPacket packet = new DataPacket();
        packet.setType(DataPacket.TYPE_FILE);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            //LENGTH + FILENAME
            output.write(IOUtils.intToByteArray(fileName.length()));
            output.write(fileName.getBytes());
            //FILE
            output.write(file);
            packet.createPacket(output.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return packet;
    }
}
