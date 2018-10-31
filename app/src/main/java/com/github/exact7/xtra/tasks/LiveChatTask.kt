package com.github.exact7.xtra.tasks

import android.util.Log
import com.github.exact7.xtra.ui.view.MessageView
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class LiveChatTask(
        private val userName: String?,
        private val userToken: String?,
        channelName: String,
        private val listener: OnMessageReceivedListener) : Thread(), MessageView.MessageSenderCallback {

    private var socketIn: Socket? = null
    private var socketOut: Socket? = null
    private lateinit var readerIn: BufferedReader
    private var readerOut: BufferedReader? = null
    private lateinit var writerIn: BufferedWriter
    private var writerOut: BufferedWriter? = null
    private val hashChannelName: String = "#$channelName"
    private val messageSenderExecutor: Executor = Executors.newSingleThreadExecutor()

    companion object {
        private const val TAG = "LiveChatTask"
    }

    override fun run() {

        fun handlePing(writer: BufferedWriter) {
            write("PONG :tmi.twitch.tv", writer)
            writer.flush()
        }

        connect()
        try {
            while (true) {
                val lineListener = readerIn.readLine() ?: break
                lineListener.run {
                    println(this)
                    when {
                        contains("PRIVMSG") -> listener.onMessage(this)
                        contains("USERNOTICE") -> listener.onUserNotice(this)
                        contains("NOTICE") -> listener.onNotice(this)
                        contains("ROOMSTATE") -> listener.onRoomState(this)
                        contains("JOIN") -> listener.onJoin(this)
                        startsWith("PING") -> handlePing(writerIn)
                    }
                }

                if (userName != null) {
                    val lineSender = readerOut?.readLine() ?: break
                    if (lineSender.startsWith("PING")) {
                        handlePing(writerOut!!)
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Connection error", e)
        } finally {
            disconnect()
        }


    }

    private fun connect() {
        Log.d(TAG, "Connecting to Twitch IRC")
        try {
            socketIn = Socket("irc.twitch.tv", 6667)
            readerIn = BufferedReader(InputStreamReader(socketIn!!.getInputStream()))
            writerIn = BufferedWriter(OutputStreamWriter(socketIn!!.getOutputStream()))
            if (userName != null) {
                socketOut = Socket("irc.twitch.tv", 6667)
                readerOut = BufferedReader(InputStreamReader(socketOut!!.getInputStream()))
                writerOut = BufferedWriter(OutputStreamWriter(socketOut!!.getOutputStream()))
                write("PASS oauth:" + userToken!!, writerOut)
                write("NICK $userName", writerOut)
            }
            write("NICK justinfan${Random().nextInt(((9999 - 1000) + 1)) + 1000}", writerIn) //random number between 1000 and 9999
            write("CAP REQ :twitch.tv/tags", writerIn, writerOut)
            write("CAP REQ :twitch.tv/commands", writerIn, writerOut)
            write("JOIN $hashChannelName", writerIn, writerOut)
            writerIn.flush()
            writerOut?.flush()
            Log.d(TAG, "Successfully connected to channel - $hashChannelName")
        } catch (e: IOException) {
            Log.e(TAG, "Error connecting to Twitch IRC", e)
        }

    }

    private fun disconnect() {
        Log.d(TAG, "Disconnecting from $hashChannelName")
        try {
            socketIn?.close()
            socketOut?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error while disconnecting", e)
        }

    }

    @Throws(IOException::class)
    private fun write(message: String, vararg writers: BufferedWriter?) {
        writers.forEach { it?.write(message + System.getProperty("line.separator")) }
    }

    fun cancel() {
        fun shutdown(socket: Socket?) {
            socket?.let {
                if (!it.isClosed) {
                    it.shutdownOutput()
                    it.shutdownInput()
                }
            }
        }
        shutdown(socketIn)
        shutdown(socketOut)
    }

    override fun send(message: String) {
        messageSenderExecutor.execute {
            try {
                write("PRIVMSG $hashChannelName :$message", writerOut)
                writerOut?.flush()
                Log.d(TAG, "Sent message to $hashChannelName: $message")
            } catch (e: IOException) {
                Log.e(TAG, "Error sending message", e)
            }
        }
    }

    interface OnMessageReceivedListener {
        fun onMessage(message: String)
        fun onNotice(message: String)
        fun onUserNotice(message: String)
        fun onRoomState(message: String)
        fun onJoin(message: String)
    }
}
