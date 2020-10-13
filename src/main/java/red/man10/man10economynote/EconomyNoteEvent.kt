package red.man10.man10economynote

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import red.man10.man10vaultapiplus.JPYBalanceFormat
import red.man10.man10vaultapiplus.MoneyPoolObject
import red.man10.man10vaultapiplus.enums.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by sho on 2017/12/15.
 */
class EconomyNoteEvent(plugin: Man10EconomyNote?) : Listener {
    var plugin: Man10EconomyNote? = null
    @EventHandler
    fun onInteract(e: PlayerInteractEvent) {
        if (e.action == Action.RIGHT_CLICK_BLOCK || e.action == Action.RIGHT_CLICK_AIR) {
            if (e.player.inventory.itemInMainHand.type == Material.BLUE_DYE && e.player.inventory.itemInMainHand.durability.toInt() == 9 && e.player.inventory.itemInMainHand.itemMeta.lore!![0].contains("§6====[Man10Bank]====")) {
                plugin!!.slotData[e.player.uniqueId] = e.player.inventory.heldItemSlot
                val id = e.player.inventory.itemInMainHand.itemMeta.lore!![0].replace("§6====[Man10Bank]====", "").replace("§", "")
                val ld = plugin!!.getLendData(id.toInt())
                if (ld == null) {
                    e.player.sendMessage("§e[§dMan10EconNote§e]§bデータが存在しません")
                    plugin!!.slotData.remove(e.player.uniqueId)
                    return
                }
                val target = Bukkit.getPlayer(ld.uuid!!)
                if (System.currentTimeMillis() / 1000 <= ld.creationTime + ld.usableDays * 86400) {
                    e.player.sendMessage("§e[§dMan10EconNote§e]§c§l現在この手形はまだ使えません")
                    return
                }
                if (target == null) {
                    e.player.sendMessage("§e[§dMan10EconNote§e]§c§l現在プレイヤーはオンラインではありません")
                    return
                }
                if (ld.valueLeft == 0L) {
                    e.player.sendMessage("§e[§dMan10EconNote§e]§c§l手形の残高がありません")
                    return
                }
                e.player.openInventory(withDrawInventory())
                plugin!!.withdrawMenu[e.player.uniqueId] = 0L
                plugin!!.inventoryMap[e.player.uniqueId] = "withdrawMenu"
                plugin!!.lendDataMap[e.player.uniqueId] = ld
            }
            if (e.player.inventory.itemInMainHand.type == Material.BLUE_DYE && e.player.inventory.itemInMainHand.durability.toInt() == 12 && e.player.inventory.itemInMainHand.itemMeta.lore!![0].contains("§e====[Man10Bank]====")) {
                plugin!!.slotData[e.player.uniqueId] = e.player.inventory.heldItemSlot
                val id = e.player.inventory.itemInMainHand.itemMeta.lore!![0].replace("§e====[Man10Bank]====", "").replace("§", "")
                val nd = plugin!!.getNoteData(id.toInt())
                if (nd == null) {
                    e.player.sendMessage("§e[§dMan10EconNote§e]§bデータが存在しません")
                    e.player.inventory.setItem(plugin!!.slotData[e.player.uniqueId]!!, ItemStack(Material.AIR))
                    plugin!!.slotData.remove(e.player.uniqueId)
                    return
                }
                e.player.openInventory(createChequeInventory(e.player, nd.value))
                plugin!!.inventoryMap[e.player.uniqueId] = "chequeConfirm"
                plugin!!.noteDataMap[e.player.uniqueId] = nd
            }
        }
    }

    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        if (!plugin!!.inventoryMap.isEmpty()) {
            if (plugin!!.inventoryMap.containsKey(e.whoClicked.uniqueId)) {
                if (plugin!!.inventoryMap[e.whoClicked.uniqueId] == "withdrawMenu") {
                    e.isCancelled = true
                    val ld = plugin!!.lendDataMap[e.whoClicked.uniqueId]
                    val s = e.slot
                    if (s == 48) {
                        plugin!!.withdrawMenu[e.whoClicked.uniqueId] = 0L
                        for (i in 0..8) {
                            e.inventory.setItem(i, ItemStack(Material.AIR))
                        }
                        redner(e.inventory, plugin!!.withdrawMenu[e.whoClicked.uniqueId]!!)
                    }
                    if (s == 42 || s == 43 || s == 51 || s == 52) {
                        e.whoClicked.closeInventory()
                        return
                    }
                    if (s == 40 || s == 41 || s == 49 || s == 50) {
                        if (plugin!!.withdrawMenu[e.whoClicked.uniqueId] == 0) {
                            e.whoClicked.sendMessage("§e[§dMan10EconNote§e]§c§l金額は1以上でなくてはなりません")
                            return
                        }
                        val target = Bukkit.getPlayer(ld!!.uuid!!)
                        if (target == null) {
                            e.whoClicked.sendMessage("§e[§dMan10EconNote§e]§c§l現在プレイヤーはオンラインではありません")
                            return
                        }
                        if (plugin!!.vault!!.getBalance(target.uniqueId) < plugin!!.withdrawMenu[e.whoClicked.uniqueId]!!) {
                            e.whoClicked.sendMessage("§e[§dMan10EconNote§e]§c§l現在プレイヤーお金を十分に持っていません")
                            return
                        }
                        if (ld.valueLeft < plugin!!.withdrawMenu[e.whoClicked.uniqueId]!!) {
                            e.whoClicked.sendMessage("§e[§dMan10EconNote§e]§c§l手形の残高を超す請求をしています")
                            return
                        }
                        plugin!!.vault.transferMoneyPlayerToPlayer(ld.uuid, e.whoClicked.uniqueId, plugin!!.withdrawMenu[e.whoClicked.uniqueId], TransactionCategory.ECONOMY_NOTE, TransactionType.COLLECT, "Economy note withdrawal by:" + e.whoClicked.name + " to:" + ld.name + " price:" + plugin!!.withdrawMenu[e.whoClicked.uniqueId])
                        val item = e.whoClicked.inventory.getItem(plugin!!.slotData[e.whoClicked.uniqueId]!!)
                        val itemMeta = item!!.itemMeta
                        val lore = itemMeta.lore
                        lore!![4] = "§d§l残金:" + (ld.valueLeft - plugin!!.withdrawMenu[e.whoClicked.uniqueId]!!).toString()
                        itemMeta.lore = lore
                        item.setItemMeta(itemMeta)
                        plugin!!.mysql!!.execute("UPDATE man10_economy_note SET value_left ='" + (ld.valueLeft - plugin!!.withdrawMenu[e.whoClicked.uniqueId]!!).toString() + "' WHERE id ='" + ld.id + "'")
                        plugin!!.lendDataCacheMap.remove(ld.id)
                        target.sendMessage("§e[§dMan10EconNote§e]§c§l" + e.whoClicked.name + "はあなたの約束手形から" + plugin!!.withdrawMenu[e.whoClicked.uniqueId] + "円引き出しました")
                        plugin!!.createLog(ld.id, e.whoClicked.name, e.whoClicked.uniqueId, "RedeemPromissoryNote", plugin!!.withdrawMenu[e.whoClicked.uniqueId])
                        e.whoClicked.closeInventory()
                        if (ld.valueLeft == 0L) {
                            plugin!!.mysql!!.execute("UPDATE man10_economy_note SET expired ='1' WHERE id  ='" + ld.id + "'")
                        }
                        return
                    }
                    val `val` = plugin!!.withdrawMenu[e.whoClicked.uniqueId]!!
                    if (plugin!!.tenKeyNum[s] == null) {
                        return
                    }
                    if (plugin!!.withdrawMenu[e.whoClicked.uniqueId] == 0) {
                        plugin!!.withdrawMenu[e.whoClicked.uniqueId] = java.lang.Long.valueOf(plugin!!.tenKeyNum[s])
                        redner(e.inventory, plugin!!.withdrawMenu[e.whoClicked.uniqueId]!!)
                        return
                    }
                    if (plugin!!.vault!!.getBalance(plugin!!.lendDataMap[e.whoClicked.uniqueId]!!.uuid) <= plugin!!.withdrawMenu[e.whoClicked.uniqueId]!!) {
                        e.whoClicked.sendMessage("§e[§dMan10EconNote§e]§c§lプレイヤーはこれ以上お金を持っていません")
                        if (plugin!!.vault!!.getBalance(ld!!.uuid) >= 0) {
                            plugin!!.withdrawMenu[e.whoClicked.uniqueId] = plugin!!.vault!!.getBalance(plugin!!.lendDataMap[e.whoClicked.uniqueId]!!.uuid).toLong()
                            redner(e.inventory, plugin!!.withdrawMenu[e.whoClicked.uniqueId]!!)
                        }
                        return
                    }
                    if (plugin!!.withdrawMenu[e.whoClicked.uniqueId]!! >= 999999999) {
                        plugin!!.withdrawMenu[e.whoClicked.uniqueId] = 999999999L
                        redner(e.inventory, plugin!!.withdrawMenu[e.whoClicked.uniqueId]!!)
                        return
                    }
                    plugin!!.withdrawMenu[e.whoClicked.uniqueId] = java.lang.Long.valueOf(`val`.toString() + plugin!!.tenKeyNum[s])
                    redner(e.inventory, plugin!!.withdrawMenu[e.whoClicked.uniqueId]!!)
                }
                if (plugin!!.inventoryMap[e.whoClicked.uniqueId] == "chequeConfirm") {
                    e.isCancelled = true
                    val greens = intArrayOf(0, 1, 2, 9, 10, 11, 18, 19, 20)
                    val reds = intArrayOf(6, 7, 8, 15, 16, 17, 24, 25, 26)
                    for (i in greens.indices) {
                        if (greens[i] == e.slot) {
                            (e.whoClicked as Player).playSound(e.whoClicked.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                            val nd = plugin!!.noteDataMap[e.whoClicked.uniqueId]
                            e.whoClicked.inventory.setItem(plugin!!.slotData[e.whoClicked.uniqueId]!!, ItemStack(Material.AIR))
                            plugin!!.slotData.remove(e.whoClicked.uniqueId)
                            plugin!!.createLog(nd.getId(), e.whoClicked.name, e.whoClicked.uniqueId, "RedeemCheque", nd.getValue().toDouble())
                            plugin!!.mysql!!.execute("UPDATE man10_economy_note SET expired ='1' WHERE id=" + nd.getId())
                            plugin!!.noteCacheMap.remove(nd.getId())
                            val toOldBalance = plugin!!.vault!!.getBalance(e.whoClicked.uniqueId)
                            plugin!!.vault.givePlayerMoney(e.whoClicked.uniqueId, nd.getValue(), TransactionType.REDEEM_CHEQUE, "Man10 Cheque Collected by :" + e.whoClicked.name + " from:" + nd.getName() + " value:" + nd.getValue(), TransactionLogType.RAW)
                            plugin!!.vault.createTransactionLog(TransactionCategory.VOID, TransactionType.REDEEM_CHEQUE, plugin!!.vault.getPluginName(), nd.getValue(), nd.getName(), nd.getUuid(), e.whoClicked.name, e.whoClicked.uniqueId, 0, 0, toOldBalance, toOldBalance + nd.getValue(), -1, TransactionLogType.RESULT, "Man10 Cheque Collected by :" + e.whoClicked.name + " from:" + nd.getName() + " value:" + nd.getValue())
                            e.whoClicked.closeInventory()
                        }
                    }
                    for (i in reds.indices) {
                        if (reds[i] == e.slot) {
                            e.whoClicked.closeInventory()
                        }
                    }
                    return
                }
                if (plugin!!.inventoryMap[e.whoClicked.uniqueId] == "LendSendConfirm") {
                    e.isCancelled = true
                    val greens = intArrayOf(0, 1, 2, 9, 10, 11, 18, 19, 20)
                    val reds = intArrayOf(6, 7, 8, 15, 16, 17, 24, 25, 26)
                    for (i in greens.indices) {
                        if (greens[i] == e.slot) {
                            val ld = plugin!!.lendDataMap[e.whoClicked.uniqueId]
                            if (Bukkit.getPlayer(ld!!.uuid!!) == null) {
                                e.whoClicked.sendMessage("§e[§dMan10EconNote§e]§cプレイヤーがオフラインになりました")
                                e.whoClicked.closeInventory()
                            }
                            e.whoClicked.sendMessage("§e[§dMan10EconNote§e]§a条件を提示しました")
                            val data = SentLendDataData(ld, e.whoClicked.name, e.whoClicked.uniqueId)
                            Bukkit.getPlayer(ld.uuid!!)!!.sendMessage("§e[§dMan10EconNote§e]§e§l" + e.whoClicked.name + "さんが借金条件を提示しました /mlend view")
                            plugin!!.sentLendDataDataHashMap[Bukkit.getPlayer(ld.uuid!!)!!.uniqueId] = data
                            e.whoClicked.closeInventory()
                        }
                    }
                    for (i in reds.indices) {
                        if (reds[i] == e.slot) {
                            e.whoClicked.closeInventory()
                        }
                    }
                    return
                }
                if (plugin!!.inventoryMap[e.whoClicked.uniqueId] == "LendConfirm") {
                    e.isCancelled = true
                    val greens = intArrayOf(0, 1, 2, 9, 10, 11, 18, 19, 20)
                    val reds = intArrayOf(6, 7, 8, 15, 16, 17, 24, 25, 26)
                    for (i in greens.indices) {
                        if (greens[i] == e.slot) {
                            val lender = Bukkit.getPlayer(plugin!!.sentLendDataDataHashMap[e.whoClicked.uniqueId]!!.fromUUID)
                            if (lender == null) {
                                e.whoClicked.sendMessage("§e[§dMan10EconNote§e]§c§l提示者がオフラインになりました")
                                return
                            }
                            if (lender.inventory.firstEmpty() == -1) {
                                e.whoClicked.sendMessage("§e[§dMan10EconNote§e]§c§l提示者のインベントリがいっぱいです")
                                return
                            }
                            if (plugin!!.vault!!.getBalance(plugin!!.sentLendDataDataHashMap[e.whoClicked.uniqueId]!!.fromUUID) <= plugin!!.sentLendDataDataHashMap[e.whoClicked.uniqueId]!!.data!!.finalValueLender) {
                                e.whoClicked.sendMessage("§e[§dMan10EconNote§e]§c§l提示者の所持金が提示金額に達していません")
                                return
                            }
                            val ld = plugin!!.sentLendDataDataHashMap[e.whoClicked.uniqueId]!!.data
                            val usableTimeStamp = ld!!.usableDays * 24 * 60 * 60 + System.currentTimeMillis() / 1000
                            val date = Date(usableTimeStamp * 1000)
                            val sdf = SimpleDateFormat("yyyy'-'MM'-'dd' 'HH':'mm':'ss")
                            val dateExpire = Date((usableTimeStamp + 31536000) * 1000)
                            val id = plugin!!.mysql!!.executeGetId("INSERT INTO man10_economy_note (`id`,`type`,`wired_to_name`,`wired_to_uuid`,`base_value`,`final_value`,`value_left`,`monthly_interest`,`usable_after_days`,`memo`,`expired`,`creation_date_time`,`creation_time`,`usable_date_time`,`usable_time`,`expire_date_time`,`expire_time`) " +
                                    "VALUES ('0','" + "PromissoryNote','" + ld.name + "','" + ld.uuid + "','" + ld.baseValue + "','" + ld.finalValue + "','" + ld.finalValue + "','" + ld.interest + "','" + ld.usableDays + "','','0','" + plugin!!.mysql!!.currentTimeNoBracket() + "','" + System.currentTimeMillis() / 1000 + "','" + sdf.format(date) + "','" + usableTimeStamp + "','" + sdf.format(dateExpire) + "','" + (usableTimeStamp + 631536000) + "');")
                            val ink = ItemStack(Material.INK_SAC, 1, 9.toShort())
                            val inkMeta = ink.itemMeta
                            inkMeta.setDisplayName("§c§l約束手形§7§l(Promissory Note)")
                            inkMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true)
                            inkMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                            val inkLore: MutableList<String> = ArrayList()
                            val sdff = SimpleDateFormat("yyyy'年'MM'月'dd'日'E'曜日'k'時'mm'分'ss'秒'")
                            inkLore.add("§6====[Man10Bank]====" + format(id.toString()))
                            inkLore.add("")
                            inkLore.add("§c§l発行者:" + ld.name)
                            inkLore.add("§c§l金額:" + ld.finalValue)
                            val time = Date(usableTimeStamp * 1000)
                            inkLore.add("§d§l残金:" + ld.finalValue)
                            inkLore.add("§4使用可能日")
                            inkLore.add("§4(" + sdff.format(time) + ")")
                            inkLore.add("")
                            inkLore.add("§6==================")
                            inkMeta.lore = inkLore
                            ink.setItemMeta(inkMeta)
                            plugin!!.createLog(id, plugin!!.sentLendDataDataHashMap[e.whoClicked.uniqueId]!!.fromName, plugin!!.sentLendDataDataHashMap[e.whoClicked.uniqueId]!!.fromUUID, "CreatePromissoryNote", ld.finalValue.toDouble())
                            val pool = MoneyPoolObject(plugin!!.vault.getPluginName(), MoneyPoolTerm.SHORT_TERM, MoneyPoolType.MEMORY, "Man10 EconomyNote MoneyFlow Pool")
                            plugin!!.vault.transferMoneyPlayerToPool(plugin!!.sentLendDataDataHashMap[e.whoClicked.uniqueId]!!.fromUUID, pool.getId(), ld.finalValueLender, TransactionCategory.ECONOMY_NOTE, TransactionType.LEND, "PromissoryNote money send")
                            plugin!!.vault.transferMoneyPoolToPlayer(pool.getId(), e.whoClicked.uniqueId, ld.baseValue, TransactionCategory.ECONOMY_NOTE, TransactionType.LEND, "PromissoryNote money receive")
                            Bukkit.getPlayer(plugin!!.sentLendDataDataHashMap[e.whoClicked.uniqueId]!!.fromUUID)!!.inventory.addItem(ink)
                            pool.sendRemainderToCountry("PromissoryNote Tax Fee Send")
                            e.whoClicked.sendMessage("§e§dMan10EconomyNote§e]§a取引が成立しました")
                            e.whoClicked.closeInventory()
                        }
                    }
                    for (i in reds.indices) {
                        if (reds[i] == e.slot) {
                            e.whoClicked.closeInventory()
                            plugin!!.sentLendDataDataHashMap.remove(e.whoClicked.uniqueId)
                        }
                    }
                    return
                }
            }
        }
    }

    fun redner(inv: Inventory, value: Long) {
        val `val` = value.toString()
        val items: MutableList<ItemStack?> = ArrayList()
        for (i in `val`.toCharArray().indices) {
            items.add(plugin!!.itemHead[`val`.toCharArray()[i].toString()])
        }
        val startFrom = 9 - items.size
        for (i in items.indices) {
            inv.setItem(i + startFrom, items[i])
        }
    }

    @EventHandler
    fun onClose(e: InventoryCloseEvent) {
        if (!plugin!!.inventoryMap.isEmpty()) {
            if (plugin!!.inventoryMap.containsKey(e.player.uniqueId)) {
                plugin!!.inventoryMap.remove(e.player.uniqueId)
                plugin!!.noteDataMap.remove(e.player.uniqueId)
                plugin!!.lendDataMap.remove(e.player.uniqueId)
                plugin!!.withdrawMenu.remove(e.player.uniqueId)
                plugin!!.sentLendDataDataHashMap.remove(e.player.uniqueId)
            }
        }
    }

    fun createChequeInventory(p: Player, value: Long): Inventory {
        val inv = Bukkit.createInventory(null, 27, "§4§l" + JPYBalanceFormat(value).getString().toString() + "円と換金しますか？")
        val green = ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1)
        val itemMeta = green.itemMeta
        itemMeta.setDisplayName("§a§l" + value + "円と換金する")
        val lore: MutableList<String> = ArrayList()
        lore.add("§a§l(" + JPYBalanceFormat(value).getString().toString() + "円)")
        itemMeta.lore = lore
        green.setItemMeta(itemMeta)
        val greens = intArrayOf(0, 1, 2, 9, 10, 11, 18, 19, 20)
        for (i in greens.indices) {
            inv.setItem(greens[i], green)
        }
        val red = ItemStack(Material.RED_STAINED_GLASS_PANE, 1)
        val itemMetaRed = red.itemMeta
        itemMetaRed.setDisplayName("§c§lキャンセル")
        red.setItemMeta(itemMetaRed)
        val reds = intArrayOf(6, 7, 8, 15, 16, 17, 24, 25, 26)
        for (i in reds.indices) {
            inv.setItem(reds[i], red)
        }
        inv.setItem(13, p.inventory.itemInMainHand)
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

    fun withDrawInventory(): Inventory {
        val inv = Bukkit.createInventory(null, 54, "§c§l§n引き出し金額を入力してください")
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
        val cancel = ItemStack(Material.REDSTONE_BLOCK, 1)
        val Accept = ItemStack(Material.EMERALD_BLOCK, 1)
        val clear = ItemStack(Material.TNT, 1)
        val clearm = clear.itemMeta
        clearm.setDisplayName("§c§lクリア")
        clear.setItemMeta(clearm)
        val a = ArrayList<String>()
        val am = Accept.itemMeta
        val cm = cancel.itemMeta
        a.add("§d§l掛け金")
        am.setDisplayName("§a§l確認")
        cm.setDisplayName("§c§lキャンセル")
        Accept.setItemMeta(am)
        cancel.setItemMeta(cm)
        val i0m = i0!!.itemMeta
        val i1m = i1!!.itemMeta
        val i2m = i2!!.itemMeta
        val i3m = i3!!.itemMeta
        val i4m = i4!!.itemMeta
        val i5m = i5!!.itemMeta
        val i6m = i6!!.itemMeta
        val i7m = i7!!.itemMeta
        val i8m = i8!!.itemMeta
        val i9m = i9!!.itemMeta
        i0m.setDisplayName("§7§l0")
        i1m.setDisplayName("§7§l1")
        i2m.setDisplayName("§7§l2")
        i3m.setDisplayName("§7§l3")
        i4m.setDisplayName("§7§l4")
        i5m.setDisplayName("§7§l5")
        i6m.setDisplayName("§7§l6")
        i7m.setDisplayName("§7§l7")
        i8m.setDisplayName("§7§l8")
        i9m.setDisplayName("§7§l9")
        i0.setItemMeta(i0m)
        i1.setItemMeta(i1m)
        i2.setItemMeta(i2m)
        i3.setItemMeta(i3m)
        i4.setItemMeta(i4m)
        i5.setItemMeta(i5m)
        i6.setItemMeta(i6m)
        i7.setItemMeta(i7m)
        i8.setItemMeta(i8m)
        i9.setItemMeta(i9m)
        val blueGlass = ItemStack(Material.BLUE_STAINED_GLASS_PANE, 1, 11.toShort())
        val itemMeta = blueGlass.itemMeta
        itemMeta.setDisplayName(" ")
        blueGlass.setItemMeta(itemMeta)
        val bGlass = intArrayOf(9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 22, 23, 24, 25, 26, 27, 31, 32, 33, 34, 35, 36, 44, 45, 53)
        for (i in bGlass.indices) {
            inv.setItem(bGlass[i], blueGlass)
        }
        val B = SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/3d43e5b3e8d14ab8f9d2318e56de4aa026e3241112426c5edd5015e6b9a6b71").withName("§1§l§nBANK").build()
        val A = SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/adb5f1a9f58c852b473b3855dce27f8bf40db7e4bd2951e62f28d61c3694ff").withName("§1§l§nBANK").build()
        val N = SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/785b8c8ae5eae18fa5fcae88d5bca351c93144384f9c4a22f75cd642d5796").withName("§1§l§nBANK").build()
        val K = SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/b331cc913f191ae9bda4ce98d05929a6fcc41622eaa8a7ed52c6c724919b31").withName("§1§l§nBANK").build()
        inv.setItem(8, i0)
        inv.setItem(46, i0)
        inv.setItem(37, i1)
        inv.setItem(38, i2)
        inv.setItem(39, i3)
        inv.setItem(28, i4)
        inv.setItem(29, i5)
        inv.setItem(30, i6)
        inv.setItem(19, i7)
        inv.setItem(20, i8)
        inv.setItem(21, i9)
        inv.setItem(23, B)
        inv.setItem(24, A)
        inv.setItem(25, N)
        inv.setItem(26, K)
        inv.setItem(48, clear)
        val accept = intArrayOf(40, 41, 49, 50)
        val cancell = intArrayOf(42, 43, 51, 52)
        for (i in accept.indices) {
            inv.setItem(accept[i], Accept)
            inv.setItem(cancell[i], cancel)
        }
        return inv
    }

    init {
        this.plugin = plugin
    }
}