package com.agrione.app.view.yojna

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
import com.agrione.app.R
import com.agrione.app.adapter.YojnaAdapter
import com.agrione.app.utilities.CellClickListener
import com.agrione.app.viewmodel.YojnaViewModel
import kotlinx.android.synthetic.main.fragment_yojna_list.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class YojnaListFragment : Fragment(), CellClickListener {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var viewModel: YojnaViewModel
    private lateinit var adapter: YojnaAdapter
    private lateinit var yojnaFragment: YojnaFragment

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

        viewModel.message3.observe(viewLifecycleOwner, Observer { response ->
            Log.d("Yojna All Data", response[0].data.toString())

            adapter = YojnaAdapter(requireContext(), response, this)
            rcyclr_yojnaList.adapter = adapter
            rcyclr_yojnaList.layoutManager = LinearLayoutManager(requireContext())
        })

        return inflater.inflate(R.layout.fragment_yojna_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.title = "Krishi Yojna"
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
