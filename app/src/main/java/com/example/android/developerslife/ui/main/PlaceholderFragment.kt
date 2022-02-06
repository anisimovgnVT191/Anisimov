package com.example.android.developerslife.ui.main

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
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
import com.example.android.developerslife.ui.main.StateHolders.ErrorType
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
                setLoadingScreen()
                pageViewModel.fetchOldPosts(postCategory)
            }
            forwardButton.setOnClickListener {
                setLoadingScreen()
                pageViewModel.fetchNewPosts(postCategory)
            }
            errorContainer.reloadButton.setOnClickListener {
                binding.errorContainer.root.visibility = View.GONE
                setLoadingScreen()
                pageViewModel.reload(postCategory)
            }
        }
        return root
    }

    override fun onStart() {
        super.onStart()
        pageViewModel.uiState.value!!.run {
            if(post==null && errorType==null)
                pageViewModel.reload(postCategory)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pageViewModel.uiState.observe(viewLifecycleOwner){ state ->
            binding.backButton.isEnabled = state.canGoBack
            if(state.exceptionOccurred){
                state.errorType?.let { e ->
                    if(e == ErrorType.NoDataRetrieved){
                        showError(
                            stringId = R.string.no_data_error,
                            drawableId = R.drawable.ic_baseline_file_download_off_24
                        )
                    }
                    if(e == ErrorType.InternetConnectionLost){
                        showError(
                            stringId = R.string.internet_error,
                            drawableId = R.drawable.ic_baseline_cloud_off_24
                        )
                    }
                }
                return@observe
            }
            state.post?.let {
                if(binding.errorContainer.root.isVisible){
                    binding.errorContainer.root.visibility = View.GONE
                }
                binding.forwardButton.isEnabled = true
                binding.descriptionText.visibility = View.VISIBLE
                showPost(it)
            }
        }
    }
    private fun setLoadingScreen(){
        binding.run{
            descriptionText.text = null
            progressCircular.visibility = View.VISIBLE
            gifPlace.setImageDrawable(null)
        }
    }
    private fun showError(stringId: Int, drawableId: Int){
        binding.progressCircular.visibility = View.GONE
        binding.descriptionText.visibility = View.GONE
        binding.forwardButton.isEnabled = false
        binding.gifPlace.setImageDrawable(null)
        binding.errorContainer.apply {
            errorImage.setImageResource(drawableId)
            errorText.text = requireContext().getString(stringId)
            root.visibility = View.VISIBLE
        }
    }
    private fun showPost(post: Post){
        val description = binding.descriptionText
        val gifPlace = binding.gifPlace
        description.text = post.description
        binding.progressCircular.visibility = View.VISIBLE
        Glide
            .with(requireContext())
            .load(post.gifURL)
            .listener(glideListener)
            .run { if(post.canBeCropped) centerCrop() else this }
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(gifPlace)

    }
    private val glideListener = object : RequestListener<Drawable>{
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ) = false

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            binding.progressCircular.visibility = View.GONE
            return false
        }
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