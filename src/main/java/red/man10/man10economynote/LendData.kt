package red.man10.man10economynote;

import java.util.UUID;

/**
 * Created by sho on 2017/12/17.
 */
public class LendData {
    String name;
    UUID uuid;
    long baseValue;
    long finalValue;
    int usableDays;
    long finalValueLender;
    double interest;
    long valueLeft;
    int id;
    long creationTime;

    public LendData(String name,UUID uuid,long baseValue,long finalValue,int usableDays,long finalValueLender,double interest,long valueLeft, int id,long creationTime){
        this.name = name;
        this.creationTime = creationTime;
        this.interest = interest;
        this.id = id;
        this.valueLeft = valueLeft;
        this.uuid = uuid;
        this.finalValueLender = finalValueLender;
        this.baseValue = baseValue;
        this.finalValue = finalValue;
        this.usableDays = usableDays;
    }

    public boolean hasNull(){
        try{
            if(name == null || uuid == null){
                return true;
            }
            return false;
        }catch (NullPointerException e){
            return true;
        }
    }
}
