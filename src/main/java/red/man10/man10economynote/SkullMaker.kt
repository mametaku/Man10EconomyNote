package red.man10.man10economynote

import com.google.common.collect.ForwardingMultimap
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*
import java.util.UUID





class SkullMaker {
    private var owner: String? = null
    private var url: String? = null
    private var amount = 1
    private var name: String? = null
    private val lore: MutableList<String> = ArrayList()
    fun withAmount(amount: Int): SkullMaker {
        this.amount = amount
        return this
    }

    fun withName(name: String?): SkullMaker {
        this.name = name
        return this
    }

    fun withLore(line: String): SkullMaker {
        lore.add(line)
        return this
    }

    fun withLore(vararg lines: String?): SkullMaker? {
        lore.addAll(Arrays.asList<String>(*lines))
        return this
    }

    fun withLore(lines: List<String>): SkullMaker? {
        lore.addAll(lines)
        return this
    }

    fun withOwner(ownerName: String?): SkullMaker {
        owner = ownerName
        return this
    }

    fun withSkinUrl(url: String?): SkullMaker {
        this.url = url
        return this
    }

    fun build(): ItemStack {
        val item = ItemStack(Material.PLAYER_HEAD, amount)
        val meta = item.itemMeta as SkullMeta
        if (owner != null) {
            meta.owner = owner
        } else if (url != null) {
            loadProfile(meta, url!!)
        }
        if (name != null) {
            meta.setDisplayName(name)
        }
        if (!lore.isEmpty()) {
            meta.lore = lore
        }
        item.itemMeta = meta
        return item
    }

    private fun loadProfile(meta: ItemMeta, url: String) {
        val profileClass = Reflection.getClass("com.mojang.authlib.GameProfile")
        val profileConstructor = Reflection.getDeclaredConstructor(profileClass!!, UUID::class.java, String::class.java)
        val profile = Reflection.newInstance(profileConstructor!!, UUID.randomUUID(), null)
        val encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).toByteArray())
        val getPropertiesMethod = Reflection.getDeclaredMethod(profileClass, "getProperties")
        val propertyMap = Reflection.invoke(getPropertiesMethod!!, profile!!)
        val propertyClass = Reflection.getClass("com.mojang.authlib.properties.Property")
        Reflection.invoke(
                Reflection.getDeclaredMethod(
                        ForwardingMultimap::class.java, "put", Any::class.java, Any::class.java
                )!!,
                propertyMap!!,
                "textures",
                Reflection.newInstance(Reflection.getDeclaredConstructor(propertyClass!!, String::class.java, String::class.java)!!, "textures", String(encodedData))!!
        )
        Reflection.setField("profile", meta, profile)
    }

    private object Reflection {
        fun getClass(forName: String): Class<*>? {
            return try {
                Class.forName(forName)
            } catch (e: ClassNotFoundException) {
                null
            }
        }

        fun <T> getDeclaredConstructor(clazz: Class<T>, vararg params: Class<*>): Constructor<T>? {
            return try {
                clazz.getDeclaredConstructor(*params)
            } catch (e: NoSuchMethodException) {
                null
            }
        }

        fun <T> newInstance(constructor: Constructor<T>, vararg params: Any?): T? {
            return try {
                constructor.newInstance(*params)
            } catch (e: IllegalAccessException) {
                null
            } catch (e: InstantiationException) {
                null
            } catch (e: InvocationTargetException) {
                null
            }
        }

        fun getDeclaredMethod(clazz: Class<*>, name: String, vararg params: Class<*>): Method? {
            return try {
                clazz.getDeclaredMethod(name, *params)
            } catch (e: NoSuchMethodException) {
                null
            }
        }

        operator fun invoke(method: Method, `object`: Any, vararg params: Any): Any? {
            method.isAccessible = true
            return try {
                method.invoke(`object`, *params)
            } catch (e: InvocationTargetException) {
                null
            } catch (e: IllegalAccessException) {
                null
            }
        }

        fun setField(name: String, instance: Any, value: Any) {
            val field = getDeclaredField(instance.javaClass, name)
            field!!.isAccessible = true
            try {
                field[instance] = value
            } catch (ignored: IllegalAccessException) {
            }
        }

        fun getDeclaredField(clazz: Class<*>, name: String): Field? {
            return try {
                clazz.getDeclaredField(name)
            } catch (e: NoSuchFieldException) {
                null
            }
        }
    }

}