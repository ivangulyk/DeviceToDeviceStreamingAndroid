package d2d.testing.net.threads.workers;

public class DataPackage {
    public static final int LENGTH_HEADER = 4;
    public static final int TYPE_POSITION = 4;
    public static final byte TYPE_MSG = 0x15;
    public static final byte TYPE_IMAGE = 0x16;
    public static final byte TYPE_FILE = 0x17;

    private byte[] data;

    DataPackage(byte[] data){
        this.data = data;
    }

    public void setDataType(String str){
        switch (str){
            case "mensaje":
        }
    }

    public byte[] getData(){
        return this.data;
    }
}
