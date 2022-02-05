package com.example.android.developerslife.ui.main

import android.util.Log
import androidx.lifecycle.*
import com.example.android.developerslife.DataLayer.MainFeature.DataModels.Result
import com.example.android.developerslife.DataLayer.MainFeature.DevsLifeRepository
import com.example.android.developerslife.DataLayer.MainFeature.PostCategory
import com.example.android.developerslife.DomainLayer.Either
import com.example.android.developerslife.ui.main.StateHolders.PageState
import com.example.android.developerslife.ui.main.StateHolders.Post
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PageViewModel(
    private val devsLifeRepository: DevsLifeRepository
) : ViewModel() {
    private var pageNumber: Int = -1

    private val _uiState = MutableLiveData(PageState(
        post = null,
        exceptionOccurred = false,
        exception = null,
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
            val result = devsLifeRepository.getPostsByPageNumber(postCategory, pageNumber)
            if(result is Either.Left){
                _uiState.value = uiState.value!!.copy(
                    post = null,
                    exceptionOccurred = true,
                    exception = result.left
                )
            }else if(result is Either.Right){
                Log.e("viewModel", result.right.result.toString())
                postsList = result.right.result.map { Post.from(it) }
                _uiState.value = uiState.value!!.copy(
                    post = postsList!!.run { if(isNotEmpty()) takePost() else null },
                    exceptionOccurred = false,
                    exception = null
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        fetchPostsJob?.cancel()
        fetchPostsJob = null
    }

}