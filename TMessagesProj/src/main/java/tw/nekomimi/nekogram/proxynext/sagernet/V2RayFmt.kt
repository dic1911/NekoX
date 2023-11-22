/******************************************************************************
 *                                                                            *
 * Copyright (C) 2021 by nekohasekai <contact-sagernet@sekai.icu>             *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 *  (at your option) any later version.                                       *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program. If not, see <http://www.gnu.org/licenses/>.       *
 *                                                                            *
 ******************************************************************************/

package tw.nekomimi.nekogram.proxynext.sagernet

import cn.hutool.core.codec.Base64
import com.google.gson.JsonObject
//import cn.hutool.json.JSONObject
//import com.v2ray.core.common.net.packetaddr.PacketAddrType
//import io.nekohasekai.sagernet.ktx.*
//import libcore.Libcore
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONObject

fun parseV2Ray(link: String): StandardV2RayBean {
    if (!link.contains("@")) {
        return parseV2RayN(link)
    }

    val url = link.toHttpUrlOrNull() ?: error("failed to parse URL")
    val bean = StandardV2RayBean()

    bean.serverAddress = url.host
    bean.serverPort = url.port
    bean.name = url.fragment

    if (url.password.isNotBlank()) { // https://github.com/v2fly/v2fly-github-io/issues/26

        var protocol = url.username
        bean.type = protocol
//        bean.alterId = url.password.substringAfterLast('-').toInt()
        bean.uuid = url.password.substringBeforeLast('-')

        if (protocol.endsWith("+tls")) {
            bean.security = "tls"
            protocol = protocol.substring(0, protocol.length - 4)

            url.queryParameter("tlsServerName")?.let {
                if (it.isNotBlank()) {
                    bean.sni = it
                }
            }
        }

        when (protocol) {
            "tcp" -> {
                url.queryParameter("type")?.let { type ->
                    if (type == "http") {
                        bean.headerType = "http"
                        url.queryParameter("host")?.let {
                            bean.host = it
                        }
                    }
                }
            }

            "http" -> {
                url.queryParameter("path")?.let {
                    bean.path = it
                }
                url.queryParameter("host")?.let {
                    bean.host = it.split("|").joinToString(",")
                }
            }

            "ws" -> {
                url.queryParameter("path")?.let {
                    bean.path = it
                }
                url.queryParameter("host")?.let {
                    bean.host = it
                }
            }

            "kcp" -> {
                url.queryParameter("type")?.let {
                    bean.headerType = it
                }
                url.queryParameter("seed")?.let {
                    bean.mKcpSeed = it
                }
            }

            "quic" -> {
                url.queryParameter("security")?.let {
                    bean.quicSecurity = it
                }
                url.queryParameter("key")?.let {
                    bean.quicKey = it
                }
                url.queryParameter("type")?.let {
                    bean.headerType = it
                }
            }
        }
    } else { // https://github.com/XTLS/Xray-core/issues/91

        bean.uuid = url.username

        val protocol = url.queryParameter("type") ?: "tcp"
        bean.type = protocol

        when (url.queryParameter("security")) {
            "tls" -> {
                bean.security = "tls"
                url.queryParameter("sni")?.let {
                    bean.sni = it
                }
                url.queryParameter("alpn")?.let {
                    bean.alpn = it
                }
                url.queryParameter("cert")?.let {
                    bean.certificates = it
                }
                url.queryParameter("chain")?.let {
                    bean.pinnedPeerCertificateChainSha256 = it
                }
            }

            "xtls" -> {
                bean.security = "xtls"
                url.queryParameter("sni")?.let {
                    bean.sni = it
                }
                url.queryParameter("alpn")?.let {
                    bean.alpn = it
                }
                url.queryParameter("flow")?.let {
                    bean.flow = it
                }
            }
        }
        when (protocol) {
            "tcp" -> {
                url.queryParameter("headerType")?.let { headerType ->
                    if (headerType == "http") {
                        bean.headerType = headerType
                        url.queryParameter("host")?.let {
                            bean.host = it
                        }
                        url.queryParameter("path")?.let {
                            bean.path = it
                        }
                    }
                }
            }

            "kcp" -> {
                url.queryParameter("headerType")?.let {
                    bean.headerType = it
                }
                url.queryParameter("seed")?.let {
                    bean.mKcpSeed = it
                }
            }

            "http" -> {
                url.queryParameter("host")?.let {
                    bean.host = it
                }
                url.queryParameter("path")?.let {
                    bean.path = it
                }
            }

            "ws" -> {
                url.queryParameter("host")?.let {
                    bean.host = it
                }
                url.queryParameter("path")?.let {
                    bean.path = it
                }
                url.queryParameter("ed")?.let { ed ->
                    bean.wsMaxEarlyData = ed.toInt()

                    url.queryParameter("eh")?.let {
                        bean.earlyDataHeaderName = it
                    }
                }
            }

            "quic" -> {
                url.queryParameter("headerType")?.let {
                    bean.headerType = it
                }
                url.queryParameter("quicSecurity")?.let { quicSecurity ->
                    bean.quicSecurity = quicSecurity
                    url.queryParameter("key")?.let {
                        bean.quicKey = it
                    }
                }
            }

            "grpc" -> {
                url.queryParameter("serviceName")?.let {
                    bean.grpcServiceName = it
                }
            }
        }

//        url.queryParameter("packetEncoding")?.let {
//            when (it) {
//                "packet" -> bean.packetEncoding = PacketAddrType.Packet_VALUE
//                "xudp" -> bean.packetEncoding = PacketAddrType.XUDP_VALUE
//            }
//        }

    }

//    Logs.d(formatObject(bean))

    return bean
}

fun String.decodeBase64UrlSafe(): String {
    return Base64.decodeStr(
            replace(' ', '-').replace('/', '_').replace('+', '-').replace("=", "")
    )
}

fun parseV2RayN(link: String): StandardV2RayBean {
    val result = link.substringAfter("vmess://").decodeBase64UrlSafe()
    if (result.contains("= vmess")) {
        return parseCsvVMess(result)
    }
    val bean = StandardV2RayBean()
    val json = JSONObject(result)

    bean.serverAddress = json.getString("add") ?: ""
    bean.serverPort = json.getInt("port") ?: 1080
    bean.encryption = json.getString("scy") ?: ""
    bean.uuid = json.getString("id") ?: ""
//    bean.alterId = json.getInt("aid") ?: 0
    bean.type = json.getString("net") ?: ""
    bean.headerType = json.getString("type") ?: ""
    bean.host = json.getString("host") ?: ""
    bean.path = json.getString("path") ?: ""

    when (bean.type) {
        "quic" -> {
            bean.quicSecurity = bean.host
            bean.quicKey = bean.path
        }

        "kcp" -> {
            bean.mKcpSeed = bean.path
        }

        "grpc" -> {
            bean.grpcServiceName = bean.path
            bean.grpcMode = bean.headerType
        }
    }

    bean.name = json.getString("ps") ?: ""
    bean.sni = json.getString("sni") ?: bean.host
    bean.security = json.getString("tls")

    if (json.optInt("v", 2) < 2) {
        when (bean.type) {
            "ws" -> {
                var path = ""
                var host = ""
                val lstParameter = bean.host.split(";")
                if (lstParameter.isNotEmpty()) {
                    path = lstParameter[0].trim()
                }
                if (lstParameter.size > 1) {
                    path = lstParameter[0].trim()
                    host = lstParameter[1].trim()
                }
                bean.path = path
                bean.host = host
            }

            "h2" -> {
                var path = ""
                var host = ""
                val lstParameter = bean.host.split(";")
                if (lstParameter.isNotEmpty()) {
                    path = lstParameter[0].trim()
                }
                if (lstParameter.size > 1) {
                    path = lstParameter[0].trim()
                    host = lstParameter[1].trim()
                }
                bean.path = path
                bean.host = host
            }
        }
    }

    return bean

}

private fun parseCsvVMess(csv: String): StandardV2RayBean {

    val args = csv.split(",")

    val bean = StandardV2RayBean()

    bean.serverAddress = args[1]
    bean.serverPort = args[2].toInt()
    bean.encryption = args[3]
    bean.uuid = args[4].replace("\"", "")

    args.subList(5, args.size).forEach {

        when {
            it == "over-tls=true" -> bean.security = "tls"
            it.startsWith("tls-host=") -> bean.host = it.substringAfter("=")
            it.startsWith("obfs=") -> bean.type = it.substringAfter("=")
            it.startsWith("obfs-path=") || it.contains("Host:") -> {
                runCatching {
                    bean.path = it.substringAfter("obfs-path=\"").substringBefore("\"obfs")
                }
                runCatching {
                    bean.host = it.substringAfter("Host:").substringBefore("[")
                }

            }

        }

    }

    return bean

}
