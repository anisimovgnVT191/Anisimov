package com.example.android.developerslife.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.android.developerslife.DataLayer.MainFeature.DevsLifeAPI
import com.example.android.developerslife.DataLayer.MainFeature.DevsLifeRemoteDataSource
import com.example.android.developerslife.DataLayer.MainFeature.DevsLifeRepository
import com.example.android.developerslife.DataLayer.MainFeature.PostCategory
import com.example.android.developerslife.DataLayer.RetrofitBuilder.ServiceBuilder
import com.example.android.developerslife.DomainLayer.Either
import com.example.android.developerslife.R
import com.example.android.developerslife.databinding.FragmentMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A placeholder fragment containing a simple view.
 */
class PlaceholderFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentMainBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
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

        return root
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