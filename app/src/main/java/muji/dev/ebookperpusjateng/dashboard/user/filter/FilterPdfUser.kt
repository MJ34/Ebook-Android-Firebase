package muji.dev.ebookperpusjateng.dashboard.user.filter

import android.widget.Filter
import muji.dev.ebookperpusjateng.dashboard.admin.categories.models.ModelPdf
import muji.dev.ebookperpusjateng.dashboard.user.adapter.AdapterPdfUser

class FilterPdfUser: Filter {
    //arraylist in which we want to search
    var filterList: ArrayList<ModelPdf>
    //adapter in which filter need to be implement
    var adapterPdfUser: AdapterPdfUser

    constructor(filterList: ArrayList<ModelPdf>, adapterPdfUser: AdapterPdfUser) : super() {
        this.filterList = filterList
        this.adapterPdfUser = adapterPdfUser
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        //value to search
        var constraint: CharSequence? = constraint
        val result = FilterResults()
        //value to be search should not be null and not empty
        if (constraint != null && constraint.isNotEmpty()) {
            //not null nor empty

            //change to upper case, or lower case to remove case sensitivity
            constraint = constraint.toString().uppercase()
            val filteredModels = ArrayList<ModelPdf>()
            for (i in filterList.indices) {
                //validate if match
                if (filterList[i].title.uppercase().contains(constraint)) {
                    //searched value mached with title, add to list
                    filteredModels.add(filterList[i])
                }
            }
            //return filtered list and size
            result.count = filteredModels.size
            result.values = filteredModels
        } else {

            result.count = filterList.size
            result.values = filterList
        }
        return result
    }

    override fun publishResults(constraint: CharSequence, result: FilterResults) {
       //apply filter changes
        adapterPdfUser.pdfArrayList = result.values as ArrayList<ModelPdf>
    }

}