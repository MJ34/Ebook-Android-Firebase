package muji.dev.ebookperpusjateng.dashboard.user

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import muji.dev.ebookperpusjateng.dashboard.admin.categories.models.ModelPdf
import muji.dev.ebookperpusjateng.dashboard.user.adapter.AdapterPdfUser
import muji.dev.ebookperpusjateng.databinding.FragmentBooksUserBinding
import java.lang.Exception

class BooksUserFragment : Fragment {

    //view binding fragment books user
    private lateinit var binding: FragmentBooksUserBinding

    public companion object {
        private const val TAG = "BOOKS_USER_TAG"

        //receive data from activity to load books e.g. categoryId, category, id
        public fun newInstance(categoryId: String, category: String, uid: String): BooksUserFragment {
            val fragment = BooksUserFragment()
            //put data to bundle intent
            val args = Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid = ""
    //arrayList to hold pdf
    private lateinit var pdfArrayList: ArrayList<ModelPdf>
    private lateinit var adapterPdfUser: AdapterPdfUser

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //get arguments that we passed in newInstance method
        val args = arguments
        if (args != null) {
            categoryId = args.getString("categoryId")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBooksUserBinding.inflate(LayoutInflater.from(context), container, false)

        //load pdf according to category, tis fragment will have new instance to load each category pdf
        Log.d(TAG, "onCreateView: Category: $category")
        if (category == "All") {
            //load all books
            loadAllBooks()
        } else if (category == "Most Viewed") {
            //load most download books
            loadMostViewDownloadedBooks("viewsCount")
        } else if (category == "Most Downloaded") {
            //load most download books
            loadMostViewDownloadedBooks("downloadCount")
        } else {
            //load selected category books
            loadCatagorizedBooks()
        }

        //search
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterPdfUser.filter.filter(s)
                } catch (e: Exception) {
                    Log.d(TAG, "onTextChanged: Search Exception: ${e.message}")
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        } )

        return binding.root
    }

    private fun loadAllBooks() {
        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before starting adding data into it
                pdfArrayList.clear()
                for (ds in snapshot.children) {
                    //get data
                    val model = ds.getValue(ModelPdf::class.java)
                    //add to list
                    pdfArrayList.add(model!!)
                }
                //setup adapter
                adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                //set adapter to recyclerview
                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun loadMostViewDownloadedBooks(orderBy: String) {
        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(orderBy).limitToLast(10)
            .addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before starting adding data into it
                pdfArrayList.clear()
                for (ds in snapshot.children) {
                    //get data
                    val model = ds.getValue(ModelPdf::class.java)
                    //add to list
                    pdfArrayList.add(model!!)
                }
                //setup adapter
                adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                //set adapter to recyclerview
                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }


    private fun loadCatagorizedBooks() {
        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear list before starting adding data into it
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        //get data
                        val model = ds.getValue(ModelPdf::class.java)
                        //add to list
                        pdfArrayList.add(model!!)
                    }
                    //setup adapter
                    adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                    //set adapter to recyclerview
                    binding.booksRv.adapter = adapterPdfUser
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

}