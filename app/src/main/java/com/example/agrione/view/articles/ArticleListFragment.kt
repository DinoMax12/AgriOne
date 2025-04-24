package com.project.farmingapp.view.articles

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
import com.project.farmingapp.R
import com.project.farmingapp.adapter.ArticleListAdapter
import com.project.farmingapp.utilities.CellClickListener
import com.project.farmingapp.viewmodel.ArticleViewModel
import kotlinx.android.synthetic.main.fragment_article_list.*

class ArticleListFragment : Fragment(), CellClickListener {

    private lateinit var viewModel: ArticleViewModel
    private lateinit var adapter: ArticleListAdapter
    private lateinit var fruitFragment: FruitsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[ArticleViewModel::class.java]
        viewModel.getAllArticles("article_fruits")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_article_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.title = "Articles"

        viewModel.message3.observe(viewLifecycleOwner) { articleList ->
            Log.d("Art All Data", articleList[0].data.toString())

            adapter = ArticleListAdapter(requireContext(), articleList, this)
            recyclerArticleListFrag.adapter = adapter
            recyclerArticleListFrag.layoutManager = GridLayoutManager(requireContext(), 2)
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
}
