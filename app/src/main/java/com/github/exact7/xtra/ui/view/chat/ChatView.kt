package com.github.exact7.xtra.ui.view.chat

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.chat.BttvEmote
import com.github.exact7.xtra.model.chat.ChatMessage
import com.github.exact7.xtra.model.chat.Emote
import com.github.exact7.xtra.model.chat.FfzEmote
import com.github.exact7.xtra.ui.common.ChatAdapter
import com.github.exact7.xtra.ui.streams.EmotesAdapter
import com.github.exact7.xtra.util.convertDpToPixels
import com.github.exact7.xtra.util.gone
import com.github.exact7.xtra.util.hideKeyboard
import com.github.exact7.xtra.util.isGone
import com.github.exact7.xtra.util.isVisible
import com.github.exact7.xtra.util.showKeyboard
import com.github.exact7.xtra.util.toggleVisibility
import com.github.exact7.xtra.util.visible
import kotlinx.android.synthetic.main.view_chat.view.*
import kotlin.math.max

private const val MAX_MESSAGE_COUNT = 125

class ChatView : ConstraintLayout {

    interface MessageSenderCallback {
        fun send(message: CharSequence)
    }

    private val adapter = ChatAdapter(context.convertDpToPixels(29.5f), context.convertDpToPixels(18.5f))

    private var isChatTouched = false

    private var twitchEmotes: MutableList<Emote> = ArrayList()
    private var otherEmotes: MutableSet<Emote> = HashSet()
    private var emotesAddedCount = 0

    private lateinit var fragmentManager: FragmentManager
    private var messagingEnabled = false

    private var messageCallback: MessageSenderCallback? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    init {
        val emotes: List<Emote> = initBttv() + initFfz()
        adapter.addEmotes(emotes)
        otherEmotes.addAll(emotes)
    }

    private fun init(context: Context) {
        View.inflate(context, R.layout.view_chat, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        recyclerView.apply {
            adapter = this@ChatView.adapter
            itemAnimator = null
            layoutManager = LinearLayoutManager(context).apply { stackFromEnd = true }
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    isChatTouched = newState == RecyclerView.SCROLL_STATE_DRAGGING
                    this@ChatView.btnDown.visible(shouldShowButton())
                }
            })
        }

        btnDown.setOnClickListener {
            post {
                recyclerView.scrollToPosition(getLastItemPosition())
                it.toggleVisibility()
            }
        }

        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
            } else {
                false
            }
        }
        editText.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            val notBlank = text?.isNotBlank() == true
            send.visible(notBlank)
            clear.visible(notBlank)
        })
        clear.setOnClickListener {
            val text = editText.text.toString().trimEnd()
            editText.setText(text.substring(0, max(text.lastIndexOf(' '), 0)))
        }
        send.setOnClickListener { sendMessage() }
    }

    fun submitList(list: MutableList<ChatMessage>) {
        adapter.messages = list
    }

    fun notifyMessageAdded() {
        adapter.messages?.let {
            adapter.notifyItemInserted(getLastItemPosition())
            if (getLastItemPosition() > MAX_MESSAGE_COUNT) {
                it.removeAt(0)
                adapter.notifyItemRemoved(0)
            }
            if (!isChatTouched && btnDown.isGone()) {
                recyclerView.scrollToPosition(getLastItemPosition())
            }
        }
    }

    fun addEmotes(list: List<Emote>?) {
        if (list?.isNotEmpty() == true) {
            when (list.first()) {
                is BttvEmote, is FfzEmote -> {
                    otherEmotes.addAll(list)
                    adapter.addEmotes(list)
                }
                else -> twitchEmotes.addAll(list)
            }
        }
        if (++emotesAddedCount == 3 && messagingEnabled) {
            initEmotesViewPager()
        }
    }

    fun setRecentEmotes(list: List<Emote>) {
        if (list.isNotEmpty()) {
            recentEmotes.apply {
                adapter = EmotesAdapter(list) { appendEmote(it) }
                visible()
            }
        }
    }

    fun setUsername(username: String) {
        adapter.setUsername(username)
    }

    fun setCallback(callback: MessageSenderCallback) {
        messageCallback = callback
    }

    fun hideEmotesMenu(): Boolean {
        return if (emotesMenu.isVisible()) {
            emotesMenu.gone()
            true
        } else {
            false
        }
    }

    fun appendEmote(emote: Emote) {
        editText.text.append(emote.name).append(' ')
    }

    @SuppressLint("SetTextI18n")
    fun reply(userName: CharSequence) {
        val text = "@$userName "
        editText.apply {
            setText(text)
            setSelection(text.length)
            showKeyboard()
        }
    }

    fun setMessage(text: CharSequence) {
        editText.setText(text)
    }

    fun enableMessaging(fragmentManager: FragmentManager) {
        this.fragmentManager = fragmentManager
        messagingEnabled = true
        adapter.setOnClickListener { original, formatted ->
            editText.hideKeyboard()
            MessageClickedDialog.newInstance(original, formatted).show(fragmentManager, null)
        }
        messageView.visible(true)
    }

    private fun sendMessage(): Boolean {
        editText.hideKeyboard()
        editText.clearFocus()
        hideEmotesMenu()
        return messageCallback?.let {
            val text = editText.text.trim()
            editText.text.clear()
            if (text.isNotEmpty()) {
                it.send(text)
                true
            } else {
                false
            }
        } == true
    }

    private fun initEmotesViewPager() {
        viewPager.adapter = object : FragmentStatePagerAdapter(fragmentManager) {

            override fun getItem(position: Int): Fragment {
                val list = when (position) {
                    0 -> twitchEmotes
                    else -> otherEmotes
                }
                return EmotesFragment.newInstance(list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }))
            }

            override fun getCount(): Int = 2

            override fun getPageTitle(position: Int): CharSequence? {
                return when (position) {
                    0 -> "Twitch"
                    else -> "BTTV/FFZ"
                }
            }
        }
        emotes.setOnClickListener {
            //TODO add animation
            emotesMenu.toggleVisibility()
        }
    }

    private fun getLastItemPosition(): Int = adapter.itemCount - 1

    private fun shouldShowButton(): Boolean {
        val offset = recyclerView.computeVerticalScrollOffset()
        if (offset < 0) {
            return false
        }
        val extent = recyclerView.computeVerticalScrollExtent()
        val range = recyclerView.computeVerticalScrollRange()
        val percentage = (100f * offset / (range - extent).toFloat())
        return percentage < 97f
    }

    private fun initBttv() = listOf(
            BttvEmote("54fa925e01e468494b85b54d", "OhMyGoodness", "png"),
            BttvEmote("54fa927801e468494b85b54e", "PancakeMix", "png"),
            BttvEmote("54fa928f01e468494b85b54f", "PedoBear", "png"),
            BttvEmote("54fa92a701e468494b85b550", "PokerFace", "png"),
            BttvEmote("54fa92d701e468494b85b552", "RageFace", "png"),
            BttvEmote("54fa92ee01e468494b85b553", "RebeccaBlack", "png"),
            BttvEmote("54fa8f1401e468494b85b537", ":tf:", "png"),
            BttvEmote("54fa8f4201e468494b85b538", "aPliS", "png"),
            BttvEmote("54fa8fce01e468494b85b53c", "CiGrip", "png"),
            BttvEmote("54fa8fb201e468494b85b53b", "CHAccepted", "png"),
            BttvEmote("54fa90d601e468494b85b544", "FuckYea", "png"),
            BttvEmote("54fa903b01e468494b85b53f", "DatSauce", "png"),
            BttvEmote("54fa909b01e468494b85b542", "ForeverAlone", "png"),
            BttvEmote("54fa90ba01e468494b85b543", "GabeN", "png"),
            BttvEmote("54fa90f201e468494b85b545", "HailHelix", "png"),
            BttvEmote("54fa913701e468494b85b546", "HerbPerve", "png"),
            BttvEmote("54fa919901e468494b85b548", "iDog", "png"),
            BttvEmote("54fa930801e468494b85b554", "rStrike", "png"),
            BttvEmote("54fa932201e468494b85b555", "ShoopDaWhoop", "png"),
            BttvEmote("54fa9cc901e468494b85b565", "SwedSwag", "png"),
            BttvEmote("54fab45f633595ca4c713abc", "M&Mjc", "png"),
            BttvEmote("54fab7d2633595ca4c713abf", "bttvNice", "png"),
            BttvEmote("54fa934001e468494b85b556", "TopHam", "png"),
            BttvEmote("54fa935601e468494b85b557", "TwaT", "png"),
            BttvEmote("54fa93d001e468494b85b559", "WhatAYolk", "png"),
            BttvEmote("54fa99b601e468494b85b55d", "WatChuSay", "png"),
            BttvEmote("54faa50d01e468494b85b578", "Blackappa", "png"),
            BttvEmote("54faa52f01e468494b85b579", "DogeWitIt", "png"),
            BttvEmote("54faa4f101e468494b85b577", "BadAss", "png"),
            BttvEmote("54fb603201abde735115ddb5", "SavageJerky", "png"),
            BttvEmote("5622aaef3286c42e57d8e4ab", "Zappa", "png"),
            BttvEmote("566ca11a65dbbdab32ec0558", "tehPoleCat", "png"),
            BttvEmote("566ca1a365dbbdab32ec055b", "AngelThump", "png"),
            BttvEmote("54fbf11001abde735115de66", "Kaged", "png"),
            BttvEmote("54fbef6601abde735115de57", "HHydro", "png"),
            BttvEmote("54fbefeb01abde735115de5b", "TaxiBro", "png"),
            BttvEmote("54fbf00a01abde735115de5c", "BroBalt", "png"),
            BttvEmote("54fbf02f01abde735115de5d", "ButterSauce", "png"),
            BttvEmote("54fbf05a01abde735115de5e", "BaconEffect", "png"),
            BttvEmote("54fbf07e01abde735115de5f", "SuchFraud", "png"),
            BttvEmote("54fbf09c01abde735115de61", "CandianRage", "png"),
            BttvEmote("54fbefc901abde735115de5a", "She'llBeRight", "png"),
            BttvEmote("54fbefa901abde735115de59", "OhhhKee", "png"),
            BttvEmote("55028cd2135896936880fdd7", "D:", "png"),
            BttvEmote("5502874d135896936880fdd2", "SexPanda", "png"),
            BttvEmote("5502883d135896936880fdd3", "(poolparty)", "png"),
            BttvEmote("55028923135896936880fdd5", ":'(", "png"),
            BttvEmote("550288fe135896936880fdd4", "(puke)", "png"),
            BttvEmote("550292c0135896936880fdef", "bttvWink", "png"),
            BttvEmote("550291a3135896936880fde3", "bttvAngry", "png"),
            BttvEmote("550291be135896936880fde4", "bttvConfused", "png"),
            BttvEmote("550291d4135896936880fde5", "bttvCool", "png"),
            BttvEmote("55029200135896936880fde7", "bttvHappy", "png"),
            BttvEmote("5502925d135896936880fdea", "bttvSad", "png"),
            BttvEmote("55029272135896936880fdeb", "bttvSleep", "png"),
            BttvEmote("55029288135896936880fdec", "bttvSurprised", "png"),
            BttvEmote("5502929b135896936880fded", "bttvTongue", "png"),
            BttvEmote("550292ad135896936880fdee", "bttvUnsure", "png"),
            BttvEmote("550291ea135896936880fde6", "bttvGrin", "png"),
            BttvEmote("55029215135896936880fde8", "bttvHeart", "png"),
            BttvEmote("55029247135896936880fde9", "bttvTwink", "png"),
            BttvEmote("550352766f86a5b26c281ba2", "VisLaud", "png"),
            BttvEmote("550b225fff8ecee922d2a3b2", "(chompy)", "gif"),
            BttvEmote("5514afe362e6bd0027aede8a", "SoSerious", "png"),
            BttvEmote("550b6b07ff8ecee922d2a3e7", "BatKappa", "png"),
            BttvEmote("550b344bff8ecee922d2a3c1", "KaRappa", "png"),
            BttvEmote("55189a5062e6bd0027aee082", "YetiZ", "png"),
            BttvEmote("552d2fc2236a1aa17a996c5b", "miniJulia", "png"),
            BttvEmote("566ca00f65dbbdab32ec0544", "FishMoley", "png"),
            BttvEmote("566ca02865dbbdab32ec0547", "Hhhehehe", "png"),
            BttvEmote("566ca04265dbbdab32ec054a", "KKona", "png"),
            BttvEmote("566ca07965dbbdab32ec0552", "OhGod", "png"),
            BttvEmote("566ca09365dbbdab32ec0555", "PoleDoge", "png"),
            BttvEmote("55288e390fa35376704a4c7a", "motnahP", "png"),
            BttvEmote("553b48a21f145f087fc15ca6", "sosGame", "png"),
            BttvEmote("55471c2789d53f2d12781713", "CruW", "png"),
            BttvEmote("555015b77676617e17dd2e8e", "RarePepe", "png"),
            BttvEmote("54fbef8701abde735115de58", "iamsocal", "png"),
            BttvEmote("555981336ba1901877765555", "haHAA", "png"),
            BttvEmote("55b6524154eefd53777b2580", "FeelsBirthdayMan", "png"),
            BttvEmote("55f324c47f08be9f0a63cce0", "RonSmug", "png"),
            BttvEmote("560577560874de34757d2dc0", "KappaCool", "png"),
            BttvEmote("566c9fc265dbbdab32ec053b", "FeelsBadMan", "png"),
            BttvEmote("566c9eeb65dbbdab32ec052b", "BasedGod", "png"),
            BttvEmote("566c9f3b65dbbdab32ec052e", "bUrself", "png"),
            BttvEmote("566c9f6365dbbdab32ec0532", "ConcernDoge", "png"),
            BttvEmote("566c9f9265dbbdab32ec0538", "FapFapFap", "png"),
            BttvEmote("566c9fde65dbbdab32ec053e", "FeelsGoodMan", "png"),
            BttvEmote("566c9ff365dbbdab32ec0541", "FireSpeed", "png"),
            BttvEmote("566ca06065dbbdab32ec054e", "NaM", "png"),
            BttvEmote("566ca38765dbbdab32ec0560", "SourPls", "gif"),
            BttvEmote("567b00c61ddbe1786688a633", "LuL", "png"),
            BttvEmote("56901914991f200c34ffa656", "SaltyCorn", "png"),
            BttvEmote("56d937f7216793c63ec140cb", "FCreep", "png"),
            BttvEmote("56e9f494fff3cc5c35e5287e", "monkaS", "png"),
            BttvEmote("56f5be00d48006ba34f530a4", "VapeNation", "png"),
            BttvEmote("56fa09f18eff3b595e93ac26", "ariW", "png"),
            BttvEmote("5709ab688eff3b595e93c595", "notsquishY", "png"),
            BttvEmote("5733ff12e72c3c0814233e20", "FeelsAmazingMan", "png"),
            BttvEmote("573d38b50ffbf6cc5cc38dc9", "DuckerZ", "png"),
            BttvEmote("59cf182fcbe2693d59d7bf46", "SqShy", "png"),
            BttvEmote("58d2e73058d8950a875ad027", "Wowee", "png")
    )

    private fun initFfz() = listOf(
            FfzEmote("ZrehplaR", "https:////cdn.frankerfacez.com/4556987ef91323110080b223f96d7400.png"),
            FfzEmote("YooHoo", "https:////cdn.frankerfacez.com/4c3f7ea69ff255ff4ee0adebbe62751c.png"),
            FfzEmote("YellowFever", "https:////cdn.frankerfacez.com/53ebdc39a0603f7a3372ad016bb8597e.png"),
            FfzEmote("ManChicken", "https:////cdn.frankerfacez.com/08c6aa877f0d946d45f30454d9fb1300.png"),
            FfzEmote("BeanieHipster", "https:////cdn.frankerfacez.com/9c727af513b96dfb0dcbc86daa2239d8.png"),
            FfzEmote("CatBag", "https:////cdn.frankerfacez.com/585e6fdea0c5d3e20678284f43af8749.PNG"),
            FfzEmote("ZreknarF", "https:////cdn.frankerfacez.com/1719f1cd21489ef579f6e8bbc861c22f.PNG"),
            FfzEmote("LilZ", "https:////cdn.frankerfacez.com/a6623002a430bcd27edc866441c1582f.PNG"),
            FfzEmote("ZliL", "https:////cdn.frankerfacez.com/0aa63d7f0ac0a6d3622b009b5af28944.PNG"),
            FfzEmote("LaterSooner", "https:////cdn.frankerfacez.com/1fcefc6216cd36fc9ce2a2f7385d854c.PNG"),
            FfzEmote("BORT", "https:////cdn.frankerfacez.com/aa4c8a9d459c866e9f9e03aac614c47a.png")
    )
}
