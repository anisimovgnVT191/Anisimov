package com.example.android.developerslife.ui.main

import android.graphics.pdf.PdfDocument
import android.util.Log
import androidx.lifecycle.*
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
    private var pageNumber: Int = 0

    private val _uiState = MutableLiveData(PageState(
        postsList = emptyList(),
        exceptionOccurred = false,
        exception = null))
    val uiState: LiveData<PageState> get() = _uiState

    fun fetchOldPosts(postCategory: PostCategory){
        if(pageNumber - 1 <= 0) { return }
        else { fetchPosts(postCategory, --pageNumber)}
    }
    fun fetchNewPosts(postCategory: PostCategory){
        fetchPosts(postCategory, pageNumber++)
    }

    private var fetchPostsJob: Job? = null
    private fun fetchPosts(postCategory: PostCategory, pageNumber: Int){

        fetchPostsJob?.cancel()
        fetchPostsJob = viewModelScope.launch {
            val result = devsLifeRepository.getPostsByPageNumber(postCategory, pageNumber)
            if(result is Either.Left){
                _uiState.value = PageState(
                    postsList = emptyList(),
                    exceptionOccurred = true,
                    exception = result.left
                )
            }else if(result is Either.Right){
                _uiState.value = PageState(
                    postsList = result.right.result.map { Post(
                        author = it.author,
                        description = it.description,
                        gifURL = it.gifURL
                    ) },
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