package com.example.walletapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.serviceproviderapp.data.models.WalletDetails
import com.example.serviceproviderapp.networking.LNBitsService
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val lnBitsService: LNBitsService
) : ViewModel() {

    var walletDetails: WalletDetails? by mutableStateOf(null)
        private set

    private val disposables = CompositeDisposable()

    init {
        loadWalletDetails()
    }

    fun loadWalletDetails() {
        disposables.add(
            lnBitsService.getWalletDetails()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = { details -> walletDetails = details },
                    onError = { it.printStackTrace() }
                )
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

}