package com.example.firebasestorageexample

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null
    private var firebaseStorage: FirebaseStorage? = null
    private var storageReference: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseStorage = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        btn_choose.setOnClickListener{ launchGallery() }
        btn_upload.setOnClickListener{ uploadImage() }
        btn_show.setOnClickListener{
            val i = Intent(this, ShowImageActivity::class.java)
            startActivity(i)
        }
    }

    private fun uploadImage() {
        if (filePath != null){
            val ref = storageReference?.child("Uploads/" + UUID.randomUUID().toString())
            val uploadTask = ref?.putFile(filePath!!)

            val urlTask = uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>>{
                if (!it.isSuccessful){
                    Toast.makeText(this, "Upload Failed", Toast.LENGTH_SHORT).show()
                    it.exception?.let { throw it }
                }
                return@Continuation ref.downloadUrl
            }) ?.addOnCompleteListener{
                    if (it.isSuccessful){
                        val downloadUri = it.result
                        Toast.makeText(this, "Upload Success", Toast.LENGTH_SHORT).show()
                        addUploadRecordToDb(downloadUri.toString())
                    }else {
                        Toast.makeText(this, "Upload Failed", Toast.LENGTH_SHORT).show()
                    }
            }?.addOnFailureListener{

            }
        }
    }

    private fun addUploadRecordToDb(uri: String) {
        val db = FirebaseFirestore.getInstance()

        val data = HashMap<String, Any>()
        data["imageUrl"] = uri
        db.collection("posts")
            .add(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Upload to DB Success", Toast.LENGTH_SHORT).show()
                val i = Intent(this, ShowImageActivity::class.java)
                startActivity(i)
            }
            .addOnFailureListener{

            }
    }

    private fun launchGallery() {
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(i, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK){
            if (data == null || data.data == null){
                return
            }
            filePath = data.data
            try{
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                img_preview.setImageBitmap(bitmap)
            } catch (e: IOException){
                e.printStackTrace()
            }
        }
    }
}
