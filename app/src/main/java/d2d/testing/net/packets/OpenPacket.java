package d2d.testing.net.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class OpenPacket extends DataPacket {
    protected ByteArrayOutputStream mDataStream;

    public OpenPacket(){
        super();
        mDataStream = new ByteArrayOutputStream();
    }

    public OpenPacket(byte[] data){
        super();
        mDataStream = new ByteArrayOutputStream();
        this.addData(data);
    }

    public int getLength() {
        return mDataStream.size();
    }

    public int getRemainingLength() { return this.getFullLength() - mDataStream.size(); }

    public void addData(byte[] data){
        try {
            mDataStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parseHeader() {
        setBodyLength(Arrays.copyOfRange(mData, BODY_LEN_POSITION, BODY_LEN_POSITION + BODY_LEN_LENGTH));
        setType(mData[DataPacket.TYPE_POSITION]);

        setStatus(DataPacket.STATUS_HEADER);
    }

    public void parseMessage()
    {

    }
}
