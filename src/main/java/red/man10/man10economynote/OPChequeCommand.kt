package red.man10.man10economynote

import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import red.man10.man10economynote.vault.JPYBalanceFormat
import java.util.*

/**
 * Created by sho on 2017/12/15.
 */
class OPChequeCommand(plugin: Man10EconomyNote?) : CommandExecutor {
    var plugin: Man10EconomyNote? = null
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is ConsoleCommandSender) {
            sender.sendMessage("This command can only be executed by a player.")
            return false
        }
        val p = sender as Player
        if (!p.hasPermission("man10.economynote.cheque.create.op")) {
            p.sendMessage("§e[§dMan10EconNote§e]§cあなたは権限を持ってません")
            return false
        }
        if (args.size == 1) {
            if (args[0].equals("help", ignoreCase = true)) {
                help(p)
                return false
            }
            if (p.inventory.firstEmpty() == -1) {
                p.sendMessage("§e[§dMan10EconNote§e]§cインベントリがいっぱいです")
                return false
            }
            try {
                val i = args[0].toLong()
                val res = createChequeData(p.name, p.uniqueId, i, null)
                val blueDye = ItemStack(Material.BLUE_DYE, 1, 12.toShort())
                val itemMeta = blueDye.itemMeta
                itemMeta.setDisplayName("§b§l小切手§7§l(Cheque)")
                val lore: MutableList<String> = ArrayList()
                lore.add("§e====[Man10Bank]====" + format(res.id.toString()))
                lore.add("")
                lore.add("§a§l発行者:" + p.name)
                lore.add("§a§l金額:" + JPYBalanceFormat(i).getString().toString() + "円")
                lore.add("")
                lore.add("§e==================")
                itemMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true)
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                itemMeta.lore = lore
                blueDye.setItemMeta(itemMeta)
                plugin!!.createLog(res.id, p.name, p.uniqueId, "OPCreateCheque", i.toDouble())
                p.inventory.addItem(blueDye)
            } catch (e: NumberFormatException) {
                p.sendMessage("§e[§dMan10EconNote§e]§b金額は数字でなくてはなりません")
                return false
            }
        } else if (args.size == 2) {
            try {
                val i = args[0].toLong()
                if (p.inventory.firstEmpty() == -1) {
                    p.sendMessage("§e[§dMan10EconNote§e]§cインベントリがいっぱいです")
                    return false
                }
                if (args[1].length >= 128) {
                    p.sendMessage("§e[§dMan10EconNote§e]§cメモが長すぎます")
                    return false
                }
                val res = createChequeData(p.name, p.uniqueId, i, args[1].replace("'", "\\'"))
                val blueDye = ItemStack(Material.BLUE_DYE, 1)
                val itemMeta = blueDye.itemMeta
                itemMeta.setDisplayName("§b§l小切手§7§l(Cheque)")
                val lore: MutableList<String> = ArrayList()
                lore.add("§e====[Man10Bank]====" + format(res.id.toString()))
                lore.add("")
                lore.add("§a§l発行者:" + p.name)
                lore.add("§a§l金額:" + JPYBalanceFormat(i).getString().toString() + "円")
                if (args[1] != null || !args[1].equals("", ignoreCase = true)) {
                    lore.add("§d§lメモ:" + args[1].replace("&".toRegex(), "§").replace("_".toRegex(), " "))
                }
                lore.add("")
                lore.add("§e==================")
                itemMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true)
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                itemMeta.lore = lore
                blueDye.setItemMeta(itemMeta)
                plugin!!.createLog(res.id, p.name, p.uniqueId, "OPCreateCheque", i.toDouble())
                p.inventory.addItem(blueDye)
            } catch (e: NumberFormatException) {
                p.sendMessage("§e[§dMan10EconNote§e]§b金額は数字でなくてはなりません")
                return false
            }
        } else {
            help(p)
        }
        return false
    }

    fun help(p: Player) {
        p.sendMessage("§e§l-----[§d§lMan10EconomyNote§e§l]-----")
        p.sendMessage("")
        p.sendMessage("§b/mchequeop <金額> 小切手を作る")
        p.sendMessage("§b/mchequeop <金額> <メモ> 小切手を作る")
        p.sendMessage("")
        p.sendMessage("§e§l---------------------------")
        p.sendMessage("§6§lCreated By Sho0")
    }

    internal inner class ChequeResult(id: Int, memo: Boolean) {
        var memo = false
        var id = -1

        init {
            this.memo = memo
            this.id = id
        }
    }

    private fun createChequeData(name: String, uuid: UUID, value: Long, memo: String?): ChequeResult {
        if (memo == null || memo.equals("", ignoreCase = true)) {
            val id = plugin!!.mysql!!.executeGetId("INSERT INTO man10_economy_note (`id`,`type`,`wired_to_name`,`wired_to_uuid`,`base_value`,`memo`,`creation_date_time`,`creation_time`,`usable_date_time`,`usable_time`,`expired`,`final_value`) VALUES ('0','Cheque','" + name + "','" + uuid + "','" + value + "','" + memo + "','" + plugin!!.mysql!!.currentTimeNoBracket() + "','" + System.currentTimeMillis() / 1000 + "','" + plugin!!.mysql!!.currentTimeNoBracket() + "','" + System.currentTimeMillis() / 1000 + "','0','" + value + "');")
            return ChequeResult(id, false)
        }
        val id = plugin!!.mysql!!.executeGetId("INSERT INTO man10_economy_note (`id`,`type`,`wired_to_name`,`wired_to_uuid`,`base_value`,`memo`,`creation_date_time`,`creation_time`,`usable_date_time`,`usable_time`,`expired`,`final_value`) VALUES ('0','Cheque','" + name + "','" + uuid + "','" + value + "','" + memo + "','" + plugin!!.mysql!!.currentTimeNoBracket() + "','" + System.currentTimeMillis() / 1000 + "','" + plugin!!.mysql!!.currentTimeNoBracket() + "','" + System.currentTimeMillis() / 1000 + "','0','" + value + "');")
        return ChequeResult(id, true)
    }

    private fun format(string: String): String {
        val list = string.toCharArray()
        var finalString = ""
        for (i in list.indices) {
            finalString = finalString + "§" + list[i]
        }
        return finalString
    }

    init {
        this.plugin = plugin
    }
}