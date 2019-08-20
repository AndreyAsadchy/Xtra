package com.github.exact7.xtra.ui.menu

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.SkuDetailsParams
import com.github.exact7.xtra.repository.LoadingState
import com.github.exact7.xtra.ui.common.BaseViewModel

class DonationDialogViewModel : BaseViewModel() {

    private var billingClient: BillingClient? = null

    private val _state = MutableLiveData<LoadingState>()
    val state: LiveData<LoadingState>
        get() = _state

    fun launchBillingFlow(activity: Activity, index: Int) {
        if (_state.value != LoadingState.LOADING) {
            _state.value = LoadingState.LOADING
            billingClient = BillingClient.newBuilder(activity)
                    .setListener { billingResult, purchases ->
                        _state.value = if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) LoadingState.LOADED else LoadingState.FAILED
                    }
                    .enablePendingPurchases()
                    .build().also {
                        it.startConnection(object : BillingClientStateListener {
                            override fun onBillingSetupFinished(billingResult: BillingResult) {
                                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                    val skuDetails = SkuDetailsParams.newBuilder()
                                            .setSkusList(listOf("donation_0.99", "donation_2.49", "donation_3.49", "donation_4.99", "donation_7.49", "donation_9.99"))
                                            .setType(BillingClient.SkuType.INAPP)
                                            .build()
                                    it.querySkuDetailsAsync(skuDetails) { skuDetailsBillingResult, skuDetailsList ->
                                        if (skuDetailsBillingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                            val flowParams = BillingFlowParams.newBuilder()
                                                    .setSkuDetails(skuDetailsList[index])
                                                    .build()
                                            it.launchBillingFlow(activity, flowParams)
                                        }
                                    }
                                }
                            }

                            override fun onBillingServiceDisconnected() {
                                println("DISC")
                                // Try to restart the connection on the next request to
                                // Google Play by calling the startConnection() method.
                            }
                        })
                    }
        }
    }

    override fun onCleared() {
        billingClient?.endConnection()
        super.onCleared()
    }
}