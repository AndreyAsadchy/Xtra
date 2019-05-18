package com.github.exact7.xtra.util.chat

import android.util.Log
import com.github.exact7.xtra.ui.view.chat.ChatView

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.util.Random
import java.util.concurrent.Executor
import java.util.concurrent.Executors

private const val TAG = "LiveChatThread"

class LiveChatThread(
        private val userName: String?,
        private val userToken: String?,
        channelName: String,
        private val listener: OnMessageReceivedListener) : Thread(), ChatView.MessageSenderCallback {
    private var socketIn: Socket? = null
    private var socketOut: Socket? = null
    private lateinit var readerIn: BufferedReader
    private var readerOut: BufferedReader? = null
    private lateinit var writerIn: BufferedWriter
    private var writerOut: BufferedWriter? = null
    private val hashChannelName: String = "#$channelName"
    private val messageSenderExecutor: Executor = Executors.newSingleThreadExecutor()

    override fun run() {

        fun handlePing(writer: BufferedWriter) {
            write("PONG :tmi.twitch.tv", writer)
            writer.flush()
        }

        try {
            connect()
            while (true) {
                val lineListener = readerIn.readLine() ?: break
                lineListener.run {
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

        } finally {
            Log.d(TAG, "Disconnecting from $hashChannelName")
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
            Log.d(TAG, "Successfully connected to - $hashChannelName")
        } catch (e: IOException) {
            Log.e(TAG, "Error connecting to Twitch IRC", e)
            throw e
        }
    }

    fun disconnect() {
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

    override fun send(message: CharSequence) {
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
