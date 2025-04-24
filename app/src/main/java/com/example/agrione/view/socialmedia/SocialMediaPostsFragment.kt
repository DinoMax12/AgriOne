package com.project.agrione.view.socialmedia

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.project.agrione.R
import com.project.agrione.adapter.SMPostListAdapter
import com.project.agrione.viewmodel.SocialMediaViewModel
import kotlinx.android.synthetic.main.fragment_social_media_posts.*

/**
 * A simple [Fragment] subclass for displaying social media posts in the Agrione app.
 */
class SocialMediaPostsFragment : Fragment() {

    private lateinit var smCreatePostFragment: SMCreatePostFragment
    private var adapter: SMPostListAdapter? = null
    private lateinit var viewModel: SocialMediaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(requireActivity())
            .get(SocialMediaViewModel::class.java)

        // Optional: Load posts if needed in the view model
        // viewModel.loadPosts()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_social_media_posts, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SocialMediaPostsFragment().apply {
                arguments = Bundle().apply {
                    putString("ARG_PARAM1", param1)
                    putString("ARG_PARAM2", param2)
                }
            }
    }

    /**
     * Fetch data from Firestore and update the UI with posts.
     */
    fun getData() {
        val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()

        firebaseFirestore.collection("posts")
            .orderBy("timeStamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener {
                Log.d("Posts data", it.documents.toString())
                adapter = SMPostListAdapter(requireContext(), it.documents)
                postsRecycler.adapter = adapter
                postsRecycler.layoutManager = LinearLayoutManager(requireContext())
            }
            .addOnFailureListener {
                Log.e("Firestore Error", "Failed to fetch posts", it)
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.title = "Social Media"

        // Load data when the fragment is created
        getData()

        // Initialize create post fragment
        smCreatePostFragment = SMCreatePostFragment()

        // Setup floating action button for creating posts
        createPostFloating.setOnClickListener {
            val transaction = activity!!.supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, smCreatePostFragment, "smCreate")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .setReorderingAllowed(true)
                .addToBackStack("smCreate")
                .commit()
        }
    }
}
