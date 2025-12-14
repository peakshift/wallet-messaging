package com.example.serviceproviderapp

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import com.example.serviceproviderapp.data.models.LightningInvoice
import com.example.serviceproviderapp.data.models.LightningInvoiceRequest
import com.example.serviceproviderapp.data.models.WalletDetails
import com.example.serviceproviderapp.networking.LNBitsService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class MainViewModel(
    private val lnBitsService: LNBitsService,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    var walletDetails: WalletDetails? by mutableStateOf(null)
        private set
    var lightningInvoice: LightningInvoice? by mutableStateOf(null)
        private set

    var invoiceAmount: String? by mutableStateOf(null)
        private set

    private val disposables = CompositeDisposable()

    var viewState: ViewState by mutableStateOf(ViewState.Idle)
        private set

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
                    .doFinally { viewState = ViewState.Idle }
                    .subscribeBy(
                        onSuccess = { lightningInvoice = it },
                        onError = { it.printStackTrace() }
                    )
            )
        }
    }

    fun checkForPayment() {
        val paymentHash = lightningInvoice?.paymentHash
        paymentHash?.let {
            disposables.add(
                Observable.interval(3, TimeUnit.SECONDS)
                    .flatMapSingle { lnBitsService.checkInvoice(paymentHash).map { it.paid } }
                    .timeout(30, TimeUnit.SECONDS)
                    .takeUntil { paid -> paid }
                    .map { paid -> if (paid) ViewState.InvoicePaid else ViewState.Error }
                    .onErrorReturn { ViewState.Error }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .startWithItem(ViewState.CheckingInvoice)
                    .subscribe {
                        viewState = it
                        getWalletDetails()
                    }
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
                onError = { Log.i("MainViewModel", "Error loading wallet details: $it") }
            ))
    }

    fun storeWalletForBackgroundPayments(walletPackageName: String) {
        sharedPreferences.edit {
            putBoolean(walletPackageName, true)
        }
    }

    sealed interface ViewState {
        object Idle : ViewState
        object CheckingInvoice : ViewState
        object InvoicePaid : ViewState
        object Error : ViewState
    }
}