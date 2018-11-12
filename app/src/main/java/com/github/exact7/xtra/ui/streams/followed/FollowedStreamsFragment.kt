package com.github.exact7.xtra.ui.streams.followed

import android.os.Bundle
import android.view.View
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.ui.streams.BaseStreamsFragment
import com.github.exact7.xtra.util.C

class FollowedStreamsFragment : BaseStreamsFragment() {

    private lateinit var user: User

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        return if (isFragmentVisible) {
//
//        } else {
//            null
//        }
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = arguments!!.getParcelable(C.USER)!!
    }

    override fun initialize() {
        super.initialize()
    }

}
