package com.example.walletapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import com.example.serviceproviderapp.networking.LNBitsService
import com.example.serviceproviderapp.networking.RetrofitFactory
import com.example.walletapp.data.models.PayInvoiceRequest
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import retrofit2.create
import javax.inject.Inject

class PaymentBroadcastReceiver : BroadcastReceiver() {

    private val tag = "BroadcastReceiver"

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(tag, "#### Broadcast received")

        val uri = intent?.data
        val lightningInvoice = uri?.getQueryParameter("invoice")



        if (lightningInvoice != null) {
            val pendingIntent = goAsync()
            val lnBitsService = RetrofitFactory.retrofit.create(LNBitsService::class.java)

            lnBitsService.payInvoice(PayInvoiceRequest(lightningInvoice))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate { pendingIntent.finish() }
                .subscribeBy(
                    onComplete = { Log.i(tag, "#### Payment successfull") },
                    onError = { Log.i(tag, "#### Payment failed: ${it.message}") }
                )
        }

    }

}