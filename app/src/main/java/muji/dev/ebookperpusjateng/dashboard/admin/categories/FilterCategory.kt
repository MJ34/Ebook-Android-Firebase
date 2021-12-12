package muji.dev.ebookperpusjateng.dashboard.admin.categories

import android.widget.Filter
import muji.dev.ebookperpusjateng.dashboard.admin.categories.models.ModelCategory

class FilterCategory: Filter {

    //Arraylist in which we want to search
    private lateinit var  filterList: ArrayList<ModelCategory>
    //Adapter in which filter need to be implement
    private lateinit var adapterCategory: AdapterCategory
    //Constructor
    constructor(filterList: ArrayList<ModelCategory>, adapterCategory: AdapterCategory) : super() {
        this.filterList = filterList
        this.adapterCategory = adapterCategory
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val result = FilterResults()

        //Value should not be null and not empty
        if (constraint != null && constraint.isNotEmpty()) {
            //Searched value is not null not empty

            //Change to upper case, or lower case to avoid case sensitivity
            constraint = constraint.toString().uppercase()
            val filterModels:ArrayList<ModelCategory> = ArrayList()
            for (i in 0 until filterList.size) {
                //validate
                if (filterList[i].category.uppercase().contains(constraint)) {
                    //add a filtered list
                    filterModels.add(filterList[i])
                }
            }
            result.count = filterModels.size
            result.values = filterModels
        } else {
            //Search value in either null or empty
            result.count = filterList.size
            result.values = filterList
        }
        return result // don't miss it
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        // Apply filter changes
        adapterCategory.categoryArrayList = results.values as ArrayList<ModelCategory>
        //notify changes
        adapterCategory.notifyDataSetChanged()
    }

}