package muji.dev.ebookperpusjateng

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storageMetadata
import muji.dev.ebookperpusjateng.utils.Constants
import java.util.*
import kotlin.collections.HashMap

class MyAplication:Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        //create a static method to convert timestamp to proper date format, so we can use it everyware in project, no need to rewrite again
        fun formatTimestamp(timestamp: Long) : String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp
            // Format dd/MM/yyyy
            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        //Function to get pdf size
        fun loadPdfSize(pdfUrl: String, pdfTitle: String, sizeTv: TextView) {
            val TAG = "PDF_SIZE_TAG"

            // Using url we can get file and its medata from firebase storage
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener {storageMetaData ->
                    Log.d(TAG, "LoadPdfSize: get metadata")
                    val bytes = storageMetaData.sizeBytes.toDouble()
                    Log.d(TAG, "LoadPdfSize: Size Bytes $bytes")
                    // Convert bytes to KB/MB
                    val kb = bytes/1024
                    val mb = kb/1024
                    if (mb >= 1) {
                        sizeTv.text = "${String.format("%.2f", mb)} MB"
                    } else if (kb >= 1) {
                        sizeTv.text = "${String.format("%.2f", kb)} KB"
                    } else {
                        sizeTv.text = "${String.format("%.2f", bytes)} bytes"
                    }
                }

                .addOnFailureListener { e->
                    Log.d(TAG, "loadPdfSize: Failed to get metadata due ro ${e.message}")
                }
        }

        /*
        instead of making new function loadPdfCount() just load pages count it would be more good to use some existing function to do that
        * i.e. loadPdfFromUrlSinglePage
        * We will add another parameter of type TextView e.g pageTv
        * Whenever we call that function
        * 1) if we require page number will pass pageTv (TextView)
        * 2) If we don't require page number we will pass null
        * and in function pageTv (TextView) parameter is not null we will set the page number count
         */
        fun loadPdfFromUrlSinglePage (
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            pageTv: TextView?
        ) {
            val TAG = "PDF_THUMBNAIL_TAG"

            //using url we can get file and its metadata from firebase storage
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener { bytes ->
                    Log.d(TAG, "LoadPdfSize: Size Bytes $bytes")

                    //Set to Pdfview
                    pdfView.fromBytes(bytes)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError { t->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onPageError { page, t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onLoad { nbPages ->
                            Log.d(TAG, "LoadPdfFromUrlSinglePage: Page: $nbPages")
                            //Pdf loaded, we can set page count, pdf thumbail
                            progressBar.visibility = View.INVISIBLE

                            // if pagesTv param is not null then set page numbers
                            if (pageTv != null) {
                                pageTv.text = "$nbPages"
                            }
                        }
                        .load()
                }

                .addOnFailureListener { e->
                    Log.d(TAG, "loadPdfSize: Failed to get metadata due ro ${e.message}")
                }
        }

        fun loadCategory(categoryId: String, categoryTv: TextView) {
            // load category using category id from firebase
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // get category
                        val category = "${snapshot.child("category").value}"
                        //set category
                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }

        fun deleteBook(context: Context, bookId: String, bookUrl: String, bookTitle: String) {
            /*
            param details
            * 1) context, used when require e.g. for progressdialog, toast
            * 2) booId, to delete book from db
            * 3) bookUrl, delete book from firebase storage
            * 4) bookTitle, show in dialog etc
             */
            val TAG = "DELETE_BOOK_TAG"

            Log.d(TAG, "deleteBook: deleting...")
            //progres dialog
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please wait..")
            progressDialog.setMessage("Deleting $bookTitle...")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            Log.d(TAG, "deleteBook: Deleting from storage...")
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageReference.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "deleteBook: Deleted from storage")
                    Log.d(TAG, "deleteBook: Deleting from db now...")

                    val ref = FirebaseDatabase.getInstance().getReference("Books")
                    ref.child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(context, "Successfully deleted...", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "deleteBook: Deleting from db to...")
                        }
                        .addOnFailureListener { e->
                            progressDialog.dismiss()
                            Log.d(TAG, "deleteBook: Failed to delete from db due to ${e.message}")
                            Toast.makeText(context, "Failed to delete from db due to ${e.message}", Toast.LENGTH_SHORT).show()
                        }

                }
                .addOnFailureListener { e->
                    progressDialog.dismiss()
                    Log.d(TAG, "deleteBook: Failed to delete from storage due to ${e.message}")
                    Toast.makeText(context, "Failed to delete from storage due to ${e.message}", Toast.LENGTH_SHORT).show()
                }

        }

        fun incrementBookViewCount(bookId: String) {
            // get current book views count
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get views count
                        var viewsCount = "${snapshot.child("viewsCount").value}"

                        if (viewsCount == "" || viewsCount == "null") {
                            viewsCount = "0";
                        }
                        //increment views count
                        val newViewsCount = viewsCount.toLong() + 1
                        //Setup data to update in db
                        val hashMap = HashMap<String, Any>()
                        hashMap["viewsCount"] = newViewsCount
                        //set to db
                        val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }

    }
}