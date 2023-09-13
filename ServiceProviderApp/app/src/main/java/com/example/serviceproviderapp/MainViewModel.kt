package com.example.serviceproviderapp

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.serviceproviderapp.data.models.LightningInvoice
import com.example.serviceproviderapp.data.models.LightningInvoiceRequest
import com.example.serviceproviderapp.data.models.WalletDetails
import com.example.serviceproviderapp.networking.LNBitsService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch
import kotlinx.coroutines.processNextEventInCurrentThread

class MainViewModel(
    val lnBitsService: LNBitsService
) : ViewModel() {

    var walletDetails: WalletDetails? by mutableStateOf(null)
        private set
    var lightningInvoice: LightningInvoice? by mutableStateOf(null)
        private set

    var invoiceAmount: String? by mutableStateOf(null)
        private set

    private val disposables = CompositeDisposable()

    init {
        getWalletDetails()
    }

    fun refresh() {
        getWalletDetails()
    }

    fun onInvoiceAmountEntered(amountText: String) {
        invoiceAmount = amountText
    }

    fun generateInvoice() {
        val amount = invoiceAmount?.toIntOrNull()
        amount?.let {
            disposables.add(
                lnBitsService.generateInvoice(LightningInvoiceRequest(amount = it))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                        onSuccess = { lightningInvoice = it },
                        onError = { it.printStackTrace() }
                    )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    private fun getWalletDetails() {
        disposables.add(lnBitsService.getWalletDetails()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { walletDetails = it },
                onError = { Log.i("MainViewModel", "Error loading wallet details: ${it.message}") }
            ))
    }
}