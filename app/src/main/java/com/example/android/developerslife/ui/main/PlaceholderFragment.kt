package com.example.android.developerslife.ui.main

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.android.developerslife.DataLayer.MainFeature.DevsLifeAPI
import com.example.android.developerslife.DataLayer.MainFeature.DevsLifeRemoteDataSource
import com.example.android.developerslife.DataLayer.MainFeature.DevsLifeRepository
import com.example.android.developerslife.DataLayer.MainFeature.PostCategory
import com.example.android.developerslife.DataLayer.RetrofitBuilder.ServiceBuilder
import com.example.android.developerslife.DomainLayer.Either
import com.example.android.developerslife.R
import com.example.android.developerslife.databinding.FragmentMainBinding
import com.example.android.developerslife.ui.main.StateHolders.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A placeholder fragment containing a simple view.
 */
class PlaceholderFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var postCategory: PostCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postCategory =  arguments?.getSerializable(ARG_POST_CATEGORY) as PostCategory
        pageViewModel = ViewModelProvider(this, PageViewModelFactory(DevsLifeRepository(
            dataSource = DevsLifeRemoteDataSource(
                devsLifeAPI = ServiceBuilder.buildService(DevsLifeAPI::class.java),
                ioDispatcher = Dispatchers.IO
            )
        ))).get(PageViewModel::class.java)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMainBinding.inflate(inflater, container, false)
        val root = binding.root

        binding.run {
            backButton.setOnClickListener {
                pageViewModel.fetchOldPosts(postCategory)
            }
            forwardButton.setOnClickListener {
                pageViewModel.fetchNewPosts(postCategory)
            }
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pageViewModel.uiState.observe(viewLifecycleOwner){ state ->
            binding.backButton.isEnabled = state.canGoBack
            if(state.exceptionOccurred){
                return@observe
            }
            state.post?.let {
                showPost(it)
            }
        }
    }
    private fun showPost(post: Post){
        val description = binding.descriptionText
        val gifPlace = binding.gifPlace
        description.text = post.description
        Glide
            .with(requireContext())
            .load(post.gifURL)
            .run { if(post.canBeCropped) centerCrop() else this }
            .into(gifPlace)

    }
    companion object {
        private const val ARG_POST_CATEGORY = "post_category"
        @JvmStatic
        fun newInstance(postCategory: PostCategory): PlaceholderFragment {
            return PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_POST_CATEGORY, postCategory)
                }
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}