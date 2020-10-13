package red.man10.man10economynote.vault;

/**
 * Created by sho on 2018/06/14.
 */
public class JPYBalanceFormat {

    long value = 0;
    public JPYBalanceFormat(Double value){
        this.value = value.longValue();
    }

    public JPYBalanceFormat(Long value){
        this.value = value;
    }

    public JPYBalanceFormat(int value){
        this.value = value;
    }

    private String complexJpyBalForm(Long val){
        if(val < 10000){
            return String.valueOf(val);
        }
        if(val < 100000000){
            long man = val/10000;
            String left = String.valueOf(val).substring(String.valueOf(val).length() - 4);
            if(Long.parseLong(left) == 0){
                return man + "万";
            }
            return man + "万" + Long.parseLong(left);
        }
        if(val < 100000000000L){
            long oku = val/100000000;
            String man = String.valueOf(val).substring(String.valueOf(val).length() - 8);
            String te = man.substring(0, 4);
            String left = String.valueOf(val).substring(String.valueOf(val).length() - 4);
            if(Long.parseLong(te)  == 0){
                if( Long.parseLong(left) == 0){
                    return oku + "億";
                }else{
                    return oku + "億"+ Long.parseLong(left);
                }
            }else{
                if( Long.parseLong(left) == 0){
                    return oku + "億" + Long.parseLong(te) + "万";
                }
            }
            return oku + "億" + Long.parseLong(te) + "万" + Long.parseLong(left);
        }
        return "Null";
    }

    public String getString(){
        return complexJpyBalForm(value);
    }


}
