package com.agrione.app.view.yojna

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.agrione.app.R
import com.agrione.app.viewmodel.YojnaViewModel
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_yojna.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class YojnaFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var yojnaViewModel: YojnaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        Log.d("YojnaFragment", this.tag.toString())

        yojnaViewModel =
            ViewModelProvider(requireActivity())[YojnaViewModel::class.java]

        yojnaViewModel.getYojna(this.tag.toString())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_yojna, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.title = "Krishi Yojna"
        progressYojna.visibility = View.VISIBLE

        yojnaViewModel.msg.observe(viewLifecycleOwner, Observer { it ->
            yojnaTitle.text = it["title"].toString()
            yojnaDesc.text = it["description"].toString()
            yojnaDate.text = it["launch"].toString()
            yojnaLaunchedBy.text = it["headedBy"].toString()
            yojnaBudget.text = it["budget"].toString()

            val eligibility = it["eligibility"] as ArrayList<String>
            val documents = it["documents"] as ArrayList<String>
            val objectives = it["objective"] as ArrayList<String>

            yojnaEligibility.text = eligibility.mapIndexed { i, item -> "${i + 1}. $item" }.joinToString("\n")
            yojnaDocumentsRequired.text = documents.mapIndexed { i, item -> "${i + 1}. $item" }.joinToString("\n")
            yojnaObjectives.text = objectives.mapIndexed { i, item -> "${i + 1}. $item" }.joinToString("\n")

            yojnaWebsite.text = it["website"].toString()
            Glide.with(this).load(it["image"].toString()).into(yojnaImage)

            progressYojna.visibility = View.GONE
        })
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            YojnaFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
