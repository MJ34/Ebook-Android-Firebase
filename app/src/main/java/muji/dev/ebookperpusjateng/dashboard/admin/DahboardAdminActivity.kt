package muji.dev.ebookperpusjateng.dashboard.admin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import muji.dev.ebookperpusjateng.MainActivity
import muji.dev.ebookperpusjateng.dashboard.admin.categories.AdapterCategory
import muji.dev.ebookperpusjateng.dashboard.admin.categories.CategoryAddActivity
import muji.dev.ebookperpusjateng.dashboard.admin.categories.PdfAddActivity
import muji.dev.ebookperpusjateng.dashboard.admin.categories.models.ModelCategory
import muji.dev.ebookperpusjateng.databinding.ActivityDahboardAdminBinding
import java.lang.Exception

class DahboardAdminActivity : AppCompatActivity() {

    private lateinit var dashboardAdminBinding: ActivityDahboardAdminBinding

    //Firebase auth
    private lateinit var firebaseAuth:FirebaseAuth
    // Arraylist to hold categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    // Adapter
    private lateinit var adapterCategory: AdapterCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dashboardAdminBinding = ActivityDahboardAdminBinding.inflate(layoutInflater)
        setContentView(dashboardAdminBinding.root)

        //Init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadCategories()

        //Seach
        dashboardAdminBinding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Called as and when user type anything
                try {
                    adapterCategory.filter.filter(s)
                } catch (e: Exception) {

                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        //Handle click, logout
        dashboardAdminBinding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        //Handle click, start add category page
        dashboardAdminBinding.addCategoryBtn.setOnClickListener {
            startActivity(Intent(this, CategoryAddActivity::class.java))
        }

        // Handle click, start add pdf page
        dashboardAdminBinding.addPdfFab.setOnClickListener {
            startActivity(Intent(this, PdfAddActivity::class.java))
        }
    }

    private fun loadCategories() {
        // Init arraylist
        categoryArrayList = ArrayList()
        // Get all categories from firebase database.. firebase db > categories
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear list before starting adding data into it
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    // Get data as model
                    val model = ds.getValue(ModelCategory::class.java)

                    //Add to arraylist
                    categoryArrayList.add(model!!)
                }
                // Setup adapter
                adapterCategory = AdapterCategory(this@DahboardAdminActivity, categoryArrayList)
                // Set adapter to recyclerview
                dashboardAdminBinding.categoriesRv.adapter = adapterCategory

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun checkUser() {
        // Get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            // Not logged in, go to main screen
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            //Logged in, get and show user info
            val email = firebaseUser.email
            // Set to textview of toolbar
            dashboardAdminBinding.subTitleTv.text = email
        }
    }
}