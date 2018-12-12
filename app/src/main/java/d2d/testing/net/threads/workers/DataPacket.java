package d2d.testing.net.threads.workers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DataPacket {
    public static final int LENGTH_HEADER = 4;
    public static final int TYPE_POSITION = 4;
    public static final byte TYPE_MSG = 0x15;
    public static final byte TYPE_IMAGE = 0x16;
    public static final byte TYPE_FILE = 0x17;

    //private byte[] data;
    private ByteArrayOutputStream data;
    private boolean isComplete;

    DataPacket(){
        this.data = new ByteArrayOutputStream();
        this.isComplete = true;
    }

    public void setDataType(String str){
        switch (str){
            case "mensaje":
                //TODO hace cabecera
                data.write(TYPE_MSG);
                break;
            case "imagen":
                data.write(TYPE_IMAGE);
                break;
            case "fichero":
                data.write(TYPE_FILE);
                break;
            default:
                //ERROR NO HAY TIPO DE MENSAJE!!
        }
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public boolean getComplete(){
        return this.isComplete;
    }

    public void addData(byte[] mDdata){
        try {
            this.data.write(mDdata);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getData() {
       return data.toByteArray();
    }
}
