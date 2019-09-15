package com.example.firebasestorageexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_show_image.*
import kotlinx.android.synthetic.main.item_image.view.*

class ShowImageActivity : AppCompatActivity() {

    lateinit var firebaseFirestore : FirebaseFirestore
    var adapter : FirestoreRecyclerAdapter<MImage, ImageHolder>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_image)

        firebaseFirestore = FirebaseFirestore.getInstance()
        getImageList()
    }

    private fun getImageList() {
        val query = firebaseFirestore.collection("posts")
        val response = FirestoreRecyclerOptions.Builder<MImage>()
            .setQuery(query, MImage::class.java).build()
        adapter = object : FirestoreRecyclerAdapter<MImage, ImageHolder>(response) {
            override fun onCreateViewHolder(parent: ViewGroup, ViewType: Int): ImageHolder
            {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_image, parent, false)
                return ImageHolder(view)
            }
            override fun onBindViewHolder(holder: ImageHolder, pos: Int, data: MImage) {
                if (holder != null) {
                    Glide.with(applicationContext).load(data?.imageUrl).into(holder.img_item)
                }
            }
        }
        adapter?.notifyDataSetChanged()
        rv_image.layoutManager = LinearLayoutManager(this)
        rv_image.adapter = adapter
    }
    inner class ImageHolder (override val containerView: View):
        RecyclerView.ViewHolder(containerView), LayoutContainer {
        val img_item = containerView.img_item
    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }
}