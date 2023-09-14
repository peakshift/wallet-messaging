package com.example.serviceproviderapp.networking

import com.example.serviceproviderapp.data.models.LightningInvoice
import com.example.serviceproviderapp.data.models.LightningInvoiceRequest
import com.example.serviceproviderapp.data.models.WalletDetails
import com.example.walletapp.data.models.DecodedLightningInvoice
import com.example.walletapp.data.models.PayInvoiceRequest
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface LNBitsService {

    @Headers("X-Api-Key: $API_READ_KEY")
    @GET("/api/v1/wallet")
    fun getWalletDetails(): Single<WalletDetails>

    @Headers("X-Api-Key: $API_READ_KEY")
    @POST("/api/v1/payments")
    fun generateInvoice(@Body lightningInvoiceRequest: LightningInvoiceRequest): Single<LightningInvoice>

    @Headers("X-Api-Key: $API_READ_KEY")
    @POST("/api/v1/payments/decode")
    fun decodeInvoice(@Body decodeInvoiceRequest: DecodeInvoiceRequest): Single<DecodedLightningInvoice>

    @Headers("X-Api-Key: $API_ADMIN_KEY")
    @POST("/api/v1/payments")
    fun payInvoice(@Body payInvoiceRequest: PayInvoiceRequest): Completable

}

data class DecodeInvoiceRequest(
    val data: String
)

private const val API_READ_KEY = "cfd53d3dd3e54df782c9350b8be22d03"
private const val API_ADMIN_KEY = "2aa9dbe17a8c4a158bef6b2c8f960b1e"