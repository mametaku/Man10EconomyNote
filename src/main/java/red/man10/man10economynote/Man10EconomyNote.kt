package red.man10.man10economynote;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import red.man10.man10vaultapiplus.Man10VaultAPI;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public final class Man10EconomyNote extends JavaPlugin {

    public MySQLAPI mysql = null;
    public Man10VaultAPI vault = null;

    public HashMap<UUID,String> inventoryMap = new HashMap<>();
    public HashMap<UUID,NoteData> noteDataMap = new HashMap<>();
    public HashMap<UUID,Integer> slotData = new HashMap<>();
    public HashMap<UUID,LendData> lendDataMap = new HashMap<>();

    public HashMap<UUID,SentLendDataData> sentLendDataDataHashMap = new HashMap<>();

    public HashMap<UUID,Long> withdrawMenu = new HashMap<>();
    
    public HashMap<String,ItemStack> itemHead = new HashMap<>();
    
    public HashMap<Integer,Integer> tenKeyNum = new HashMap<>();

    public HashMap<Integer,NoteData> noteCacheMap = new HashMap<>();
    public HashMap<Integer,LendData> lendDataCacheMap = new HashMap<>();
    

    public double tax = 0.1;

    @Override
    public void onEnable() {
        // Plugin startup logic
        ItemStack i0 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/0ebe7e5215169a699acc6cefa7b73fdb108db87bb6dae2849fbe24714b27").build();
        ItemStack i1 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/71bc2bcfb2bd3759e6b1e86fc7a79585e1127dd357fc202893f9de241bc9e530").build();
        ItemStack i2 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/4cd9eeee883468881d83848a46bf3012485c23f75753b8fbe8487341419847").build();
        ItemStack i3 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/1d4eae13933860a6df5e8e955693b95a8c3b15c36b8b587532ac0996bc37e5").build();
        ItemStack i4 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/d2e78fb22424232dc27b81fbcb47fd24c1acf76098753f2d9c28598287db5").build();
        ItemStack i5 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/6d57e3bc88a65730e31a14e3f41e038a5ecf0891a6c243643b8e5476ae2").build();
        ItemStack i6 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/334b36de7d679b8bbc725499adaef24dc518f5ae23e716981e1dcc6b2720ab").build();
        ItemStack i7 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/6db6eb25d1faabe30cf444dc633b5832475e38096b7e2402a3ec476dd7b9").build();
        ItemStack i8 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/59194973a3f17bda9978ed6273383997222774b454386c8319c04f1f4f74c2b5").build();
        ItemStack i9 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/e67caf7591b38e125a8017d58cfc6433bfaf84cd499d794f41d10bff2e5b840").build();
        ItemStack dot = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/733aa24916c88696ee71db7ac8cd306ad73096b5b6ffd868e1c384b1d62cfb3c").build();
        ItemStack e = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/dbb2737ecbf910efe3b267db7d4b327f360abc732c77bd0e4eff1d510cdef").build();
        itemHead.put("0",i0);
        itemHead.put("1",i1);
        itemHead.put("2",i2);
        itemHead.put("3",i3);
        itemHead.put("4",i4);
        itemHead.put("5",i5);
        itemHead.put("6",i6);
        itemHead.put("7",i7);
        itemHead.put("8",i8);
        itemHead.put("9",i9);
        tenKeyNum.put(46,0);
        tenKeyNum.put(37,1);
        tenKeyNum.put(38,2);
        tenKeyNum.put(39,3);
        tenKeyNum.put(28,4);
        tenKeyNum.put(29,5);
        tenKeyNum.put(30,6);
        tenKeyNum.put(19,7);
        tenKeyNum.put(20,8);
        tenKeyNum.put(21,9);
        Bukkit.getServer().getPluginManager().registerEvents(new EconomyNoteEvent(this), this);
        getCommand("mcheque").setExecutor(new ChequeCommand(this));
        getCommand("mchequeop").setExecutor(new OPChequeCommand(this));
        getCommand("mlend").setExecutor(new LendCommand(this));
        getCommand("mviewdebt").setExecutor(new ViewDebt(this));
        getCommand("man10economynote").setExecutor(new MainCommand(this));
        saveDefaultConfig();
        vault = new Man10VaultAPI("Man10EconomyNote");
        mysql = new MySQLAPI(this, "Man10EconNote");
        mysql.execute(mainDBQuery);
        mysql.execute(logDbQuery);

        NoteData nd = null;
        ResultSet rs = mysql.query("SELECT id,type,wired_to_name,wired_to_uuid,final_value,usable_time FROM man10_economy_note WHERE expired = 0");
        try {
            while (rs.next()){
                nd = new NoteData(rs.getInt("id"),
                        rs.getString("type"),
                        rs.getString("wired_to_name"),
                        UUID.fromString(rs.getString("wired_to_uuid")),
                        rs.getLong("final_value"),
                        rs.getLong("usable_time"));
                noteCacheMap.put(rs.getInt("id"), nd);
            }
            rs.close();
            mysql.close();
        } catch (SQLException ee) {
            ee.printStackTrace();
        }

        tax = getConfig().getDouble("settings.tax");
    }

    String mainDBQuery = "" +
            "CREATE TABLE `man10_economy_note` (\n" +
            "\t`id` INT(11) NOT NULL AUTO_INCREMENT,\n" +
            "\t`type` VARCHAR(50) NULL DEFAULT NULL,\n" +
            "\t`wired_to_name` VARCHAR(50) NULL DEFAULT NULL,\n" +
            "\t`wired_to_uuid` VARCHAR(50) NULL DEFAULT NULL,\n" +
            "\t`base_value` DOUBLE NULL DEFAULT '0',\n" +
            "\t`final_value` DOUBLE NULL DEFAULT '0',\n" +
            "\t`value_left` DOUBLE NULL DEFAULT '0',\n" +
            "\t`monthly_interest` DOUBLE NULL DEFAULT '0',\n" +
            "\t`usable_after_days` INT(11) NULL DEFAULT '0',\n" +
            "\t`memo` TEXT NULL,\n" +
            "\t`expired` TINYINT(4) NULL DEFAULT NULL,\n" +
            "\t`creation_date_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,\n" +
            "\t`creation_time` BIGINT(20) NULL DEFAULT '0',\n" +
            "\t`usable_date_time` DATETIME NULL DEFAULT NULL,\n" +
            "\t`usable_time` BIGINT(20) NULL DEFAULT '0',\n" +
            "\t`expire_date_time` DATETIME NULL DEFAULT NULL,\n" +
            "\t`expire_time` BIGINT(20) NULL DEFAULT '0',\n" +
            "\tPRIMARY KEY (`id`)\n" +
            ")\n" +
            "COLLATE='utf8_general_ci'\n" +
            "ENGINE=InnoDB\n" +
            ";\n";

    String logDbQuery = "CREATE TABLE `man10_economy_note_log` (\n" +
            "\t`id` INT(11) NOT NULL AUTO_INCREMENT,\n" +
            "\t`ticket_id` INT(11) NOT NULL DEFAULT '0',\n" +
            "\t`name` VARCHAR(50) NULL DEFAULT NULL,\n" +
            "\t`uuid` VARCHAR(50) NULL DEFAULT NULL,\n" +
            "\t`action` VARCHAR(50) NULL DEFAULT NULL,\n" +
            "\t`value` DOUBLE NULL DEFAULT NULL,\n" +
            "\t`date_time` DATETIME NULL DEFAULT NULL,\n" +
            "\t`time` BIGINT(20) NULL DEFAULT NULL,\n" +
            "\tPRIMARY KEY (`id`)\n" +
            ")\n" +
            "COLLATE='utf8_general_ci'\n" +
            "ENGINE=InnoDB\n" +
            ";\n";

    void createLog(int id,String name, UUID uuid,String action,double value){
        mysql.execute("INSERT INTO man10_economy_note_log VALUES ('0','" + id + "','" + name + "','" + uuid +"','" + action + "','" + value + "','" + mysql.currentTimeNoBracket() + "','" + System.currentTimeMillis()/1000 + "');");
    }



    class NoteData{
        private String type;
        private String name;
        private UUID uuid;
        private long value;
        private long usable;
        private int id;

        public NoteData(int id,String type,String name,UUID uuid,long finalValue,long usable){
            this.type = type;
            this.id = id;
            this.name = name;
            this.uuid = uuid;
            this.value = finalValue;
            this.usable = usable;
        }

        public boolean hasNull(){
            try {
                if (type == null || name == null || uuid == null || value == -1 || usable == -1) {
                    return true;
                }
                return false;
            }catch (NullPointerException e){
                return true;
            }
        }

        public String getType(){
            return type;
        }

        public String getName(){
            return name;
        }

        public UUID getUuid(){
            return uuid;
        }

        public long getValue(){
            return value;
        }

        public long getUsabeTime(){
            return usable;
        }

        public int getId(){
            return id;
        }
    }


    NoteData getNoteData(int id){
        if(!noteCacheMap.containsKey(id)){
            NoteData nd = null;
            ResultSet rs = mysql.query("SELECT type,wired_to_name,wired_to_uuid,final_value,usable_time FROM man10_economy_note WHERE id = '" + id  + "' and expired = 0");
            String type = null;
            String name = null;
            UUID uuid = null;
            long final_value = -1;
            long usable_time = -1;
            try {
                while (rs.next()){
                    type = rs.getString("type");
                    name = rs.getString("wired_to_name");
                    uuid = UUID.fromString(rs.getString("wired_to_uuid"));
                    final_value = rs.getLong("final_value");
                    usable_time = rs.getLong("usable_time");
                }
                rs.close();
                mysql.close();
                nd = new NoteData(id,type,name,uuid,final_value,usable_time);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if(!nd.hasNull()){
                noteCacheMap.put(id, nd);
            }else{
                noteCacheMap.put(id, null);
            }
        }
        return noteCacheMap.get(id);
    }

    LendData getLendData(int id){
        if(!lendDataCacheMap.containsKey(id)){
            LendData nd = null;
            ResultSet rs = mysql.query("SELECT type,wired_to_name,wired_to_uuid,final_value,base_value,usable_time,usable_after_days,monthly_interest,value_left,id,creation_time FROM man10_economy_note WHERE id = '" + id  + "' and expired = 0");
            try {
                while (rs.next()){
                    long valu = (long) (rs.getLong("base_value") + (rs.getLong("base_value") * tax ));
                    nd = new LendData(rs.getString("wired_to_name"),UUID.fromString(rs.getString("wired_to_uuid")),rs.getLong("base_value"),rs.getLong("final_value"),rs.getInt("usable_after_days"),valu,rs.getDouble("monthly_interest"),rs.getLong("value_left"),rs.getInt("id"),rs.getLong("creation_time"));
                }
                rs.close();
                mysql.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if(nd == null){
                lendDataCacheMap.put(id, null);
            }else{
                lendDataCacheMap.put(id, nd);
            }
        }
        return lendDataCacheMap.get(id);
    }



    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
