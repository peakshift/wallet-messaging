package com.example.serviceproviderapp.data.models

data class LightningInvoiceRequest(
    val out: Boolean = false,
    val amount: Int,
    val memo: String = ""
)
