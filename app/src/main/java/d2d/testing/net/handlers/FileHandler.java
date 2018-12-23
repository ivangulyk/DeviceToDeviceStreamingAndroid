package d2d.testing.net.handlers;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import d2d.testing.helpers.Logger;
import d2d.testing.net.helpers.IOUtils;
import d2d.testing.net.packets.DataPacket;
import d2d.testing.net.packets.FilePacket;

public class FileHandler {
     public static void handle(DataPacket packet) {
            final FilePacket packetFile = (FilePacket) packet;

            //final File f = new File(Environment.getExternalStorageDirectory() + "/" + packetFile.getFileName());
        final File f = IOUtils.getOutputMediaFile(packetFile.getFileName());

        try {
            if(f.createNewFile()) {
                final FileOutputStream fileOutputStream = new FileOutputStream(f);
                Logger.d("FileHandler: copying file... " + f.toString());
                fileOutputStream.write(packet.getBodyData(), 0, packet.getBodyLength());
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
