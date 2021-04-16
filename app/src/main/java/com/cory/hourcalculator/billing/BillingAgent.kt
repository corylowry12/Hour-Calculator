package com.cory.hourcalculator.billing

import android.app.Activity
import android.widget.Toast
import com.android.billingclient.api.*
import com.cory.hourcalculator.R
import java.lang.Exception

class BillingAgent(private val activity: Activity, private val callback: BillingCallback) : PurchasesUpdatedListener {

    private var billingClient = BillingClient.newBuilder(activity).setListener(this).enablePendingPurchases().build()
    private val productsSKUList = listOf("five_dollar_donation")
    private val productsList = arrayListOf<SkuDetails>()

    private var selectionBilling: Int = 0

    init {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(p0: BillingResult) {
                if (p0.responseCode == BillingClient.BillingResponseCode.OK) {
                    getAvailableProducts()
                }
            }

            override fun onBillingServiceDisconnected() {
            }

        })
    }

    fun onDestroy() {
        billingClient.endConnection()
    }

    override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?) {
        checkProduct(p0, p1)
    }

    private fun checkProduct(p0: BillingResult, p1: MutableList<Purchase>?) {
        p1?.let {
            var token: String? = null
            if (p0.responseCode == BillingClient.BillingResponseCode.OK &&
                p1.size > 0
            ) {
                    /*if(selection_billing == 0) {
                        token = p1[0].purchaseToken
                    }
                    else if(selection_billing == 1) {
                        token = p1[1].purchaseToken
                    }*/
                token = p1[0].purchaseToken
                //Toast.makeText(activity, p1[1].toString(), Toast.LENGTH_LONG).show()

            } /*else if (p0.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                val purchasesList = billingClient.queryPurchases(BillingClient.SkuType.INAPP).purchasesList
                if (purchasesList!!.size > 0) {
                    token = purchasesList[0]!!.purchaseToken
                }
            }*/

            token?.let {
                val params = ConsumeParams
                    .newBuilder()
                    .setPurchaseToken(token)
                    .build()
                billingClient.consumeAsync(params) { billingResult, _ ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        callback.onTokenConsumed()
                    }
                }
            }
        }
    }

    fun getAvailableProducts() {
        if (billingClient.isReady) {
            val params = SkuDetailsParams
                .newBuilder()
                .setSkusList(productsSKUList)
                .setType(BillingClient.SkuType.INAPP)
                .build()
            billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
                //productsList.clear()
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    productsList.clear()
                    productsList.addAll(skuDetailsList!!)
                }
            }
        }
    }

    fun purchaseView(selection: Int) {
        //Toast.makeText(activity, productsList[0].toString() + " " + productsList[1].toString(), Toast.LENGTH_LONG).show()
        try {
            if (selection == 0) {
                selectionBilling = 0
                val billingFlowParams = BillingFlowParams
                    .newBuilder()
                    .setSkuDetails(productsList[selection])
                    .build()
                billingClient.launchBillingFlow(activity, billingFlowParams)
                //Toast.makeText(activity, productsList[0].toString(), Toast.LENGTH_LONG).show()
            }
        } catch (e : Exception) {
            e.printStackTrace()
            Toast.makeText(activity, activity.getString(R.string.whoops_error), Toast.LENGTH_LONG).show()
        }

        }
    }
