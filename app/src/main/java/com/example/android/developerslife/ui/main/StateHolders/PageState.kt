package com.example.android.developerslife.ui.main.StateHolders

import android.util.Log
import java.lang.Exception
import com.example.android.developerslife.DataLayer.MainFeature.DataModels.Result
data class PageState(
    val post: Post?,
    val canGoBack: Boolean,
    val exceptionOccurred: Boolean,
    val exception: Exception?
)

data class Post(
    val author: String,
    val description: String,
    val gifURL: String,
    val canBeCropped: Boolean
){
    companion object{
        fun from(result: Result): Post{
            val _canBeCropped = result.height.toFloat()/result.width.toFloat()
            Log.e("Post", "canBeCropped = $_canBeCropped")
            return result.run {
                Post(
                    author = author,
                    description = description,
                    gifURL = gifURL,
                    canBeCropped = _canBeCropped >= 0.75F
                )
            }
        }
    }
}
