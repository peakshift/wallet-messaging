package com.example.walletapp.data.models

data class PayInvoiceRequest(
    val bolt11: String,
    val out: Boolean = true
)
