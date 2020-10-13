package red.man10.man10economynote

import java.util.*

/**
 * Created by sho on 2017/12/17.
 */
class LendData(var name: String?, var uuid: UUID?, var baseValue: Long, var finalValue: Long, var usableDays: Int, var finalValueLender: Long, var interest: Double, var valueLeft: Long, var id: Int, var creationTime: Long) {
    fun hasNull(): Boolean {
        return try {
            if (name == null || uuid == null) {
                true
            } else false
        } catch (e: NullPointerException) {
            true
        }
    }
}