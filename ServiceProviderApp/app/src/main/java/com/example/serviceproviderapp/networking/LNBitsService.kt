package com.example.serviceproviderapp.networking

import com.example.serviceproviderapp.data.models.LightningInvoice
import com.example.serviceproviderapp.data.models.LightningInvoiceRequest
import com.example.serviceproviderapp.data.models.WalletDetails
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface LNBitsService {

    @Headers("X-Api-Key: 9f8f22d360964c09b9c28d1486334f3f")
    @GET("/api/v1/wallet")
    suspend fun getWalletDetails(): WalletDetails

    @Headers("X-Api-Key: 9f8f22d360964c09b9c28d1486334f3f")
    @POST("/api/v1/payments")
    suspend fun generateInvoice(@Body lightningInvoiceRequest: LightningInvoiceRequest): LightningInvoice

}