package com.github.andreyasadchy.xtra.ui.player

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.common.ExpandingBottomSheetDialogFragment
import com.github.andreyasadchy.xtra.ui.common.RadioButtonDialogFragment
import com.github.andreyasadchy.xtra.util.FragmentUtils

class PlayerSettingsDialog : ExpandingBottomSheetDialogFragment(), RadioButtonDialogFragment.OnSortOptionChanged {

    interface PlayerSettingsListener {
        fun onChangeQuality(index: Int)
        fun onChangeSpeed(speed: Float)
    }

    companion object {

        private val SPEEDS = arrayOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f)
        private val SPEED_LABELS = listOf(R.string.speed0_25, R.string.speed0_5, R.string.speed0_75, R.string.speed1, R.string.speed1_25, R.string.speed1_5, R.string.speed1_75, R.string.speed2)
        private const val QUALITIES = "qualities"
        private const val QUALITY = "quality"
        private const val SPEED = "speed"

        fun newInstance(qualities: Collection<CharSequence>, quality: Int, speed: Float) : PlayerSettingsDialog {
            return PlayerSettingsDialog().apply {
                arguments = bundleOf(QUALITIES to ArrayList(qualities), QUALITY to quality, SPEED to speed)
            }
        }
    }

    private lateinit var listener: PlayerSettingsListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as PlayerSettingsListener
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val arguments = requireArguments()
        val qualities = arguments.getCharSequenceArrayList(QUALITIES)!!
        val quality = arguments.getInt(QUALITY)
        val speed = arguments.getFloat(SPEED)

        return inflater.inflate(R.layout.player_settings, container, false).apply {
            findViewById<TextView>(R.id.quality).text = qualities[quality]
            findViewById<TextView>(R.id.speed).text = speed.toString() + "x"

            findViewById<View>(R.id.selectQuality).setOnClickListener {
                FragmentUtils.showRadioButtonDialogFragment(childFragmentManager, qualities, quality)
            }

            findViewById<View>(R.id.selectSpeed).setOnClickListener {
                FragmentUtils.showRadioButtonDialogFragment(context, childFragmentManager, SPEED_LABELS, SPEEDS.indexOfFirst { it == speed })
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onChange(index: Int, text: CharSequence, tag: Int?) {
        val view = requireView()
        if (tag == null) {
            listener.onChangeQuality(index)
            view.findViewById<TextView>(R.id.quality).text = requireArguments().getCharSequenceArrayList(QUALITIES)!![index]
        } else {
            val speed = SPEEDS[index]
            listener.onChangeSpeed(speed)
            view.findViewById<TextView>(R.id.speed).text = speed.toString() + "x"
        }
    }
}
