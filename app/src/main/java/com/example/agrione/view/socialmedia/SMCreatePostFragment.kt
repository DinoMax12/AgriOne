package com.example.agrione.view.socialmedia

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.example.agrione.R
import com.example.agrione.viewmodel.UserDataViewModel
import com.example.agrione.viewmodel.UserProfilePostsViewModel
import com.example.agrione.databinding.FragmentSMCreatePostBinding
import java.io.IOException
import java.util.*

class SMCreatePostFragment : Fragment() {
    private var _binding: FragmentSMCreatePostBinding? = null
    private val binding get() = _binding!!

    private var param1: String? = null
    private var param2: String? = null
    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null
    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    private var authUser: FirebaseAuth? = null
    private var postID: UUID? = null
    private var bitmap: Bitmap? = null
    lateinit var socialMediaPostsFragment: SocialMediaPostsFragment
    lateinit var userDataViewModel: UserDataViewModel
    val db = FirebaseFirestore.getInstance()
    val data2 = HashMap<String, Any>()

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SMCreatePostFragment().apply {
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
        }
        storageReference = FirebaseStorage.getInstance().reference
        authUser = FirebaseAuth.getInstance()
        firebaseStore = FirebaseStorage.getInstance()

        userDataViewModel = ViewModelProvider(requireActivity())
            .get(UserDataViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSMCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.title = "Social Media"

        binding.progressCreatePost.visibility = View.GONE
        binding.progressTitle.visibility = View.GONE

        data2["uploadType"] = ""
        binding.uploadImagePreview.setOnClickListener {
            val intent = Intent()
            intent.type = "image/* video/*"
            intent.action = Intent.ACTION_PICK
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE_REQUEST
            )
        }

        val googleLoggedUser = authUser!!.currentUser!!.displayName
        if (googleLoggedUser.isNullOrEmpty()) {
            db.collection("users").document(authUser!!.currentUser!!.email!!)
                .get()
                .addOnCompleteListener {
                    val data = it.result
                    data2["name"] = data!!.getString("name").toString()
                    data!!.getString("name")?.let { it1 -> Log.d("Google User", it1) }
                }
        } else {
            data2["name"] = googleLoggedUser.toString()
            Log.d("Normal User", googleLoggedUser)
        }

        binding.createPostBtnSM.setOnClickListener {
            if (binding.postTitleSM.text.toString().isNullOrEmpty()) {
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Please enter title",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                uploadImage()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data == null || data.data == null) {
                return
            }

            filePath = data.data
            binding.uploadImagePreview.setImageURI(filePath)
            try {
                val lastIndex = filePath.toString().length - 1
                val type =
                    filePath.toString().slice((filePath.toString().lastIndexOf(".") + 1)..lastIndex)

                Log.d("File Type", filePath.toString())

                if (filePath.toString().contains("png") || filePath.toString().contains("jpg") || filePath.toString().contains("jpeg") || filePath.toString().contains("image") || filePath.toString().contains("images")){
                    data2["uploadType"] = "image"
                } else if(filePath.toString().contains("videos") || filePath.toString().contains("video") || filePath.toString().contains("mp4")){
                    data2["uploadType"] = "video"
                }

                Log.d("File Type 3", data2["uploadType"].toString())
                bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, filePath)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage() {
        binding.progressCreatePost.visibility = View.VISIBLE
        binding.progressTitle.visibility = View.VISIBLE
        if (filePath != null) {
            postID = UUID.randomUUID()
            val ref = storageReference?.child("posts/" + postID.toString())
            val uploadTask = ref?.putFile(filePath!!)

            val urlTask =
                uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                            binding.progressCreatePost.visibility = View.GONE
                            binding.progressTitle.visibility = View.GONE
                        }
                    }
                    return@Continuation ref.downloadUrl
                })?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        addUploadRecordWithImageToDb(downloadUri.toString(), postID!!)
                    } else {
                        binding.progressCreatePost.visibility = View.GONE
                        binding.progressTitle.visibility = View.GONE
                    }
                }?.addOnFailureListener {
                    binding.progressCreatePost.visibility = View.GONE
                    binding.progressTitle.visibility = View.GONE
                    Toast.makeText(requireActivity().applicationContext, it.message, Toast.LENGTH_LONG).show()
                }
        } else {
            data2["uploadType"] = ""
            addUploadRecordWithImageToDb(null, null)
        }
    }

    private fun addUploadRecordWithImageToDb(uri: String?, postID: UUID?) {

        if (!uri.isNullOrEmpty()) {
            data2["imageUrl"] = uri.toString()
            data2["imageID"] = postID.toString()
        }

        val data3 = HashMap<String, Any>()
        val postTimeStamp = System.currentTimeMillis()

        data2["userID"] = authUser!!.currentUser?.email.toString()
        data2["timeStamp"] = postTimeStamp
        data2["title"] = binding.postTitleSM.text.toString()
        data2["description"] = binding.descPostSM.text.toString()

        db.collection("posts")
            .add(data2)
            .addOnSuccessListener { documentReference ->

                val data = HashMap<String, Any>()
                val posts = arrayListOf<String>()
                val postRecordID = documentReference.id.toString()

                posts.add(postRecordID)
                data["posts"] = posts

                db.collection("users")
                    .document("${authUser!!.currentUser?.email.toString()}")
                    .update("posts", FieldValue.arrayUnion(postRecordID))
                    .addOnSuccessListener { documentReference ->

                        Toast.makeText(
                            requireActivity().applicationContext,
                            "Post Created",
                            Toast.LENGTH_LONG
                        ).show()

                        binding.progressCreatePost.visibility = View.GONE
                        binding.progressTitle.visibility = View.GONE
                        userDataViewModel.getUserData(authUser!!.currentUser?.email.toString())
                        socialMediaPostsFragment = SocialMediaPostsFragment()
                        val transaction = requireActivity().supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.frame_layout, socialMediaPostsFragment, "smPostList")
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .setReorderingAllowed(true)
                            .addToBackStack("smPostList")
                            .commit()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireActivity().applicationContext,
                            "Error saving to DB",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.progressCreatePost.visibility = View.GONE
                        binding.progressTitle.visibility = View.GONE
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Error saving to DB",
                    Toast.LENGTH_LONG
                ).show()
                binding.progressCreatePost.visibility = View.GONE
                binding.progressTitle.visibility = View.GONE
            }
    }
}
