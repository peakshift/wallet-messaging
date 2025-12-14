package com.example.walletapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.walletapp.ui.theme.LightOrange
import com.example.walletapp.ui.theme.WalletAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PayInvoiceActivity : ComponentActivity() {

    private val viewModel: PayInvoiceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PayInvoiceScreen(
                invoiceAmount = viewModel.invoiceAmount,
                paymentRequesterName = viewModel.appName,
                paymentRequesterIcon = viewModel.appIcon,
                onConfirmPaymentClick = { viewModel.payInvoice() },
                backgroundPaymentsEnabled = viewModel.backgroundPaymentsEnabled,
                onEnableBackgroundPaymentsToggled = { viewModel.onEnableBackgroundPaymentsToggled(it) }
            )
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is PayInvoiceViewModel.Event.CloseAndGoBack -> {
                            val intent = Intent()
                            if (event.backgroundPaymentsEnabled) {
                                intent.putExtra("package_name", packageName)
                            }
                            setResult(RESULT_OK, intent)
                            finish()
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val INVOICE_EXTRA = "invoice_extra"
        const val LAUNCHING_APP_PACKAGE_EXTRA = "launching_package_extra"
        const val LAUNCHING_APP_NAME_EXTRA = "app_name_extra"
        const val LAUNCHING_APP_ICON_EXTRA = "app_icon_extra"

        fun newIntent(
            context: Context,
            invoice: String,
            launchingPackage: String? = null,
            appName: String? = null,
            appIcon: Drawable? = null
        ): Intent {
            val intent = Intent(context, PayInvoiceActivity::class.java)
            intent.putExtra(INVOICE_EXTRA, invoice)
            intent.putExtra(LAUNCHING_APP_PACKAGE_EXTRA, launchingPackage)
            intent.putExtra(LAUNCHING_APP_NAME_EXTRA, appName)
            intent.putExtra(LAUNCHING_APP_ICON_EXTRA, appIcon?.toBitmap())
            return intent
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PayInvoiceScreen(
    invoiceAmount: Int?,
    paymentRequesterName: String? = null,
    paymentRequesterIcon: Bitmap? = null,
    onConfirmPaymentClick: () -> Unit,
    backgroundPaymentsEnabled: Boolean,
    onEnableBackgroundPaymentsToggled: (Boolean) -> Unit
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
                        if (invoiceAmount != null) {
                            append("$invoiceAmount sats ")
                        } else {
                            append("No invoice amount")
                        }
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
            Spacer(modifier = Modifier.height(48.dp))
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = backgroundPaymentsEnabled,
                    onCheckedChange = onEnableBackgroundPaymentsToggled
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Enable background payments for this app",
                    style = MaterialTheme.typography.bodyLarge
                )
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
            onConfirmPaymentClick = {},
            backgroundPaymentsEnabled = false,
            onEnableBackgroundPaymentsToggled = {}
        )
    }
}