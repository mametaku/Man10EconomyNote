package red.man10.man10economynote;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by sho on 2017/12/18.
 */
public class MainCommand implements CommandExecutor {
    Man10EconomyNote plugin = null;

    public MainCommand(Man10EconomyNote plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player) sender;
        p.sendMessage("§e§l-----[§d§lMan10EconomyNote§e§l]-----");
        p.sendMessage("");
        p.sendMessage("§b/mlend 手形作成コマンド");
        p.sendMessage("§b/mcheque 小切手作成コマンド");
        p.sendMessage("§b/mviewdebt 借金額を見る");
        p.sendMessage("");
        p.sendMessage("§e§l---------------------------");
        p.sendMessage("§6§lCreated By Sho0");
        return false;
    }
}
