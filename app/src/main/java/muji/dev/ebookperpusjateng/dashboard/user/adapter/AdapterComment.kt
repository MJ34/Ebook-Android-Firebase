package muji.dev.ebookperpusjateng.dashboard.user.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import muji.dev.ebookperpusjateng.MyAplication
import muji.dev.ebookperpusjateng.R
import muji.dev.ebookperpusjateng.dashboard.admin.categories.models.ModelComment
import muji.dev.ebookperpusjateng.databinding.RowKomenBinding
import java.lang.Exception

class AdapterComment : RecyclerView.Adapter<AdapterComment.HolderComment> {
    //context
    val context: Context

    //Arraylist to hold comment
    val commentArrayList: ArrayList<ModelComment>

    private lateinit var binding: RowKomenBinding


    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    constructor(context: Context, commentArrayList: ArrayList<ModelComment>) {
        this.context = context
        this.commentArrayList = commentArrayList

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComment {
        //bind/inflate row_comment
        binding = RowKomenBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderComment(binding.root)
    }

    override fun onBindViewHolder(holder: HolderComment, position: Int) {
        // get data, set data, handle click and etc..

        //get data
        val model = commentArrayList[position]

        val id = model.id
        val bookId = model.bookId
        val comment = model.comment
        val uid = model.uid
        val timestamp = model.timestamp

        //format timestamp
        val date = MyAplication.formatTimestamp(timestamp.toLong())

        //set data
        holder.dateTv.text = date
        holder.commentTv.text = comment

        //we don't have user name, profile picture, but we have user uid, so we will load using that uid
        loadUserDetails(model, holder)

        //handle click, show dialog to delete comment
        holder.itemView.setOnClickListener {
            /*
            Requirement to delete a comment
            1. user must be logged
            2. uid in comment (to be delete) must be some as uid for current user i.e user can delete only his own comment
             */
            if (firebaseAuth.currentUser != null && firebaseAuth.uid == uid) {
                deleteCommentDialog(model, holder)
            }
        }

    }

    private fun deleteCommentDialog(model: ModelComment, holder: AdapterComment.HolderComment) {
        //alert dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Hapus Komentar")
            .setMessage("Apakah kamu yakin akan menghapus komentar ini?")
            .setPositiveButton("Hapus") {d, e->

                val bookId = model.bookId
                val commentId = model.id

                //delete komentar
                val ref = FirebaseDatabase.getInstance().getReference("Books")
                ref.child(bookId).child("Comments").child(commentId)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Komentar dihapus...", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e->
                        Toast.makeText(context, "Gagal hapus komentar karena..${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel") {d, e->
                d.dismiss()
            }
            .show()
    }

    private fun loadUserDetails(model: ModelComment, holder: AdapterComment.HolderComment) {
        var uid = model.uid
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get name, profile image
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"

                    //set data
                    holder.nameTv.text = name
                    try {
                        Glide.with(context)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(holder.profileTv)
                    } catch (e: Exception) {
                        //in case Image is empty or null, set default image
                        holder.profileTv.setImageResource(R.drawable.ic_person_gray)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun getItemCount(): Int {
        return commentArrayList.size
    }

    //ViewHolder class for row_komen
    inner class HolderComment(itemView: View): RecyclerView.ViewHolder(itemView) {
        //init ui views of row komen
        val profileTv = binding.profileTv
        val nameTv = binding.namaTv
        val dateTv = binding.dateTv
        val commentTv = binding.komenTv
    }
}