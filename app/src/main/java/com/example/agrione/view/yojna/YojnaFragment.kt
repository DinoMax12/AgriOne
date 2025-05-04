package com.example.agrione.view.yojna

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.agrione.R
import com.example.agrione.viewmodel.YojnaViewModel
import com.bumptech.glide.Glide
import com.example.agrione.databinding.FragmentYojnaBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class YojnaFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var yojnaViewModel: YojnaViewModel
    private var _binding: FragmentYojnaBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentYojnaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.title = "Krishi Yojna"
        binding.progressYojna.visibility = View.VISIBLE

        yojnaViewModel.msg.observe(viewLifecycleOwner, Observer { it ->
            binding.yojnaTitle.text = it["title"].toString()
            binding.yojnaDesc.text = it["description"].toString()
            binding.yojnaDate.text = it["launch"].toString()
            binding.yojnaLaunchedBy.text = it["headedBy"].toString()
            binding.yojnaBudget.text = it["budget"].toString()

            val eligibility = it["eligibility"] as ArrayList<String>
            val documents = it["documents"] as ArrayList<String>
            val objectives = it["objective"] as ArrayList<String>

            binding.yojnaEligibility.text = eligibility.mapIndexed { i, item -> "${i + 1}. $item" }.joinToString("\n")
            binding.yojnaDocumentsRequired.text = documents.mapIndexed { i, item -> "${i + 1}. $item" }.joinToString("\n")
            binding.yojnaObjectives.text = objectives.mapIndexed { i, item -> "${i + 1}. $item" }.joinToString("\n")

            binding.yojnaWebsite.text = it["website"].toString()
            Glide.with(this).load(it["image"].toString()).into(binding.yojnaImage)

            binding.progressYojna.visibility = View.GONE
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
