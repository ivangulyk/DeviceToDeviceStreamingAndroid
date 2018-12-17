package d2d.testing.net.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class DataPacket {

    public static final int STATUS_UNCOMPLETED_HEADER = 0;
    public static final int STATUS_UNCOMPLETED_BODY = 1;
    public static final int STATUS_UNCOMPLETED_POST_BODY = 2;
    public static final int STATUS_COMPLETED = 3;


    //private byte[] data;
    private ByteArrayOutputStream mData;
    private byte mType;
    private int mBodyLength;
    private int mStatus;

    DataPacket(){
        mData = new ByteArrayOutputStream();
        mStatus = STATUS_UNCOMPLETED_HEADER;
    }

    public void setType(byte type){
        mType = type;
    }
    public void setBodyLength(int length) {
        mBodyLength = length;
    }
    public void setStatus(int status){mStatus = status;}

    public byte getType(){
        return mType;
    }
    public int getBodyLength() {
        return mBodyLength;
    }
    public int getBodyRemainingLength() {
        return mBodyLength + DataFormatter.LENGTH_HEADER - mData.size();
    }

    public int getFullLength() {
        return mBodyLength + DataFormatter.LENGTH_HEADER;
    }
    public int getFullRemainingLength() {
        return mBodyLength + DataFormatter.LENGTH_HEADER - mData.size();
    }

    public int getStatus(){return mStatus;}
    public boolean isCompleted(){
        return mStatus == STATUS_COMPLETED;
    }


    public void addData(byte[] data){
        try {
            mData.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getData() {
       return mData.toByteArray();
    }

    //TODO DEVOLVER SOLO LOS DATOS DEL PAQUETE SIN LAS CABECERAS Y ESO
    public byte[] getBodyData() {

        return Arrays.copyOfRange(mData.toByteArray(),DataFormatter.LENGTH_HEADER,mData.size());
    }
}
