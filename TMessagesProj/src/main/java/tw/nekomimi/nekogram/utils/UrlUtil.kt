package tw.nekomimi.nekogram.utils

import android.net.Uri
import tw.nekomimi.nekogram.NekoConfig

object UrlUtil {
    @JvmStatic
    private val twitterDomains = listOf("x.com", "twitter.com")

    @JvmStatic
    private val twitterPreviewDomains = listOf("vxtwitter.com", "fxtwitter.com", "fixupx.com", "fixvx.com")

    @JvmStatic
    private val youtubeDomains = listOf("youtube.com", "youtu.be")

    @JvmStatic
    private val trackingQueryPostfixes = listOf("shid", "clid", "wtrid", "spm", "tracking_source",
        "__s", "__hssc", "__hstc", "hsCtaTracking", "msclkid", "oly_anon_id", "oly_enc_id", "rb_clickid", "s_clid", "wickedid")

    @JvmStatic
    private val trackingQueryPrefixes = listOf("utm")

    @JvmStatic
    fun isTwitter(url: String): Boolean {
        return twitterDomains.contains(url)
    }

    @JvmStatic
    fun isTwitterPreview(url: String): Boolean {
        return twitterPreviewDomains.contains(url)
    }

    @JvmStatic
    fun cleanUrl(src: String): String {
        return cleanUrl(Uri.parse(src)).toString()
    }

    @JvmStatic
    fun cleanUrl(src: Uri): Uri {
        if (!src.scheme!!.startsWith("http")) return src
        if (isTwitter(src.host.toString()) || isTwitterPreview(src.host.toString())) {
            var host = src.host
            if (NekoConfig.patchAndCleanupLinks.Bool()) {
                host = "twitter.com"
            }
            return Uri.Builder().scheme("https").authority(host).path(src.path).build()
        } else if (youtubeDomains.contains(src.host.toString())) {
            val out = Uri.Builder().scheme("https").authority(src.host).path(src.path)
            val appendTimestamp = src.getQueryParameter("t") != null
            if (appendTimestamp) out.appendQueryParameter("t", src.getQueryParameter("t"))
            return out.build()
        }

        val appendPort = (src.scheme == "http" && src.port != 80) || (src.scheme == "https" && src.port != 443)
        val out = Uri.Builder().scheme(src.scheme).encodedAuthority(if (appendPort) "${src.host}:${src.port}" else src.host)
            .appendEncodedPath(src.path?.replaceFirst("/", "")).encodedFragment(src.encodedFragment)
        val queries = src.queryParameterNames
        for (q in queries) {
            var block = false
            for (f in trackingQueryPrefixes) {
                if (q.startsWith(f)) {
                    block = true
                    break
                }
            }
            if (block) continue
            for (f in trackingQueryPostfixes) {
                if (q.startsWith(f)) {
                    block = true
                    break
                }
            }
            if (block) continue

            if (NekoConfig.customGetQueryBlacklistData.contains(q)) continue

            out.appendQueryParameter(q, src.getQueryParameter(q))
        }

        return out.build()
    }

    @JvmStatic
    fun stripAllQueries(src: String): String {
        return src.substring(0, src.lastIndexOf("?"))
    }
}