package com.exact.xtra.tasks

import android.util.Log

import com.exact.xtra.ui.view.MessageView

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class LiveChatTask(
        private val userName: String?,
        private val userToken: String?,
        channelName: String,
        private val listener: OnMessageReceivedListener) : Thread(), MessageView.MessageSenderCallback {

    private var socket: Socket? = null
    private lateinit var reader: BufferedReader
    private lateinit var writer: BufferedWriter
    private val hashChannelName: String = "#$channelName"
    private val messageSenderExecutor: Executor = Executors.newSingleThreadExecutor()

    companion object {
        private const val TAG = "LiveChatTask"
    }

    override fun run() {
        connect()
        //        var serverMessage: String? = null
        try {
            reader.lineSequence().forEach {
                when {
                    it.contains("PRIVMSG") -> listener.onMessage(it)
                    it.contains("USERNOTICE") -> listener.onUserNotice(it)
                    it.contains("NOTICE") -> listener.onNotice(it)
                    it.contains("ROOMSTATE") -> listener.onRoomState(it)
                    it.contains("JOIN") -> listener.onJoin(it)
                    it.startsWith("PING") -> {
                        write("PONG :tmi.twitch.tv")
                        writer.flush()
                    }
                }
            }
//            while (reader?.readLine().let { serverMessage = it; it != null }) {
//                    if (serverMessage.contains("PRIVMSG")) {
//                        listener.onMessage(serverMessage)
//                    } else if (serverMessage.contains("USERNOTICE")) {
//                        listener.onUserNotice(serverMessage)
//                    } else if (serverMessage.contains("NOTICE")) {
//                        listener.onNotice(serverMessage)
//                    } else if (serverMessage.contains("ROOMSTATE")) {
//                        listener.onRoomState(serverMessage)
//                    } else if (serverMessage.startsWith("PING")) {
//                        write("PONG :tmi.twitch.tv")
//                        writer!!.flush()
//                    } else if (serverMessage.contains("JOIN")) {
//                        listener.onJoin(serverMessage)
//                }
//            }
        } catch (e: IOException) {
            Log.e(TAG, "Connection error", e)
        } finally {
            disconnect()
        }
    }

    private fun connect() {
        Log.d(TAG, "Connecting to Twitch IRC")
        try {
            socket = Socket("irc.twitch.tv", 6667)
            reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
            writer = BufferedWriter(OutputStreamWriter(socket!!.getOutputStream()))
            if (userName == null) {
                write("NICK justinfan3896") //random numbers //TODO change to Random()
            } else {
                write("PASS oauth:" + userToken!!)
                write("NICK $userName")
            }
            write("CAP REQ :twitch.tv/tags")
            write("CAP REQ :twitch.tv/commands")
            write("JOIN $hashChannelName")
            writer.flush()
            Log.d(TAG, "Successfully connected to channel - $hashChannelName")
        } catch (e: IOException) {
            Log.e(TAG, "Error connecting to Twitch IRC", e)
        }

    }

    private fun disconnect() {
        Log.d(TAG, "Disconnecting from $hashChannelName")
        try {
            socket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error while disconnecting", e)
        }

    }

    @Throws(IOException::class)
    private fun write(message: String) {
        writer.write(message + System.getProperty("line.separator"))
    }

    fun shutdown() {}

    override fun send(message: String) {
        messageSenderExecutor.execute {
            try {
                write("PRIVMSG $hashChannelName :$message")
                writer.flush()
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
