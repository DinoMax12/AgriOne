package com.example.agrione.view.user

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
import com.example.agrione.R
import com.example.agrione.adapter.PostListUserProfileAdapter
import com.example.agrione.utilities.CellClickListener
import com.example.agrione.viewmodel.UserDataViewModel
import com.example.agrione.viewmodel.UserProfilePostsViewModel
import com.example.agrione.databinding.FragmentUserBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var viewModel: UserProfilePostsViewModel
private lateinit var userDataViewModel: UserDataViewModel

class UserFragment : Fragment(), CellClickListener {
    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

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

        // Check if user is logged in before accessing email
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            viewModel.getAllPosts(currentUser.email!!)
        } else {
            // Handle case when user is not logged in
            Toast.makeText(requireContext(), "Please log in to view profile", Toast.LENGTH_SHORT).show()
            // Navigate back or to login screen
            requireActivity().onBackPressed()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

        binding.cityEditUserProfile.visibility = View.GONE

        viewModel.userProfilePostsLiveData.observe(viewLifecycleOwner) {
            Log.d("Some Part", it.toString())
        }

        userDataViewModel.userliveData.observe(viewLifecycleOwner) {
            Log.d("User Fragment", it.data.toString())
        }

        binding.uploadProgressBarProfile.visibility = View.GONE
        binding.uploadBackProgressProfile.visibility = View.GONE

        binding.uploadUserBackgroundImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/* video/*" }
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
            uploadProfOrBack = 1
            Toast.makeText(requireContext(), "Uploading Background Image", Toast.LENGTH_SHORT).show()
        }

        binding.uploadProfilePictureImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/* video/*" }
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
            uploadProfOrBack = 0
            Toast.makeText(requireContext(), "Uploading your Image", Toast.LENGTH_SHORT).show()
        }

        userDataViewModel.userliveData.observe(viewLifecycleOwner) {
            binding.userNameUserProfileFrag.text = it.getString("name")
            binding.userCityUserProfileFrag.text = "City: " + (it.getString("city") ?: "")

            if (it.getString("profileImage").isNullOrBlank()) {
                binding.uploadProfilePictureImage.visibility = View.VISIBLE
            } else {
                binding.uploadProfilePictureImage.visibility = View.GONE
                Glide.with(requireContext()).load(it.getString("profileImage")).into(binding.userImageUserFrag)
            }

            if (it.getString("backImage").isNullOrBlank()) {
                binding.uploadUserBackgroundImage.visibility = View.VISIBLE
            } else {
                binding.uploadUserBackgroundImage.visibility = View.GONE
                Glide.with(requireContext()).load(it.getString("backImage")).into(binding.userBackgroundImage)
            }

            val posts = it.get("posts") as? List<*> ?: emptyList<String>()
            binding.userPostsCountUserProfileFrag.text = "Posts: ${posts.size}"
            
            // Safely access current user email
            val currentUser = firebaseAuth.currentUser
            binding.userEmailUserProfileFrag.text = currentUser?.email ?: "Not logged in"

            val about = it.getString("about")
            if (about.isNullOrBlank()) {
                binding.aboutValueUserProfileFrag.visibility = View.GONE
                binding.aboutValueEditUserProfileFrag.visibility = View.GONE
                binding.saveBtnAboutUserProfileFrag.visibility = View.GONE
            } else {
                binding.aboutValueUserProfileFrag.visibility = View.VISIBLE
                binding.aboutValueUserProfileFrag.text = about
            }
        }

        binding.imageEdit.setOnClickListener {
            binding.uploadProfilePictureImage.visibility = View.VISIBLE
            binding.uploadUserBackgroundImage.visibility = View.VISIBLE
            binding.imageChecked.visibility = View.VISIBLE
            binding.imageEdit.visibility = View.GONE
            binding.cityEditUserProfile.setText(binding.userCityUserProfileFrag.text.toString().removePrefix("City: "))
            binding.cityEditUserProfile.visibility = View.VISIBLE
            binding.aboutValueEditUserProfileFrag.setText(binding.aboutValueUserProfileFrag.text.toString())
            binding.aboutValueEditUserProfileFrag.visibility = View.VISIBLE
            binding.aboutValueUserProfileFrag.visibility = View.GONE
        }

        binding.imageChecked.setOnClickListener {
            binding.uploadProfilePictureImage.visibility = View.GONE
            binding.uploadUserBackgroundImage.visibility = View.GONE
            binding.imageEdit.visibility = View.VISIBLE
            binding.cityEditUserProfile.visibility = View.GONE
            binding.imageChecked.visibility = View.GONE
            
            // Safely access current user email
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                coroutineScope.launch {
                    try {
                        userDataViewModel.updateUserField(
                            requireContext(),
                            currentUser.email!!,
                            binding.aboutValueEditUserProfileFrag.text.toString(),
                            binding.cityEditUserProfile.text.toString()
                        )
                        userDataViewModel.getUserData(currentUser.email!!)
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Error updating user data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please log in to update profile", Toast.LENGTH_SHORT).show()
            }
            
            binding.aboutValueEditUserProfileFrag.visibility = View.GONE
            binding.aboutValueUserProfileFrag.visibility = View.VISIBLE
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
                        binding.uploadProgressBarProfile.visibility = View.VISIBLE
                        binding.uploadProfilePictureImage.visibility = View.GONE
                    } else {
                        binding.uploadBackProgressProfile.visibility = View.VISIBLE
                        binding.uploadUserBackgroundImage.visibility = View.GONE
                    }
                    binding.userImageUserFrag.setImageBitmap(bitmap)
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
        binding.uploadProgressBarProfile.visibility = View.GONE
        binding.uploadBackProgressProfile.visibility = View.GONE
        binding.uploadUserBackgroundImage.visibility = View.VISIBLE
        binding.uploadProfilePictureImage.visibility = View.VISIBLE
    }

    fun uploadUserPhotos(uri: String?, postID: UUID?) {
        val userRef = firebaseFirestore.collection("users").document(firebaseAuth.currentUser!!.email!!)
        if (uploadProfOrBack == 0) {
            userRef.update("profileImage", uri)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Profile Updated", Toast.LENGTH_SHORT).show()
                    binding.uploadProgressBarProfile.visibility = View.GONE
                    binding.imageEdit.visibility = View.VISIBLE
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                    binding.uploadProgressBarProfile.visibility = View.GONE
                }
        } else {
            userRef.update("backImage", uri)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Background Updated", Toast.LENGTH_SHORT).show()
                    binding.uploadBackProgressProfile.visibility = View.GONE
                    binding.imageEdit.visibility = View.VISIBLE
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to update background", Toast.LENGTH_SHORT).show()
                    binding.uploadBackProgressProfile.visibility = View.GONE
                }
        }
    }

    override fun onCellClickListener(name: String) {
        AlertDialog.Builder(activity)
            .setTitle("Your Post")
            .setMessage("Do you want to edit your post?")
            .setPositiveButton("View") { _, _ -> }
            .setNegativeButton("Delete") { _, _ ->
                coroutineScope.launch {
                    try {
                        userDataViewModel.deleteUserPost(firebaseAuth.currentUser!!.email!!, name)
                        userDataViewModel.getUserData(firebaseAuth.currentUser!!.email!!)
                        viewModel.getAllPosts(firebaseAuth.currentUser!!.email)
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Error deleting post: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNeutralButton("Cancel", null)
            .show()

        Toast.makeText(requireContext(), "You Clicked $name", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.liveData3.observe(viewLifecycleOwner) {
            val adapter = PostListUserProfileAdapter(requireContext(), it, this)
            binding.userProfilePostsRecycler.adapter = adapter
            binding.userProfilePostsRecycler.layoutManager = LinearLayoutManager(requireContext())
            adapter.notifyDataSetChanged()
        }
    }
}


