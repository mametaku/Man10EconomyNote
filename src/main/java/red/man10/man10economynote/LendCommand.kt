package red.man10.man10economynote

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by sho on 2017/12/15.
 */
class LendCommand(plugin: Man10EconomyNote?) : CommandExecutor {
    var plugin: Man10EconomyNote? = null
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is ConsoleCommandSender) {
            sender.sendMessage("This command can only be executed by a player.")
            return false
        }
        val p = sender as Player
        if (args.size == 1) {
            if (args[0].equals("view", ignoreCase = true)) {
                if (!p.hasPermission("man10.economynote.cheque.view")) {
                    p.sendMessage("§e[§dMan10EconNote§e]§cあなたは権限を持っていません")
                    return false
                }
                if (plugin!!.sentLendDataDataHashMap[p.uniqueId] == null) {
                    p.sendMessage("§e[§dMan10EconNote§e]§c現在あなたに提示は来てません")
                    return false
                }
                p.openInventory(createLendConfirmMenu(p, plugin!!.sentLendDataDataHashMap[p.uniqueId]!!.data))
                plugin!!.inventoryMap[p.uniqueId] = "LendConfirm"
                return false
            }
            if (args[0].equals("help", ignoreCase = true)) {
                help(p)
                return false
            }
        }
        if (args.size == 4) {
            if (!p.hasPermission("man10.economynote.cheque.lend")) {
                p.sendMessage("§e[§dMan10EconNote§e]§cあなたは権限を持っていません")
                return false
            }
            return try {
                val target = Bukkit.getPlayer(args[0])
                if (target == null) {
                    p.sendMessage("§e[§dMan10EconNote§e]§cプレイヤーがオフラインです")
                    return false
                }
                if (p.name == target.name) {
                    p.sendMessage("§e[§dMan10EconNote§e]§c自分には申請できません")
                    return false
                }
                val value = args[1].toLong()
                val days = args[2].toInt()
                val intrest = args[3].toDouble()
                val finalIntrest = days * (intrest / 30)
                if (value <= 0) {
                    p.sendMessage("§e[§dMan10EconNote§e]§b金額は1以上でなくてはなりません")
                    return false
                }
                if (!(intrest >= 0 && intrest <= 0.5)) {
                    p.sendMessage("§e[§dMan10EconNote§e]§b金利は0 ～ 0.5 でなくてはなりません")
                    return false
                }
                if (days < 0 || days > 100) {
                    p.sendMessage("§e[§dMan10EconNote§e]§c借用日数は100日以内でなくてはなりません")
                    return false
                }
                if (plugin!!.inventoryMap[target.uniqueId] != null && plugin!!.inventoryMap[target.uniqueId] == "LendConfirm") {
                    p.sendMessage("§e[§dMan10EconNote§e]§c現在プレイヤーは借金の審議中です")
                    return false
                }
                val finalValue = (value + value * plugin!!.tax).toLong()
                if (plugin!!.vault!!.getBalance(p.uniqueId) < value + value * plugin!!.tax) {
                    p.sendMessage("§e[§dMan10EconNote§e]§b所持金が税金を足した" + (value + value * plugin!!.tax) + "に達していません")
                    return false
                }
                val finalPayerValue = (value + value * finalIntrest).toLong()
                val ld = LendData(target.name, target.uniqueId, value, finalPayerValue, days, finalValue, intrest, finalValue, 0, System.currentTimeMillis() / 1000)
                p.openInventory(createLendSendConfirmMenu(target, ld))
                plugin!!.inventoryMap[p.uniqueId] = "LendSendConfirm"
                plugin!!.lendDataMap[p.uniqueId] = ld
                false
            } catch (e: NumberFormatException) {
                p.sendMessage("§e[§dMan10EconNote§e]§c数字的エラー")
                false
            }
        }
        help(p)
        return false
    }

    fun help(p: Player) {
        p.sendMessage("§e§l-----[§d§lMan10EconomyNote§e§l]-----")
        p.sendMessage("")
        p.sendMessage("§b/mlend view 提示が来ていれば見る")
        p.sendMessage("§b/mlend <プレイヤー> <金額> <日数> <金利（月利）> 借金条件を提示する")
        p.sendMessage("")
        p.sendMessage("§e§l---------------------------")
        p.sendMessage("§6§lCreated By Sho0")
    }

    fun createLendConfirmMenu(p: Player, ld: LendData?): Inventory {
        val sdf = SimpleDateFormat("yyyy'年'MM'月'dd'日'E'曜日'k'時'mm'分'ss'秒'")
        val inv = Bukkit.createInventory(null, 27, "§4§lこの条件を受け入れますか？")
        val ink = ItemStack(Material.INK_SAC, 1)
        val inkMeta = ink.itemMeta
        inkMeta.setDisplayName("§c§l約束手形§7§l(Promissory Note)")
        inkMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true)
        inkMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        val inkLore: MutableList<String> = ArrayList()
        inkLore.add("§6====[Man10Bank]====")
        inkLore.add("")
        inkLore.add("§c§l発行者:" + p.name)
        inkLore.add("§c§l金額:" + ld!!.finalValue)
        val usableTimeStamp = ld.usableDays * 24 * 60 * 60 + System.currentTimeMillis() / 1000
        val time = Date(usableTimeStamp * 1000)
        inkLore.add("§d§l残金:" + ld.finalValue)
        inkLore.add("§4使用可能日")
        inkLore.add("§4(" + sdf.format(time) + ")")
        inkLore.add("")
        inkLore.add("§6==================")
        inkMeta.lore = inkLore
        ink.itemMeta = inkMeta
        inv.setItem(13, ink)
        val paper = ItemStack(Material.PAPER)
        val paperMeta = paper.itemMeta
        paperMeta.setDisplayName("§6§l契約内容")
        val loree: MutableList<String> = ArrayList()
        loree.add("§6===[Man10Bank]===")
        loree.add("§4借金金額:" + ld.baseValue)
        loree.add("§4返済金額:" + ld.finalValue)
        loree.add("§4取立開始日:" + ld.usableDays + "日後")
        loree.add("§4" + sdf.format(time))
        loree.add("§6==================")
        paperMeta.lore = loree
        paper.itemMeta = paperMeta
        inv.setItem(22, paper)
        val green = ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1)
        val itemMeta = green.itemMeta
        itemMeta.setDisplayName("§a§l承諾する")
        green.itemMeta = itemMeta
        val greens = intArrayOf(0, 1, 2, 9, 10, 11, 18, 19, 20)
        for (i in greens.indices) {
            inv.setItem(greens[i], green)
        }
        val red = ItemStack(Material.RED_STAINED_GLASS_PANE, 1)
        val itemMetaRed = red.itemMeta
        itemMetaRed.setDisplayName("§c§l提示を拒否する")
        red.itemMeta = itemMetaRed
        val reds = intArrayOf(6, 7, 8, 15, 16, 17, 24, 25, 26)
        for (i in reds.indices) {
            inv.setItem(reds[i], red)
        }
        return inv
    }

    fun createLendSendConfirmMenu(p: Player, ld: LendData): Inventory {
        val inv = Bukkit.createInventory(null, 27, "§4§lこの条件を" + p.name + "に提示しますか？")
        val sdf = SimpleDateFormat("yyyy'年'MM'月'dd'日'E'曜日'k'時'mm'分'ss'秒'")
        val ink = ItemStack(Material.INK_SAC, 1, 9.toShort())
        val inkMeta = ink.itemMeta
        inkMeta.setDisplayName("§c§l約束手形§7§l(Promissory Note)")
        inkMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true)
        inkMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        val inkLore: MutableList<String> = ArrayList()
        inkLore.add("§6====[Man10Bank]====")
        inkLore.add("")
        inkLore.add("§c§l発行者:" + p.name)
        inkLore.add("§c§l金額:" + ld.finalValue)
        val usableTimeStamp = ld.usableDays * 24 * 60 * 60 + System.currentTimeMillis() / 1000
        val time = Date(usableTimeStamp * 1000)
        inkLore.add("§d§l残金:" + ld.finalValue)
        inkLore.add("§4使用可能日")
        inkLore.add("§4(" + sdf.format(time) + ")")
        inkLore.add("")
        inkLore.add("§6==================")
        inkMeta.lore = inkLore
        ink.itemMeta = inkMeta
        inv.setItem(13, ink)
        val green = ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1)
        val itemMeta = green.itemMeta
        itemMeta.setDisplayName("§a§l提示する")
        green.itemMeta = itemMeta
        val greens = intArrayOf(0, 1, 2, 9, 10, 11, 18, 19, 20)
        for (i in greens.indices) {
            inv.setItem(greens[i], green)
        }
        val red = ItemStack(Material.RED_STAINED_GLASS_PANE, 1)
        val itemMetaRed = red.itemMeta
        itemMetaRed.setDisplayName("§c§lキャンセル")
        red.itemMeta = itemMetaRed
        val reds = intArrayOf(6, 7, 8, 15, 16, 17, 24, 25, 26)
        for (i in reds.indices) {
            inv.setItem(reds[i], red)
        }
        return inv
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