package muji.dev.ebookperpusjateng.dashboard.profile

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import muji.dev.ebookperpusjateng.MyAplication
import muji.dev.ebookperpusjateng.dashboard.admin.categories.models.ModelPdf
import muji.dev.ebookperpusjateng.dashboard.admin.detail.PdfDetailActivity
import muji.dev.ebookperpusjateng.databinding.RowPdfFavoriteBinding

class AdapterPdfFavorite : RecyclerView.Adapter<AdapterPdfFavorite.HolderPdfFavorite>{

    //Context
    private val context: Context
    //Arraylist to hold books
    private var bookArrayList: ArrayList<ModelPdf>
    //view binding
    private lateinit var binding: RowPdfFavoriteBinding

    //constructor
    constructor(context: Context, bookArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.bookArrayList = bookArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfFavorite {
        //bind/inflate row_pdf_favorite
        binding = RowPdfFavoriteBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderPdfFavorite(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfFavorite, position: Int) {
        //Get data, set data, handle click etc..

        //get data: from [user > uid > favorites] we will only have ids of favorite books so we have to load their details from [Books] node
        val model = bookArrayList[position]

        loadBookDetail(model, holder)

        //handle click, open pdf details page, pass book id to load details
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", model.id) //pass book id not category id
            context.startActivity(intent)
        }

        //handle click remove from favorite
        holder.removeFavBtn.setOnClickListener {

        }
    }

    private fun loadBookDetail(model: ModelPdf, holder: AdapterPdfFavorite.HolderPdfFavorite) {
        val bookId = model.id

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get book info
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadCount = "${snapshot.child("downloadCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val title = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    //set data to model
                    model.isFavorite = true
                    model.title = title
                    model.description = description
                    model.categoryId = categoryId
                    model.timestamp = timestamp.toLong()
                    model.uid = uid
                    model.url = url
                    model.viewsCount = viewsCount.toLong()
                    model.downloadsCount = downloadCount.toLong()

                    //format date
                    val date = MyAplication.formatTimestamp(timestamp.toLong())
                    MyAplication.loadCategory("$categoryId", holder.catTv)
                    MyAplication.loadPdfFromUrlSinglePage("$url", "$title", holder.pdfView, holder.progressBar, null)
                    MyAplication.loadPdfSize("$url", "$title", holder.sizeTv)

                    holder.titleTv.text = title
                    holder.descTv.text = description
                    holder.dateTv.text = date
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun getItemCount(): Int {
        return bookArrayList.size //return size of list | number of items in list
    }

    //view holder class to manage UI views of row_pdf_favorite.xml
    inner class HolderPdfFavorite(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //Init UI
        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var removeFavBtn = binding.removeFavBtn
        var descTv = binding.descTv
        var catTv = binding.catTv
        var sizeTv = binding.sizeTv
        var dateTv = binding.dateTv

    }

}