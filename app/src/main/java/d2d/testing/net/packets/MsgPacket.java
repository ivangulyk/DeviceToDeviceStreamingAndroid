package d2d.testing.net.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class MsgPacket extends DataPacket {

    public MsgPacket(String msg){
        this.setType(TYPE_MSG);

        try {
            //CREATE THE MSG WITH JUST THE MSG
            this.createPacket(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMsg()
    {
        return new String(getBodyData());
    }
}
