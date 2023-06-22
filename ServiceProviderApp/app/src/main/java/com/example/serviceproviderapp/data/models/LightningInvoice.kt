package com.example.serviceproviderapp.data.models

import com.google.gson.annotations.SerializedName

data class LightningInvoice(
    @SerializedName("payment_hash")
    val paymentHash: String,
    @SerializedName("payment_request")
    val paymentRequest: String,
    @SerializedName("checking_id")
    val checkingId: String,
)
