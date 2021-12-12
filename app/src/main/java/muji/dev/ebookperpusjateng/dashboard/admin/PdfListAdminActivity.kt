package muji.dev.ebookperpusjateng.dashboard.admin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import muji.dev.ebookperpusjateng.dashboard.admin.categories.models.ModelPdf
import muji.dev.ebookperpusjateng.databinding.ActivityPdfListAdminBinding
import java.lang.Exception

class PdfListAdminActivity : AppCompatActivity() {

    private lateinit var pdfBin: ActivityPdfListAdminBinding

    private companion object {
        const val TAG = "PDF_LIST_ADMIN_TAG"
    }

    //Category id, title
    private var categoryId = ""
    private var category = ""
    //arraylist to hold books
    private lateinit var pdfArrayList: ArrayList<ModelPdf>
    //adapter
    private lateinit var adapterPdfAdmin: AdapterPdfAdmin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pdfBin = ActivityPdfListAdminBinding.inflate(layoutInflater)
        setContentView(pdfBin.root)

        //Get from intent, that we passed from adapter
        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!

        // set pdf category
        pdfBin.subTitleTv.text = category

        //load pdfbooks
        loadPdfList()

        //Search
        pdfBin.searchEt.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Filter data
                try {
                    adapterPdfAdmin.filter!!.filter(s)
                } catch (e: Exception) {
                    Log.d(TAG, "onTextChange: ${e.message}")
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        //Handle click , go back
        pdfBin.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadPdfList() {
        //init arrayList
        pdfArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear list before start adding data into it
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        //get data
                        val model = ds.getValue(ModelPdf::class.java)
                        //add to list
                        if (model != null) {
                            pdfArrayList.add(model)
                            Log.d(TAG, "onDataChange: ${model.title} ${model.categoryId}")
                        }
                    }
                    //setup adapter
                    adapterPdfAdmin = AdapterPdfAdmin(this@PdfListAdminActivity, pdfArrayList)
                    pdfBin.booksRv.adapter = adapterPdfAdmin
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}