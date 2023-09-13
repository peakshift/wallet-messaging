package com.example.serviceproviderapp.networking

import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitFactory {

    val retrofit: Retrofit =
        Retrofit.Builder()
            .baseUrl("https://bits.skunkworks.si/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()


}