package red.man10.man10economynote;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static red.man10.man10economynote.EconomyNoteEvent.format;


/**
 * Created by sho on 2017/12/15.
 */
public class ChequeCommand implements CommandExecutor {
    Man10EconomyNote plugin = null;

    public ChequeCommand(Man10EconomyNote plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof ConsoleCommandSender){
            sender.sendMessage("This command can only be executed by a player.");
            return false;
        }
        Player p = (Player) sender;
        if (!p.hasPermission("man10.economynote.cheque.create")){
            p.sendMessage("§e[§dMan10EconNote§e]§cあなたは権限を持っていません");
            return false;
        }
        if (args.length == 1){
            if(args[0].equalsIgnoreCase("help")){
                help(p);
                return false;
            }
            if(p.getInventory().firstEmpty() == -1){
                p.sendMessage("§e[§dMan10EconNote§e]§cインベントリがいっぱいです");
                return false;
            }
            try {
                long i = Long.parseLong(args[0]);
                if(i <= 0){
                    p.sendMessage("§e[§dMan10EconNote§e]§c金額は1以上でなくてはなりません");
                    return false;
                }
                if(Man10EconomyNote.vault.getBalance(p.getUniqueId()) < i){
                    p.sendMessage("§e[§dMan10EconNote§e]§c所持金額が足りません");
                    return false;
                }
//                plugin.vault.takePlayerMoney(p.getUniqueId(),(double) i, TransactionType.SEND_CHEQUE, "Created Cheque user:" + p.getName() + " price:" + i, TransactionLogType.RAW );
                Man10EconomyNote.vault.withdraw(p.getUniqueId(),i);
                ChequeResult res = createChequeData(p.getName(), p.getUniqueId(), i, null);

                ItemStack blueDye = new ItemStack(Material.LIGHT_BLUE_DYE, 1);
                ItemMeta itemMeta = blueDye.getItemMeta();
                itemMeta.setDisplayName("§b§l小切手§7§l(Cheque)");
                itemMeta.setCustomModelData(10);
                List<String> lore = new ArrayList<>();
                lore.add("§e====[Man10Bank]====" + format(String.valueOf(res.getId())));
                lore.add("");
                lore.add("§a§l発行者:" + p.getName());
                lore.add("§a§l金額:" + Man10EconomyNote.formatMoney(i) + "円") ;
                lore.add("");
                lore.add("§e==================");
                itemMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1,true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                itemMeta.setLore(lore);
                blueDye.setItemMeta(itemMeta);
                plugin.createLog(res.getId(),p.getName(),p.getUniqueId(),"CreateCheque",i);
                p.getInventory().addItem(blueDye);
            }catch (NumberFormatException e){
                p.sendMessage("§e[§dMan10EconNote§e]§b金額は数字でなくてはなりません");
                return false;
            }
        }else if (args.length == 2){
            try {
                long i = Long.parseLong(args[0]);
                if(i <= 0){
                    p.sendMessage("§e[§dMan10EconNote§e]§c金額は1以上でなくてはなりません");
                    return false;
                }
                if(p.getInventory().firstEmpty() == -1){
                    p.sendMessage("§e[§dMan10EconNote§e]§cインベントリがいっぱいです");
                    return false;
                }
                if(args[1].length() >= 128){
                    p.sendMessage("§e[§dMan10EconNote§e]§cメモが長すぎます");
                    return false;
                }
                if(Man10EconomyNote.vault.getBalance(p.getUniqueId()) < i){
                    p.sendMessage("§e[§dMan10EconNote§e]§c所持金額が足りません");
                    return false;
                }
//                plugin.vault.takePlayerMoney(p.getUniqueId(),(double) i, TransactionType.SEND_CHEQUE, "Created Cheque user:" + p.getName() + " price:" + i, TransactionLogType.RAW );
                Man10EconomyNote.vault.withdraw(p.getUniqueId(),i);

                ChequeResult res = createChequeData(p.getName(), p.getUniqueId(), i, args[1].replace("'","\\'"));

                ItemStack blueDye = new ItemStack(Material.LIGHT_BLUE_DYE, 1);
                ItemMeta itemMeta = blueDye.getItemMeta();
                itemMeta.setDisplayName("§b§l小切手§7§l(Cheque)");
                itemMeta.setCustomModelData(10);
                List<String> lore = new ArrayList<>();
                lore.add("§e====[Man10Bank]====" + format(String.valueOf(res.getId())));
                lore.add("");
                lore.add("§a§l発行者:" + p.getName());
                lore.add("§a§l金額:" + Man10EconomyNote.formatMoney(i) + "円") ;
                if(args[1] != null || !args[1].equalsIgnoreCase("")){
                    lore.add("§d§lメモ:" + args[1].replaceAll("&", "§").replaceAll("_", " "));
                }
                lore.add("");
                lore.add("§e==================");
                itemMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1,true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                itemMeta.setLore(lore);
                blueDye.setItemMeta(itemMeta);
                plugin.createLog(res.getId(),p.getName(),p.getUniqueId(),"CreateCheque",i);
                p.getInventory().addItem(blueDye);
            }catch (NumberFormatException e){
                p.sendMessage("§e[§dMan10EconNote§e]§b金額は数字でなくてはなりません");
                return false;
            }
        }else{
            help(p);
        }
        return false;
    }

    class ChequeResult{
        private boolean memo = false;
        private int id = -1;
        public ChequeResult(int id, boolean memo){
            this.memo = memo;
            this.id = id;

        }

        public int getId(){
            return id;
        }

        public boolean getMemo(){
            return memo;
        }

    }

    public void help(Player p){
        p.sendMessage("§e§l-----[§d§lMan10EconomyNote§e§l]-----");
        p.sendMessage("");
        p.sendMessage("§b/mcheque <金額> 小切手を作る");
        p.sendMessage("§b/mcheque <金額> <メモ> 小切手を作る");
        p.sendMessage("");
        p.sendMessage("§e§l---------------------------");
        p.sendMessage("§6§lCreated By Sho0");
    }

    private ChequeResult createChequeData(String name, UUID uuid,long value,String memo){
        if(memo == null || memo.equalsIgnoreCase("")){
            int id = plugin.mysql.executeGetId("INSERT INTO man10_economy_note (`id`,`type`,`wired_to_name`,`wired_to_uuid`,`base_value`,`memo`,`creation_date_time`,`creation_time`,`usable_date_time`,`usable_time`,`expired`,`final_value`) VALUES ('0','Cheque','" + name + "','" + uuid + "','" + value + "','" + memo + "','" + plugin.mysql.currentTimeNoBracket() + "','" + System.currentTimeMillis()/1000 + "','" + plugin.mysql.currentTimeNoBracket() + "','" + System.currentTimeMillis()/1000 + "','0','" + value + "');");
            return new ChequeResult(id, false);
        }
        int id = plugin.mysql.executeGetId("INSERT INTO man10_economy_note (`id`,`type`,`wired_to_name`,`wired_to_uuid`,`base_value`,`memo`,`creation_date_time`,`creation_time`,`usable_date_time`,`usable_time`,`expired`,`final_value`) VALUES ('0','Cheque','" + name + "','" + uuid + "','" + value + "','" + memo + "','" + plugin.mysql.currentTimeNoBracket() + "','" + System.currentTimeMillis()/1000 + "','" + plugin.mysql.currentTimeNoBracket() + "','" + System.currentTimeMillis()/1000 + "','0','" + value + "');");
        return new ChequeResult(id, true);
    }

//    private String format(String string){
//        char[] list = string.toCharArray();
//        String finalString = "";
//        for (char c : list) {
//            finalString = finalString + "§" + c;
//        }
//        return finalString.replaceAll("0","０").replaceAll("1","１")
//                .replaceAll("2","２").replaceAll("3","３")
//                .replaceAll("4","４").replaceAll("5","５")
//                .replaceAll("6","６").replaceAll("8","８")
//                .replaceAll("7","７").replaceAll("9","９");
//    }

}
