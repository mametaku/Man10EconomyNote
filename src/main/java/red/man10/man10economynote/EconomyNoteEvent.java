package red.man10.man10economynote;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import red.man10.man10vaultapiplus.JPYBalanceFormat;
import red.man10.man10vaultapiplus.MoneyPoolObject;
import red.man10.man10vaultapiplus.enums.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sho on 2017/12/15.
 */
public class EconomyNoteEvent implements Listener {
    Man10EconomyNote plugin = null;

    public EconomyNoteEvent(Man10EconomyNote plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        if(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR){
            if(e.getPlayer().getInventory().getItemInMainHand().getType() == Material.PINK_DYE && e.getPlayer().getInventory().getItemInMainHand().getItemMeta().getLore().get(0).contains("§6====[Man10Bank]====" )){
                plugin.slotData.put(e.getPlayer().getUniqueId(), e.getPlayer().getInventory().getHeldItemSlot());
                String id = e.getPlayer().getInventory().getItemInMainHand().getItemMeta().getLore().get(0).replace("§6====[Man10Bank]====", "").replace("§", "");
                Bukkit.broadcastMessage("[debug]ID:"+id);
                LendData ld = plugin.getLendData(Integer.parseInt(id));
                if(ld == null){
                    e.getPlayer().sendMessage("§e[§dMan10EconNote§e]§bデータが存在しません");
                    plugin.slotData.remove(e.getPlayer().getUniqueId());
                    return;
                }
                Player target = Bukkit.getPlayer(ld.uuid);
                if(System.currentTimeMillis()/1000 <= ld.creationTime + (ld.usableDays * 86400)){
                    e.getPlayer().sendMessage("§e[§dMan10EconNote§e]§c§l現在この手形はまだ使えません");
                    return;
                }
                if(target == null){
                    e.getPlayer().sendMessage("§e[§dMan10EconNote§e]§c§l現在プレイヤーはオンラインではありません");
                    return;
                }
                if(ld.valueLeft == 0){
                    e.getPlayer().sendMessage("§e[§dMan10EconNote§e]§c§l手形の残高がありません");
                    return;
                }
                e.getPlayer().openInventory(withDrawInventory());
                plugin.withdrawMenu.put(e.getPlayer().getUniqueId(), 0L);
                plugin.inventoryMap.put(e.getPlayer().getUniqueId(), "withdrawMenu");
                plugin.lendDataMap.put(e.getPlayer().getUniqueId(), ld);
            }
            if(e.getPlayer().getInventory().getItemInMainHand().getType() == Material.LIGHT_BLUE_DYE &&e.getPlayer().getInventory().getItemInMainHand().getItemMeta().getLore().get(0).contains("§e====[Man10Bank]====" )){
                plugin.slotData.put(e.getPlayer().getUniqueId(), e.getPlayer().getInventory().getHeldItemSlot());
                String id = e.getPlayer().getInventory().getItemInMainHand().getItemMeta().getLore().get(0).replace("§e====[Man10Bank]====", "").replace("§", "");
                Bukkit.broadcastMessage("[debug]ID:"+id);
                Man10EconomyNote.NoteData nd = plugin.getNoteData(Integer.parseInt(id));
                if(nd == null){
                    e.getPlayer().sendMessage("§e[§dMan10EconNote§e]§bデータが存在しません");
                    e.getPlayer().getInventory().setItem(plugin.slotData.get(e.getPlayer().getUniqueId()), new ItemStack(Material.AIR));
                    plugin.slotData.remove(e.getPlayer().getUniqueId());
                    return;
                }
                e.getPlayer().openInventory(createChequeInventory(e.getPlayer(), nd.getValue()));
                plugin.inventoryMap.put(e.getPlayer().getUniqueId(), "chequeConfirm");
                plugin.noteDataMap.put(e.getPlayer().getUniqueId(), nd);
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if(!plugin.inventoryMap.isEmpty()){
            if(plugin.inventoryMap.containsKey(e.getWhoClicked().getUniqueId())){
                if(plugin.inventoryMap.get(e.getWhoClicked().getUniqueId()).equals("withdrawMenu")){
                    e.setCancelled(true);
                    LendData ld = plugin.lendDataMap.get(e.getWhoClicked().getUniqueId());
                    int s = e.getSlot();
                    if(s == 48){
                        plugin.withdrawMenu.put(e.getWhoClicked().getUniqueId(), 0L);
                        for(int i = 0;i < 9;i++){
                            e.getInventory().setItem(i, new ItemStack(Material.AIR));
                        }
                        redner(e.getInventory(),plugin.withdrawMenu.get(e.getWhoClicked().getUniqueId()));
                    }
                    if(s == 42 || s == 43 || s == 51 || s == 52){
                        e.getWhoClicked().closeInventory();
                        return;
                    }
                    if(s == 40 || s == 41 || s == 49 || s == 50){
                        if(plugin.withdrawMenu.get(e.getWhoClicked().getUniqueId()) == 0){
                            e.getWhoClicked().sendMessage("§e[§dMan10EconNote§e]§c§l金額は1以上でなくてはなりません");
                            return;
                        }
                        Player target = Bukkit.getPlayer(ld.uuid);
                        if(target == null){
                            e.getWhoClicked().sendMessage("§e[§dMan10EconNote§e]§c§l現在プレイヤーはオンラインではありません");
                            return;
                        }
                        if(plugin.vault.getBalance(target.getUniqueId()) < plugin.withdrawMenu.get(e.getWhoClicked().getUniqueId())){
                            e.getWhoClicked().sendMessage("§e[§dMan10EconNote§e]§c§l現在プレイヤーお金を十分に持っていません");
                            return;
                        }
                        if(ld.valueLeft < plugin.withdrawMenu.get(e.getWhoClicked().getUniqueId())){
                            e.getWhoClicked().sendMessage("§e[§dMan10EconNote§e]§c§l手形の残高を超す請求をしています");
                            return;
                        }
                        plugin.vault.transferMoneyPlayerToPlayer(ld.uuid, e.getWhoClicked().getUniqueId(),plugin.withdrawMenu.get(e.getWhoClicked().getUniqueId()), TransactionCategory.ECONOMY_NOTE, TransactionType.COLLECT, "Economy note withdrawal by:" + e.getWhoClicked().getName() + " to:" + ld.name  + " price:" + plugin.withdrawMenu.get(e.getWhoClicked().getUniqueId()) );
                        ItemStack item = e.getWhoClicked().getInventory().getItem(plugin.slotData.get(e.getWhoClicked().getUniqueId()));
                        ItemMeta itemMeta = item.getItemMeta();
                        List<String> lore = itemMeta.getLore();
                        lore.set(4, "§d§l残金:" + String.valueOf(ld.valueLeft - plugin.withdrawMenu.get(e.getWhoClicked().getUniqueId())));
                        itemMeta.setLore(lore);
                        item.setItemMeta(itemMeta);
                        plugin.mysql.execute("UPDATE man10_economy_note SET value_left ='" +  String.valueOf(ld.valueLeft - plugin.withdrawMenu.get(e.getWhoClicked().getUniqueId())) + "' WHERE id ='" + ld.id + "'");
                        plugin.lendDataCacheMap.remove(ld.id);
                        target.sendMessage("§e[§dMan10EconNote§e]§c§l" + e.getWhoClicked().getName() + "はあなたの約束手形から" + plugin.withdrawMenu.get(e.getWhoClicked().getUniqueId()) + "円引き出しました");
                        plugin.createLog(ld.id,e.getWhoClicked().getName(), e.getWhoClicked().getUniqueId(), "RedeemPromissoryNote", plugin.withdrawMenu.get(e.getWhoClicked().getUniqueId()));
                        e.getWhoClicked().closeInventory();
                        if(ld.valueLeft == 0){
                            plugin.mysql.execute("UPDATE man10_economy_note SET expired ='1' WHERE id  ='" + ld.id + "'");
                        }
                        return;
                    }
                    long val = plugin.withdrawMenu.get(e.getWhoClicked().getUniqueId());
                    if(plugin.tenKeyNum.get(s) == null){
                        return;
                    }
                    if(plugin.withdrawMenu.get(e.getWhoClicked().getUniqueId()) == 0){
                        plugin.withdrawMenu.put(e.getWhoClicked().getUniqueId(), Long.valueOf(plugin.tenKeyNum.get(s)));
                        redner(e.getInventory(),plugin.withdrawMenu.get(e.getWhoClicked().getUniqueId()));
                        return;
                    }
                    if(plugin.vault.getBalance(plugin.lendDataMap.get(e.getWhoClicked().getUniqueId()).uuid) <= plugin.withdrawMenu.get(e.getWhoClicked().getUniqueId())){
                        e.getWhoClicked().sendMessage("§e[§dMan10EconNote§e]§c§lプレイヤーはこれ以上お金を持っていません");
                        if(!(plugin.vault.getBalance(ld.uuid) < 0)){
                            plugin.withdrawMenu.put(e.getWhoClicked().getUniqueId(), (long) plugin.vault.getBalance(plugin.lendDataMap.get(e.getWhoClicked().getUniqueId()).uuid));
                            redner(e.getInventory(),plugin.withdrawMenu.get(e.getWhoClicked().getUniqueId()));
                        }
                        return;
                    }
                    if(plugin.withdrawMenu.get(e.getWhoClicked().getUniqueId()) >= 999999999){
                        plugin.withdrawMenu.put(e.getWhoClicked().getUniqueId(), 999999999L);
                        redner(e.getInventory(),plugin.withdrawMenu.get(e.getWhoClicked().getUniqueId()));
                        return;
                    }
                    plugin.withdrawMenu.put(e.getWhoClicked().getUniqueId(), Long.valueOf( String.valueOf(val) + plugin.tenKeyNum.get(s)));
                    redner(e.getInventory(),plugin.withdrawMenu.get(e.getWhoClicked().getUniqueId()));
                }
                    if(plugin.inventoryMap.get(e.getWhoClicked().getUniqueId()).equals("chequeConfirm")){
                        e.setCancelled(true);
                        int[] greens = {0,1,2,9,10,11,18,19,20};
                        int[] reds = {6,7,8,15,16,17,24,25,26};
                        for(int i = 0;i < greens.length;i++){
                            if(greens[i] == e.getSlot()){
                                ((Player)e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_PLAYER_LEVELUP,1,1);
                                Man10EconomyNote.NoteData nd = plugin.noteDataMap.get(e.getWhoClicked().getUniqueId());
                                e.getWhoClicked().getInventory().setItem(plugin.slotData.get(e.getWhoClicked().getUniqueId()), new ItemStack(Material.AIR));
                                plugin.slotData.remove(e.getWhoClicked().getUniqueId());
                                plugin.createLog(nd.getId(),e.getWhoClicked().getName(),e.getWhoClicked().getUniqueId(),"RedeemCheque",nd.getValue());
                                plugin.mysql.execute("UPDATE man10_economy_note SET expired ='1' WHERE id=" + nd.getId());
                                plugin.noteCacheMap.remove(nd.getId());
                                double toOldBalance = plugin.vault.getBalance(e.getWhoClicked().getUniqueId());
                                plugin.vault.givePlayerMoney(e.getWhoClicked().getUniqueId(), nd.getValue(), TransactionType.REDEEM_CHEQUE, "Man10 Cheque Collected by :" + e.getWhoClicked().getName() + " from:" + nd.getName() + " value:" + nd.getValue(), TransactionLogType.RAW);
                                plugin.vault.createTransactionLog(TransactionCategory.VOID, TransactionType.REDEEM_CHEQUE, plugin.vault.getPluginName(), nd.getValue(), nd.getName(), nd.getUuid(), e.getWhoClicked().getName(), e.getWhoClicked().getUniqueId(), 0, 0, toOldBalance, toOldBalance+nd.getValue(), -1, TransactionLogType.RESULT, "Man10 Cheque Collected by :" + e.getWhoClicked().getName() + " from:" + nd.getName() + " value:" + nd.getValue());
                                e.getWhoClicked().closeInventory();
                            }
                        }
                        for(int i = 0;i < reds.length;i++){
                            if(reds[i] == e.getSlot()){
                                e.getWhoClicked().closeInventory();
                            }
                        }
                        return;
                    }
                    if(plugin.inventoryMap.get(e.getWhoClicked().getUniqueId()).equals("LendSendConfirm")){
                        e.setCancelled(true);
                        int[] greens = {0,1,2,9,10,11,18,19,20};
                        int[] reds = {6,7,8,15,16,17,24,25,26};
                        for(int i = 0;i < greens.length;i++){
                            if(greens[i] == e.getSlot()){
                                LendData ld = plugin.lendDataMap.get(e.getWhoClicked().getUniqueId());
                                if(Bukkit.getPlayer(ld.uuid) == null){
                                    e.getWhoClicked().sendMessage("§e[§dMan10EconNote§e]§cプレイヤーがオフラインになりました");
                                    e.getWhoClicked().closeInventory();
                                }
                                e.getWhoClicked().sendMessage("§e[§dMan10EconNote§e]§a条件を提示しました");
                                SentLendDataData data = new SentLendDataData(ld, e.getWhoClicked().getName(), e.getWhoClicked().getUniqueId());
                                Bukkit.getPlayer(ld.uuid).sendMessage("§e[§dMan10EconNote§e]§e§l" + e.getWhoClicked().getName() + "さんが借金条件を提示しました /mlend view");
                                plugin.sentLendDataDataHashMap.put(Bukkit.getPlayer(ld.uuid).getUniqueId(), data);
                                e.getWhoClicked().closeInventory();
                            }
                        }

                        for(int i = 0;i < reds.length;i++){
                            if(reds[i] == e.getSlot()){
                                e.getWhoClicked().closeInventory();
                            }
                        }
                        return;
                    }
                    if(plugin.inventoryMap.get(e.getWhoClicked().getUniqueId()).equals("LendConfirm")){
                        e.setCancelled(true);
                        int[] greens = {0,1,2,9,10,11,18,19,20};
                        int[] reds = {6,7,8,15,16,17,24,25,26};
                        for(int i = 0;i < greens.length;i++){
                            if(greens[i] == e.getSlot()){
                                Player lender = Bukkit.getPlayer(plugin.sentLendDataDataHashMap.get(e.getWhoClicked().getUniqueId()).fromUUID);
                                if(lender == null){
                                    e.getWhoClicked().sendMessage("§e[§dMan10EconNote§e]§c§l提示者がオフラインになりました");
                                    return;
                                }
                                if(lender.getInventory().firstEmpty() == -1){
                                    e.getWhoClicked().sendMessage("§e[§dMan10EconNote§e]§c§l提示者のインベントリがいっぱいです");
                                    return;
                                }
                                if(plugin.vault.getBalance(plugin.sentLendDataDataHashMap.get(e.getWhoClicked().getUniqueId()).fromUUID) <= plugin.sentLendDataDataHashMap.get(e.getWhoClicked().getUniqueId()).data.finalValueLender){
                                    e.getWhoClicked().sendMessage("§e[§dMan10EconNote§e]§c§l提示者の所持金が提示金額に達していません");
                                    return;
                                }
                                LendData ld = plugin.sentLendDataDataHashMap.get(e.getWhoClicked().getUniqueId()).data;

                                long usableTimeStamp = ld.usableDays * 24 * 60 * 60 + System.currentTimeMillis() / 1000;
                                java.util.Date date = new java.util.Date(usableTimeStamp * 1000);
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy'-'MM'-'dd' 'HH':'mm':'ss");
                                java.util.Date dateExpire = new java.util.Date((usableTimeStamp + 31536000) * 1000);
                                int id = plugin.mysql.executeGetId("INSERT INTO man10_economy_note (`id`,`type`,`wired_to_name`,`wired_to_uuid`,`base_value`,`final_value`,`value_left`,`monthly_interest`,`usable_after_days`,`memo`,`expired`,`creation_date_time`,`creation_time`,`usable_date_time`,`usable_time`,`expire_date_time`,`expire_time`) " +
                                        "VALUES ('0','" + "PromissoryNote','" + ld.name + "','" + ld.uuid + "','" + ld.baseValue + "','" + ld.finalValue + "','" + ld.finalValue + "','" + ld.interest + "','" + ld.usableDays + "','','0','" + plugin.mysql.currentTimeNoBracket() + "','" + System.currentTimeMillis()/1000 + "','" + sdf.format(date) + "','" + usableTimeStamp + "','" + sdf.format(dateExpire) + "','" + (usableTimeStamp + 631536000) + "');");
                                ItemStack ink = new ItemStack(Material.PINK_DYE, 1);
                                ItemMeta inkMeta = ink.getItemMeta();
                                inkMeta.setDisplayName("§c§l約束手形§7§l(Promissory Note)");
                                inkMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
                                inkMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                                List<String> inkLore = new ArrayList<>();
                                SimpleDateFormat sdff = new SimpleDateFormat("yyyy'年'MM'月'dd'日'E'曜日'k'時'mm'分'ss'秒'");
                                inkLore.add("§6====[Man10Bank]====" + format(String.valueOf(id)));
                                inkLore.add("");
                                inkLore.add("§c§l発行者:" + ld.name);
                                inkLore.add("§c§l金額:" + ld.finalValue);
                                java.util.Date time = new java.util.Date((long) usableTimeStamp * 1000);
                                inkLore.add("§d§l残金:" + ld.finalValue);
                                inkLore.add("§4使用可能日");
                                inkLore.add("§4(" + sdff.format(time) + ")");
                                inkLore.add("");
                                inkLore.add("§6==================");
                                inkMeta.setLore(inkLore);
                                ink.setItemMeta(inkMeta);

                                plugin.createLog(id,plugin.sentLendDataDataHashMap.get(e.getWhoClicked().getUniqueId()).fromName,plugin.sentLendDataDataHashMap.get(e.getWhoClicked().getUniqueId()).fromUUID,"CreatePromissoryNote",ld.finalValue);
                                MoneyPoolObject pool = new MoneyPoolObject(plugin.vault.getPluginName(), MoneyPoolTerm.SHORT_TERM, MoneyPoolType.MEMORY, "Man10 EconomyNote MoneyFlow Pool");
                                plugin.vault.transferMoneyPlayerToPool(plugin.sentLendDataDataHashMap.get(e.getWhoClicked().getUniqueId()).fromUUID, pool.getId(), ld.finalValueLender, TransactionCategory.ECONOMY_NOTE, TransactionType.LEND, "PromissoryNote money send");
                                plugin.vault.transferMoneyPoolToPlayer(pool.getId(),e.getWhoClicked().getUniqueId(), ld.baseValue,TransactionCategory.ECONOMY_NOTE, TransactionType.LEND, "PromissoryNote money receive");
                                Bukkit.getPlayer(plugin.sentLendDataDataHashMap.get(e.getWhoClicked().getUniqueId()).fromUUID).getInventory().addItem(ink);
                                pool.sendRemainderToCountry("PromissoryNote Tax Fee Send");
                                e.getWhoClicked().sendMessage("§e[§dMan10EconNote§e]§a取引が成立しました");
                                e.getWhoClicked().closeInventory();
                            }
                        }
                        for(int i = 0;i < reds.length;i++){
                            if(reds[i] == e.getSlot()){
                                e.getWhoClicked().closeInventory();
                                plugin.sentLendDataDataHashMap.remove(e.getWhoClicked().getUniqueId());
                            }
                        }
                        return;
                    }
            }
        }
    }

    public void redner(Inventory inv, long value){
        String val = String.valueOf(value);
        List<ItemStack> items = new ArrayList<>();
        for(int i = 0;i < val.toCharArray().length;i++){
            items.add(plugin.itemHead.get(String.valueOf(val.toCharArray()[i])));
        }
        int startFrom = (9 - items.size());
        for(int i = 0;i < items.size();i++){
            inv.setItem(i + startFrom,items.get(i));
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e){
        if(!plugin.inventoryMap.isEmpty()){
            if(plugin.inventoryMap.containsKey(e.getPlayer().getUniqueId())){
                plugin.inventoryMap.remove(e.getPlayer().getUniqueId());
                plugin.noteDataMap.remove(e.getPlayer().getUniqueId());
                plugin.lendDataMap.remove(e.getPlayer().getUniqueId());
                plugin.withdrawMenu.remove(e.getPlayer().getUniqueId());
                plugin.sentLendDataDataHashMap.remove(e.getPlayer().getUniqueId());
            }
        }
    }

    Inventory createChequeInventory(Player p, long value){
        Inventory inv = Bukkit.createInventory(null, 27,"§4§l" + new JPYBalanceFormat(value).getString() + "円と換金しますか？");
        ItemStack green = new ItemStack(Material.GREEN_STAINED_GLASS_PANE,1);
        ItemMeta itemMeta = green.getItemMeta();
        itemMeta.setDisplayName("§a§l" + value + "円と換金する");
        List<String> lore = new ArrayList<>();
        lore.add("§a§l(" + new JPYBalanceFormat(value).getString()+ "円)");
        itemMeta.setLore(lore);
        green.setItemMeta(itemMeta);
        int[] greens = {0,1,2,9,10,11,18,19,20};
        for(int i = 0;i < greens.length;i++){
            inv.setItem(greens[i], green);
        }

        ItemStack red = new ItemStack(Material.RED_STAINED_GLASS_PANE,1);
        ItemMeta itemMetaRed = red.getItemMeta();
        itemMetaRed.setDisplayName("§c§lキャンセル");
        red.setItemMeta(itemMetaRed);

        int[] reds = {6,7,8,15,16,17,24,25,26};
        for(int i = 0;i < reds.length;i++){
            inv.setItem(reds[i], red);
        }

        inv.setItem(13, p.getInventory().getItemInMainHand());
        return inv;
    }

    private String format(String string){
        char[] list = string.toCharArray();
        String finalString = "";
        for(int i = 0;i < list.length;i++){
            finalString = finalString + "§" + list[i];
        }
        return finalString.replaceAll("0","０").replaceAll("1","１")
                .replaceAll("2","２").replaceAll("3","３")
                .replaceAll("4","４").replaceAll("5","５")
                .replaceAll("6","６").replaceAll("8","８")
                .replaceAll("7","７").replaceAll("9","９");
    }

    Inventory withDrawInventory(){
        Inventory inv = Bukkit.createInventory(null,54,"§c§l§n引き出し金額を入力してください");
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
        ItemStack cancel = new ItemStack(Material.REDSTONE_BLOCK, 1);
        ItemStack Accept = new ItemStack(Material.EMERALD_BLOCK, 1);
        ItemStack clear = new ItemStack(Material.TNT, 1);
        ItemMeta clearm = clear.getItemMeta();
        clearm.setDisplayName("§c§lクリア");
        clear.setItemMeta(clearm);
        ArrayList<String> a = new ArrayList<String>();
        ItemMeta am = Accept.getItemMeta();
        ItemMeta cm = cancel.getItemMeta();
        a.add("§d§l掛け金");
        am.setDisplayName("§a§l確認");
        cm.setDisplayName("§c§lキャンセル");
        Accept.setItemMeta(am);
        cancel.setItemMeta(cm);
        ItemMeta i0m = i0.getItemMeta();
        ItemMeta i1m = i1.getItemMeta();
        ItemMeta i2m = i2.getItemMeta();
        ItemMeta i3m = i3.getItemMeta();
        ItemMeta i4m = i4.getItemMeta();
        ItemMeta i5m = i5.getItemMeta();
        ItemMeta i6m = i6.getItemMeta();
        ItemMeta i7m = i7.getItemMeta();
        ItemMeta i8m = i8.getItemMeta();
        ItemMeta i9m = i9.getItemMeta();
        i0m.setDisplayName("§7§l0");
        i1m.setDisplayName("§7§l1");
        i2m.setDisplayName("§7§l2");
        i3m.setDisplayName("§7§l3");
        i4m.setDisplayName("§7§l4");
        i5m.setDisplayName("§7§l5");
        i6m.setDisplayName("§7§l6");
        i7m.setDisplayName("§7§l7");
        i8m.setDisplayName("§7§l8");
        i9m.setDisplayName("§7§l9");
        i0.setItemMeta(i0m);
        i1.setItemMeta(i1m);
        i2.setItemMeta(i2m);
        i3.setItemMeta(i3m);
        i4.setItemMeta(i4m);
        i5.setItemMeta(i5m);
        i6.setItemMeta(i6m);
        i7.setItemMeta(i7m);
        i8.setItemMeta(i8m);
        i9.setItemMeta(i9m);

        ItemStack blueGlass = new ItemStack(Material.BLUE_STAINED_GLASS_PANE,1,(short) 11);
        ItemMeta itemMeta = blueGlass.getItemMeta();
        itemMeta.setDisplayName(" ");
        blueGlass.setItemMeta(itemMeta);

        int[] bGlass = {9,10,11,12,13,14,15,16,17,18,22,23,24,25,26,27,31,32,33,34,35,36,44,45,53};
        for(int i = 0;i < bGlass.length;i++){
            inv.setItem(bGlass[i],blueGlass);
        }
        ItemStack B = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/3d43e5b3e8d14ab8f9d2318e56de4aa026e3241112426c5edd5015e6b9a6b71").withName("§1§l§nBANK").build();
        ItemStack A = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/adb5f1a9f58c852b473b3855dce27f8bf40db7e4bd2951e62f28d61c3694ff").withName("§1§l§nBANK").build();
        ItemStack N = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/785b8c8ae5eae18fa5fcae88d5bca351c93144384f9c4a22f75cd642d5796").withName("§1§l§nBANK").build();
        ItemStack K = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/b331cc913f191ae9bda4ce98d05929a6fcc41622eaa8a7ed52c6c724919b31").withName("§1§l§nBANK").build();
        inv.setItem(8,i0);
        inv.setItem(46,i0);
        inv.setItem(37,i1);
        inv.setItem(38,i2);
        inv.setItem(39,i3);
        inv.setItem(28,i4);
        inv.setItem(29,i5);
        inv.setItem(30,i6);
        inv.setItem(19,i7);
        inv.setItem(20,i8);
        inv.setItem(21,i9);
        inv.setItem(23,B);
        inv.setItem(24,A);
        inv.setItem(25,N);
        inv.setItem(26,K);
        inv.setItem(48,clear);

        int[] accept = {40,41,49,50};
        int[] cancell = {42,43,51,52};
        for(int i = 0;i < accept.length;i++){
            inv.setItem(accept[i],Accept);
            inv.setItem(cancell[i],cancel);
        }
        return inv;
    }
}
