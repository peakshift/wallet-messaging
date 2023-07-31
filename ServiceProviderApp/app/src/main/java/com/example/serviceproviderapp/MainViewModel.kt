package com.example.serviceproviderapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.serviceproviderapp.data.models.LightningInvoice
import com.example.serviceproviderapp.data.models.LightningInvoiceRequest
import com.example.serviceproviderapp.data.models.WalletDetails
import com.example.serviceproviderapp.networking.LNBitsService
import kotlinx.coroutines.launch

class MainViewModel(
    val lnBitsService: LNBitsService
) : ViewModel() {

    var walletDetails: WalletDetails? by mutableStateOf(null)
        private set
    var lightningInvoice: LightningInvoice? by mutableStateOf(null)
        private set

    var invoiceAmount: Int by mutableStateOf(0)
        private set

    init {
        getWalletDetails()
    }

    fun refresh() {
        getWalletDetails()
    }

    fun onInvoiceAmountEntered(amountText: String) {
        amountText.toIntOrNull()?.let { amount ->
            invoiceAmount = amount
        }
    }

    fun generateInvoice() {
        viewModelScope.launch {
            lightningInvoice = lnBitsService.generateInvoice(LightningInvoiceRequest(amount = invoiceAmount))
        }
    }

    private fun getWalletDetails() {
        viewModelScope.launch {
            val details = lnBitsService.getWalletDetails()
            walletDetails = details
        }
    }
}