package com.example.walletapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.serviceproviderapp.data.models.WalletDetails
import com.example.serviceproviderapp.networking.LNBitsService
import kotlinx.coroutines.launch

class MainViewModel(
    val lnBitsService: LNBitsService
) : ViewModel() {

    var walletDetails: WalletDetails? by mutableStateOf(null)
        private set

    init {
        loadWalletDetails()
    }

    fun loadWalletDetails() {
        viewModelScope.launch {
            walletDetails = lnBitsService.getWalletDetails()
        }
    }

}