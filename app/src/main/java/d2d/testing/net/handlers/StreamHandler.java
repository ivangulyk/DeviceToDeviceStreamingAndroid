package d2d.testing.net.handlers;

import android.net.Uri;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import d2d.testing.MainActivity;
import d2d.testing.helpers.Logger;
import d2d.testing.net.WifiP2pController;
import d2d.testing.net.helpers.IOUtils;
import d2d.testing.net.packets.DataPacket;

public class StreamHandler {

    private final File mFile;
    private FileOutputStream mFileOutputStream;
    private StreamHandler mInstance;
    private int cont = 0;
    private boolean running = false;

    public StreamHandler() {
        mFile = IOUtils.getOutputMediaFile("temp_video_stream.mp4");
        try {
            if(mFile.createNewFile()) {
                Logger.d("StreamHandler: Archivo y outputstream creados");
            } else {
                Logger.e("StreamHandler: No se ha podido crear el archivo...");
            }

            mFileOutputStream = new FileOutputStream(mFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     public void handle(DataPacket packet) {
         byte[] data = packet.getBodyData();

        try {
            Logger.d("FileHandler: copying " + data.length + " bytes to temp stream file");


            if(!running && cont > 15000)
            {
                running = true;
                mFileOutputStream.close();
                WifiP2pController.getInstance().getMainActivity().openMediaActivity(Uri.fromFile(mFile));
                Logger.d("FileHandler: starting activity with " + cont + " bytes");
            }
            else
            {
                mFileOutputStream.write(data, 0, data.length);
                cont += data.length;
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

    public void endStream() {
        try {
            mFileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
