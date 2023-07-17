package com.example.serviceproviderapp.networking

import com.example.serviceproviderapp.data.models.LightningInvoice
import com.example.serviceproviderapp.data.models.LightningInvoiceRequest
import com.example.serviceproviderapp.data.models.WalletDetails
import com.example.walletapp.data.models.DecodedLightningInvoice
import com.example.walletapp.data.models.PayInvoiceRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface LNBitsService {

    @Headers("X-Api-Key: $API_READ_KEY")
    @GET("/api/v1/wallet")
    suspend fun getWalletDetails(): WalletDetails

    @Headers("X-Api-Key: $API_READ_KEY")
    @POST("/api/v1/payments")
    suspend fun generateInvoice(@Body lightningInvoiceRequest: LightningInvoiceRequest): LightningInvoice

    @Headers("X-Api-Key: $API_READ_KEY")
    @POST("/api/v1/payments/decode")
    suspend fun decodeInvoice(@Body decodeInvoiceRequest: DecodeInvoiceRequest): DecodedLightningInvoice

    @Headers("X-Api-Key: $API_ADMIN_KEY")
    @POST("/api/v1/payments")
    suspend fun payInvoice(@Body payInvoiceRequest: PayInvoiceRequest)

}

data class DecodeInvoiceRequest(
    val data: String
)

private const val API_READ_KEY = "cfd53d3dd3e54df782c9350b8be22d03"
private const val API_ADMIN_KEY = "2aa9dbe17a8c4a158bef6b2c8f960b1e"