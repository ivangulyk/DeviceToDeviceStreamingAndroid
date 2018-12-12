package d2d.testing.net.threads.workers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DataPacket {
    public static final int LENGTH_HEADER = 4;
    public static final int TYPE_POSITION = 4;
    public static final byte TYPE_MSG = 0x15;
    public static final byte TYPE_IMAGE = 0x16;
    public static final byte TYPE_FILE = 0x17;

    public static final int STATUS_NOTHING = 0;
    public static final int STATUS_HEADER = 1;
    public static final int STATUS_BODY = 2;

    //private byte[] data;
    private ByteArrayOutputStream mData;
    private boolean isCompleted;
    private byte mType;
    private int mLength;
    private int mStatus;

    DataPacket(){
        mData = new ByteArrayOutputStream();
        isCompleted = false;
        mStatus = STATUS_NOTHING;
    }

    public void setType(byte type){
        mType = type;
    }
    public void setLength(int length) {
        mLength = length;
    }
    public void setStatus(int status){mStatus=status;}
    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }


    public byte getType(){
        return mType;
    }
    public int getLength() {
        return mLength;
    }
    public int getRemainingLength() {
        return mLength - mData.size() + LENGTH_HEADER;
    }
    public int getStatus(){return mStatus;}
    public boolean isCompleted(){
        return this.isCompleted;
    }


    public void addData(byte[] mDdata){
        try {
            this.mData.write(mDdata);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getData() {
       return mData.toByteArray();
    }

    //TODO DEVOLVER SOLO LOS DATOS DEL PAQUETE SIN LAS CABECERAS Y ESO
    public byte[] getPacketData() {
        return mData.toByteArray();
    }
}
