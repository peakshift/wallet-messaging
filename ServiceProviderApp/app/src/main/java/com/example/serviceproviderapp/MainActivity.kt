package com.example.serviceproviderapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.example.serviceproviderapp.data.models.WalletDetails
import com.example.serviceproviderapp.networking.LNBitsService
import com.example.serviceproviderapp.networking.RetrofitFactory
import com.example.serviceproviderapp.ui.theme.ServiceProviderAppTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var sharedPreferences: SharedPreferences

    private val walletAppResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val packageName = it.data?.getStringExtra("package_name")
                Log.i("MAIN_ACTIVITY", "Package name: $packageName")
                if (packageName != null) {
//                    viewModel.storeWalletForBackgroundPayments(packageName)
                    sharedPreferences.edit {
                        putString("background_payments_wallet", packageName)
                    }
                }
                viewModel.refresh()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lnBitsService = RetrofitFactory.retrofit.create(LNBitsService::class.java)
        sharedPreferences = applicationContext.getSharedPreferences("background_payments", Context.MODE_PRIVATE)
        viewModel = MainViewModel(lnBitsService, sharedPreferences)

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
                    onPayWithWalletClick = { invoice ->
                        val backgroundPaymentWallet = sharedPreferences.getString("background_payments_wallet", null)
                        if (backgroundPaymentWallet != null) {
                            payInBackground(invoice)
                        } else {
                            openWalletApp(invoice)
                        }
                    }
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

        val intent = Intent(Intent.ACTION_VIEW, uri)
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
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(8.dp))

                val balance by animateIntAsState(
                    targetValue = walletDetails.balance / 1000,
                    animationSpec = tween()
                )
                Text(
                    text = buildAnnotatedString {
                        append("Balance: ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("$balance sats")
                        }
                    },
                    style = MaterialTheme.typography.headlineMedium
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

                Spacer(modifier = Modifier.height(32.dp))
                when (viewState) {
                    MainViewModel.ViewState.Idle ->
                        lightningInvoice?.let { invoice ->
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
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }

                    MainViewModel.ViewState.InvoicePaid ->
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(color = Color(0xFF7DC182), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check_large),
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Text("Invoice paid!", style = TextStyle(fontSize = 24.sp))
                        }

                    MainViewModel.ViewState.Error ->
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(color = Color(0xFFFD4A43), shape = CircleShape)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_x_large),
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Text("Something went wrong!", style = TextStyle(fontSize = 24.sp))
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
            viewState = MainViewModel.ViewState.InvoicePaid,
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