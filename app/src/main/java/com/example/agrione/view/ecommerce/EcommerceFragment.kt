package com.example.agrione.view.ecommerce

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
import com.example.agrione.R
import com.example.agrione.adapter.EcommerceAdapter
import com.example.agrione.utilities.CellClickListener
import com.example.agrione.viewmodel.EcommViewModel
import com.example.agrione.databinding.FragmentEcommerceBinding

class EcommerceFragment : Fragment(), CellClickListener {
    private var _binding: FragmentEcommerceBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewmodel: EcommViewModel
    private var adapter: EcommerceAdapter? = null
    lateinit var ecommerceItemFragment: EcommerceItemFragment

    private var param1: String? = null
    private var param2: String? = null

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EcommerceFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

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
        _binding = FragmentEcommerceBinding.inflate(inflater, container, false)
        viewmodel = ViewModelProvider(requireActivity())
            .get(EcommViewModel::class.java)

        viewmodel.ecommLiveData.observe(viewLifecycleOwner, Observer {
            adapter = EcommerceAdapter(requireContext(), it, this)
            binding.ecommrcyclr.adapter = adapter
            binding.ecommrcyclr.layoutManager = LinearLayoutManager(requireContext())
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.title = "E-Commerce"

        binding.chipgrp.check(R.id.chip1)
        viewmodel.loadAllEcommItems()

        binding.chipgrp.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chip1 -> {
                    Log.d("EcommerceFragment", "Loading all items")
                    viewmodel.loadAllEcommItems().observe(viewLifecycleOwner, Observer {
                        Log.d("EcommerceFragment", "All items loaded: ${it.size}")
                        binding.ecommrcyclr.adapter =
                            EcommerceAdapter(requireContext(), it, this)
                        binding.noProductsText.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                    })
                }
                R.id.chip2 -> {
                    Log.d("EcommerceFragment", "Loading fertilizer items")
                    viewmodel.getSpecificCategoryItems("fertilizer")
                        .observe(viewLifecycleOwner, Observer {
                            Log.d("EcommerceFragment", "Fertilizer items loaded: ${it.size}")
                            binding.ecommrcyclr.adapter =
                                EcommerceAdapter(requireContext(), it, this)
                            binding.noProductsText.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                        })
                }
                R.id.chip3 -> {
                    Log.d("EcommerceFragment", "Loading pesticide items")
                    viewmodel.getSpecificCategoryItems("pesticide")
                        .observe(viewLifecycleOwner, Observer {
                            Log.d("EcommerceFragment", "Pesticide items loaded: ${it.size}")
                            binding.ecommrcyclr.adapter =
                                EcommerceAdapter(requireContext(), it, this)
                            binding.noProductsText.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                        })
                }
                R.id.chip4 -> {
                    Log.d("EcommerceFragment", "Loading machine items")
                    viewmodel.getSpecificCategoryItems("machine")
                        .observe(viewLifecycleOwner, Observer {
                            Log.d("EcommerceFragment", "Machine items loaded: ${it.size}")
                            binding.ecommrcyclr.adapter =
                                EcommerceAdapter(requireContext(), it, this)
                            binding.noProductsText.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                        })
                }
                R.id.chip5 -> {
                    Log.d("EcommerceFragment", "Loading seed items")
                    viewmodel.getSpecificCategoryItems("seed")
                        .observe(viewLifecycleOwner, Observer {
                            Log.d("EcommerceFragment", "Seed items loaded: ${it.size}")
                            binding.ecommrcyclr.adapter =
                                EcommerceAdapter(requireContext(), it, this)
                            binding.noProductsText.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                        })
                }
            }
        }
    }

    override fun onCellClickListener(name: String) {
        ecommerceItemFragment = EcommerceItemFragment()
        val bundle = Bundle()
        bundle.putString("name", name)
        ecommerceItemFragment.arguments = bundle

        val transaction = requireActivity().supportFragmentManager
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
                val transaction = requireActivity().supportFragmentManager
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
