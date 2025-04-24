package com.project.agrione.view.articles

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
import com.project.agrione.R
import com.project.agrione.utilities.hide
import com.project.agrione.utilities.show
import com.project.agrione.viewmodel.ArticleListener
import com.project.agrione.viewmodel.ArticleViewModel
import kotlinx.android.synthetic.main.fragment_fruits.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class FruitsFragment : Fragment(), ArticleListener {

    private var param1: String? = null
    private var param2: String? = null
    private var param3: String? = null
    private lateinit var viewModel: ArticleViewModel

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
        viewModel.message1.observe(viewLifecycleOwner, Observer {
            progressArticle.show()

            val attributes = it["attributes"] as Map<String, String>
            val desc = it["description"].toString()
            val diseases = it["diseases"] as List<String>

            tempTextFruitFragArt.text = attributes["Temperature"]
            monthTextFruitFragArt.text = attributes["Time"]
            titleTextFruitFragArt.text = it["title"].toString()
            descTextValueFruitFragArt.text = desc
            processTextValueFruitFragArt.text = it["process"].toString()
            soilTextValueFruitFragArt.text = it["soil"].toString()
            stateTextValueFruitFragArt.text = it["state"].toString()

            val images = it["images"] as List<String>
            Glide.with(this).load(images[0]).into(imageFruitFragArt)

            attr1ValueFruitFragArt.text = attributes["Weight"]
            attr2ValueFruitFragArt.text = attributes["Vitamins"]
            attr3ValueFruitFragArt.text = attributes["Tree Height"]
            attr4ValueFruitFragArt.text = attributes["growthTime"]

            diseaseTextValueFruitFragArt.text = ""
            for (i in diseases.indices) {
                diseaseTextValueFruitFragArt.append("${i + 1}. ${diseases[i]}\n")
            }

            progressArticle.hide()
        })

        return inflater.inflate(R.layout.fragment_fruits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.title = "Articles"

        var toggle = 0

        descToggleBtnFruitFragArt.setOnClickListener {
            val anim = RotateAnimation(
                if (toggle == 0) 0f else 180f,
                if (toggle == 0) 180f else 0f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f
            )
            anim.duration = 2
            anim.fillAfter = true
            descToggleBtnFruitFragArt.startAnimation(anim)

            descTextValueFruitFragArt.maxLines = if (toggle == 0) Int.MAX_VALUE else 3
            toggle = 1 - toggle
        }

        viewModel.message3.value?.let { newData ->
            Log.d("New data length", newData.size.toString())
            for (item in newData) {
                if (item.data?.get("title") == this.tag) {
                    val data = item.data!!
                    val attributes = data["attributes"] as Map<String, String>
                    val desc = data["description"].toString()
                    val diseases = data["diseases"] as List<String>

                    tempTextFruitFragArt.text = attributes["Temperature"]
                    monthTextFruitFragArt.text = attributes["Time"]
                    titleTextFruitFragArt.text = data["title"].toString()
                    descTextValueFruitFragArt.text = desc
                    processTextValueFruitFragArt.text = data["process"].toString()
                    soilTextValueFruitFragArt.text = data["soil"].toString()
                    stateTextValueFruitFragArt.text = data["state"].toString()

                    val images = data["images"] as List<String>
                    Glide.with(this).load(images[0]).into(imageFruitFragArt)

                    attr1ValueFruitFragArt.text = attributes["Weight"]
                    attr2ValueFruitFragArt.text = attributes["Vitamins"]
                    attr3ValueFruitFragArt.text = attributes["Tree Height"]
                    attr4ValueFruitFragArt.text = attributes["growthTime"]

                    diseaseTextValueFruitFragArt.text = ""
                    for (i in diseases.indices) {
                        diseaseTextValueFruitFragArt.append("${i + 1}. ${diseases[i]}\n")
                    }

                    progressArticle.hide()
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
