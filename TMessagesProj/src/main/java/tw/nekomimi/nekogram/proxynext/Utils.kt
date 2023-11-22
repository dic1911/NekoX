package tw.nekomimi.nekogram.proxynext

import java.net.URLDecoder

object Utils {
    fun String.parseInt(): Int {
        return Integer.parseInt(this)
    }

    fun String.urlDecode(): String {
        return try {
            URLDecoder.decode(this, "UTF-8")
        } catch (e: Exception) {
            e.printStackTrace()
            this
        }
    }
    @JvmStatic
    fun isIpv6Address(value: String): Boolean {
        var addr = value
        if (addr.indexOf("[") == 0 && addr.lastIndexOf("]") > 0) {
            addr = addr.drop(1)
            addr = addr.dropLast(addr.count() - addr.lastIndexOf("]"))
        }
        val regV6 = Regex("^((?:[0-9A-Fa-f]{1,4}))?((?::[0-9A-Fa-f]{1,4}))*::((?:[0-9A-Fa-f]{1,4}))?((?::[0-9A-Fa-f]{1,4}))*|((?:[0-9A-Fa-f]{1,4}))((?::[0-9A-Fa-f]{1,4})){7}$")
        return regV6.matches(addr)
    }
}