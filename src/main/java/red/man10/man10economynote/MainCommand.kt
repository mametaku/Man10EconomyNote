package red.man10.man10economynote

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Created by sho on 2017/12/18.
 */
class MainCommand(plugin: Man10EconomyNote?) : CommandExecutor {
    var plugin: Man10EconomyNote? = null
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        val p = sender as Player
        p.sendMessage("§e§l-----[§d§lMan10EconomyNote§e§l]-----")
        p.sendMessage("")
        p.sendMessage("§b/mlend 手形作成コマンド")
        p.sendMessage("§b/mcheque 小切手作成コマンド")
        p.sendMessage("§b/mviewdebt 借金額を見る")
        p.sendMessage("")
        p.sendMessage("§e§l---------------------------")
        p.sendMessage("§6§lCreated By Sho0")
        return false
    }

    init {
        this.plugin = plugin
    }
}