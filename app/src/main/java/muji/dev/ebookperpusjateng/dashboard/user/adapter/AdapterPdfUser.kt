package muji.dev.ebookperpusjateng.dashboard.user.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import muji.dev.ebookperpusjateng.MyAplication
import muji.dev.ebookperpusjateng.dashboard.admin.categories.models.ModelPdf
import muji.dev.ebookperpusjateng.dashboard.admin.detail.PdfDetailActivity
import muji.dev.ebookperpusjateng.dashboard.user.filter.FilterPdfUser
import muji.dev.ebookperpusjateng.databinding.RowPdfUserBinding

class AdapterPdfUser : RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser>, Filterable {

    //Context get using constructor
    private var context: Context
    //array list to hold pdf, get using contructor
    public var pdfArrayList: ArrayList<ModelPdf>
    // arraylist to hold filtered pdf
    private var filterList: ArrayList<ModelPdf>

    //viewBinding row_pdf_user => rowPdfuserBinding
    private lateinit var binding: RowPdfUserBinding

    private var filter: FilterPdfUser? = null

    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfUser {
        //Inflate/bind layout row_pdf_user
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderPdfUser(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfUser, position: Int) {
        //get data, set data, handle click etc

        //get data
        val model = pdfArrayList[position]
        val bookId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val desc = model.description
        val uid = model.uid
        val url = model.url
        val timestamp = model.timestamp
        //convert time
        val date = MyAplication.formatTimestamp(timestamp)
        //set data
        holder.titleTv.text = title
        holder.descTv.text = desc
        holder.dateTv.text = date

        MyAplication.loadPdfFromUrlSinglePage(url, title, holder.pdfView, holder.progressBar, null)//no need number of pages so pass null

        MyAplication.loadCategory(categoryId, holder.catTV)

        MyAplication.loadPdfSize(url, title, holder.sizeTv)

        //handle click, open pdf details page
        holder.itemView.setOnClickListener {
            //pass bookId in intent, that will be used to get pdf info
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", bookId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size //returm list size/ number of record
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterPdfUser(filterList, this)
        }
        return filter as FilterPdfUser
    }

    //View holder class row_pdf_user
    inner class HolderPdfUser(itemView: View): RecyclerView.ViewHolder(itemView) {
        //Init UI component of row_pdf_user
        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var descTv = binding.descTv
        var catTV = binding.catTv
        var sizeTv = binding.sizeTv
        var dateTv = binding.dateTv
    }


}