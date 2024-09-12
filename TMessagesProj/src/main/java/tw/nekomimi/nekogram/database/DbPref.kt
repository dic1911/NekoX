package tw.nekomimi.nekogram.database

import android.content.SharedPreferences
import cn.hutool.core.lang.Singleton.put
import org.dizitart.no2.*
import org.dizitart.no2.collection.Document
import org.dizitart.no2.collection.FindOptions
import org.dizitart.no2.collection.NitriteCollection
import org.dizitart.no2.collection.UpdateOptions
import org.dizitart.no2.filters.EqualsFilter
import org.dizitart.no2.filters.Filter
import org.dizitart.no2.filters.FluentFilter
import org.dizitart.no2.index.IndexOptions
import org.dizitart.no2.index.IndexType
import org.telegram.messenger.FileLog
import tw.nekomimi.nekogram.utils.UIUtil
import tw.nekomimi.nekogram.utils.applyIf

class DbPref(val connection: NitriteCollection) : SharedPreferences {

    init {
        if (!connection.hasIndex("key")) {
            connection.createIndex("key")
        }
    }

    val listeners = LinkedHashSet<SharedPreferences.OnSharedPreferenceChangeListener>()

    val isEmpty get() = connection.find(FindOptions.limitBy(1)).count() == 0

    private inline fun <reified T> getAs(key: String, defValue: T): T {
        val filter = FluentFilter.where("key").eq(key)
        connection.find(filter).apply {
            runCatching {
                return first().get("value", T::class.java)
            }
        }
        return defValue
    }

    override fun contains(key: String): Boolean {
        val filter = FluentFilter.where("key").eq(key)
        return connection.find(filter).count() > 0
    }

    override fun getBoolean(key: String, defValue: Boolean) = getAs(key, defValue)

    override fun getInt(key: String, defValue: Int) = getAs(key, defValue)

    override fun getAll(): MutableMap<String, *> {
        val allValues = HashMap<String, Any>()
        connection.find().forEach {
            allValues[it.get("key", String::class.java)] = it["value"]
        }
        return allValues
    }

    override fun getLong(key: String, defValue: Long) = getAs(key, defValue)

    override fun getFloat(key: String, defValue: Float) = getAs(key, defValue)

    override fun getString(key: String, defValue: String?) = getAs(key, defValue)

    override fun getStringSet(key: String, defValues: MutableSet<String>?) = getAs(key, defValues)

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        listeners.remove(listener)
    }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        listener?.apply { listeners.add(this) }
    }

    override fun edit(): PrefEditor {
        return PrefEditor()
    }

    inner class PrefEditor : SharedPreferences.Editor {

        private var clear = false
        private val toRemove = HashSet<String>()
        private val toApply = HashMap<String, Any?>()

        override fun clear(): PrefEditor {
            clear = true
            return this
        }

        override fun putLong(key: String, value: Long): PrefEditor {
            toApply[key] = value
            return this
        }

        override fun putInt(key: String, value: Int): PrefEditor {
            toApply[key] = value
            return this
        }

        override fun remove(key: String): PrefEditor {
            toApply.remove(key)
            toRemove.add(key)
            return this
        }

        override fun putBoolean(key: String, value: Boolean): PrefEditor {
            toApply[key] = value
            return this
        }

        override fun putStringSet(key: String, values: MutableSet<String>?): PrefEditor {
            toApply[key] = values
            return this
        }

        override fun putFloat(key: String, value: Float): PrefEditor {
            toApply[key] = value
            return this
        }

        override fun putString(key: String, value: String?): PrefEditor {
            toApply[key] = value
            return this
        }

        override fun commit(): Boolean {
            try {
                if (clear) {
                    connection.remove(Filter.ALL)
                } else {
                    toRemove.forEach {
                        val filter = FluentFilter.where("key").eq(it)
                        connection.remove(filter)
                    }
                }
                toApply.forEach { (key, value) ->
                    val filter = FluentFilter.where("key").eq(key)
                    if (value == null) {
                        connection.remove(filter)
                    } else {
                        connection.update(filter, Document.createDocument().apply {
                            put("key", key)
                            put("value", value)
                        }, UpdateOptions.updateOptions(true))
                    }
                }
                return true
            } catch (ex: Exception) {
                FileLog.e(ex)
                return false
            }
        }

        override fun apply() {
            UIUtil.runOnIoDispatcher(Runnable { commit() })
        }

    }

}