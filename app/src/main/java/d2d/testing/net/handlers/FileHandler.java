package d2d.testing.net.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import d2d.testing.utils.Logger;
import d2d.testing.utils.IOUtils;
import d2d.testing.net.packets.DataPacket;

public class FileHandler {

     public static void handle(DataPacket packet) {
         byte[] data = packet.getBodyData();
         int fileNameLength = IOUtils.fromByteArray(Arrays.copyOfRange(data,0,4));
         String fileName = new String(Arrays.copyOfRange(data,4,4+fileNameLength));

         final File f = IOUtils.getOutputMediaFile(fileName);

        try {
            if(f.createNewFile()) {
                final FileOutputStream fileOutputStream = new FileOutputStream(f);
                Logger.d("FileHandler: copying to file " + f.toString() + " - " + data.length + " bytes");
                //fileOutputStream.write(Arrays.copyOfRange(data,4+fileNameLength,data.length), 0 ,data.length-4-fileNameLength);
                fileOutputStream.write(data,4+fileNameLength,data.length-4-fileNameLength);
                fileOutputStream.close();
                Logger.d("FileHandler: file created successfully");
            } else {
                Logger.e("FileHandler: No se ha podido crear el archivo...");
            }
        }
        catch (FileNotFoundException e) {
            Logger.d(e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            Logger.d(e.toString());
            e.printStackTrace();
        }
    }

}
