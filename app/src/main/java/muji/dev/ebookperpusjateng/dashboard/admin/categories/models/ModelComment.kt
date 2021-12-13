package muji.dev.ebookperpusjateng.dashboard.admin.categories.models

class ModelComment {

    //Variable should be with same spealing and type as we added in firebase
    var id = ""
    var bookId = ""
    var timestamp = ""
    var comment = ""
    var uid = ""

    //empty consructor, required by firebase
    constructor()

    //param constructor
    constructor(id: String, bookId: String, timestamp: String, comment: String, uid: String) {
        this.id = id
        this.bookId = bookId
        this.timestamp = timestamp
        this.comment = comment
        this.uid = uid
    }
}