package com.example.android.developerslife.ui.main

import android.os.UserManager
import android.util.Log
import androidx.lifecycle.*
import com.example.android.developerslife.DataLayer.CacheManager
import com.example.android.developerslife.DataLayer.MainFeature.DataModels.DevsLifeResponse
import com.example.android.developerslife.DataLayer.MainFeature.DataModels.Result
import com.example.android.developerslife.DataLayer.MainFeature.DevsLifeRepository
import com.example.android.developerslife.DataLayer.MainFeature.PostCategory
import com.example.android.developerslife.DomainLayer.Either
import com.example.android.developerslife.ui.main.StateHolders.ErrorType
import com.example.android.developerslife.ui.main.StateHolders.PageState
import com.example.android.developerslife.ui.main.StateHolders.Post
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException

class PageViewModel(
    private val devsLifeRepository: DevsLifeRepository
) : ViewModel() {
    private var pageNumber: Int = -1

    private val _uiState = MutableLiveData(PageState(
        post = null,
        exceptionOccurred = false,
        errorType = null,
        canGoBack = false))
    val uiState: LiveData<PageState> get() = _uiState

    private var postsList: List<Post>? = null

    fun fetchOldPosts(postCategory: PostCategory){
        uiState.value!!.post?.let { post ->
            val index = postsList!!.indexOf(post)
            postsList!!.getOrNull(index-1)?.let {
                _uiState.value = uiState.value!!.copy(
                    post = it,
                    canGoBack = !((index-1) == 0 && pageNumber == 0))
                return
            }
        }
        _uiState.value = uiState.value!!.copy(canGoBack = --pageNumber!=-1)
        fetchPosts(postCategory, pageNumber, List<Post>::last)
    }

    fun fetchNewPosts(postCategory: PostCategory){
        uiState.value!!.post?.let { post ->
            val index = postsList!!.indexOf(post)
            postsList!!.getOrNull(index+1)?.let {
                _uiState.value = uiState.value!!.copy(post = it, canGoBack = true)
                return
            }
        }
        _uiState.value = uiState.value!!.copy(canGoBack = ++pageNumber!=0)
        fetchPosts(postCategory, pageNumber, List<Post>::first)
    }

    private var fetchPostsJob: Job? = null
    private fun fetchPosts(
        postCategory: PostCategory,
        pageNumber: Int,
        takePost: List<Post>.() -> Post){

        fetchPostsJob?.cancel()
        fetchPostsJob = viewModelScope.launch {
            val key = Key(pageNumber, postCategory)
            if(isCached(key)){
                val result = getFromCache(key)
                postsList = result!!.result.map { Post.from(it) }
                _uiState.value = uiState.value!!.copy(
                    post = postsList!!.run { if(isNotEmpty()) takePost() else null },
                    exceptionOccurred = postsList!!.isEmpty(),
                    errorType = if(postsList!!.isEmpty()) ErrorType.NoDataRetrieved else null
                )
                return@launch
            }
            val result = devsLifeRepository.getPostsByPageNumber(postCategory, pageNumber)
            if(result is Either.Left){
                _uiState.value = uiState.value!!.copy(
                    post = null,
                    exceptionOccurred = true,
                    errorType = if(result.left is IOException) ErrorType.InternetConnectionLost
                                else null
                )
            }else if(result is Either.Right){
                Log.e("viewModel", result.right.result.toString())
                putToCache(key, result.right)
                postsList = result.right.result.map { Post.from(it) }
                _uiState.value = uiState.value!!.copy(
                    post = postsList!!.run { if(isNotEmpty()) takePost() else null },
                    exceptionOccurred = postsList!!.isEmpty(),
                    errorType = if(postsList!!.isEmpty()) ErrorType.NoDataRetrieved else null
                )
            }
        }
    }
    fun reload(postCategory: PostCategory){
        if(pageNumber==-1){
            pageNumber++
        }
        fetchPosts(postCategory, pageNumber, List<Post>::first)
    }
    private fun isCached(key: Key):Boolean {
        return CacheManager.isCached(key.toString())
    }
    private fun getFromCache(key: Key) = CacheManager.get(key.toString())
    private fun putToCache(key: Key, data: DevsLifeResponse) { CacheManager.put(key.toString(), data)}


    private class Key(private val pageNumber: Int, private val postCategory: PostCategory){
        override fun toString() = "${postCategory}_$pageNumber"
    }
    override fun onCleared() {
        super.onCleared()
        fetchPostsJob?.cancel()
        fetchPostsJob = null
    }

}