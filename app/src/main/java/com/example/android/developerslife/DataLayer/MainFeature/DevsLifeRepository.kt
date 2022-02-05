package com.example.android.developerslife.DataLayer.MainFeature

import com.example.android.developerslife.DataLayer.MainFeature.DataModels.DevsLifeResponse
import com.example.android.developerslife.DomainLayer.Either
import java.lang.Exception

class DevsLifeRepository(
    private val dataSource: DevsLifeRemoteDataSource
) {
    suspend fun getPostsByPageNumber(
        postCategory: PostCategory,
        pageNumber: Int): Either<Exception, DevsLifeResponse>{

        val response = try {
            dataSource.getPostsByPageNumber(postCategory, pageNumber)
        }catch (e: Exception){
            return Either.Left(e)
        }
        if(response.isSuccessful){
            response.body()?.let {
                return Either.Right(it)
            }?: return Either.Left(Exception("Error code = ${response.code()}"))
        }else{
            return Either.Left(Exception("Error code = ${response.code()}"))
        }
    }
}