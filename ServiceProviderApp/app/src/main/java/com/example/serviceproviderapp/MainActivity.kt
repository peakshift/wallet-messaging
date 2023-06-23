package com.example.serviceproviderapp

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.serviceproviderapp.data.models.LightningInvoice
import com.example.serviceproviderapp.data.models.LightningInvoiceRequest
import com.example.serviceproviderapp.networking.LNBitsService
import com.example.serviceproviderapp.networking.RetrofitFactory
import com.example.serviceproviderapp.data.models.WalletDetails
import com.example.serviceproviderapp.ui.theme.ServiceProviderAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lnBitsService = RetrofitFactory.retrofit.create(LNBitsService::class.java)
        viewModel = MainViewModel(lnBitsService)

        setContent {
            ServiceProviderAppTheme {
                ServiceProviderAppMainScreen(
                    walletDetails = viewModel.walletDetails,
                    lightningInvoice = viewModel.lightningInvoice?.paymentRequest,
                    invoiceAmount = viewModel.invoiceAmount,
                    onInvoiceAmountEntered = { viewModel.onInvoiceAmountEntered(it) },
                    onGenerateInvoiceClick = { viewModel.generateInvoice() },
                    onCopyInvoiceClick = { copyToClipboard(it, "invoice") }
                )
            }
        }
    }

    private fun copyToClipboard(text: String, label: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceProviderAppMainScreen(
    walletDetails: WalletDetails?,
    lightningInvoice: String? = null,
    invoiceAmount: Int,
    onInvoiceAmountEntered: (String) -> Unit,
    onGenerateInvoiceClick: () -> Unit,
    onCopyInvoiceClick: (String) -> Unit
) {
    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                        value = invoiceAmount.toString(),
                        onValueChange = onInvoiceAmountEntered,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text(text = "Sats amount") }
                    )
                    Text(text = "sats")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onGenerateInvoiceClick,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.imePadding()
                ) {
                    Text(text = "Generate invoice")
                }
                
                lightningInvoice?.let { invoice ->
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(text = invoice)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { onCopyInvoiceClick(lightningInvoice) }) {
                        Text(text = "Copy")
                    }
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
            walletDetails = WalletDetails( "Wallet Name", 100000),
            onGenerateInvoiceClick = {},
            invoiceAmount = 0,
            onInvoiceAmountEntered = {},
            onCopyInvoiceClick = {}
        )
    }
}