package red.man10.man10economynote

import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import red.man10.man10economynote.vault.VaultManager
import java.sql.SQLException
import java.util.*

class Man10EconomyNote : JavaPlugin() {
    var mysql: MySQLManager? = null
    var vault: VaultManager? = null
    var inventoryMap = HashMap<UUID?, String?>()
    var noteDataMap = HashMap<UUID?, NoteData?>()
    var slotData = HashMap<UUID?, Int?>()
    var lendDataMap = HashMap<UUID?, LendData?>()
    var sentLendDataDataHashMap = HashMap<UUID?, SentLendDataData?>()
    var withdrawMenu = HashMap<UUID?, Long?>()
    var itemHead = HashMap<String, ItemStack?>()
    var tenKeyNum = HashMap<Int, Int>()
    var noteCacheMap = HashMap<Int, NoteData?>()
    var lendDataCacheMap = HashMap<Int, LendData?>()
    var tax = 0.1
    override fun onEnable() {
        // Plugin startup logic
        val i0 = SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/0ebe7e5215169a699acc6cefa7b73fdb108db87bb6dae2849fbe24714b27").build()
        val i1 = SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/71bc2bcfb2bd3759e6b1e86fc7a79585e1127dd357fc202893f9de241bc9e530").build()
        val i2 = SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/4cd9eeee883468881d83848a46bf3012485c23f75753b8fbe8487341419847").build()
        val i3 = SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/1d4eae13933860a6df5e8e955693b95a8c3b15c36b8b587532ac0996bc37e5").build()
        val i4 = SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/d2e78fb22424232dc27b81fbcb47fd24c1acf76098753f2d9c28598287db5").build()
        val i5 = SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/6d57e3bc88a65730e31a14e3f41e038a5ecf0891a6c243643b8e5476ae2").build()
        val i6 = SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/334b36de7d679b8bbc725499adaef24dc518f5ae23e716981e1dcc6b2720ab").build()
        val i7 = SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/6db6eb25d1faabe30cf444dc633b5832475e38096b7e2402a3ec476dd7b9").build()
        val i8 = SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/59194973a3f17bda9978ed6273383997222774b454386c8319c04f1f4f74c2b5").build()
        val i9 = SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/e67caf7591b38e125a8017d58cfc6433bfaf84cd499d794f41d10bff2e5b840").build()
        val dot = SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/733aa24916c88696ee71db7ac8cd306ad73096b5b6ffd868e1c384b1d62cfb3c").build()
        val e = SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/dbb2737ecbf910efe3b267db7d4b327f360abc732c77bd0e4eff1d510cdef").build()
        itemHead["0"] = i0
        itemHead["1"] = i1
        itemHead["2"] = i2
        itemHead["3"] = i3
        itemHead["4"] = i4
        itemHead["5"] = i5
        itemHead["6"] = i6
        itemHead["7"] = i7
        itemHead["8"] = i8
        itemHead["9"] = i9
        tenKeyNum[46] = 0
        tenKeyNum[37] = 1
        tenKeyNum[38] = 2
        tenKeyNum[39] = 3
        tenKeyNum[28] = 4
        tenKeyNum[29] = 5
        tenKeyNum[30] = 6
        tenKeyNum[19] = 7
        tenKeyNum[20] = 8
        tenKeyNum[21] = 9
        Bukkit.getServer().pluginManager.registerEvents(EconomyNoteEvent(this), this)
        getCommand("mcheque")!!.setExecutor(ChequeCommand(this))
        getCommand("mchequeop")!!.setExecutor(OPChequeCommand(this))
        getCommand("mlend")!!.setExecutor(LendCommand(this))
        getCommand("mviewdebt")!!.setExecutor(ViewDebt(this))
        getCommand("man10economynote")!!.setExecutor(MainCommand(this))
        saveDefaultConfig()
        mysql = MySQLManager(this, "Man10EconNote")
        mysql!!.execute(mainDBQuery)
        mysql!!.execute(logDbQuery)
        var nd: NoteData? = null
        val rs = mysql!!.query("SELECT id,type,wired_to_name,wired_to_uuid,final_value,usable_time FROM man10_economy_note WHERE expired = 0")
        try {
            while (rs!!.next()) {
                nd = NoteData(rs.getInt("id"),
                        rs.getString("type"),
                        rs.getString("wired_to_name"),
                        UUID.fromString(rs.getString("wired_to_uuid")),
                        rs.getLong("final_value"),
                        rs.getLong("usable_time"))
                noteCacheMap[rs.getInt("id")] = nd
            }
            rs.close()
            mysql!!.close()
        } catch (ee: SQLException) {
            ee.printStackTrace()
        }
        tax = config.getDouble("settings.tax")
    }

    var mainDBQuery = """CREATE TABLE `man10_economy_note` (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	`type` VARCHAR(50) NULL DEFAULT NULL,
	`wired_to_name` VARCHAR(50) NULL DEFAULT NULL,
	`wired_to_uuid` VARCHAR(50) NULL DEFAULT NULL,
	`base_value` DOUBLE NULL DEFAULT '0',
	`final_value` DOUBLE NULL DEFAULT '0',
	`value_left` DOUBLE NULL DEFAULT '0',
	`monthly_interest` DOUBLE NULL DEFAULT '0',
	`usable_after_days` INT(11) NULL DEFAULT '0',
	`memo` TEXT NULL,
	`expired` TINYINT(4) NULL DEFAULT NULL,
	`creation_date_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
	`creation_time` BIGINT(20) NULL DEFAULT '0',
	`usable_date_time` DATETIME NULL DEFAULT NULL,
	`usable_time` BIGINT(20) NULL DEFAULT '0',
	`expire_date_time` DATETIME NULL DEFAULT NULL,
	`expire_time` BIGINT(20) NULL DEFAULT '0',
	PRIMARY KEY (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;
"""
    var logDbQuery = """CREATE TABLE `man10_economy_note_log` (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	`ticket_id` INT(11) NOT NULL DEFAULT '0',
	`name` VARCHAR(50) NULL DEFAULT NULL,
	`uuid` VARCHAR(50) NULL DEFAULT NULL,
	`action` VARCHAR(50) NULL DEFAULT NULL,
	`value` DOUBLE NULL DEFAULT NULL,
	`date_time` DATETIME NULL DEFAULT NULL,
	`time` BIGINT(20) NULL DEFAULT NULL,
	PRIMARY KEY (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;
"""

    fun createLog(id: Int, name: String?, uuid: UUID?, action: String, value: Double) {
        mysql!!.execute("INSERT INTO man10_economy_note_log VALUES ('0','" + id + "','" + name + "','" + uuid + "','" + action + "','" + value + "','" + mysql!!.currentTimeNoBracket() + "','" + System.currentTimeMillis() / 1000 + "');")
    }

    inner class NoteData(val id: Int, val type: String?, val name: String?, val uuid: UUID?, val value: Long, val usabeTime: Long) {
        fun hasNull(): Boolean {
            return try {
                if (type == null || name == null || uuid == null || value == -1L || usabeTime == -1L) {
                    true
                } else false
            } catch (e: NullPointerException) {
                true
            }
        }
    }

    fun getNoteData(id: Int): NoteData? {
        if (!noteCacheMap.containsKey(id)) {
            var nd: NoteData? = null
            val rs = mysql!!.query("SELECT type,wired_to_name,wired_to_uuid,final_value,usable_time FROM man10_economy_note WHERE id = '$id' and expired = 0")
            var type: String? = null
            var name: String? = null
            var uuid: UUID? = null
            var final_value: Long = -1
            var usable_time: Long = -1
            try {
                while (rs!!.next()) {
                    type = rs.getString("type")
                    name = rs.getString("wired_to_name")
                    uuid = UUID.fromString(rs.getString("wired_to_uuid"))
                    final_value = rs.getLong("final_value")
                    usable_time = rs.getLong("usable_time")
                }
                rs.close()
                mysql!!.close()
                nd = NoteData(id, type, name, uuid, final_value, usable_time)
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            if (!nd!!.hasNull()) {
                noteCacheMap[id] = nd
            } else {
                noteCacheMap[id] = null
            }
        }
        return noteCacheMap[id]
    }

    fun getLendData(id: Int): LendData? {
        if (!lendDataCacheMap.containsKey(id)) {
            var nd: LendData? = null
            val rs = mysql!!.query("SELECT type,wired_to_name,wired_to_uuid,final_value,base_value,usable_time,usable_after_days,monthly_interest,value_left,id,creation_time FROM man10_economy_note WHERE id = '$id' and expired = 0")
            try {
                while (rs!!.next()) {
                    val valu = (rs.getLong("base_value") + rs.getLong("base_value") * tax).toLong()
                    nd = LendData(rs.getString("wired_to_name"), UUID.fromString(rs.getString("wired_to_uuid")), rs.getLong("base_value"), rs.getLong("final_value"), rs.getInt("usable_after_days"), valu, rs.getDouble("monthly_interest"), rs.getLong("value_left"), rs.getInt("id"), rs.getLong("creation_time"))
                }
                rs.close()
                mysql!!.close()
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            if (nd == null) {
                lendDataCacheMap[id] = null
            } else {
                lendDataCacheMap[id] = nd
            }
        }
        return lendDataCacheMap[id]
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}