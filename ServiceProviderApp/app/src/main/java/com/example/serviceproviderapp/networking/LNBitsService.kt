package com.example.serviceproviderapp.networking

import com.example.serviceproviderapp.data.models.LightningInvoice
import com.example.serviceproviderapp.data.models.LightningInvoiceRequest
import com.example.serviceproviderapp.data.models.WalletDetails
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface LNBitsService {

    @Headers("X-Api-Key: $API_KEY")
    @GET("/api/v1/wallet")
    fun getWalletDetails(): Single<WalletDetails>

    @Headers("X-Api-Key: $API_KEY")
    @POST("/api/v1/payments")
    fun generateInvoice(@Body lightningInvoiceRequest: LightningInvoiceRequest): Single<LightningInvoice>

}

private const val API_KEY = "9f8f22d360964c09b9c28d1486334f3f"