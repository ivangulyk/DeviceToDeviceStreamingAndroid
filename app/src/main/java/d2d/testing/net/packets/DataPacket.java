package d2d.testing.net.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import d2d.testing.helpers.Logger;
import d2d.testing.net.helpers.IOUtils;

public class DataPacket {
    private static final byte[] START_PACKET = {0x11,0x17,0x16,0x15};
    private static final byte[] END_PACKET   = {0x11,0x15,0x16,0x17};


    private static final int TYPE_POSITION       = 4;
    private static final int BODY_LEN_POSITION   = 5;

    private static final int START_PACKET_LENGTH = 4;
    private static final int BODY_LEN_LENGTH     = 4;
    private static final int HEADER_LENGTH       = 9;



    public static final byte    TYPE_OPEN = -1;
    public static final byte    TYPE_MSG  = 0x15;
    public static final byte    TYPE_FILE = 0x17;
    public static final byte    TYPE_VIDEO_STREAM = 0x19;
    public static final byte[]  TYPE_LIST = {TYPE_MSG, TYPE_FILE, TYPE_VIDEO_STREAM};

    public static final int STATUS_INVALID   = -1;
    public static final int STATUS_OPEN      = 0;
    public static final int STATUS_HEADER    = 1;
    public static final int STATUS_BODY      = 2; //POST BODY DATA?
    public static final int STATUS_COMPLETED = 3;

    private byte mType;
    private int mStatus;
    private int mBodyLength;
    private byte[] mData;
    private ByteArrayOutputStream mDataStream;

    public DataPacket(){
        mType = TYPE_OPEN;
        mStatus = STATUS_OPEN;
        mBodyLength = 0;
        mData = null;
        mDataStream = new ByteArrayOutputStream();
    }

    public void setType(byte type){
        mType = type;
    }

    public void setBodyLength(int length) {
        mBodyLength = length;
    }
    private void setBodyLength(byte[] length) {
        mBodyLength = IOUtils.fromByteArray(length);
    }

    public byte getType(){
        return mType;
    }
    public int getStatus(){return mStatus;}
    public boolean isCompleted(){
        return mStatus == STATUS_COMPLETED;
    }
    public boolean isInvalid(){
        return mStatus == STATUS_INVALID;
    }

    public int getBodyLength() {
        return mBodyLength;
    }
    public int getFullLength() {
        return mBodyLength + HEADER_LENGTH;
    }
    public int getRemainingLength() { return mBodyLength + HEADER_LENGTH - mDataStream.size(); }

    public byte[] getData() { return mData; }
    public byte[] getBodyData() {
        return Arrays.copyOfRange(mData,HEADER_LENGTH,mData.length);
    }


    public void addData(byte[] data) {
        try {
            mDataStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parsePacket() {
        mData = mDataStream.toByteArray();

        if (getRemainingLength() > 0)
            return;     //la cabecera no esta llena

        if(mStatus == STATUS_OPEN) {
            parseHeader();
            return;
        }

        //ASUMIMOS QUE TENEMOS EL MENSAJE COMPLETO
        //setStatus(STATUS_COMPLETED);
        mStatus = STATUS_COMPLETED;
    }

    public void parseHeader() {
        //TODO OJO COOMPROBAR..
        //comprobamos el tipo de mensaje
        if(!Arrays.equals(Arrays.copyOfRange(mData, 0, START_PACKET_LENGTH), START_PACKET)) {
            mStatus = STATUS_INVALID;
            Logger.d("DataFormatter: No start packet prefix found!");
            return;
        }


        if(!IOUtils.contains(TYPE_LIST, mData[TYPE_POSITION])){
            mStatus = STATUS_INVALID;
            Logger.d("DataFormatter: No packet type found!");
            return;
        }

        //ASIGNAMOS
        setBodyLength(Arrays.copyOfRange(mData, BODY_LEN_POSITION, BODY_LEN_POSITION + BODY_LEN_LENGTH));
        setType(mData[TYPE_POSITION]);

        //todo mas cosas de cabecera hashes etc

        //todo SUBTIPO DE STATUS PARA PARAMETROS ADICIONALES DE CABECERA
        //todo VAMOS A TENER QUE HACER ALGO MAS AQUI SEPARADO? CABECERAS PROPIAS... ETC

        mStatus = STATUS_HEADER;
    }

    protected void createPacket(byte[] data) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        //CABECERA
        output.write(START_PACKET);
        output.write(mType);
        output.write(IOUtils.intToByteArray(data.length));

        //ESCRIBIMOS LOS DATOS
        output.write(data);

        //POST CABECERA?
        //output.write(END_PACKET);
        mData = output.toByteArray();
    }
}
