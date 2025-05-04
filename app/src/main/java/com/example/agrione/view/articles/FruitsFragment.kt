package com.example.agrione.view.articles

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.RotateAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.agrione.R
import com.example.agrione.databinding.FragmentFruitsBinding
import com.example.agrione.utilities.CellClickListener
import com.example.agrione.viewmodel.ArticleListener
import com.example.agrione.viewmodel.ArticleViewModel
import com.exmaple.agrione.utilities.hide
import com.exmaple.agrione.utilities.show

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class FruitsFragment : Fragment(), ArticleListener {

    private var param1: String? = null
    private var param2: String? = null
    private var param3: String? = null
    private lateinit var viewModel: ArticleViewModel
    private var _binding: FragmentFruitsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            param3 = it.getString("name")
        }

        viewModel = ViewModelProvider(requireActivity())[ArticleViewModel::class.java]
        Log.d("I'm called 2", viewModel.message3.value.toString())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFruitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.title = "Articles"

        var toggle = 0

        binding.descToggleBtnFruitFragArt.setOnClickListener {
            val anim = RotateAnimation(
                if (toggle == 0) 0f else 180f,
                if (toggle == 0) 180f else 0f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f
            )
            anim.duration = 2
            anim.fillAfter = true
            binding.descToggleBtnFruitFragArt.startAnimation(anim)

            binding.descTextValueFruitFragArt.maxLines = if (toggle == 0) Int.MAX_VALUE else 3
            toggle = 1 - toggle
        }

        viewModel.message1.observe(viewLifecycleOwner, Observer {
            binding.progressArticle.show()

            val attributes = it["attributes"] as Map<String, String>
            val desc = it["description"].toString()
            val diseases = it["diseases"] as List<String>

            binding.tempTextFruitFragArt.text = attributes["Temperature"]
            binding.monthTextFruitFragArt.text = attributes["Time"]
            binding.titleTextFruitFragArt.text = it["title"].toString()
            binding.descTextValueFruitFragArt.text = desc
            binding.processTextValueFruitFragArt.text = it["process"].toString()
            binding.soilTextValueFruitFragArt.text = it["soil"].toString()
            binding.stateTextValueFruitFragArt.text = it["state"].toString()

            val images = it["images"] as List<String>
            Glide.with(this).load(images[0]).into(binding.imageFruitFragArt)

            binding.attr1ValueFruitFragArt.text = attributes["Weight"]
            binding.attr2ValueFruitFragArt.text = attributes["Vitamins"]
            binding.attr3ValueFruitFragArt.text = attributes["Tree Height"]
            binding.attr4ValueFruitFragArt.text = attributes["growthTime"]

            binding.diseaseTextValueFruitFragArt.text = ""
            for (i in diseases.indices) {
                binding.diseaseTextValueFruitFragArt.append("${i + 1}. ${diseases[i]}\n")
            }

            binding.progressArticle.hide()
        })

        viewModel.message3.value?.let { newData ->
            Log.d("New data length", newData.size.toString())
            for (item in newData) {
                if (item.data?.get("title") == this.tag) {
                    val data = item.data!!
                    val attributes = data["attributes"] as Map<String, String>
                    val desc = data["description"].toString()
                    val diseases = data["diseases"] as List<String>

                    binding.tempTextFruitFragArt.text = attributes["Temperature"]
                    binding.monthTextFruitFragArt.text = attributes["Time"]
                    binding.titleTextFruitFragArt.text = data["title"].toString()
                    binding.descTextValueFruitFragArt.text = desc
                    binding.processTextValueFruitFragArt.text = data["process"].toString()
                    binding.soilTextValueFruitFragArt.text = data["soil"].toString()
                    binding.stateTextValueFruitFragArt.text = data["state"].toString()

                    val images = data["images"] as List<String>
                    Glide.with(this).load(images[0]).into(binding.imageFruitFragArt)

                    binding.attr1ValueFruitFragArt.text = attributes["Weight"]
                    binding.attr2ValueFruitFragArt.text = attributes["Vitamins"]
                    binding.attr3ValueFruitFragArt.text = attributes["Tree Height"]
                    binding.attr4ValueFruitFragArt.text = attributes["growthTime"]

                    binding.diseaseTextValueFruitFragArt.text = ""
                    for (i in diseases.indices) {
                        binding.diseaseTextValueFruitFragArt.append("${i + 1}. ${diseases[i]}\n")
                    }

                    binding.progressArticle.hide()
                }
            }
        }
    }

    override fun onStarted() {}
    override fun onSuccess(authRepo: LiveData<String>) {
        authRepo.observe(viewLifecycleOwner, Observer {
            Log.d("Fruit", "Success")
        })
    }

    override fun onFailure(message: String) {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up the binding reference
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FruitsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
