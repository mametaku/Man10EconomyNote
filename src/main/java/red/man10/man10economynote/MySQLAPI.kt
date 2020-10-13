package red.man10.man10economynote

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Level

/**
 * Created by sho-pc on 2017/05/21.
 */
class MySQLAPI {
    var debugMode = false
    private var plugin: JavaPlugin? = null
    private var HOST: String? = null

    ////////////////////////////////
    //     行数を数える
    ////////////////////////////////
    var dB: String? = null
        private set
    var uSER: String? = null
        private set
    private var PASS: String? = null
    private var PORT: String? = null
    private var connected = false
    private var st: Statement? = null
    private var con: Connection? = null
    private var conName: String
    private var MySQL: MySQLFunc? = null

    ////////////////////////////////
    //      コンストラクタ
    ////////////////////////////////
    val confirmation: String
        get() = "%QltpzRbj$4AFjRSRqAblzrbdiDqAblzs4t\$sbRpQ5bqFlbbsVFe3eIbxfbmbIsA"
    val mySqlSetting: List<String?>
        get() {
            val list: MutableList<String?> = ArrayList()
            list.add(HOST)
            list.add(dB)
            list.add(uSER)
            list.add(PASS)
            list.add(PORT)
            return list
        }

    constructor(plugin: JavaPlugin, name: String) {
        this.plugin = plugin
        conName = name
        connected = false
        loadConfig()
        connected = Connect(HOST, dB, uSER, PASS, PORT)
        if (!connected) {
            plugin.logger.info("Unable to establish a MySQL connection.")
        }
    }

    constructor(conName: String, host: String?, user: String?, pass: String?, port: String?, db: String?) {
        this.conName = conName
        connected = false
        HOST = host
        uSER = user
        PASS = pass
        PORT = port
        dB = db
        connected = Connect(host, db, user, pass, port)
        if (!connected) {
            Bukkit.getLogger().info("Unable to establish a MySQL connection.")
        }
    }

    /////////////////////////////////
    //       設定ファイル読み込み
    /////////////////////////////////
    fun loadConfig() {
        plugin!!.logger.info("MYSQL Config loading")
        plugin!!.reloadConfig()
        HOST = plugin!!.config.getString("mysql.host")
        uSER = plugin!!.config.getString("mysql.user")
        PASS = plugin!!.config.getString("mysql.pass")
        PORT = plugin!!.config.getString("mysql.port")
        dB = plugin!!.config.getString("mysql.db")
        plugin!!.logger.info("Config loaded")
    }

    fun connectable(): Boolean {
        connected = false
        connected = Connect(HOST, dB, uSER, PASS, PORT)
        if (!connected) {
            return false
        }
        connected = true
        return true
    }

    ////////////////////////////////
    //       接続
    ////////////////////////////////
    fun Connect(host: String?, db: String?, user: String?, pass: String?, port: String?): Boolean {
        HOST = host
        dB = db
        uSER = user
        PASS = pass
        MySQL = MySQLFunc(host, db, user, pass, port)
        con = MySQL!!.open()
        if (con == null) {
            Bukkit.getLogger().info("failed to open MYSQL")
            return false
        }
        try {
            st = con!!.createStatement()
            connected = true
            Bukkit.getLogger().info("[" + conName + "] Connected to the database.")
        } catch (var6: SQLException) {
            connected = false
            Bukkit.getLogger().info("[" + conName + "] Could not connect to the database.")
        }
        //this.MySQL.close(this.con);
        try {
            con!!.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return java.lang.Boolean.valueOf(connected)
    }

    fun countRows(table: String): Int {
        var count = 0
        val set = query(String.format("SELECT * FROM %s", *arrayOf<Any>(table)))
        try {
            while (set!!.next()) {
                ++count
            }
        } catch (var5: SQLException) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not select all rows from table: " + table + ", error: " + var5.errorCode)
        }
        return count
    }

    ////////////////////////////////
    //     レコード数
    ////////////////////////////////
    fun count(table: String): Int {
        var count = 0
        val set = query(String.format("SELECT count(*) from %s", table))
        count = try {
            set!!.getInt("count(*)")
        } catch (var5: SQLException) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not select all rows from table: " + table + ", error: " + var5.errorCode)
            return -1
        }
        return count
    }

    ////////////////////////////////
    //      実行
    ////////////////////////////////
    fun executeGetId(query: String?): Int {
        var key = -1
        try {
            MySQL = MySQLFunc(HOST, dB, uSER, PASS, PORT)
            con = MySQL!!.open()
            //open();
            val pstmt = con!!.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
            pstmt.executeUpdate()
            pstmt.queryTimeout = 10
            val keys = pstmt.generatedKeys
            keys.next()
            key = keys.getInt(1)
            keys.close()
            pstmt.close()
            //close();
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return key
    }

    fun execute(query: String): Boolean {
        var result = true
        MySQL = MySQLFunc(HOST, dB, uSER, PASS, PORT)
        con = MySQL!!.open()
        if (con == null) {
            Bukkit.getLogger().info("failed to open MYSQL")
            return false
        }
        if (debugMode) {
            plugin!!.logger.info("query:$query")
        }
        try {
            st = con!!.createStatement()
            st.execute(query)
        } catch (var3: SQLException) {
            plugin!!.logger.info("[" + conName + "] Error executing statement: " + var3.errorCode + ":" + var3.localizedMessage)
            plugin!!.logger.info(query)
            result = false
        }
        MySQL!!.close(con)
        close()
        return result
    }

    ////////////////////////////////
    //      クエリ
    //////////////////////////////
    //
    fun query(query: String): ResultSet? {
        var rs: ResultSet? = null
        MySQL = MySQLFunc(HOST, dB, uSER, PASS, PORT)
        con = MySQL!!.open()
        if (debugMode) {
            Bukkit.getLogger().info("query:$query")
        }
        try {
            st = con!!.createStatement()
            st.setQueryTimeout(10)
            rs = st.executeQuery(query)
        } catch (var4: SQLException) {
            Bukkit.getLogger().info("[" + conName + "] Error executing query: " + var4.errorCode)
            plugin!!.logger.info(query)
        }
        return rs
    }

    fun close() {
        MySQL!!.close(con)
        try {
            if (!st!!.isClosed) {
                st!!.close()
                st = null
            }
            if (!con!!.isClosed) {
                con!!.close()
                con = null
            }
        } catch (e: SQLException) {
        } catch (e: Exception) {
        } finally {
        }
    }

    fun currentTimeNoBracket(): String {
        val date = Date()
        val sdf = SimpleDateFormat("yyyy'-'MM'-'dd' 'HH':'mm':'ss")
        return sdf.format(date)
    }

    fun convertBooleanToMysql(b: Boolean): Int {
        return if (b) {
            1
        } else 0
    }

    fun convertMysqlToBoolean(i: Int): Boolean {
        return if (i == 1) {
            true
        } else false
    }
}