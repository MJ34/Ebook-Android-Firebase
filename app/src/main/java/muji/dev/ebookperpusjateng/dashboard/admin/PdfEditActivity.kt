package muji.dev.ebookperpusjateng.dashboard.admin

import android.app.AlertDialog
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import muji.dev.ebookperpusjateng.databinding.ActivityPdfEditBinding

class PdfEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPdfEditBinding

    private companion object {
        private const val TAG = "PDF_EDIT_TAG"
    }
    //book id get from intent started from adapterpdfadmin
    private var bookId = ""
    //progress dialog
    private lateinit var progressDialog: ProgressDialog
    //arraylist to hold category title
    private lateinit var categoryTitleArrayList: ArrayList<String>
    //arraylist to hold category ids
    private lateinit var categoryIdArrayList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId")!!

        //Setup progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait..")
        progressDialog.setCanceledOnTouchOutside(false)

        loadCategories()
        loadBookInfo()

        //handle click, go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        //handle click, pick category
        binding.catTv.setOnClickListener {
            categoryDialog()
        }
        //handle click, begin update
        binding.submitBtn.setOnClickListener {
            validateData()
        }

    }

    private fun loadBookInfo() {
        Log.d(TAG, "loadBookInfo: Loading book info")

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // get book info
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val title = snapshot.child("title").value.toString()
                    //set to views
                    binding.titleEt.setText(title)
                    binding.descEt.setText(description)
                    // load book category info using categoryId
                    Log.d(TAG, "onDataChange: Loading book category info")
                    val refBookCategory = FirebaseDatabase.getInstance().getReference("Categories")
                    refBookCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                //get Category
                                val category = snapshot.child("category").value
                                // set to textview
                                binding.catTv.text = category.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private var title = ""
    private var description = ""

    private fun validateData() {
        //get data
        title = binding.titleEt.text.toString().trim()
        description = binding.descEt.text.toString().trim()
        //validate data
        if (title.isEmpty()) {
            Toast.makeText(this, "Enter title", Toast.LENGTH_SHORT).show()
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Enter description", Toast.LENGTH_SHORT).show()
        } else if (selectedCatagoryTitle.isEmpty()) {
            Toast.makeText(this, "Pick category", Toast.LENGTH_SHORT).show()
        } else {
            updatePdf()
        }
    }

    private fun updatePdf() {
        Log.d(TAG, "updatePdf: Starting updating pdf info...")
        //show progress
        progressDialog.setMessage("Updating book info")
        progressDialog.show()
        //Setup data to update to db, spellings of keys must be some as in firebase
        val hashMap = HashMap<String, Any>()
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        //start updating
        val  ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.d(TAG, "updatePdf: Updated successfully...")
                Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                Log.d(TAG, "Failed to update due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to update due to ${e.message}", Toast.LENGTH_SHORT).show()

            }
    }

    private var selectedCategoryId = ""
    private var selectedCatagoryTitle = ""

    private fun categoryDialog() {
        // Show dialog to pick the category of pdf/book, we already get the categories

        // make string array from arraylost of string
        val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)
        for (i in categoryTitleArrayList.indices) {
            categoriesArray[i] = categoryTitleArrayList[i]
        }
        //alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Category")
            .setItems(categoriesArray){dialog, position->
                //handle click, save clicked category id and title
                selectedCategoryId = categoryIdArrayList[position]
                selectedCatagoryTitle = categoryTitleArrayList[position]
                //set to textview
                binding.catTv.text = selectedCatagoryTitle
            }
            .show() //show dialog
    }

    private fun loadCategories() {
        Log.d(TAG, "loadCategories: loading categories...")

        categoryTitleArrayList = ArrayList()
        categoryIdArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before starting adding data into then
                categoryIdArrayList.clear()
                categoryTitleArrayList.clear()

                for (ds in snapshot.children) {
                    val id = "${ds.child("id").value}"
                    val category = "${ds.child("category").value}"

                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(category)

                    Log.d(TAG, "onDataChange: Category ID $id")
                    Log.d(TAG, "onDataChange: Category $category")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}