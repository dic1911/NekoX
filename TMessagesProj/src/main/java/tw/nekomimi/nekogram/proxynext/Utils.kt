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
}