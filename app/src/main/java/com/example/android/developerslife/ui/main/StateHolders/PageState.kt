package com.example.android.developerslife.ui.main.StateHolders

import java.lang.Exception

data class PageState(
    val postsList: List<Post>,
    val exceptionOccurred: Boolean,
    val exception: Exception?
)

data class Post(
    val author: String,
    val description: String,
    val gifURL: String
)
