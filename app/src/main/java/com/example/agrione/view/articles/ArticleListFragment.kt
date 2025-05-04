package com.example.agrione.view.articles

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.agrione.adapter.ArticleListAdapter
import com.example.agrione.R
import com.example.agrione.utilities.CellClickListener
import com.example.agrione.viewmodel.ArticleViewModel
import com.example.agrione.databinding.FragmentArticleListBinding // Import the view binding class

class ArticleListFragment : Fragment(), CellClickListener {

    private lateinit var viewModel: ArticleViewModel
    private lateinit var adapter: ArticleListAdapter
    private lateinit var fruitFragment: FruitsFragment
    private var _binding: FragmentArticleListBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[ArticleViewModel::class.java]
        viewModel.getAllArticles("article_fruits")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentArticleListBinding.inflate(inflater, container, false)
        return binding.root // Use the root view from the view binding
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.title = "Articles"

        viewModel.message3.observe(viewLifecycleOwner) { articleList ->
            Log.d("Art All Data", articleList[0].data.toString())

            // Pass the article list as the first parameter, and cell click listener (this) as the second
            adapter = ArticleListAdapter(articleList, this)
            binding.recyclerArticleListFrag.adapter = adapter
            binding.recyclerArticleListFrag.layoutManager = GridLayoutManager(requireContext(), 2)
        }

    }

    override fun onCellClickListener(name: String) {
        fruitFragment = FruitsFragment().apply {
            arguments = Bundle().apply {
                putString("name", name)
            }
        }

        activity?.supportFragmentManager
            ?.beginTransaction()
            ?.replace(R.id.frame_layout, fruitFragment, name)
            ?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            ?.setReorderingAllowed(true)
            ?.addToBackStack("name")
            ?.commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up binding when the view is destroyed
    }
}
