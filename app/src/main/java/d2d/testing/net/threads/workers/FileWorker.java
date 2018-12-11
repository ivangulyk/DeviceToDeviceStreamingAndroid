package d2d.testing.net.threads.workers;

import android.content.ContentResolver;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import d2d.testing.helpers.Logger;
import d2d.testing.net.WifiP2pHandler;

public class FileWorker implements Runnable{

    private Uri mFileUri;
    private WifiP2pHandler mHandler;

    public FileWorker(Uri uri, WifiP2pHandler handler) {
        this.mFileUri = uri;
        this.mHandler = handler;
    }

    @Override
    public void run() {
        try {
            Logger.d( "File worker: Starting send file thread with URI: " + mFileUri.toString());

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ContentResolver cr = mHandler.getActivity().getContentResolver();
            InputStream is = null;

            is = cr.openInputStream(Uri.parse(mFileUri.toString()));

            Logger.d("File worker: Streams opened we are going to construct the msg...");


            copyFile(is, output);
            Logger.d("File worker: Data written");
        } catch (FileNotFoundException e) {
            Logger.e("File worker: FileNotFoundException... something wrong");
            Logger.e(e.getMessage());
        }
    }

    private boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Logger.d(e.toString());
            return false;
        }
        return true;
    }
}
