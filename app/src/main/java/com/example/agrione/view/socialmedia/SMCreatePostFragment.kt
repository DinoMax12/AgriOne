package com.project.agrione.view.socialmedia

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
import com.project.agrione.R
import com.project.agrione.viewmodel.UserDataViewModel
import com.project.agrione.viewmodel.UserProfilePostsViewModel
import kotlinx.android.synthetic.main.fragment_s_m_create_post.*
import kotlinx.android.synthetic.main.nav_header.view.*
import java.io.IOException
import java.util.*

class SMCreatePostFragment : Fragment() {

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
        return inflater.inflate(R.layout.fragment_s_m_create_post, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SMCreatePostFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.title = "Social Media"

        progress_create_post.visibility = View.GONE
        progressTitle.visibility = View.GONE

        data2["uploadType"] = ""
        uploadImagePreview.setOnClickListener {
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
                    Log.d("Google User", data!!.getString("name"))
                }
        } else {
            data2["name"] = googleLoggedUser.toString()
            Log.d("Normal User", googleLoggedUser)
        }

        createPostBtnSM.setOnClickListener {

            if (postTitleSM.text.toString().isNullOrEmpty()) {
                Toast.makeText(
                    activity!!.applicationContext,
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
            uploadImagePreview.setImageURI(filePath)
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
                bitmap = MediaStore.Images.Media.getBitmap(activity!!.contentResolver, filePath)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage() {
        progress_create_post.visibility = View.VISIBLE
        progressTitle.visibility = View.VISIBLE
        if (filePath != null) {
            postID = UUID.randomUUID()
            val ref = storageReference?.child("posts/" + postID.toString())
            val uploadTask = ref?.putFile(filePath!!)

            val urlTask =
                uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                            progress_create_post.visibility = View.GONE
                            progressTitle.visibility = View.GONE
                        }
                    }
                    return@Continuation ref.downloadUrl
                })?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        addUploadRecordWithImageToDb(downloadUri.toString(), postID!!)
                    } else {
                        progress_create_post.visibility = View.GONE
                        progressTitle.visibility = View.GONE
                    }
                }?.addOnFailureListener {
                    progress_create_post.visibility = View.GONE
                    progressTitle.visibility = View.GONE
                    Toast.makeText(activity!!.applicationContext, it.message, Toast.LENGTH_LONG).show()
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
        data2["title"] = postTitleSM.text.toString()
        data2["description"] = descPostSM.text.toString()

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
                            activity!!.applicationContext,
                            "Post Created",
                            Toast.LENGTH_LONG
                        ).show()

                        progress_create_post.visibility = View.GONE
                        progressTitle.visibility = View.GONE
                        userDataViewModel.getUserData(authUser!!.currentUser?.email.toString())
                        socialMediaPostsFragment = SocialMediaPostsFragment()
                        val transaction = activity!!.supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.frame_layout, socialMediaPostsFragment, "smPostList")
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .setReorderingAllowed(true)
                            .addToBackStack("smPostList")
                            .commit()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            activity!!.applicationContext,
                            "Error saving to DB",
                            Toast.LENGTH_LONG
                        ).show()
                        progress_create_post.visibility = View.GONE
                        progressTitle.visibility = View.GONE
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    activity!!.applicationContext,
                    "Error saving to DB",
                    Toast.LENGTH_LONG
                ).show()
                progress_create_post.visibility = View.GONE
                progressTitle.visibility = View.GONE
            }
    }
}
