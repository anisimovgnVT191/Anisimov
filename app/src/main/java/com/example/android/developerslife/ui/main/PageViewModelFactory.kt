package com.example.android.developerslife.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.developerslife.DataLayer.MainFeature.DevsLifeRepository
import java.lang.IllegalArgumentException

class PageViewModelFactory(
    private val devsLifeRepository: DevsLifeRepository
):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(PageViewModel::class.java)){
            return PageViewModel(devsLifeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}