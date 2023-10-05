package tw.nekomimi.nekogram.proxynext.sagernet;

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

/**
 * https://github.com/XTLS/Xray-core/issues/91
 */
public class StandardV2RayBean {

    public String serverAddress;
    public Integer serverPort;
    public String name;

    /**
     * UUID。对应配置文件该项出站中 settings.vnext[0].users[0].id 的值。
     * <p>
     * 不可省略，不能为空字符串。
     */
    public String uuid;

    /**
     * 当协议为 VMess 时，对应配置文件出站中 settings.security，可选值为 auto / aes-128-gcm / chacha20-poly1305 / none。
     * <p>
     * 省略时默认为 auto，但不可以为空字符串。除非指定为 none，否则建议省略。
     * <p>
     * 当协议为 VLESS 时，对应配置文件出站中 settings.encryption，当前可选值只有 none。
     * <p>
     * 省略时默认为 none，但不可以为空字符串。
     * <p>
     * 特殊说明：之所以不使用 security 而使用 encryption，是因为后面还有一个底层传输安全类型 security 与这个冲突。
     * 由 @huyz 提议，将此字段重命名为 encryption，这样不仅能避免命名冲突，还与 VLESS 保持了一致。
     */
    public String encryption;

    /**
     * 协议的传输方式。对应配置文件出站中 settings.vnext[0].streamSettings.network 的值。
     * <p>
     * 当前的取值必须为 tcp、kcp、ws、http、quic 其中之一，分别对应 TCP、mKCP、WebSocket、HTTP/2、QUIC 传输方式。
     */
    public String type;

    /**
     * 客户端进行 HTTP/2 通信时所发送的 Host 头部。
     * <p>
     * 省略时复用 remote-host，但不可以为空字符串。
     * <p>
     * 若有多个域名，可使用英文逗号隔开，但中间及前后不可有空格。
     * <p>
     * 必须使用 encodeURIComponent 转义。
     * -----------------------------------
     * WebSocket 请求时 Host 头的内容。不推荐省略，不推荐设为空字符串。
     * <p>
     * 必须使用 encodeURIComponent 转义。
     */
    public String host;

    /**
     * HTTP/2 的路径。省略时默认为 /，但不可以为空字符串。不推荐省略。
     * <p>
     * 必须使用 encodeURIComponent 转义。
     * -----------------------------------
     * WebSocket 的路径。省略时默认为 /，但不可以为空字符串。不推荐省略。
     * <p>
     * 必须使用 encodeURIComponent 转义。
     */
    public String path;

    /**
     * mKCP 的伪装头部类型。当前可选值有 none / srtp / utp / wechat-video / dtls / wireguard。
     * <p>
     * 省略时默认值为 none，即不使用伪装头部，但不可以为空字符串。
     * -----------------------------------
     * QUIC 的伪装头部类型。其他同 mKCP headerType 字段定义。
     */
    public String headerType;

    /**
     * mKCP 种子。省略时不使用种子，但不可以为空字符串。建议 mKCP 用户使用 seed。
     * <p>
     * 必须使用 encodeURIComponent 转义。
     */
    public String mKcpSeed;

    /**
     * QUIC 的加密方式。当前可选值有 none / aes-128-gcm / chacha20-poly1305。
     * <p>
     * 省略时默认值为 none。
     */
    public String quicSecurity;

    /**
     * 当 QUIC 的加密方式不为 none 时的加密密钥。
     * <p>
     * 当 QUIC 的加密方式为 none 时，此项不得出现；否则，此项必须出现，且不可为空字符串。
     * <p>
     * 若出现此项，则必须使用 encodeURIComponent 转义。
     */
    public String quicKey;

    /**
     * 底层传输安全 security
     * <p>
     * 设定底层传输所使用的 TLS 类型。当前可选值有 none，tls 和 xtls。
     * <p>
     * 省略时默认为 none，但不可以为空字符串。
     */
    public String security;

    /**
     * TLS SNI，对应配置文件中的 serverName 项目。
     * <p>
     * 省略时复用 remote-host，但不可以为空字符串。
     */
    public String sni;

    /**
     * TLS ALPN，对应配置文件中的 alpn 项目。
     * <p>
     * 多个 ALPN 之间用英文逗号隔开，中间无空格。
     * <p>
     * 省略时由内核决定具体行为，但不可以为空字符串。
     * <p>
     * 必须使用 encodeURIComponent 转义。
     */
    public String alpn;

    // --------------------------------------- //

    public String grpcServiceName;
    public String grpcMode;
    public Integer wsMaxEarlyData;
    public String earlyDataHeaderName;

    public String certificates;
    public String pinnedPeerCertificateChainSha256;

    // --------------------------------------- //

    public Boolean wsUseBrowserForwarder;
    public Boolean allowInsecure;
    public Integer packetEncoding;

    // --------------------------------------- //

    /**
     * XTLS 的流控方式。可选值为 xtls-rprx-direct、xtls-rprx-splice 等。
     * <p>
     * 若使用 XTLS，此项不可省略，否则无此项。此项不可为空字符串。
     */
    public String flow;

}
