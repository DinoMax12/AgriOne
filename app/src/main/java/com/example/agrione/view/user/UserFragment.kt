package com.agrione.view.user

import android.app.Activity
import android.app.AlertDialog
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
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.agrione.R
import com.agrione.adapter.PostListUserProfileAdapter
import com.agrione.utilities.CellClickListener
import com.agrione.viewmodel.UserDataViewModel
import com.agrione.viewmodel.UserProfilePostsViewModel
import kotlinx.android.synthetic.main.fragment_user.*
import java.io.IOException
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var viewModel: UserProfilePostsViewModel
private lateinit var userDataViewModel: UserDataViewModel

class UserFragment : Fragment(), CellClickListener {
    private var param1: String? = null
    private var param2: String? = null
    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null
    private var postID: UUID? = null
    private var storageReference: StorageReference? = null
    private var bitmap: Bitmap? = null
    private var uploadProfOrBack: Int? = null

    val firebaseFirestore = FirebaseFirestore.getInstance()
    val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        viewModel = ViewModelProvider(requireActivity())[UserProfilePostsViewModel::class.java]
        userDataViewModel = ViewModelProvider(requireActivity())[UserDataViewModel::class.java]
        storageReference = FirebaseStorage.getInstance().reference
        viewModel.getAllPosts(firebaseAuth.currentUser!!.email)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.liveData1.observe(viewLifecycleOwner, Observer {
            it?.let { user -> viewModel.getAllPostsOfUser(user) }
        })

        viewModel.liveData2.observe(viewLifecycleOwner) {
            Log.d("Live Data In Frag", it.toString())
        }

        viewModel.userProfilePostsLiveData2.observe(viewLifecycleOwner) {
            Log.d("Some Part 2", it.toString())
        }

        return inflater.inflate(R.layout.fragment_user, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.title = "Profile"

        cityEditUserProfile.visibility = View.GONE

        viewModel.userProfilePostsLiveData.observe(viewLifecycleOwner) {
            Log.d("Some Part", it.toString())
        }

        userDataViewModel.userliveData.observe(viewLifecycleOwner) {
            Log.d("User Fragment", it.data.toString())
        }

        uploadProgressBarProfile.visibility = View.GONE
        uploadBackProgressProfile.visibility = View.GONE

        uploadUserBackgroundImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/* video/*" }
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
            uploadProfOrBack = 1
            Toast.makeText(requireContext(), "Uploading Background Image", Toast.LENGTH_SHORT).show()
        }

        uploadProfilePictureImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/* video/*" }
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
            uploadProfOrBack = 0
            Toast.makeText(requireContext(), "Uploading your Image", Toast.LENGTH_SHORT).show()
        }

        userDataViewModel.userliveData.observe(viewLifecycleOwner) {
            userNameUserProfileFrag.text = it.getString("name")
            userCityUserProfileFrag.text = "City: " + (it.getString("city") ?: "")

            if (it.getString("profileImage").isNullOrBlank()) {
                uploadProfilePictureImage.visibility = View.VISIBLE
            } else {
                uploadProfilePictureImage.visibility = View.GONE
                Glide.with(requireContext()).load(it.getString("profileImage")).into(userImageUserFrag)
            }

            if (it.getString("backImage").isNullOrBlank()) {
                uploadUserBackgroundImage.visibility = View.VISIBLE
            } else {
                uploadUserBackgroundImage.visibility = View.GONE
                Glide.with(requireContext()).load(it.getString("backImage")).into(userBackgroundImage)
            }

            val posts = it.get("posts") as? List<*> ?: emptyList<String>()
            userPostsCountUserProfileFrag.text = "Posts: ${posts.size}"
            userEmailUserProfileFrag.text = firebaseAuth.currentUser!!.email

            val about = it.getString("about")
            if (about.isNullOrBlank()) {
                aboutValueUserProfileFrag.visibility = View.GONE
                aboutValueEditUserProfileFrag.visibility = View.GONE
                saveBtnAboutUserProfileFrag.visibility = View.GONE
            } else {
                aboutValueUserProfileFrag.visibility = View.VISIBLE
                aboutValueUserProfileFrag.text = about
            }
        }

        imageEdit.setOnClickListener {
            uploadProfilePictureImage.visibility = View.VISIBLE
            uploadUserBackgroundImage.visibility = View.VISIBLE
            imageChecked.visibility = View.VISIBLE
            imageEdit.visibility = View.GONE
            cityEditUserProfile.setText(userCityUserProfileFrag.text.toString().removePrefix("City: "))
            cityEditUserProfile.visibility = View.VISIBLE
            aboutValueEditUserProfileFrag.setText(aboutValueUserProfileFrag.text.toString())
            aboutValueEditUserProfileFrag.visibility = View.VISIBLE
            aboutValueUserProfileFrag.visibility = View.GONE
        }

        imageChecked.setOnClickListener {
            uploadProfilePictureImage.visibility = View.GONE
            uploadUserBackgroundImage.visibility = View.GONE
            imageEdit.visibility = View.VISIBLE
            cityEditUserProfile.visibility = View.GONE
            imageChecked.visibility = View.GONE
            userDataViewModel.updateUserField(requireContext(), firebaseAuth.currentUser!!.email!!, aboutValueEditUserProfileFrag.text.toString(), cityEditUserProfile.text.toString())
            userDataViewModel.getUserData(firebaseAuth.currentUser!!.email!!)
            aboutValueEditUserProfileFrag.visibility = View.GONE
            aboutValueUserProfileFrag.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            filePath = data.data
            try {
                bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, filePath)
                if (bitmap != null) {
                    if (uploadProfOrBack == 0) {
                        uploadProgressBarProfile.visibility = View.VISIBLE
                        uploadProfilePictureImage.visibility = View.GONE
                    } else {
                        uploadBackProgressProfile.visibility = View.VISIBLE
                        uploadUserBackgroundImage.visibility = View.GONE
                    }
                    userImageUserFrag.setImageBitmap(bitmap)
                    uploadImage()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage() {
        filePath?.let {
            postID = UUID.randomUUID()
            val ref = storageReference?.child("users/${postID}")
            val uploadTask = ref?.putFile(it)
            uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    throw task.exception ?: Exception("Upload failed")
                }
                ref.downloadUrl
            })?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    uploadUserPhotos(task.result.toString(), postID)
                } else {
                    showUploadError()
                }
            }?.addOnFailureListener {
                showUploadError()
            }
        }
    }

    private fun showUploadError() {
        Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
        uploadProgressBarProfile.visibility = View.GONE
        uploadBackProgressProfile.visibility = View.GONE
        uploadUserBackgroundImage.visibility = View.VISIBLE
        uploadProfilePictureImage.visibility = View.VISIBLE
    }

    fun uploadUserPhotos(uri: String?, postID: UUID?) {
        val userRef = firebaseFirestore.collection("users").document(firebaseAuth.currentUser!!.email!!)
        if (uploadProfOrBack == 0) {
            userRef.update("profileImage", uri)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Profile Updated", Toast.LENGTH_SHORT).show()
                    uploadProgressBarProfile.visibility = View.GONE
                    imageEdit.visibility = View.VISIBLE
                    imageChecked.visibility = View.GONE
                    userImageUserFrag.setImageBitmap(bitmap)
                    userDataViewModel.getUserData(firebaseAuth.currentUser!!.email!!)
                }.addOnFailureListener {
                    uploadProgressBarProfile.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
        } else if (uploadProfOrBack == 1) {
            userRef.update("backImage", uri)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Background Updated", Toast.LENGTH_SHORT).show()
                    uploadBackProgressProfile.visibility = View.GONE
                    userBackgroundImage.setImageBitmap(bitmap)
                    imageEdit.visibility = View.VISIBLE
                    imageChecked.visibility = View.GONE
                    userDataViewModel.getUserData(firebaseAuth.currentUser!!.email!!)
                }.addOnFailureListener {
                    uploadBackProgressProfile.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed to update background", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onCellClickListener(name: String) {
        AlertDialog.Builder(activity)
            .setTitle("Your Post")
            .setMessage("Do you want to edit your post?")
            .setPositiveButton("View") { _, _ -> }
            .setNegativeButton("Delete") { _, _ ->
                userDataViewModel.deleteUserPost(firebaseAuth.currentUser!!.email!!, name)
                userDataViewModel.getUserData(firebaseAuth.currentUser!!.email!!)
                viewModel.getAllPosts(firebaseAuth.currentUser!!.email)
            }
            .setNeutralButton("Cancel", null)
            .show()

        Toast.makeText(requireContext(), "You Clicked $name", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.liveData3.observe(viewLifecycleOwner) {
            val adapter = PostListUserProfileAdapter(requireContext(), it, this)
            userProfilePostsRecycler.adapter = adapter
            userProfilePostsRecycler.layoutManager = LinearLayoutManager(requireContext())
            adapter.notifyDataSetChanged()
        }
    }
}