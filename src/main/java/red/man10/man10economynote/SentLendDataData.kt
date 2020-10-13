package red.man10.man10economynote;

import java.util.UUID;

/**
 * Created by sho on 2017/12/17.
 */
public class SentLendDataData {

    LendData data;
    String fromName;
    UUID fromUUID;

    public SentLendDataData(LendData data, String fromName, UUID fromUUID){
        this.data = data;
        this.fromName = fromName;
        this.fromUUID = fromUUID;
    }
}
