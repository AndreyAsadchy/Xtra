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

    private var socketListener: Socket? = null
    private var socketSender: Socket? = null
    private lateinit var readerListener: BufferedReader
    private var readerSender: BufferedReader? = null
    private lateinit var writerListener: BufferedWriter
    private var writerSender: BufferedWriter? = null
    private val hashChannelName: String = "#$channelName"
    private val messageSenderExecutor: Executor = Executors.newSingleThreadExecutor()
    private var running = false

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
            while (running) {
                val lineListener = readerListener.readLine() ?: break
                lineListener.run {
                    when {
                        contains("PRIVMSG") -> listener.onMessage(this)
                        contains("USERNOTICE") -> listener.onUserNotice(this)
                        contains("NOTICE") -> listener.onNotice(this)
                        contains("ROOMSTATE") -> listener.onRoomState(this)
                        contains("JOIN") -> listener.onJoin(this)
                        startsWith("PING") -> handlePing(writerListener)
                    }
                }

                if (userName != null) {
                    val lineSender = readerSender?.readLine() ?: break
                    if (lineSender.startsWith("PING")) {
                        handlePing(writerSender!!)
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
            socketListener = Socket("irc.twitch.tv", 6667)
            readerListener = BufferedReader(InputStreamReader(socketListener!!.getInputStream()))
            writerListener = BufferedWriter(OutputStreamWriter(socketListener!!.getOutputStream()))
            if (userName != null) {
                socketSender = Socket("irc.twitch.tv", 6667)
                readerSender = BufferedReader(InputStreamReader(socketSender!!.getInputStream()))
                writerSender = BufferedWriter(OutputStreamWriter(socketSender!!.getOutputStream()))
                write("PASS oauth:" + userToken!!, writerSender)
                write("NICK $userName", writerSender)
            }
            write("NICK justinfan3896", writerListener) //random numbers //TODO change to Random()
            write("CAP REQ :twitch.tv/tags", writerListener, writerSender)
            write("CAP REQ :twitch.tv/commands", writerListener, writerSender)
            write("JOIN $hashChannelName", writerListener, writerSender)
            writerListener.flush()
            writerSender?.flush()
            running = true
            Log.d(TAG, "Successfully connected to channel - $hashChannelName")
        } catch (e: IOException) {
            Log.e(TAG, "Error connecting to Twitch IRC", e)
        }

    }

    private fun disconnect() {
        Log.d(TAG, "Disconnecting from $hashChannelName")
        try {
            socketListener?.close()
            socketSender?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error while disconnecting", e)
        }

    }

    @Throws(IOException::class)
    private fun write(message: String, vararg writers: BufferedWriter?) {
        writers.forEach { it?.write(message + System.getProperty("line.separator")) }
    }

    fun cancel() {
        running = false
    }

    override fun send(message: String) {
        messageSenderExecutor.execute {
            try {
                write("PRIVMSG $hashChannelName :$message", writerSender)
                writerSender?.flush()
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
