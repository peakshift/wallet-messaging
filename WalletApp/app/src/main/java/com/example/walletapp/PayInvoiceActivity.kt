package com.example.walletapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
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
        val lnBitsService = RetrofitFactory.retrofit.create(LNBitsService::class.java)

        var decodedInvoice: DecodedLightningInvoice? by mutableStateOf(null)

        lifecycleScope.launch {
            decodedInvoice = lnBitsService.decodeInvoice(DecodeInvoiceRequest(invoice))
        }

        setContent {
            val coroutineScope = rememberCoroutineScope()

            PayInvoiceScreen(
                invoiceAmount = (decodedInvoice?.amountMSat ?: 0) / 1000,
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

        fun newIntent(context: Context, invoice: String): Intent {
            val intent = Intent(context, PayInvoiceActivity::class.java)
            intent.putExtra(INVOICE_EXTRA, invoice)
            return intent
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PayInvoiceScreen(
    invoiceAmount: Int,
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
            Text(
                text = buildAnnotatedString {
                    append("You're about to pay an invoice in the amount of:\n\n")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("$invoiceAmount sats")
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
            onConfirmPaymentClick = {}
        )
    }
}