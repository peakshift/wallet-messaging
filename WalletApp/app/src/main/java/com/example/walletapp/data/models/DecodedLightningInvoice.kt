package com.example.walletapp.data.models

import com.google.gson.annotations.SerializedName

data class DecodedLightningInvoice(
    @SerializedName("payment_hash")
    val paymentHash: String,
    @SerializedName("amount_msat")
    val amountMSat: Int,
    val description: String,
    @SerializedName("description_hash")
    val descriptionHash: String?,
    val payee: String,
    val date: Long,
    val expiry: Int,
    val secret: String
)
