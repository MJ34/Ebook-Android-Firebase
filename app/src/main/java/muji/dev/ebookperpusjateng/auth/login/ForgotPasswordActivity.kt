package muji.dev.ebookperpusjateng.auth.login

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import muji.dev.ebookperpusjateng.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance()

        //init setup progressDialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle back button
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //handle click, begin password recovery prossess
        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private var email = ""
    private fun validateData() {
       //get data
        email = binding.emailEt.text.toString().trim()

        //validate data
        if (email.isEmpty()) {
            Toast.makeText(this, "Masukan email...", Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email...", Toast.LENGTH_SHORT).show()
        } else {
            recoveryPassword()
        }
    }

    private fun recoveryPassword() {
        //show progress
        progressDialog.setMessage("Mengirim instruksi password reset ke $email")
        progressDialog.show()

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                //sent
                progressDialog.dismiss()
                Toast.makeText(this, "Instruksi dikirim ke \n$email", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                //failed
                progressDialog.dismiss()
                Toast.makeText(this, "Gagal mengirim karena ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}