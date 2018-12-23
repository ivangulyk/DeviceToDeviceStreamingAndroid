package d2d.testing.net.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import d2d.testing.net.helpers.IOUtils;

public abstract class DataPacket {
    public static final byte[] START_PACKET = {0x11,0x17,0x16,0x15};
    public static final byte[] END_PACKET   = {0x11,0x15,0x16,0x17};

    public static final int TYPE_POSITION       = 0;
    public static final int BODY_LEN_POSITION   = 1;
    public static final int BODY_LEN_LENGTH     = 4;
    public static final int HEADER_LENGTH       = 5;


    public static final byte    TYPE_OPEN = -1;
    public static final byte    TYPE_MSG  = 0x15;
    public static final byte    TYPE_FILE = 0x17;
    public static final byte[]  TYPE_LIST = {0x15,0x17};

    public static final int STATUS_NOTHING   = 0;
    public static final int STATUS_HEADER    = 1;
    public static final int STATUS_BODY      = 2; //POST BODY DATA?
    public static final int STATUS_COMPLETED = 3;

    //private byte[] data;

    private byte mType;
    private int mStatus;
    private int mBodyLength;
    protected byte[] mData;


    DataPacket(){
        mType = TYPE_OPEN;
        mStatus = STATUS_NOTHING;
        mData = null;
        mBodyLength = 0;
    }

    protected void createPacket(byte[] data) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        //CABECERA
        output.write(START_PACKET);
        output.write(mType);
        output.write(IOUtils.intToByteArray(data.length));
        //ESCRIBIMOS LOS DATOS
        output.write(data);
        mData = output.toByteArray();
    }

    //protected void parsePacket();

    public void setType(byte type){
        mType = type;
    }
    public void setStatus(int status){mStatus = status;}
    public void setBodyLength(int length) {
        mBodyLength = length;
    }
    public void setBodyLength(byte[] length) {
        mBodyLength = IOUtils.fromByteArray(length);
    }

    public byte getType(){
        return mType;
    }
    public int getBodyLength() {
        return mBodyLength;
    }
    public int getFullLength() {
        return mBodyLength + HEADER_LENGTH;
    }
    public int getStatus(){return mStatus;}
    public boolean isCompleted(){
        return this.getStatus() == STATUS_COMPLETED;
    }

    public byte[] getData() { return mData; }
    public byte[] getBodyData() {
        return Arrays.copyOfRange(mData,HEADER_LENGTH,mData.length);
    }
}
