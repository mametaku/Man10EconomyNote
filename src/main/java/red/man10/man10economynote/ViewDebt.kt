package red.man10.man10economynote

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import red.man10.man10vaultapiplus.JPYBalanceFormat
import java.sql.SQLException
import java.util.*

/**
 * Created by sho on 2017/12/18.
 */
class ViewDebt(plugin: Man10EconomyNote?) : CommandExecutor {
    var plugin: Man10EconomyNote? = null
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.size == 1) {
            val player = Bukkit.getOfflinePlayer(args[0])
            if (!player.hasPlayedBefore()) {
                (sender as Player).sendMessage("§e[§dMan10EconNote§e]§c" + args[0] + "さんはサーバーに存在しません")
                return false
            }
            val t = Thread { tellPlayerValue(sender as Player, player.uniqueId) }
            t.start()
        }
        return false
    }

    fun tellPlayerValue(p: Player, uuid: UUID) {
        val rs = plugin!!.mysql!!.query("SELECT sum(value_left) FROM man10_economy_note WHERE wired_to_uuid ='$uuid'")
        val pl = Bukkit.getOfflinePlayer(uuid)
        try {
            while (rs!!.next()) {
                p.sendMessage("§e[§dMan10EconNote§e]§b" + pl.name + "さんの総借金額は" + JPYBalanceFormat(rs.getLong("sum(value_left)")).getString() + "円です")
            }
            rs.close()
            plugin!!.mysql!!.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    init {
        this.plugin = plugin
    }
}