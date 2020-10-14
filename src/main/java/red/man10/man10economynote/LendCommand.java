package red.man10.man10economynote;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by sho on 2017/12/15.
 */
public class LendCommand implements CommandExecutor {
    Man10EconomyNote plugin = null;

    public LendCommand(Man10EconomyNote plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof ConsoleCommandSender){
            sender.sendMessage("This command can only be executed by a player.");
            return false;
        }
        Player p = (Player) sender;
        if(args.length == 1){
            if(args[0].equalsIgnoreCase("view")){
                if(!p.hasPermission("man10.economynote.cheque.view")){
                    p.sendMessage("§e[§dMan10EconNote§e]§cあなたは権限を持っていません");
                    return false;
                }
                if(plugin.sentLendDataDataHashMap.get(p.getUniqueId()) == null){
                    p.sendMessage("§e[§dMan10EconNote§e]§c現在あなたに提示は来てません");
                    return false;
                }
                p.openInventory(createLendConfirmMenu(p,plugin.sentLendDataDataHashMap.get(p.getUniqueId()).data));
                plugin.inventoryMap.put(p.getUniqueId(), "LendConfirm");
                return false;
            }
            if(args[0].equalsIgnoreCase("help")){
                help(p);
                return false;
            }
        }
        if (args.length == 4){
            if(!p.hasPermission("man10.economynote.cheque.lend")){
                p.sendMessage("§e[§dMan10EconNote§e]§cあなたは権限を持っていません");
                return false;
            }
            try{

                Player target  = Bukkit.getPlayer(args[0]);
                if(target == null){
                    p.sendMessage("§e[§dMan10EconNote§e]§cプレイヤーがオフラインです");
                    return false;
                }
                if(p.getName().equals(target.getName())){
                    p.sendMessage("§e[§dMan10EconNote§e]§c自分には申請できません");
                    return false;
                }
                long value = Long.parseLong(args[1]);
                int days = Integer.parseInt(args[2]);
                double intrest = Double.parseDouble(args[3]);
                double finalIntrest = days * (intrest/30);

                if(value <= 0){
                    p.sendMessage("§e[§dMan10EconNote§e]§b金額は1以上でなくてはなりません");
                    return false;
                }
                if(!(intrest >= 0 && intrest <= 0.5)){
                    p.sendMessage("§e[§dMan10EconNote§e]§b金利は0 ～ 0.5 でなくてはなりません");
                    return false;
                }
                if(days < 0 || days > 100){
                    p.sendMessage("§e[§dMan10EconNote§e]§c借用日数は100日以内でなくてはなりません");
                    return false;
                }
                if(plugin.inventoryMap.get(target.getUniqueId()) != null && plugin.inventoryMap.get(target.getUniqueId()).equals("LendConfirm")){
                    p.sendMessage("§e[§dMan10EconNote§e]§c現在プレイヤーは借金の審議中です");
                    return false;
                }
                long finalValue = (long) (value + (value * plugin.tax));
                if(plugin.vault.getBalance(p.getUniqueId()) < value + (value * plugin.tax)){
                    p.sendMessage("§e[§dMan10EconNote§e]§b所持金が税金を足した" +  (value + (value * plugin.tax)) + "に達していません");
                    return false;
                }
                long finalPayerValue = (long) (value + (value * finalIntrest));
                LendData ld = new LendData(target.getName(),target.getUniqueId(),value,finalPayerValue,days,finalValue,intrest,finalValue,0,System.currentTimeMillis()/1000);
                p.openInventory(createLendSendConfirmMenu(target,ld));
                plugin.inventoryMap.put(p.getUniqueId(), "LendSendConfirm");
                plugin.lendDataMap.put(p.getUniqueId(), ld);
                return false;
            }catch (NumberFormatException e){
                p.sendMessage("§e[§dMan10EconNote§e]§c数字的エラー");
                return false;
            }
        }
        help(p);

        return false;
    }

    public void help(Player p){
        p.sendMessage("§e§l-----[§d§lMan10EconomyNote§e§l]-----");
        p.sendMessage("");
        p.sendMessage("§b/mlend view 提示が来ていれば見る");
        p.sendMessage("§b/mlend <プレイヤー> <金額> <日数> <金利（月利）> 借金条件を提示する");
        p.sendMessage("");
        p.sendMessage("§e§l---------------------------");
        p.sendMessage("§6§lCreated By Sho0");
    }

    Inventory createLendConfirmMenu(Player p,LendData ld) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy'年'MM'月'dd'日'E'曜日'k'時'mm'分'ss'秒'");
        Inventory inv = Bukkit.createInventory(null, 27, "§4§lこの条件を受け入れますか？");

        ItemStack ink = new ItemStack(Material.PINK_DYE, 1);
        ItemMeta inkMeta = ink.getItemMeta();
        inkMeta.setCustomModelData(10);
        inkMeta.setDisplayName("§c§l約束手形§7§l(Promissory Note)");
        inkMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
        inkMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        List<String> inkLore = new ArrayList<>();
        inkLore.add("§6====[Man10Bank]====");
        inkLore.add("");
        inkLore.add("§c§l発行者:" + p.getName());
        inkLore.add("§c§l金額:" + ld.finalValue);
        long usableTimeStamp = ld.usableDays * 24 * 60 * 60 + System.currentTimeMillis() / 1000;
        java.util.Date time = new java.util.Date((long) usableTimeStamp * 1000);
        inkLore.add("§d§l残金:" + ld.finalValue);
        inkLore.add("§4使用可能日");
        inkLore.add("§4(" + sdf.format(time) + ")");
        inkLore.add("");
        inkLore.add("§6==================");
        inkMeta.setLore(inkLore);
        ink.setItemMeta(inkMeta);

        inv.setItem(13, ink);

        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta paperMeta = paper.getItemMeta();
        paperMeta.setDisplayName("§6§l契約内容");
        List<String> loree = new ArrayList<>();
        loree.add("§6===[Man10Bank]===");
        loree.add("§4借金金額:" + ld.baseValue);
        loree.add("§4返済金額:" + ld.finalValue);
        loree.add("§4取立開始日:" + ld.usableDays + "日後");
        loree.add("§4" + sdf.format(time));
        loree.add("§6==================");
        paperMeta.setLore(loree);
        paper.setItemMeta(paperMeta);

        inv.setItem(22, paper);

        ItemStack green = new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1);
        ItemMeta itemMeta = green.getItemMeta();
        itemMeta.setDisplayName("§a§l承諾する");
        green.setItemMeta(itemMeta);
        int[] greens = {0, 1, 2, 9, 10, 11, 18, 19, 20};
        for (int i = 0; i < greens.length; i++) {
            inv.setItem(greens[i], green);
        }

        ItemStack red = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
        ItemMeta itemMetaRed = red.getItemMeta();
        itemMetaRed.setDisplayName("§c§l提示を拒否する");
        red.setItemMeta(itemMetaRed);

        int[] reds = {6, 7, 8, 15, 16, 17, 24, 25, 26};
        for (int i = 0; i < reds.length; i++) {
            inv.setItem(reds[i], red);
        }

        return inv;
    }

    Inventory createLendSendConfirmMenu(Player p,LendData ld) {
        Inventory inv = Bukkit.createInventory(null, 27, "§4§lこの条件を" + p.getName() + "に提示しますか？");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy'年'MM'月'dd'日'E'曜日'k'時'mm'分'ss'秒'");
        ItemStack ink = new ItemStack(Material.PINK_DYE,1);
        ItemMeta inkMeta = ink.getItemMeta();
        inkMeta.setCustomModelData(10);
        inkMeta.setDisplayName("§c§l約束手形§7§l(Promissory Note)");
        inkMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
        inkMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        List<String> inkLore = new ArrayList<>();
        inkLore.add("§6====[Man10Bank]====");
        inkLore.add("");
        inkLore.add("§c§l発行者:" + p.getName());
        inkLore.add("§c§l金額:" + ld.finalValue);
        long usableTimeStamp = ld.usableDays * 24 * 60 * 60 + System.currentTimeMillis()/1000;
        java.util.Date time=new java.util.Date((long)usableTimeStamp*1000);
        inkLore.add("§d§l残金:" + ld.finalValue);
        inkLore.add("§4使用可能日");
        inkLore.add("§4(" + sdf.format(time) + ")");
        inkLore.add("");
        inkLore.add("§6==================");
        inkMeta.setLore(inkLore);
        ink.setItemMeta(inkMeta);

        inv.setItem(13, ink);

        ItemStack green = new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1);
        ItemMeta itemMeta = green.getItemMeta();
        itemMeta.setDisplayName("§a§l提示する");
        green.setItemMeta(itemMeta);
        int[] greens = {0, 1, 2, 9, 10, 11, 18, 19, 20};
        for (int i = 0; i < greens.length; i++) {
            inv.setItem(greens[i], green);
        }

        ItemStack red = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
        ItemMeta itemMetaRed = red.getItemMeta();
        itemMetaRed.setDisplayName("§c§lキャンセル");
        red.setItemMeta(itemMetaRed);

        int[] reds = {6, 7, 8, 15, 16, 17, 24, 25, 26};
        for (int i = 0; i < reds.length; i++) {
            inv.setItem(reds[i], red);
        }
        return inv;
    }

    private String format(String string){
        char[] list = string.toCharArray();
        String finalString = "";
        for(int i = 0;i < list.length;i++){
            finalString = finalString + "§" + list[i];
        }
        return finalString;
    }
}
