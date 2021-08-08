package com.github.andreyasadchy.xtra.util

import okhttp3.TlsVersion
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory


/**
 * Enables TLS v1.2 when creating SSLSockets.
 *
 *
 * For some reason, android supports TLS v1.2 from API 16, but enables it by
 * get only from API 20.
 * @link https://developer.android.com/reference/javax/net/ssl/SSLSocket.html
 */
class TlsSocketFactory(private val delegate: SSLSocketFactory) : SSLSocketFactory() {

    private fun Socket.patch(): Socket {
        return (this as? SSLSocket)?.apply {
            enabledProtocols += arrayOf(TlsVersion.TLS_1_1.javaName(), TlsVersion.TLS_1_2.javaName())
        } ?: this
    }

    override fun getDefaultCipherSuites(): Array<String> {
        return delegate.defaultCipherSuites
    }

    override fun getSupportedCipherSuites(): Array<String> {
        return delegate.supportedCipherSuites
    }

    @Throws(IOException::class)
    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket? {
        return delegate.createSocket(s, host, port, autoClose).patch()
    }

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String, port: Int): Socket? {
        return delegate.createSocket(host, port).patch()
    }

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket? {
        return delegate.createSocket(host, port, localHost, localPort).patch()
    }

    @Throws(IOException::class)
    override fun createSocket(host: InetAddress, port: Int): Socket? {
        return delegate.createSocket(host, port).patch()
    }

    @Throws(IOException::class)
    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket? {
        return delegate.createSocket(address, port, localAddress, localPort).patch()
    }
}