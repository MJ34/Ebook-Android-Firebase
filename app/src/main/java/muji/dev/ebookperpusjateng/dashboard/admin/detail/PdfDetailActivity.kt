package muji.dev.ebookperpusjateng.dashboard.admin.detail

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import muji.dev.ebookperpusjateng.MyAplication
import muji.dev.ebookperpusjateng.R
import muji.dev.ebookperpusjateng.dashboard.admin.categories.models.ModelComment
import muji.dev.ebookperpusjateng.dashboard.admin.readPdf.PdfViewActivity
import muji.dev.ebookperpusjateng.dashboard.user.adapter.AdapterComment
import muji.dev.ebookperpusjateng.databinding.ActivityPdfDetailBinding
import muji.dev.ebookperpusjateng.databinding.DialogCommenAddBinding
import muji.dev.ebookperpusjateng.utils.Constants
import java.io.FileOutputStream
import java.lang.Exception

class PdfDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfDetailBinding

    private companion object {
        const val TAG ="BOOK_DETAILS_TAG"
    }
    //book id, get from intent
    private var bookId = ""
    // get from firebase
    private var bookTitle = ""
    private var bookUrl = ""
    //will hold a boolean value false
    private var isMyFavorite = false

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    //arraylist to hold comment
    private lateinit var commentArrayList: ArrayList<ModelComment>
    //adapter to be set recyclerView
    private lateinit var adapterComment: AdapterComment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get book id from intent
        bookId = intent.getStringExtra("bookId")!!

        //init progress bar
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser != null) {
            //user is logged in, check if book is in fav or not
            checkIsFavorite()
        }

        //increment book view count, whenever this page starts
        MyAplication.incrementBookViewCount(bookId)

        loadBookDetails()
        showComments()

        //handle backButton click, goback
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //handle click, open pdf view activity
        binding.readBookBtn.setOnClickListener {
            val intent = Intent(this, PdfViewActivity::class.java)
            intent.putExtra("bookId", bookId)
            startActivity(intent)
        }

        //handle click download book/pdf
        binding.downloadBookBtn.setOnClickListener {
            // Let's check WRITE_EXTERNAL_STORAGE permission first, if granted download book, if not granted request permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onCreate: STORAGE PERMISSION is already granted")
                downloadBook()
            } else {
                Log.d(TAG, "onCreate: STORAGE PERMISSION was not granted, Let's request it")
                requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        //Handle click, add/ remove favorite
        binding.favBtn.setOnClickListener {
            // We can add only if user is loggen in
            // check if user is logged in or not
            if (firebaseAuth.currentUser == null) {
                //user not logged in, can do favorite functionality
                Toast.makeText(this, "Kamu belum login", Toast.LENGTH_SHORT).show()
            } else {
                //user is logged in, we can do favorite functionality
                if (isMyFavorite) {
                    //already in fav, remove
                    MyAplication.removeToFavorite(this, bookId)
                } else {
                    //not in fav, add
                    addToFavorite()
                }
            }
        }

        //handle click, show add comment dialog
        binding.addCommenBtn.setOnClickListener {
            //to add a comment, user must logged in, if not just show message you'r not login
            if (firebaseAuth.currentUser == null) {
                //user not logged in, can do favorite functionality
                Toast.makeText(this, "Kamu belum login", Toast.LENGTH_SHORT).show()
            } else {
                //user loggd in, allow adding komen
                addCommentDialog()
            }
        }
    }

    private fun showComments() {
        //init arraylist
        commentArrayList = ArrayList()

        //db path to load comment
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear list
                    commentArrayList.clear()
                    for (ds in snapshot.children) {
                        //get data a model be carefull of spealings and data type
                        val model = ds.getValue(ModelComment::class.java)
                        //add to list
                        commentArrayList.add(model!!)
                    }
                    //setup adapter
                    adapterComment = AdapterComment(this@PdfDetailActivity, commentArrayList)
                    //set data to recyclerView
                    binding.commentRv.adapter = adapterComment
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private var comment = ""

    private fun addCommentDialog() {
        //inflate/bind for dialog dialog.commen_add.xml
        val commenAddBinding = DialogCommenAddBinding.inflate(LayoutInflater.from(this))

        //setup alert dialog
        val builder = AlertDialog.Builder(this, R.style.CustomDialog)
        builder.setView(commenAddBinding.root)

        //create and show alert dialog
        val alertDialog = builder.create()
        alertDialog.show()

        //handle click, dismiss dialog
        commenAddBinding.backBtn.setOnClickListener {
            alertDialog.dismiss()
        }

        //handle click, and comment
        commenAddBinding.submitBtn.setOnClickListener {
            //get data
            comment = commenAddBinding.komenEt.text.toString().trim()
            //validate data
            if (comment.isEmpty()) {
                Toast.makeText(this, "Masukan Komentar...", Toast.LENGTH_SHORT).show()
            } else {
                alertDialog.dismiss()
                addComment()
            }
        }
    }

    private fun addComment() {
        //show progress
        progressDialog.setMessage("Menambahkan Komentar")
        progressDialog.show()

        //timestamp for comment id, comment timestamp etc.
        val timestamp = "${System.currentTimeMillis()}"

        //setup data to add in db for comment
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["bookId"] = "$bookId"
        hashMap["timestamp"] = "$timestamp"
        hashMap["comment"] = "$comment"
        hashMap["uid"] = "${firebaseAuth.uid}"

        //Db path to add data into it
        //Books, bookId > Comment > CommentId > commentData
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments").child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Komentar ditambahkan...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "Gagal menambahkan Komentar ${e.message}...", Toast.LENGTH_SHORT).show()
            }
    }

    private val requestStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {isGranted: Boolean ->
        // Let's check if granted or not
        if (isGranted) {
            Log.d(TAG, "onCreate: STORAGE PERMISSION is already granted")
            downloadBook()
        } else {
            Log.d(TAG, "onCreate: STORAGE PERMISSION is denied")
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadBook() {
        progressDialog.setMessage("Downloading book")
        progressDialog.show()
        //Let's download book from firebase storage using url
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        storageReference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                Log.d(TAG, "downloadsBook: Book Download...")
                saveToDownloadFolder(bytes)
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Log.d(TAG, "downloadsBook: Failed to download due to ${e.message}")
                Toast.makeText(this, "Failed to download book due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToDownloadFolder(bytes: ByteArray?) {
        Log.d(TAG, "downloadsBook: Saving Download...")

        val nameWithExtension = "${System.currentTimeMillis()}.pdf"

        try {
            val downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadFolder.mkdirs() // create folder if not exist

            val filePath = downloadFolder.path +"/"+ nameWithExtension

            val out = FileOutputStream(filePath)
            out.write(bytes)
            out.close()

            Toast.makeText(this, "Saved to Download Folder", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "saveDownloadFolder: Saved to Download Folder")
            progressDialog.dismiss()
            incrementDownloadCount()

        } catch (e: Exception) {
            progressDialog.dismiss()
            Log.d(TAG, "downloadsBook: Failed to download due to ${e.message}")
            Toast.makeText(this, "Failed to download book due to ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun incrementDownloadCount() {
        //Increment downloads count to firebase db
        Log.d(TAG, "incrementDownloadCount")

        //get previous downloads count
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get download count
                    var downloadsCount = "${snapshot.child("downloadCount").value}"
                    Log.d(TAG, "onDataChange: Current downloads count: $downloadsCount")

                    if (downloadsCount == "" || downloadsCount == "null") {
                        downloadsCount = "0"
                    }
                    //convert to long and increment
                    val newDownloadCount: Long = downloadsCount.toLong() + 1
                    Log.d(TAG, "onDataChange: new Downloads Count: $newDownloadCount")
                    //setup data to update to db
                    val hashMap: HashMap<String, Any> = HashMap()
                    hashMap["downloadCount"] = newDownloadCount
                    //update new increment downloads count to db
                    val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                    dbRef.child(bookId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener {
                            Log.d(TAG, "onDataChange: Downloads count incremented")
                        }
                        .addOnFailureListener { e->
                            Log.d(TAG, "onDataChange: Failed to incremented due to ${e.message}")
                        }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadBookDetails() {
        //Books > bookId > details
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get data
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadCount = "${snapshot.child("downloadCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    bookTitle = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    bookUrl = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"


                    //format date
                    val  date = MyAplication.formatTimestamp(timestamp.toLong())
                    //Load pdf category
                    MyAplication.loadCategory(categoryId, binding.categoryTv)
                    //Load thumbnail, page count
                    MyAplication.loadPdfFromUrlSinglePage("$bookUrl", "$bookTitle", binding.pdfView, binding.progressBar, binding.pagesTv)
                    //Load pdf size
                    MyAplication.loadPdfSize("$bookUrl", "$bookTitle", binding.sizeTv)
                    //set data
                    binding.titleTv.text = bookTitle
                    binding.descTv.text = description
                    binding.viewsTv.text = viewsCount
                    binding.downloadsTv.text = downloadCount
                    binding.dateTv.text = date

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun checkIsFavorite() {
        Log.d(TAG, "checkIsFavorite: Checking if book is in fav or not")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isMyFavorite = snapshot.exists()
                    if (isMyFavorite) {
                        //available in favorite
                            Log.d(TAG, "onDataChange: available in favorite")
                            // set drawable top icon
                        binding.favBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_white, 0, 0)
                        binding.favBtn.text = "Hapus Favorite"

                    } else {
                        //not available in favorite
                        Log.d(TAG, "onDataChange: not available in favorite")
                        // set drawable top icon
                        binding.favBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_border_white, 0, 0)
                        binding.favBtn.text = "Tambah Favorite"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun addToFavorite() {
        Log.d(TAG, "addToFavorite: Adding to fav")
        val timestamp = System.currentTimeMillis()

        //setup data to add in db
        val hashMap = HashMap<String, Any>()
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp

        //save to db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .setValue(hashMap)
            .addOnSuccessListener {
                //added to fav
                Log.d(TAG, "addToFavorite: Added to fav")
                Toast.makeText(this, "Added to favorite", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                //Failed to add to fav
                Log.d(TAG, "addToFavorite: Failed to added to fav due to ${e.message}")
                Toast.makeText(this, "Failed to added to fav due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}