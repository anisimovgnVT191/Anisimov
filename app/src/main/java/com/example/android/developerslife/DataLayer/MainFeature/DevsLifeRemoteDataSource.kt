package com.example.android.developerslife.DataLayer.MainFeature

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class DevsLifeRemoteDataSource(
    private val devsLifeAPI: DevsLifeAPI,
    private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun getPostsByPageNumber(postCategory: PostCategory, pageNumber: Int) =
        withContext(ioDispatcher){
            devsLifeAPI.getPostsByPageNumber(pageNumber, postCategory.toString())
        }
}