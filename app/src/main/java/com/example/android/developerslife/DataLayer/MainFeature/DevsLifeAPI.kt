package com.example.android.developerslife.DataLayer.MainFeature

import com.example.android.developerslife.DataLayer.MainFeature.DataModels.DevsLifeResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface DevsLifeAPI {

    @GET("{postCategory}/{pageNumber}")
    fun getPostsByPageNumber(
        @Path(value = "pageNumber", encoded = true) pageNumber: Int,
        @Path(value = "postCategory", encoded = true) postCategory: String
    ):Response<DevsLifeResponse>
}