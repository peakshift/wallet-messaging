package com.example.walletapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.serviceproviderapp.networking.DecodeInvoiceRequest
import com.example.serviceproviderapp.networking.LNBitsService
import com.example.serviceproviderapp.networking.RetrofitFactory
import com.example.walletapp.data.models.DecodedLightningInvoice
import com.example.walletapp.data.models.PayInvoiceRequest
import com.example.walletapp.ui.theme.LightOrange
import com.example.walletapp.ui.theme.WalletAppTheme
import kotlinx.coroutines.launch

class PayInvoiceActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val invoice = checkNotNull(intent.getStringExtra(INVOICE_EXTRA))
        val appName = intent.getStringExtra(APP_NAME_EXTRA)
        val appIcon = intent.getParcelableExtra<Bitmap>(APP_ICON_EXTRA)
        val lnBitsService = RetrofitFactory.retrofit.create(LNBitsService::class.java)

        var decodedInvoice: DecodedLightningInvoice? by mutableStateOf(null)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                decodedInvoice = lnBitsService.decodeInvoice(DecodeInvoiceRequest(invoice))
            }
        }

        setContent {
            val coroutineScope = rememberCoroutineScope()

            PayInvoiceScreen(
                invoiceAmount = (decodedInvoice?.amountMSat ?: 0) / 1000,
                paymentRequesterName = appName,
                paymentRequesterIcon = appIcon,
                onConfirmPaymentClick = {
                    coroutineScope.launch {
                        lnBitsService.payInvoice(PayInvoiceRequest(invoice))
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            )
        }
    }

    companion object {
        private const val INVOICE_EXTRA = "invoice_extra"
        private const val APP_NAME_EXTRA = "app_name_extra"
        private const val APP_ICON_EXTRA = "app_icon_extra"

        fun newIntent(context: Context, invoice: String, appName: String? = null, appIcon: Drawable? = null): Intent {
            val intent = Intent(context, PayInvoiceActivity::class.java)
            intent.putExtra(INVOICE_EXTRA, invoice)
            intent.putExtra(APP_NAME_EXTRA, appName)
            intent.putExtra(APP_ICON_EXTRA, appIcon?.toBitmap())
            return intent
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PayInvoiceScreen(
    invoiceAmount: Int,
    paymentRequesterName: String? = null,
    paymentRequesterIcon: Bitmap? = null,
    onConfirmPaymentClick: () -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding(),
                    start = 24.dp,
                    end = 24.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(200.dp))
            paymentRequesterIcon?.let {
                Image(it.asImageBitmap(), contentDescription = null)
                Spacer(modifier = Modifier.height(16.dp))
            }
            Text(
                text = buildAnnotatedString {
                    append("You're about to pay:\n\n")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("$invoiceAmount sats ")
                    }
                    if (paymentRequesterName != null) {
                        append("to $paymentRequesterName")
                    }
                },
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = onConfirmPaymentClick,
                colors = ButtonDefaults.buttonColors(containerColor = LightOrange)
            ) {
                Text(text = "Confirm Payment")
            }
        }
    }
}

@Preview
@Composable
private fun PayInvoiceScreenPreview() {
    WalletAppTheme {
        PayInvoiceScreen(
            invoiceAmount = 1000,
            paymentRequesterName = "Service Provider",
            paymentRequesterIcon = null,
            onConfirmPaymentClick = {}
        )
    }
}