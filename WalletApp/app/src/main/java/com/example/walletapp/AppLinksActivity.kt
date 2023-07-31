package com.example.walletapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult

class AppLinksActivity : ComponentActivity() {

    private val activityResultLauncher = registerForActivityResult(StartActivityForResult()) {
        setResult(it.resultCode)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get data from uri and launch activity
        val uri = intent.data
        val lightningInvoice = uri?.lastPathSegment

        if (lightningInvoice != null) {
            val intent = PayInvoiceActivity.newIntent(this, lightningInvoice)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            activityResultLauncher.launch(intent)
        }
    }

}