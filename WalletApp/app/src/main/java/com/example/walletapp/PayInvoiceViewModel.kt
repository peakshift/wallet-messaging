package com.example.walletapp

import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.serviceproviderapp.networking.DecodeInvoiceRequest
import com.example.serviceproviderapp.networking.LNBitsService
import com.example.walletapp.data.models.DecodedLightningInvoice
import com.example.walletapp.data.models.PayInvoiceRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PayInvoiceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val lnBitsService: LNBitsService,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _events = Channel<Event>()
    val events = _events.receiveAsFlow()

    val invoice: String = checkNotNull(savedStateHandle[PayInvoiceActivity.INVOICE_EXTRA])
    val appName: String? = savedStateHandle[PayInvoiceActivity.LAUNCHING_APP_NAME_EXTRA]
    val appIcon: Bitmap? = savedStateHandle[PayInvoiceActivity.LAUNCHING_APP_ICON_EXTRA]

    private val launchingPackageName: String? = savedStateHandle[PayInvoiceActivity.LAUNCHING_APP_PACKAGE_EXTRA]

    private var decodedInvoice: DecodedLightningInvoice? by mutableStateOf(null)

    val invoiceAmount: Int?
        get() = decodedInvoice?.let { it.amountMSat / 1000 }

    var backgroundPaymentsEnabled by mutableStateOf(false)
        private set

    private val disposables = CompositeDisposable()

    init {
        disposables.add(
            lnBitsService.decodeInvoice(DecodeInvoiceRequest(invoice))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { decodedLightningInvoice ->
                    decodedInvoice = decodedLightningInvoice
                })
    }

    fun payInvoice() {
        disposables.add(lnBitsService.payInvoice(PayInvoiceRequest(invoice))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                sharedPreferences.edit {
                    putBoolean(launchingPackageName, backgroundPaymentsEnabled)
                }
                viewModelScope.launch {
                    _events.send(Event.CloseAndGoBack(backgroundPaymentsEnabled))
                }
            })
    }

    fun onEnableBackgroundPaymentsToggled(checked: Boolean) {
        backgroundPaymentsEnabled = checked
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    sealed class Event {
        data class CloseAndGoBack(val backgroundPaymentsEnabled: Boolean) : Event()
    }

}