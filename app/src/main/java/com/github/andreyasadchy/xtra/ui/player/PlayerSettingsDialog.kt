package com.github.andreyasadchy.xtra.ui.player

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.common.ExpandingBottomSheetDialogFragment
import com.github.andreyasadchy.xtra.ui.common.RadioButtonDialogFragment
import com.github.andreyasadchy.xtra.util.FragmentUtils
import kotlinx.android.synthetic.main.player_settings.*

class PlayerSettingsDialog : ExpandingBottomSheetDialogFragment(), RadioButtonDialogFragment.OnSortOptionChanged {

    interface PlayerSettingsListener {
        fun onChangeQuality(index: Int)
        fun onChangeSpeed(speed: Float)
    }

    companion object {

        private val SPEEDS = listOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f)
        private val SPEED_LABELS = listOf(R.string.speed0_25, R.string.speed0_5, R.string.speed0_75, R.string.speed1, R.string.speed1_25, R.string.speed1_5, R.string.speed1_75, R.string.speed2)
        private const val QUALITIES = "qualities"
        private const val QUALITY_INDEX = "quality"
        private const val SPEED = "speed"

        private const val REQUEST_CODE_QUALITY = 0
        private const val REQUEST_CODE_SPEED = 1

        fun newInstance(qualities: Collection<CharSequence>, qualityIndex: Int, speed: Float): PlayerSettingsDialog {
            return PlayerSettingsDialog().apply {
                arguments = bundleOf(QUALITIES to ArrayList(qualities), QUALITY_INDEX to qualityIndex, SPEED to speed)
            }
        }
    }

    private lateinit var listener: PlayerSettingsListener

    private lateinit var qualities: List<CharSequence>
    private var qualityIndex = 0
    private var speedIndex = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as PlayerSettingsListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.player_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val arguments = requireArguments()
        qualities = arguments.getCharSequenceArrayList(QUALITIES)!!
        setSelectedQuality(arguments.getInt(QUALITY_INDEX))
        setSelectedSpeed(SPEEDS.indexOf(arguments.getFloat(SPEED)))

        selectQuality.setOnClickListener {
            FragmentUtils.showRadioButtonDialogFragment(childFragmentManager, qualities, qualityIndex, REQUEST_CODE_QUALITY)
        }
        selectSpeed.setOnClickListener {
            FragmentUtils.showRadioButtonDialogFragment(requireContext(), childFragmentManager, SPEED_LABELS, speedIndex, REQUEST_CODE_SPEED)
        }
    }

    override fun onChange(requestCode: Int, index: Int, text: CharSequence, tag: Int?) {
        when (requestCode) {
            REQUEST_CODE_QUALITY -> {
                listener.onChangeQuality(index)
                setSelectedQuality(index)
            }
            REQUEST_CODE_SPEED -> {
                listener.onChangeSpeed(SPEEDS[index])
                setSelectedSpeed(index)
            }
        }
    }

    private fun setSelectedQuality(index: Int) {
        quality.text = qualities[index]
        qualityIndex = index
    }

    private fun setSelectedSpeed(index: Int) {
        speedIndex = index
        speed.text = getString(SPEED_LABELS[index])
    }
}
