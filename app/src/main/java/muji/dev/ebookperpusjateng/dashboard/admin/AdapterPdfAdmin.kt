package muji.dev.ebookperpusjateng.dashboard.admin

import android.app.AlertDialog
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
import muji.dev.ebookperpusjateng.databinding.RowPdfAdminBinding

class AdapterPdfAdmin : RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin>, Filterable {

    private lateinit var rowPdfBin: RowPdfAdminBinding
    //context
    private var context: Context
    //ArrayList to hold pdf
    public var pdfArrayList: ArrayList<ModelPdf>
    public val filterList: ArrayList<ModelPdf>
    //filter object
    private var filter: FilterPdfAdmin? = null

    //Constructor
    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {
        //bind inflate layout row_pdf_admin
        rowPdfBin = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderPdfAdmin(rowPdfBin.root)
    }

    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {
        /*Get data, set data;handle click*/
        val model = pdfArrayList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val pdfUrl = model.url
        val timestamp = model.timestamp
        //convert timestamp to dd/MM/yyyy
        val formattedDate = MyAplication.formatTimestamp(timestamp)
        //set data
        holder.titleTv.text = title
        holder.descTv.text = description
        holder.dateTv.text = formattedDate
        //load further details like category, pdf from url, pdf size

        //load category
        MyAplication.loadCategory(categoryId, holder.catTv)
        //we don't need page number here, pas null for page number || load pdf thumbail
        MyAplication.loadPdfFromUrlSinglePage(pdfUrl, title, holder.pdfView, holder.progressBar, null)
        //load pdf size
        MyAplication.loadPdfSize(pdfUrl, title, holder.sizeTv)

        // Handle click, show dialog with options 1) Edit Book 2) Delete Book
        holder.moreTv.setOnClickListener {
            moreOptionsDialog(model, holder)
        }
        // handle item click, open PdfDetailsActivity
        holder.itemView.setOnClickListener {
            //intent with bookId
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", pdfId) //will be used to load book details
            context.startActivity(intent)
        }
    }

    private fun moreOptionsDialog(model: ModelPdf, holder: AdapterPdfAdmin.HolderPdfAdmin) {
        //get id,url, title of books
        val bookId = model.id
        val bookUrl = model.url
        val bookTitle = model.title
        //options to show in dialog
        val options = arrayOf("Edit", "Delete")
        //alert dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Option")
            .setItems(options){dialog, position->
                //handle item click
                if (position==0) {
                    //Edit is clicked
                    val intent = Intent(context, PdfEditActivity::class.java)
                    intent.putExtra("bookId", bookId) //passed bookId, will be used to edit book
                    context.startActivity(intent)
                } else if (position == 1) {
                    // Delete is clicked

                    // show confirmation dialog first if you need...
                    MyAplication.deleteBook(context, bookId, bookUrl, bookTitle)

                }
            }
            .show()
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size //items count
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterPdfAdmin(filterList, this)
        }
        return filter as FilterPdfAdmin
    }

    /*
    Views holder class for row_pdf_admin.xml
     */
    inner class HolderPdfAdmin(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //UI Views of row_pdf_admin
        val pdfView = rowPdfBin.pdfView
        val progressBar = rowPdfBin.progressBar
        val titleTv = rowPdfBin.titleTv
        val descTv = rowPdfBin.descTv
        val catTv = rowPdfBin.catTv
        val sizeTv = rowPdfBin.sizeTv
        val dateTv = rowPdfBin.dateTv
        val moreTv = rowPdfBin.moreBtn
    }

}