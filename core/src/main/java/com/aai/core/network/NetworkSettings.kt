package com.aai.core.network

import java.security.SecureRandom
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager

class NetworkSettings {

    companion object {
        fun set(networkConfig: NetworkConfig) {
            if (networkConfig.enablePacketCaptureDebug) {
                val sc = SSLContext.getInstance("TLS")
                sc.init(null, arrayOf<TrustManager>(IgnoreSSLTrust()), SecureRandom())
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
            }
        }
    }

}