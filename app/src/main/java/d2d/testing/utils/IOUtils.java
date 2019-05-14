package d2d.testing.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class IOUtils {

    /**
     * https://stackoverflow.com/questions/7619058/convert-a-byte-array-to-integer-in-java-and-vice-versa
     */
    public static byte[] intToByteArray(int value) {
        return new byte[] {(byte)(value >> 24),(byte)(value >> 16), (byte)(value >> 8), (byte)value};
    }

    public static int fromByteArray(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    public static boolean contains(final byte[] array, final byte search) {
        boolean result = false;

        for(byte b : array){
            if(b == search){
                result = true;
                break;
            }
        }

        return result;
    }

    public static byte[] copyMax(byte[] byteArr, int offset, int max)
    {
        if(byteArr.length > offset + max){
            return Arrays.copyOfRange(byteArr, offset, offset + max);
        } else {
            return Arrays.copyOfRange(byteArr, offset, byteArr.length);
        }
    }

    /** Create a File for saving an image or video */
    public static File getOutputMediaFile(String fileName){
        //TODO SECURITY CHECK FILENAME?

        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "D2DNetwork");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        if (! mediaStorageDir.exists() && ! mediaStorageDir.mkdirs()){
            Log.d("D2DNetwork", "failed to create ... PERMISSIONS?");
            return null;
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator + timeStamp + "_" + fileName);
    }
}
