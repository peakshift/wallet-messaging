package com.example.walletapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.graphics.drawable.toBitmap

class AppLinksActivity : ComponentActivity() {

    private val activityResultLauncher = registerForActivityResult(StartActivityForResult()) {
        setResult(it.resultCode, it.data)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get data from uri and launch activity
        val uri = intent.data
        val lightningInvoice = uri?.getQueryParameter("invoice")

        val launchingPackage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            referrer?.host
        } else {
            callingPackage
        }

        val applicationInfo = packageManager.getApplicationInfo(launchingPackage ?: "", 0)
        val appName = packageManager.getApplicationLabel(applicationInfo).toString()
        val appIcon = packageManager.getApplicationIcon(applicationInfo)

        if (lightningInvoice != null) {
            val intent = PayInvoiceActivity.newIntent(
                context = this,
                invoice = lightningInvoice,
                launchingPackage = launchingPackage,
                appName = appName,
                appIcon = appIcon
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            activityResultLauncher.launch(intent)
        }
    }
}