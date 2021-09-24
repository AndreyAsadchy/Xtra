package com.github.andreyasadchy.xtra.ui.common

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.getSpans
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.andreyasadchy.xtra.GlideApp
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.chat.ChatMessage
import com.github.andreyasadchy.xtra.model.chat.Emote
import com.github.andreyasadchy.xtra.model.chat.Image
import com.github.andreyasadchy.xtra.model.chat.TwitchEmote
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.set

class ChatAdapter(
        private val fragment: Fragment,
        private val emoteSize: Int,
        private val badgeSize: Int,
        private val animateGifs: Boolean,
        private val zeroWidth: Boolean) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    var messages: MutableList<ChatMessage>? = null
        set(value) {
            val oldSize = field?.size ?: 0
            if (oldSize > 0) {
                notifyItemRangeRemoved(0, oldSize)
            }
            field = value
        }
    private val twitchColors = intArrayOf(-65536, -16776961, -16744448, -5103070, -32944, -6632142, -47872, -13726889, -2448096, -2987746, -10510688, -14774017, -38476, -7722014, -16711809)
    private val random = Random()
    private val userColors = HashMap<String, Int>()
    private val savedColors = HashMap<String, Int>()
    private val emotes = HashMap<String, Emote>()
    private var username: String? = null
    private val scaledEmoteSize = (emoteSize * 0.78f).toInt()

    private var messageClickListener: ((CharSequence, CharSequence) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.chat_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatMessage = messages?.get(position) ?: return
        val builder = SpannableStringBuilder()
        val images = ArrayList<Image>()
        var index = 0
        var badgesCount = 0
        chatMessage.badges?.forEach { (id, version) ->
            val url: String? = when (id) {
                "admin" -> BADGES_URL + "admin.png"
                "bits" -> {
                    val count = version.toInt()
                    val color = when {
                        count < 100 -> "gray"
                        count < 1000 -> "purple"
                        count < 5000 -> "green"
                        count < 10000 -> "blue"
                        else -> "red"
                    }
                    "https://static-cdn.jtvnw.net/bits/dark/static/$color/2" //TODO change theme based on app theme
                }
                "broadcaster" -> BADGES_URL + "broadcaster.png"
                "global_mod" -> BADGES_URL + "globalmod.png"
                "moderator" -> BADGES_URL + "mod.png"
                "subscriber" -> chatMessage.subscriberBadge?.imageUrl2x
                "staff" -> BADGES_URL + "staff.png"
                "turbo" -> BADGES_URL + "turbo.png"
                "sub-gifter" -> "https://static-cdn.jtvnw.net/badges/v1/4592e9ea-b4ca-4948-93b8-37ac198c0433/2"
                "premium" -> "https://static-cdn.jtvnw.net/badges/v1/a1dd5073-19c3-4911-8cb4-c464a7bc1510/2"
                "partner" -> "https://static-cdn.jtvnw.net/badges/v1/d12a2e27-16f6-41d0-ab77-b780518f00a3/2"
                "clip-champ" -> "https://static-cdn.jtvnw.net/badges/v1/f38976e0-ffc9-11e7-86d6-7f98b26a9d79/2"
                "vip" -> "https://static-cdn.jtvnw.net/badges/v1/b817aba4-fad8-49e2-b88a-7cc744dfa6ec/2"
                "glhf-pledge" -> "https://static-cdn.jtvnw.net/badges/v1/3158e758-3cb4-43c5-94b3-7639810451c5/2"
                else -> null
            }
            url?.let {
                builder.append("  ")
                images.add(Image(url, index++, index++, false))
                badgesCount++
            }
        }
        val userName = chatMessage.displayName
        val userNameLength = userName.length
        val userNameEndIndex = index + userNameLength
        val originalMessage: String
        val userNameWithPostfixLength: Int
        builder.append(userName)
        if (!chatMessage.isAction) {
            builder.append(": ")
            originalMessage = "$userName: ${chatMessage.message}"
            userNameWithPostfixLength = userNameLength + 2
        } else {
            builder.append(" ")
            originalMessage = "$userName ${chatMessage.message}"
            userNameWithPostfixLength = userNameLength + 1
        }
        builder.append(chatMessage.message)
        val color = chatMessage.color.let { userColor ->
            if (userColor == null) {
                userColors[userName] ?: getRandomColor().also { userColors[userName] = it }
            } else {
                savedColors[userColor]
                        ?: Color.parseColor(userColor).also { savedColors[userColor] = it }
            }
        }
        builder.setSpan(ForegroundColorSpan(color), index, userNameEndIndex, SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.setSpan(StyleSpan(Typeface.BOLD), index, userNameEndIndex, SPAN_EXCLUSIVE_EXCLUSIVE)
        try {
            chatMessage.emotes?.let { emotes ->
                val copy = emotes.map {
                    val realBegin = chatMessage.message.offsetByCodePoints(0, it.begin)
                    val realEnd = if (it.begin == realBegin) {
                        it.end
                    } else {
                        it.end + realBegin - it.begin
                    }
                    TwitchEmote(it.name, realBegin, realEnd)
                }
                index += userNameWithPostfixLength
                for (e in copy) {
                    val begin = index + e.begin
                    builder.replace(begin, index + e.end + 1, ".")
                    builder.setSpan(ForegroundColorSpan(Color.TRANSPARENT), begin, begin + 1, SPAN_EXCLUSIVE_EXCLUSIVE)
                    val length = e.end - e.begin
                    for (e1 in copy) {
                        if (e.begin < e1.begin) {
                            e1.begin -= length
                            e1.end -= length
                        }
                    }
                    e.end -= length
                }
                copy.forEach { images.add(Image(it.url, index + it.begin, index + it.end + 1, true)) }
            }
            val split = builder.split(" ")
            var builderIndex = 0
            var emotesFound = 0
            var wasMentioned = false
            for (value in split) {
                val length = value.length
                val endIndex = builderIndex + length
                val emote = emotes[value]
                builderIndex += if (emote == null) {
                    if (!Patterns.WEB_URL.matcher(value).matches()) {
                        if (value.startsWith('@')) {
                            builder.setSpan(StyleSpan(Typeface.BOLD), builderIndex, endIndex, SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        username?.let {
                            if (!wasMentioned && value.contains(it, true) && chatMessage.userName != it) {
                                wasMentioned = true
                            }
                        }
                    } else {
                        val url = if (value.startsWith("http")) value else "https://$value"
                        builder.setSpan(URLSpan(url), builderIndex, endIndex, SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    length + 1
                } else {
                    for (j in images.lastIndex - emotesFound downTo badgesCount) {
                        val e = images[j]
                        if (e.start > builderIndex) {
                            val remove = length - 1
                            e.start -= remove
                            e.end -= remove
                        }
                    }
                    builder.replace(builderIndex, endIndex, ".")
                    builder.setSpan(ForegroundColorSpan(Color.TRANSPARENT), builderIndex, builderIndex + 1, SPAN_EXCLUSIVE_EXCLUSIVE)
                    images.add(Image(emote.url, builderIndex, builderIndex + 1, true, emote.isPng, emote.zerowidth))
                    emotesFound++
                    2
                }
            }
            if (chatMessage.isAction) {
                builder.setSpan(ForegroundColorSpan(color), userNameEndIndex + 1, builder.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            if (wasMentioned) {
                builder.setSpan(ForegroundColorSpan(Color.WHITE), 0, builder.length, SPAN_INCLUSIVE_INCLUSIVE)
                holder.textView.setBackgroundColor(Color.RED)
            } else {
                holder.textView.background = null
            }
        } catch (e: Exception) {
//            Crashlytics.logException(e)
        }
        holder.bind(originalMessage, builder)
        loadImages(holder, images, originalMessage, builder)
    }

    override fun getItemCount(): Int = messages?.size ?: 0

    private fun loadImages(holder: ViewHolder, images: List<Image>, originalMessage: CharSequence, builder: SpannableStringBuilder) {
        images.forEach { (url, start, end, isEmote, isPng, zerowidth) ->
            if (isPng || !animateGifs) {
                GlideApp.with(fragment)
                        .load(url)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(object : CustomTarget<Drawable>() {
                            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                val width: Int
                                val height: Int
                                if (isEmote) {
                                    val size = calculateEmoteSize(resource)
                                    width = size.first
                                    height = size.second
                                } else {
                                    width = badgeSize
                                    height = badgeSize
                                }
                                if (zerowidth && zeroWidth) resource.setBounds(-90, 0, width - 90, height)
                                else resource.setBounds(0, 0, width, height)
                                try {
                                    builder.setSpan(ImageSpan(resource), start, end, SPAN_EXCLUSIVE_EXCLUSIVE)
                                } catch (e: IndexOutOfBoundsException) {
//                                    Crashlytics.logException(e)
                                }
                                holder.bind(originalMessage, builder)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                            }
                        })
            } else {
                GlideApp.with(fragment)
                        .asGif()
                        .load(url)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(object : CustomTarget<GifDrawable>() {
                            override fun onResourceReady(resource: GifDrawable, transition: Transition<in GifDrawable>?) {
                                resource.apply {
                                    val size = calculateEmoteSize(this)
                                    if (zerowidth && zeroWidth) setBounds(-90, 0, size.first - 90, size.second)
                                    else setBounds(0, 0, size.first, size.second)
                                    setLoopCount(GifDrawable.LOOP_FOREVER)
                                    callback = object : Drawable.Callback {
                                        override fun unscheduleDrawable(who: Drawable, what: Runnable) {
                                            holder.textView.removeCallbacks(what)
                                        }

                                        override fun invalidateDrawable(who: Drawable) {
                                            holder.textView.invalidate()
                                        }

                                        override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
                                            holder.textView.postDelayed(what, `when`)
                                        }
                                    }
                                    start()
                                }
                                try {
                                    builder.setSpan(ImageSpan(resource), start, end, SPAN_EXCLUSIVE_EXCLUSIVE)
                                } catch (e: IndexOutOfBoundsException) {
//                                    Crashlytics.logException(e)
                                }
                                holder.bind(originalMessage, builder)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                            }
                        })
            }
        }
    }

    fun addEmotes(list: List<Emote>) {
        emotes.putAll(list.associateBy { it.name })
    }

    fun setUsername(username: String) {
        this.username = username
    }

    fun setOnClickListener(listener: (CharSequence, CharSequence) -> Unit) {
        messageClickListener = listener
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        (holder.textView.text as? Spannable)?.getSpans<ImageSpan>()?.forEach {
            (it.drawable as? GifDrawable)?.start()
        }
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        (holder.textView.text as? Spannable)?.getSpans<ImageSpan>()?.forEach {
            (it.drawable as? GifDrawable)?.stop()
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        val childCount = recyclerView.childCount
        for (i in 0 until childCount) {
            ((recyclerView.getChildAt(i) as TextView).text as? Spannable)?.getSpans<ImageSpan>()?.forEach {
                (it.drawable as? GifDrawable)?.stop()
            }
        }
        super.onDetachedFromRecyclerView(recyclerView)
    }

    private fun getRandomColor(): Int = twitchColors[random.nextInt(twitchColors.size)]

    private fun calculateEmoteSize(resource: Drawable): Pair<Int, Int> {
        val widthRatio = resource.intrinsicWidth.toFloat() / resource.intrinsicHeight.toFloat()
        val width: Int
        val height: Int
        when {
            widthRatio == 1f -> {
                width = emoteSize
                height = emoteSize
            }
            widthRatio <= 1.2f -> {
                width = (emoteSize * widthRatio).toInt()
                height = emoteSize
            }
            else -> {
                width = (scaledEmoteSize * widthRatio).toInt()
                height = scaledEmoteSize
            }
        }
        return width to height
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textView = itemView as TextView

        fun bind(originalMessage: CharSequence, formattedMessage: SpannableStringBuilder) {
            textView.apply {
                text = formattedMessage
                movementMethod = LinkMovementMethod.getInstance()
                setOnClickListener { messageClickListener?.invoke(originalMessage, formattedMessage) }
            }
        }
    }

    private companion object {
        const val BADGES_URL = "https://static-cdn.jtvnw.net/chat-badges/"
    }
}
