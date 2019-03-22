package com.github.exact7.xtra.ui.common

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.method.LinkMovementMethod
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.util.Patterns
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.github.exact7.xtra.GlideApp
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.chat.BttvEmote
import com.github.exact7.xtra.model.chat.ChatMessage
import com.github.exact7.xtra.model.chat.Emote
import com.github.exact7.xtra.model.chat.FfzEmote
import com.github.exact7.xtra.model.chat.Image
import java.util.Random
import kotlin.collections.set

const val EMOTES_URL = "https://static-cdn.jtvnw.net/emoticons/v1/"
const val BTTV_URL = "https://cdn.betterttv.net/emote/"

class ChatAdapter(private val context: Context) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    lateinit var messages: MutableList<ChatMessage>
    private val twitchColors = intArrayOf(-65536, -16776961, -16744448, -5103070, -32944, -6632142, -47872, -13726889, -2448096, -2987746, -10510688, -14774017, -38476, -7722014, -16711809)
    private val random = Random()
    private val userColors = HashMap<String, Int>()
    private val savedColors = HashMap<String, Int>()
    private val emotes: HashMap<String, Emote> = initBttv().also { it.putAll(initFfz()) }
    private var userNickname: String? = null
    private val emoteSize = convertDpToPixels(26f)
    private val badgeSize = convertDpToPixels(18f)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.chat_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatMessage = messages[position]
        val builder = SpannableStringBuilder()
        val badgesUrl = "https://static-cdn.jtvnw.net/chat-badges/"
        val images = ArrayList<Image>()
        var index = 0
        chatMessage.badges?.forEach { (id, version) ->
            val url: String? = when (id) {
                "admin" -> badgesUrl + "admin.png"
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
                "broadcaster" -> badgesUrl + "broadcaster.png"
                "global_mod" -> badgesUrl + "globalmod.png"
                "moderator" -> badgesUrl + "mod.png"
                "subscriber" -> chatMessage.subscriberBadge?.imageUrl2x
                "staff" -> badgesUrl + "staff.png"
                "turbo" -> badgesUrl + "turbo.png"
                "sub-gifter" -> "https://static-cdn.jtvnw.net/badges/v1/4592e9ea-b4ca-4948-93b8-37ac198c0433/2"
                "premium" -> "https://static-cdn.jtvnw.net/badges/v1/a1dd5073-19c3-4911-8cb4-c464a7bc1510/2"
                "partner" -> "https://static-cdn.jtvnw.net/badges/v1/d12a2e27-16f6-41d0-ab77-b780518f00a3/2"
                "clip-champ" -> "https://static-cdn.jtvnw.net/badges/v1/f38976e0-ffc9-11e7-86d6-7f98b26a9d79/2"
                else -> null
            }
            url?.let {
                builder.append("  ")
                images.add(Image(url, index++, index++, false))
            }
        }
        val userName = chatMessage.displayName
        builder.append(userName).append(": ").append(chatMessage.message)
        val color = chatMessage.color.let { userColor ->
            if (userColor == null) {
                userColors[userName] ?: getRandomColor().also { userColors[userName] = it }
            } else {
                savedColors[userColor]
                        ?: Color.parseColor(userColor).also { savedColors[userColor] = it }
            }
        }
        val userNameLength = userName.length
        builder.setSpan(ForegroundColorSpan(color), index, index + userNameLength, SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.setSpan(StyleSpan(Typeface.BOLD), index, index + userNameLength, SPAN_EXCLUSIVE_EXCLUSIVE)
        chatMessage.emotes?.let {
            val copy = it.map { e -> e.copy() }
            index += userNameLength + 2
            for (e in copy) {
                val begin = index + e.begin
                builder.replace(begin, index + e.end + 1, ".") //TODO emojis break this
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
            copy.forEach { (id, begin, end) -> images.add(Image("$EMOTES_URL$id/2.0", index + begin, index + end + 1, true)) }
        }
        val split = builder.split(" ")
        var builderIndex = 0
        for (i in 0 until split.size) {
            val value = split[i]
            val length = value.length
            val endIndex = builderIndex + length
            val emote = emotes[value]
            builderIndex += if (emote == null) {
                if (Patterns.WEB_URL.matcher(value).matches()) {
                    var url = value
                    if (!value.startsWith("http")) {
                        url = "https://$url"
                    }
                    builder.setSpan(URLSpan(url), builderIndex, endIndex, SPAN_EXCLUSIVE_EXCLUSIVE)
                } else {
                    if (value.startsWith('@')) {
                        builder.setSpan(StyleSpan(Typeface.BOLD), builderIndex, endIndex, SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    userNickname?.let {
                        if (value.contains(it, true) && !value.endsWith(':')) {
                            builder.setSpan(BackgroundColorSpan(Color.RED), 0, builder.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                }
                length + 1
            } else {
                chatMessage.emotes?.let {
                    for (j in it.size - 1 downTo 0) {
                        val e = images[j]
                        if (e.start > builderIndex) {
                            e.start -= length
                            e.end -= length
                        } else {
                            break
                        }
                    }
                }
                builder.replace(builderIndex, endIndex, ".")
                builder.setSpan(ForegroundColorSpan(Color.TRANSPARENT), builderIndex, builderIndex + 1, SPAN_EXCLUSIVE_EXCLUSIVE)
                val url: String
                val isPng: Boolean
                val width: Float?
                if (emote is BttvEmote) {
                    url = "$BTTV_URL${emote.id}/2x"
                    isPng = emote.isPng
                    width = null
                } else { //FFZ
                    (emote as FfzEmote).also {
                        url = it.url
                        isPng = true
                        width = it.width
                    }
                }
                images.add(Image(url, builderIndex, builderIndex + 1, true, isPng, width))
                if (i != split.lastIndex) 2 else 1
            }
        }
        holder.bind(builder)
        loadImages(holder, images, builder)
    }

    override fun getItemCount(): Int = if (this::messages.isInitialized) messages.size else 0

    private fun loadImages(holder: ViewHolder, images: List<Image>, builder: SpannableStringBuilder) {
        images.forEach { (url, start, end, isEmote, isPng, width) ->
            if (isPng) {
                GlideApp.with(context)
                        .load(url)
                        .into(object : SimpleTarget<Drawable>() {
                            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                try {
                                    val size = if (isEmote) emoteSize else badgeSize
                                    resource.setBounds(0, 0, if (width == null) size else convertPixelsToDp(width * 1.65f), size)
                                    builder.setSpan(ImageSpan(resource), start, end, SPAN_EXCLUSIVE_EXCLUSIVE)
                                } catch (e: Exception) {
                                    //TODO find error
                                }
                                holder.bind(builder)
                            }
                        })
            } else {
                GlideApp.with(context)
                        .asGif()
                        .load(url)
                        .into(object : SimpleTarget<GifDrawable>() {
                            override fun onResourceReady(resource: GifDrawable, transition: Transition<in GifDrawable>?) {
                                val textView = holder.itemView as TextView
                                val callback = object : Drawable.Callback {
                                    override fun unscheduleDrawable(who: Drawable, what: Runnable) {
                                        textView.removeCallbacks(what)
                                    }

                                    override fun invalidateDrawable(who: Drawable) {
                                        textView.invalidate()
                                    }

                                    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
                                        textView.postDelayed(what, `when`)
                                    }
                                }
                                resource.apply {
                                    setBounds(0, 0, emoteSize, emoteSize)
                                    setLoopCount(GifDrawable.LOOP_FOREVER)
                                    this.callback = callback
                                    start()
                                }
                                try {
                                    builder.setSpan(ImageSpan(resource), start, end, SPAN_EXCLUSIVE_EXCLUSIVE)
                                } catch (e: Exception) {
                                    //TODO find error
                                }
                                holder.bind(builder)
                            }
                        })
            }
        }
    }

    fun addEmotes(list: List<Emote>) {
        emotes.putAll(list.associateBy { it.name })
    }

    fun setUserNickname(nickname: String) {
        userNickname = nickname
    }

    private fun getRandomColor(): Int = twitchColors[random.nextInt(twitchColors.size)]

    private fun convertDpToPixels(dp: Float) = try { //TODO
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics).toInt()
    } catch (e: NullPointerException) {
        0
    }

    private fun convertPixelsToDp(pixels: Float) = try {
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, pixels, context.resources.displayMetrics).toInt()
    } catch (e: NullPointerException) {
        0
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(formattedMessage: SpannableStringBuilder) {
            (itemView as TextView).apply {
                text = formattedMessage
                movementMethod = LinkMovementMethod.getInstance()
            }
        }
    }

    private fun initBttv(): HashMap<String, Emote> = hashMapOf(
            "OhMyGoodness" to BttvEmote("54fa925e01e468494b85b54d", "OhMyGoodness", "png")
            ,"PancakeMix" to BttvEmote("54fa927801e468494b85b54e", "PancakeMix", "png")
            ,"PedoBear" to BttvEmote("54fa928f01e468494b85b54f", "PedoBear", "png")
            ,"PokerFace" to BttvEmote("54fa92a701e468494b85b550", "PokerFace", "png")
            ,"RageFace" to BttvEmote("54fa92d701e468494b85b552", "RageFace", "png")
            ,"RebeccaBlack" to BttvEmote("54fa92ee01e468494b85b553", "RebeccaBlack", "png")
            ,":tf:" to BttvEmote("54fa8f1401e468494b85b537", ":tf:", "png")
            ,"aPliS" to BttvEmote("54fa8f4201e468494b85b538", "aPliS", "png")
            ,"CiGrip" to BttvEmote("54fa8fce01e468494b85b53c", "CiGrip", "png")
            ,"CHAccepted" to BttvEmote("54fa8fb201e468494b85b53b", "CHAccepted", "png")
            ,"FuckYea" to BttvEmote("54fa90d601e468494b85b544", "FuckYea", "png")
            ,"DatSauce" to BttvEmote("54fa903b01e468494b85b53f", "DatSauce", "png")
            ,"ForeverAlone" to BttvEmote("54fa909b01e468494b85b542", "ForeverAlone", "png")
            ,"GabeN" to BttvEmote("54fa90ba01e468494b85b543", "GabeN", "png")
            ,"HailHelix" to BttvEmote("54fa90f201e468494b85b545", "HailHelix", "png")
            ,"HerbPerve" to BttvEmote("54fa913701e468494b85b546", "HerbPerve", "png")
            ,"iDog" to BttvEmote("54fa919901e468494b85b548", "iDog", "png")
            ,"rStrike" to BttvEmote("54fa930801e468494b85b554", "rStrike", "png")
            ,"ShoopDaWhoop" to BttvEmote("54fa932201e468494b85b555", "ShoopDaWhoop", "png")
            ,"SwedSwag" to BttvEmote("54fa9cc901e468494b85b565", "SwedSwag", "png")
            ,"M&Mjc" to BttvEmote("54fab45f633595ca4c713abc", "M&Mjc", "png")
            ,"bttvNice" to BttvEmote("54fab7d2633595ca4c713abf", "bttvNice", "png")
            ,"TopHam" to BttvEmote("54fa934001e468494b85b556", "TopHam", "png")
            ,"TwaT" to BttvEmote("54fa935601e468494b85b557", "TwaT", "png")
            ,"WhatAYolk" to BttvEmote("54fa93d001e468494b85b559", "WhatAYolk", "png")
            ,"WatChuSay" to BttvEmote("54fa99b601e468494b85b55d", "WatChuSay", "png")
            ,"Blackappa" to BttvEmote("54faa50d01e468494b85b578", "Blackappa", "png")
            ,"DogeWitIt" to BttvEmote("54faa52f01e468494b85b579", "DogeWitIt", "png")
            ,"BadAss" to BttvEmote("54faa4f101e468494b85b577", "BadAss", "png")
            ,"SavageJerky" to BttvEmote("54fb603201abde735115ddb5", "SavageJerky", "png")
            ,"Zappa" to BttvEmote("5622aaef3286c42e57d8e4ab", "Zappa", "png")
            ,"tehPoleCat" to BttvEmote("566ca11a65dbbdab32ec0558", "tehPoleCat", "png")
            ,"AngelThump" to BttvEmote("566ca1a365dbbdab32ec055b", "AngelThump", "png")
            ,"Kaged" to BttvEmote("54fbf11001abde735115de66", "Kaged", "png")
            ,"HHydro" to BttvEmote("54fbef6601abde735115de57", "HHydro", "png")
            ,"TaxiBro" to BttvEmote("54fbefeb01abde735115de5b", "TaxiBro", "png")
            ,"BroBalt" to BttvEmote("54fbf00a01abde735115de5c", "BroBalt", "png")
            ,"ButterSauce" to BttvEmote("54fbf02f01abde735115de5d", "ButterSauce", "png")
            ,"BaconEffect" to BttvEmote("54fbf05a01abde735115de5e", "BaconEffect", "png")
            ,"SuchFraud" to BttvEmote("54fbf07e01abde735115de5f", "SuchFraud", "png")
            ,"CandianRage" to BttvEmote("54fbf09c01abde735115de61", "CandianRage", "png")
            ,"She'llBeRight" to BttvEmote("54fbefc901abde735115de5a", "She'llBeRight", "png")
            ,"OhhhKee" to BttvEmote("54fbefa901abde735115de59", "OhhhKee", "png")
            ,"D:" to BttvEmote("55028cd2135896936880fdd7", "D:", "png")
            ,"SexPanda" to BttvEmote("5502874d135896936880fdd2", "SexPanda", "png")
            ,"(poolparty)" to BttvEmote("5502883d135896936880fdd3", "(poolparty)", "png")
            ,":'(" to BttvEmote("55028923135896936880fdd5", ":'(", "png")
            ,"(puke)" to BttvEmote("550288fe135896936880fdd4", "(puke)", "png")
            ,"bttvWink" to BttvEmote("550292c0135896936880fdef", "bttvWink", "png")
            ,"bttvAngry" to BttvEmote("550291a3135896936880fde3", "bttvAngry", "png")
            ,"bttvConfused" to BttvEmote("550291be135896936880fde4", "bttvConfused", "png")
            ,"bttvCool" to BttvEmote("550291d4135896936880fde5", "bttvCool", "png")
            ,"bttvHappy" to BttvEmote("55029200135896936880fde7", "bttvHappy", "png")
            ,"bttvSad" to BttvEmote("5502925d135896936880fdea", "bttvSad", "png")
            ,"bttvSleep" to BttvEmote("55029272135896936880fdeb", "bttvSleep", "png")
            ,"bttvSurprised" to BttvEmote("55029288135896936880fdec", "bttvSurprised", "png")
            ,"bttvTongue" to BttvEmote("5502929b135896936880fded", "bttvTongue", "png")
            ,"bttvUnsure" to BttvEmote("550292ad135896936880fdee", "bttvUnsure", "png")
            ,"bttvGrin" to BttvEmote("550291ea135896936880fde6", "bttvGrin", "png")
            ,"bttvHeart" to BttvEmote("55029215135896936880fde8", "bttvHeart", "png")
            ,"bttvTwink" to BttvEmote("55029247135896936880fde9", "bttvTwink", "png")
            ,"VisLaud" to BttvEmote("550352766f86a5b26c281ba2", "VisLaud", "png")
            ,"(chompy)" to BttvEmote("550b225fff8ecee922d2a3b2", "(chompy)", "gif")
            ,"SoSerious" to BttvEmote("5514afe362e6bd0027aede8a", "SoSerious", "png")
            ,"BatKappa" to BttvEmote("550b6b07ff8ecee922d2a3e7", "BatKappa", "png")
            ,"KaRappa" to BttvEmote("550b344bff8ecee922d2a3c1", "KaRappa", "png")
            ,"YetiZ" to BttvEmote("55189a5062e6bd0027aee082", "YetiZ", "png")
            ,"miniJulia" to BttvEmote("552d2fc2236a1aa17a996c5b", "miniJulia", "png")
            ,"FishMoley" to BttvEmote("566ca00f65dbbdab32ec0544", "FishMoley", "png")
            ,"Hhhehehe" to BttvEmote("566ca02865dbbdab32ec0547", "Hhhehehe", "png")
            ,"KKona" to BttvEmote("566ca04265dbbdab32ec054a", "KKona", "png")
            ,"OhGod" to BttvEmote("566ca07965dbbdab32ec0552", "OhGod", "png")
            ,"PoleDoge" to BttvEmote("566ca09365dbbdab32ec0555", "PoleDoge", "png")
            ,"motnahP" to BttvEmote("55288e390fa35376704a4c7a", "motnahP", "png")
            ,"sosGame" to BttvEmote("553b48a21f145f087fc15ca6", "sosGame", "png")
            ,"CruW" to BttvEmote("55471c2789d53f2d12781713", "CruW", "png")
            ,"RarePepe" to BttvEmote("555015b77676617e17dd2e8e", "RarePepe", "png")
            ,"iamsocal" to BttvEmote("54fbef8701abde735115de58", "iamsocal", "png")
            ,"haHAA" to BttvEmote("555981336ba1901877765555", "haHAA", "png")
            ,"FeelsBirthdayMan" to BttvEmote("55b6524154eefd53777b2580", "FeelsBirthdayMan", "png")
            ,"RonSmug" to BttvEmote("55f324c47f08be9f0a63cce0", "RonSmug", "png")
            ,"KappaCool" to BttvEmote("560577560874de34757d2dc0", "KappaCool", "png")
            ,"FeelsBadMan" to BttvEmote("566c9fc265dbbdab32ec053b", "FeelsBadMan", "png")
            ,"BasedGod" to BttvEmote("566c9eeb65dbbdab32ec052b", "BasedGod", "png")
            ,"bUrself" to BttvEmote("566c9f3b65dbbdab32ec052e", "bUrself", "png")
            ,"ConcernDoge" to BttvEmote("566c9f6365dbbdab32ec0532", "ConcernDoge", "png")
            ,"FapFapFap" to BttvEmote("566c9f9265dbbdab32ec0538", "FapFapFap", "png")
            ,"FeelsGoodMan" to BttvEmote("566c9fde65dbbdab32ec053e", "FeelsGoodMan", "png")
            ,"FireSpeed" to BttvEmote("566c9ff365dbbdab32ec0541", "FireSpeed", "png")
            ,"NaM" to BttvEmote("566ca06065dbbdab32ec054e", "NaM", "png")
            ,"SourPls" to BttvEmote("566ca38765dbbdab32ec0560", "SourPls", "gif")
            ,"LuL" to BttvEmote("567b00c61ddbe1786688a633", "LuL", "png")
            ,"SaltyCorn" to BttvEmote("56901914991f200c34ffa656", "SaltyCorn", "png")
            ,"FCreep" to BttvEmote("56d937f7216793c63ec140cb", "FCreep", "png")
            ,"monkaS" to BttvEmote("56e9f494fff3cc5c35e5287e", "monkaS", "png")
            ,"VapeNation" to BttvEmote("56f5be00d48006ba34f530a4", "VapeNation", "png")
            ,"ariW" to BttvEmote("56fa09f18eff3b595e93ac26", "ariW", "png")
            ,"notsquishY" to BttvEmote("5709ab688eff3b595e93c595", "notsquishY", "png")
            ,"FeelsAmazingMan" to BttvEmote("5733ff12e72c3c0814233e20", "FeelsAmazingMan", "png")
            ,"DuckerZ" to BttvEmote("573d38b50ffbf6cc5cc38dc9", "DuckerZ", "png")
            ,"SqShy" to BttvEmote("59cf182fcbe2693d59d7bf46", "SqShy", "png")
            ,"Wowee" to BttvEmote("58d2e73058d8950a875ad027", "Wowee", "png")
    )

    private fun initFfz(): HashMap<String, Emote> = hashMapOf(
            "ZrehplaR" to FfzEmote("ZrehplaR", "https:////cdn.frankerfacez.com/4556987ef91323110080b223f96d7400.png", 33f),
            "YooHoo" to FfzEmote("YooHoo", "https:////cdn.frankerfacez.com/4c3f7ea69ff255ff4ee0adebbe62751c.png", 28f),
            "YellowFever" to FfzEmote("YellowFever", "https:////cdn.frankerfacez.com/53ebdc39a0603f7a3372ad016bb8597e.png", 23f),
            "ManChicken" to FfzEmote("ManChicken", "https:////cdn.frankerfacez.com/08c6aa877f0d946d45f30454d9fb1300.png", 30f),
            "BeanieHipster" to FfzEmote("BeanieHipster", "https:////cdn.frankerfacez.com/9c727af513b96dfb0dcbc86daa2239d8.png", 28f),
            "CatBag" to FfzEmote("CatBag", "https:////cdn.frankerfacez.com/585e6fdea0c5d3e20678284f43af8749.PNG", 32f),
            "ZreknarF" to FfzEmote("ZreknarF", "https:////cdn.frankerfacez.com/1719f1cd21489ef579f6e8bbc861c22f.PNG", 40f),
            "LilZ" to FfzEmote("LilZ", "https:////cdn.frankerfacez.com/a6623002a430bcd27edc866441c1582f.PNG", 32f),
            "ZliL" to FfzEmote("ZliL", "https:////cdn.frankerfacez.com/0aa63d7f0ac0a6d3622b009b5af28944.PNG", 32f),
            "LaterSooner" to FfzEmote("LaterSooner", "https:////cdn.frankerfacez.com/1fcefc6216cd36fc9ce2a2f7385d854c.PNG", 25f),
            "BORT" to FfzEmote("BORT", "https:////cdn.frankerfacez.com/aa4c8a9d459c866e9f9e03aac614c47a.png", 19f)
    )
}
