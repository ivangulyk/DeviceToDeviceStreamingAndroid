package d2d.testing.net.handlers;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import d2d.testing.helpers.Logger;
import d2d.testing.net.packets.DataPacket;

public class FileHandler {
     public static void handle(DataPacket packet) {
            //final FilePacket packetFile = (FilePacket) packet;
            //TODO SECURITY CHECK FILENAME?
            //final File f = new File(Environment.getExternalStorageDirectory() + "/" + packetFile.getFileName());
        final File f = new File(Environment.getExternalStorageDirectory() + "/"+"/d2d-network-" + System.currentTimeMillis() + ".jpg");
        final File dirs = new File(f.getParent());


         if (!dirs.exists() && !dirs.mkdirs()) {
                Logger.e("FileHandler: No se encuentran ni se han podido crear los directorios");
                return;
            }

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
