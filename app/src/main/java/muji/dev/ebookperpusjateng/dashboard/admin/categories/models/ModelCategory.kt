package muji.dev.ebookperpusjateng.dashboard.admin.categories.models

class ModelCategory {
    // Variabel must match as in firebase
    var id:String = ""
    var category:String = ""
    var timestamp:Long = 0
    var uid:String = ""
    // Empty constructor, required by firebase
    constructor()

    //parameterized contructor
    constructor(id: String, category: String, timestamp: Long, uid: String) {
        this.id = id
        this.category = category
        this.timestamp = timestamp
        this.uid = uid
    }

}