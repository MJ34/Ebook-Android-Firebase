package muji.dev.ebookperpusjateng.dashboard.admin

import android.widget.Filter
import muji.dev.ebookperpusjateng.dashboard.admin.categories.models.ModelPdf
import kotlin.concurrent.fixedRateTimer

class FilterPdfAdmin : Filter {
    //array list in which we wont to search
    private lateinit var filterList: ArrayList<ModelPdf>
    //adapter in which filter need to be implement
    private lateinit var adapterPdfAdmin: AdapterPdfAdmin

    //constructor
    constructor(filterList: ArrayList<ModelPdf>, adapterPdfAdmin: AdapterPdfAdmin) : super() {
        this.filterList = filterList
        this.adapterPdfAdmin = adapterPdfAdmin
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint:CharSequence? = constraint //value to search
        val results = FilterResults()
        //value to be search should not be null and not empty
        if (constraint != null && constraint.isNotEmpty()) {
            //change to upper case, or lowercase to avoid case sensivity
            constraint = constraint.toString().lowercase()
            var filteredModels = ArrayList<ModelPdf>()
            for (i in filterList.indices) {
                //validate if match
                if (filterList[i].title.lowercase().contains(constraint)) {
                    //Search value is similiar to value in list, add to filtered list
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        } else {
            // Search value is either null or empty, return all data
            results.count = filterList.size
            results.values = filterList
        }
        return results // don't miss
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        // apply filter changes
        adapterPdfAdmin.pdfArrayList = results!!.values as ArrayList<ModelPdf>

        //notify changes
        adapterPdfAdmin.notifyDataSetChanged()
    }


}