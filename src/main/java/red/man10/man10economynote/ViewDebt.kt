package red.man10.man10economynote;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import red.man10.man10vaultapiplus.JPYBalanceFormat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by sho on 2017/12/18.
 */
public class ViewDebt implements CommandExecutor {
    Man10EconomyNote plugin = null;

    public ViewDebt(Man10EconomyNote plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 1){
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
            if(!player.hasPlayedBefore()){
                ((Player)sender).sendMessage("§e[§dMan10EconNote§e]§c" + args[0] + "さんはサーバーに存在しません");
                return false;
            }
            Thread t = new Thread(() -> tellPlayerValue(((Player)sender), player.getUniqueId()));
            t.start();
        }
        return false;
    }

    public void tellPlayerValue(Player p,UUID uuid){
        ResultSet rs = plugin.mysql.query("SELECT sum(value_left) FROM man10_economy_note WHERE wired_to_uuid ='" + uuid + "'");
        OfflinePlayer pl = Bukkit.getOfflinePlayer(uuid);
        try {
            while (rs.next()){
                p.sendMessage("§e[§dMan10EconNote§e]§b" + pl.getName() + "さんの総借金額は" + new JPYBalanceFormat(rs.getLong("sum(value_left)")).getString() + "円です");
            }
            rs.close();
            plugin.mysql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
