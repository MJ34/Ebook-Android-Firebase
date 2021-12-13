package muji.dev.ebookperpusjateng.auth.login

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import muji.dev.ebookperpusjateng.auth.register.RegisterActivity
import muji.dev.ebookperpusjateng.dashboard.admin.DahboardAdminActivity
import muji.dev.ebookperpusjateng.dashboard.user.DashboardUserActivity
import muji.dev.ebookperpusjateng.databinding.ActivityLoginBinding
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {

    private lateinit var loginBin: ActivityLoginBinding
    //Firebase Auth
    private lateinit var firebaseAuth: FirebaseAuth
    //Progress Dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBin = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginBin.root)

        //Init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        //Init progress dialog, akan muncul saat create account dan register user
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        //Handle click, not have account, goto register screen
        loginBin.noAccountTv.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        //Handle click, begin login
        loginBin.loginBtn.setOnClickListener {
            /*Steps
            *1) Input data
            * 2) Validate data
            * 3) Login - Firebase Auth
            * 4) Check user type - Firebase Auth
            *  if user - move to user dashboard
            *  if user - move to admin dashboard
             */
            validateData()
        }

        //handle click to forgot password activity
        loginBin.forgotTv.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

    }

    private var email = ""
    private  var password = ""

    private fun validateData() {
        // 1) Input data
        email = loginBin.emailEt.text.toString().trim()
        password = loginBin.passwordEt.text.toString().trim()

        // 2) Validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email format invalid..", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Masukan Password..", Toast.LENGTH_SHORT).show()
        } else {
            loginUser()
        }
    }

    private fun loginUser() {
        // 3) Login - Firebase Auth

        // Show Proggres
        progressDialog.setMessage("Loggin In...")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // Login success
                checkUser()
            }
            .addOnFailureListener { e->
                // Failed Login
                progressDialog.dismiss()
                Toast.makeText(this, "Login gagal karena ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUser() {
        /* 4) Check user type - Firebase Auth
            *  if user - move to user dashboard
            *  if user - move to admin dashboard
             */
        progressDialog.setMessage("Checking user...")

        val firebaseUser = firebaseAuth.currentUser!!

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    progressDialog.dismiss()

                    //Get user type e.g user or admin
                    val userType = snapshot.child("userType").value
                    if (userType == "user") {
                        //Its simple user, open user dashboard
                        startActivity(Intent(this@LoginActivity, DashboardUserActivity::class.java))
                        finish()
                    } else if (userType == "admin") {
                        // Its admin open admin dashboard
                        startActivity(Intent(this@LoginActivity, DahboardAdminActivity::class.java))
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })


    }
}