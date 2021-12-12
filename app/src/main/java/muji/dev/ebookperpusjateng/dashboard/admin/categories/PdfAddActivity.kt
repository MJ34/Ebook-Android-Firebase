package muji.dev.ebookperpusjateng.dashboard.admin.categories

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import muji.dev.ebookperpusjateng.dashboard.admin.categories.models.ModelCategory
import muji.dev.ebookperpusjateng.databinding.ActivityPdfAddBinding

class PdfAddActivity : AppCompatActivity() {

    private lateinit var pdfAddBinding: ActivityPdfAddBinding
    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth
    //Progresdialog (show uploading pdf)
    private lateinit var progresDialog: ProgressDialog
    //Arraylist to hold pdf category
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    //uri of picked pdf
    private var pdfUri: Uri? = null
    //TAG
    private val TAG = "PDF_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pdfAddBinding = ActivityPdfAddBinding.inflate(layoutInflater)
        setContentView(pdfAddBinding.root)

        //Init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        loadPdfCategories()
        //Setup progress dialog
        progresDialog = ProgressDialog(this)
        progresDialog.setTitle("Please wait...")
        progresDialog.setCanceledOnTouchOutside(false)
        //handle click, go back
        pdfAddBinding.backBtn.setOnClickListener {
            onBackPressed()
        }
        //handle click, show category
        pdfAddBinding.catTv.setOnClickListener {
            categoryPickDialog()
        }
        //handle click, pick pdf intent
        pdfAddBinding.attackPdfBtn.setOnClickListener {
            pdfPickIntent()
        }
        //handle click,
        pdfAddBinding.submitBtn.setOnClickListener {
            /*
            1) validate data
            2) upload pdf to firebase storage
            3) get url of upload pdf
            4) upload pdf info to firebase db
             */

            validateData()
        }
    }

    private var title = ""
    private var description = ""
    private var category = ""

    private fun validateData() {
        // 1) validate data
        Log.d(TAG, "validateData: validating data")
        // Get data
        title = pdfAddBinding.titleEt.text.toString().trim()
        description = pdfAddBinding.descEt.text.toString().trim()
        category = pdfAddBinding.catTv.text.toString().trim()

        // cek data valid or empty
        if (title.isEmpty()) {
            Toast.makeText(this, "Enter title...", Toast.LENGTH_SHORT).show()
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Enter Deskripsi...", Toast.LENGTH_SHORT).show()
        } else if (category.isEmpty()) {
            Toast.makeText(this, "Enter Catagory...", Toast.LENGTH_SHORT).show()
        } else if (pdfUri == null) {
            Toast.makeText(this, "Pick Category...", Toast.LENGTH_SHORT).show()
        } else {
            //data validated begin upload
            uploadPdfToStorage()
        }
    }

    private fun uploadPdfToStorage() {
        // upload pdf to firebase storage
        Log.d(TAG, "uploadPdfToStorage: uploading to storage...")
        //Show progress dialog
        progresDialog.setMessage("Uploading PDF...")
        progresDialog.show()
        //Timestamp
        val timestamp = System.currentTimeMillis()
        //Path of pdf in firebase storage
        val filePathAndName = "Books/$timestamp"
        //Storage reference
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "uploadPdfToStorage: PDF uploaded now getting url...")
                // 3) get url of upload pdf
                val urlTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!urlTask.isSuccessful);
                val uploadedPdfUrl = "${urlTask.result}"

                uploadPdfInfoToDb(uploadedPdfUrl, timestamp)
            }
            .addOnFailureListener {e->
                Log.d(TAG, "uploadedPdfToStorage: failed to upload due to ${e.message}")
                progresDialog.dismiss()
                Toast.makeText(this, "Failed to upload due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadPdfInfoToDb(uploadedPdfUrl: String, timestamp: Long) {
        // 4) upload pdf info to firebase db
        Log.d(TAG, "uploadPdfToDb: uploading to db..")
        progresDialog.setMessage("Uploading pdf info..")
        //uid of current user
        val uid = firebaseAuth.uid
        //Setup data to upload
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["url"] = "$uploadedPdfUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewsCount"] = 0
        hashMap["downloadCount"] = 0

        //db references DB > Books > BookId > (Book info)
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "uploadPdfToDb: uploaded to db...")
                progresDialog.dismiss()
                Toast.makeText(this, "Upload...", Toast.LENGTH_SHORT).show()
                pdfUri = null
            }
            .addOnFailureListener { e->
                Log.d(TAG, "uploadedPdfToDb: failed to upload due to ${e.message}")
                progresDialog.dismiss()
                Toast.makeText(this, "Failed to upload due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Loading pdf categories")
        //init arraylist
        categoryArrayList = ArrayList()
        //db reference to load categories Pdf > Categories
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before adding data
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    //get data
                    val model = ds.getValue(ModelCategory::class.java)
                    //add a arraylist
                    categoryArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: Showing pdf category pick dialog")
        //get string array of categories from array list
        val categoriesArrayList = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices) {
            categoriesArrayList[i] = categoryArrayList[i].category
        }
        //Alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            .setItems(categoriesArrayList){dialog, which ->
                //Handle item click
                //get clicked item
                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].id
                //Set category of textview
                pdfAddBinding.catTv.text = selectedCategoryTitle

                Log.d(TAG, "categoryPick Dialog: Selected Category ID: $selectedCategoryId")
                Log.d(TAG, "categoryPick Dialog: Selected Category Title: $selectedCategoryTitle")

            }
            .show()
    }

    private fun pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }

    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "PDF Picked")
                pdfUri = result.data!!.data
            } else {
                Log.d(TAG, "PDF Pick Cancelled")
                Toast.makeText(this, "Cancelled..", Toast.LENGTH_SHORT).show()
            }
        }
    )
}