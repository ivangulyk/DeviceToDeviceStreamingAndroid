package d2d.testing.net.helpers;

import java.nio.ByteBuffer;

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
}
