package muji.dev.ebookperpusjateng.dashboard.admin.categories

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import muji.dev.ebookperpusjateng.databinding.ActivityCategoryAddBinding

class CategoryAddActivity : AppCompatActivity() {

    private lateinit var categoryBin: ActivityCategoryAddBinding
    //Firebase auth
    private lateinit var firebaseAuth: FirebaseAuth
    //Proggres dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryBin = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(categoryBin.root)
        // Init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        // Configure progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)
        // Handle click, go back
        categoryBin.backBtn.setOnClickListener {
            onBackPressed()
        }
        //Handle click, begin upload category
        categoryBin.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private var category = ""

    private fun validateData() {
        // Get data
        category = categoryBin.categoryEt.text.toString().trim()
        // Validate data
        if (category.isEmpty()) {
            Toast.makeText(this, "Enter Category...", Toast.LENGTH_SHORT).show()
        } else {
            addCategoryFirebase()
        }
    }

    private fun addCategoryFirebase() {
        // Show progress
        progressDialog.show()
        //get timestamp
        val  timestamp = System.currentTimeMillis()
        // Setup data to add in firebase db
        val hashMap = HashMap<String, Any>() //second param is any; because the value could be of any type
        hashMap["id"] = "$timestamp" // Put in string quotes because timestamp is in double, we need in string for id
        hashMap["category"] = category
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"
        // add to firebase db: Database Root > Categories > Category Id > Category Info
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                // added successfully
                progressDialog.dismiss()
                Toast.makeText(this, "Added successfully...", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener { e->
                // Failed to add
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to add due to ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }
}