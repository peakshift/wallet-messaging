package com.example.walletapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.serviceproviderapp.networking.DecodeInvoiceRequest
import com.example.serviceproviderapp.networking.LNBitsService
import com.example.serviceproviderapp.networking.RetrofitFactory
import com.example.walletapp.data.models.DecodedLightningInvoice
import com.example.walletapp.data.models.PayInvoiceRequest
import com.example.walletapp.ui.theme.LightOrange
import com.example.walletapp.ui.theme.WalletAppTheme
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class PayInvoiceActivity : ComponentActivity() {

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val invoice = checkNotNull(intent.getStringExtra(INVOICE_EXTRA))
        val appName = intent.getStringExtra(APP_NAME_EXTRA)
        val appIcon = intent.getParcelableExtra<Bitmap>(APP_ICON_EXTRA)
        val lnBitsService = RetrofitFactory.retrofit.create(LNBitsService::class.java)

        var decodedInvoice: DecodedLightningInvoice? by mutableStateOf(null)

        disposables.add(
            lnBitsService.decodeInvoice(DecodeInvoiceRequest(invoice))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { decodedLightningInvoice ->
                    decodedInvoice = decodedLightningInvoice
                })

        setContent {


            PayInvoiceScreen(
                invoiceAmount = (decodedInvoice?.amountMSat ?: 0) / 1000,
                paymentRequesterName = appName,
                paymentRequesterIcon = appIcon,
                onConfirmPaymentClick = {
                    disposables.add(lnBitsService.payInvoice(PayInvoiceRequest(invoice))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doAfterTerminate {
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                        .subscribe {
                            // do nothing
                        })
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
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