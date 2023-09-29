package com.example.serviceproviderapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.BuildCompat
import com.example.serviceproviderapp.data.models.WalletDetails
import com.example.serviceproviderapp.networking.LNBitsService
import com.example.serviceproviderapp.networking.RetrofitFactory
import com.example.serviceproviderapp.ui.theme.ServiceProviderAppTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    private val walletAppResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                viewModel.refresh()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lnBitsService = RetrofitFactory.retrofit.create(LNBitsService::class.java)
        viewModel = MainViewModel(lnBitsService)

        setContent {
            ServiceProviderAppTheme {
                ServiceProviderAppMainScreen(
                    viewState = viewModel.viewState,
                    walletDetails = viewModel.walletDetails,
                    lightningInvoice = viewModel.lightningInvoice?.paymentRequest,
                    invoiceAmount = viewModel.invoiceAmount,
                    onInvoiceAmountEntered = { viewModel.onInvoiceAmountEntered(it) },
                    onGenerateInvoiceClick = { viewModel.generateInvoice() },
                    onCopyInvoiceClick = { copyToClipboard(it, "invoice") },
                    onPayWithWalletClick = { payInBackground(it) }
                )
            }
        }
    }

    private fun copyToClipboard(text: String, label: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    private fun openWalletApp(lightningInvoice: String) {
        val uri = Uri.Builder()
            .scheme("lightning")
            .appendQueryParameter("invoice", lightningInvoice)
            .build();

        walletAppResultLauncher.launch(intent)
    }

    private fun payInBackground(lightningInvoice: String) {
        viewModel.checkForPayment()
        val uri = Uri.Builder()
            .scheme("lightning")
            .appendQueryParameter("invoice", lightningInvoice)
            .build();

        Intent.ACTION_VIEW
        val intent = Intent("com.example.walletapp.PAYMENT_REQUEST", uri)
        intent.setPackage("com.example.walletapp")
        sendBroadcast(intent)
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceProviderAppMainScreen(
    viewState: MainViewModel.ViewState,
    walletDetails: WalletDetails?,
    lightningInvoice: String? = null,
    invoiceAmount: String?,
    onInvoiceAmountEntered: (String) -> Unit,
    onGenerateInvoiceClick: () -> Unit,
    onCopyInvoiceClick: (String) -> Unit,
    onPayWithWalletClick: (String) -> Unit
) {
    Scaffold {
        val focusManager = LocalFocusManager.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
        ) {
            if (walletDetails != null) {
                Text(
                    text = walletDetails.name,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Balance: ${walletDetails.balance / 1000} sats",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = invoiceAmount ?: "",
                        onValueChange = onInvoiceAmountEntered,
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text(text = "Sats amount") }
                    )
                    Text(text = "sats")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        onGenerateInvoiceClick()
                        focusManager.clearFocus()
                    },
                    shape = RoundedCornerShape(50),
                    enabled = !invoiceAmount.isNullOrBlank(),
                ) {
                    Text(text = "Generate invoice")
                }

                when (viewState) {
                    MainViewModel.ViewState.Idle ->
                        lightningInvoice?.let { invoice ->
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(text = invoice)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(onClick = { onCopyInvoiceClick(lightningInvoice) }) {
                                    Text(text = "Copy")
                                }

                                Button(
                                    onClick = { onPayWithWalletClick(lightningInvoice) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF7931A))
                                ) {
                                    Text(text = "Pay with Wallet ₿")
                                }
                            }
                        }
                    MainViewModel.ViewState.CheckingInvoice ->
                        CircularProgressIndicator()
                    MainViewModel.ViewState.Error ->
                        Text("Something went wrong!")
                    MainViewModel.ViewState.InvoicePaid ->
                        Text("Invoice paid!")
                }

            } else {
                Text(
                    text = "No Wallet Details",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ServiceProviderAppMainScreenPreview() {
    ServiceProviderAppTheme {
        ServiceProviderAppMainScreen(
            viewState = MainViewModel.ViewState.Idle,
            walletDetails = WalletDetails("Wallet Name", 100000),
            onGenerateInvoiceClick = {},
            lightningInvoice = null,
            invoiceAmount = "",
            onInvoiceAmountEntered = {},
            onCopyInvoiceClick = {},
            onPayWithWalletClick = {}
        )
    }
}