package d2d.testing.net.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import d2d.testing.net.helpers.IOUtils;

public class FilePacket extends DataPacket {

    public FilePacket(byte[] file, String fileName){
        this.setType(TYPE_FILE);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            //LENGTH + FILENAME
            output.write(IOUtils.intToByteArray(fileName.length()));
            output.write(fileName.getBytes());
            //FILE
            output.write(file);

            this.createPacket(output.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
