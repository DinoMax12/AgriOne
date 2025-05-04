package com.example.agrione.view.yojna

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agrione.R
import com.example.agrione.adapter.YojnaAdapter
import com.example.agrione.utilities.CellClickListener
import com.example.agrione.viewmodel.YojnaViewModel
import com.example.agrione.databinding.FragmentYojnaListBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class YojnaListFragment : Fragment(), CellClickListener {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var viewModel: YojnaViewModel
    private lateinit var adapter: YojnaAdapter
    private lateinit var yojnaFragment: YojnaFragment
    private var _binding: FragmentYojnaListBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        viewModel = ViewModelProvider(requireActivity())[YojnaViewModel::class.java]
        viewModel.getAllYojna("yojnas")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentYojnaListBinding.inflate(inflater, container, false)
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

        viewModel.message3.observe(viewLifecycleOwner, Observer { response ->
            Log.d("Yojna All Data", response[0].data.toString())

            adapter = YojnaAdapter(requireContext(), response, this)
            binding.rcyclrYojnaList.adapter = adapter
            binding.rcyclrYojnaList.layoutManager = LinearLayoutManager(requireContext())
        })
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            YojnaListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onCellClickListener(name: String) {
        yojnaFragment = YojnaFragment().apply {
            arguments = Bundle().apply {
                putString("name", name)
            }
        }
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, yojnaFragment, name)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .setReorderingAllowed(true)
            .addToBackStack("yojnaListFrag")
            .commit()
    }
}
