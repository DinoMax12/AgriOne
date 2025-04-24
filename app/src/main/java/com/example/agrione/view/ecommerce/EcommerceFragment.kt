package com.project.agrione.view.ecommerce

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.project.agrione.R
import com.project.agrione.adapter.EcommerceAdapter
import com.project.agrione.utilities.CellClickListener
import com.project.agrione.viewmodel.EcommViewModel
import kotlinx.android.synthetic.main.fragment_ecommerce.*

class EcommerceFragment : Fragment(), CellClickListener {
    private lateinit var viewmodel: EcommViewModel
    private var adapter: EcommerceAdapter? = null
    lateinit var ecommerceItemFragment: EcommerceItemFragment

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)

            viewmodel = ViewModelProvider(requireActivity())
                .get(EcommViewModel::class.java)

            viewmodel.loadAllEcommItems()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewmodel = ViewModelProvider(requireActivity())
            .get(EcommViewModel::class.java)

        viewmodel.ecommLiveData.observe(viewLifecycleOwner, Observer {
            adapter = EcommerceAdapter(requireContext(), it, this)
            ecommrcyclr.adapter = adapter
            ecommrcyclr.layoutManager = LinearLayoutManager(requireContext())
        })

        return inflater.inflate(R.layout.fragment_ecommerce, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.title = "E-Commerce"

        chipgrp.check(R.id.chip1)
        viewmodel.loadAllEcommItems()

        chipgrp.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chip1 -> {
                    viewmodel.loadAllEcommItems().observe(viewLifecycleOwner, Observer {
                        ecommrcyclr.adapter =
                            EcommerceAdapter(requireContext(), it, this)
                    })
                }
                R.id.chip2 -> {
                    viewmodel.getSpecificCategoryItems("fertilizer")
                        .observe(viewLifecycleOwner, Observer {
                            ecommrcyclr.adapter =
                                EcommerceAdapter(requireContext(), it, this)
                        })
                }
                R.id.chip3 -> {
                    viewmodel.getSpecificCategoryItems("pesticide")
                        .observe(viewLifecycleOwner, Observer {
                            ecommrcyclr.adapter =
                                EcommerceAdapter(requireContext(), it, this)
                        })
                }
                R.id.chip4 -> {
                    viewmodel.getSpecificCategoryItems("machine")
                        .observe(viewLifecycleOwner, Observer {
                            ecommrcyclr.adapter =
                                EcommerceAdapter(requireContext(), it, this)
                        })
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EcommerceFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onCellClickListener(name: String) {
        ecommerceItemFragment = EcommerceItemFragment()
        val bundle = Bundle()
        bundle.putString("name", name)
        ecommerceItemFragment.arguments = bundle

        val transaction = activity!!.supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout, ecommerceItemFragment, name)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .setReorderingAllowed(true)
            .addToBackStack("ecommItem")
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.cart_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.cart_item -> {
                val cartFragment = CartFragment()
                val transaction = activity!!.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout, cartFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .setReorderingAllowed(true)
                    .addToBackStack("cart")
                    .commit()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
