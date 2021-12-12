package muji.dev.ebookperpusjateng.auth.register

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import muji.dev.ebookperpusjateng.dashboard.user.DashboardUserActivity
import muji.dev.ebookperpusjateng.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var registerBinding: ActivityRegisterBinding
    //Firebase Auth
    private lateinit var firebaseAuth: FirebaseAuth
    //Progress Dialog
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(registerBinding.root)
        //Init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        //Init progress dialog, akan muncul saat create account dan register user
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        //Handle tombol kembali
        registerBinding.backBtn.setOnClickListener {
            onBackPressed()
        }
        //Handle clik, begin register
        registerBinding.regisBtn.setOnClickListener {
            /*Steps
            * 1. Input data
            * 2. Validate data
            * 3. Create account - Firebase Auth
            * 4. Save user info - Firebase Realtime database*/
            validateData()
        }

    }

    private var name = ""
    private var email = ""
    private var password = ""

    private fun validateData() {
        //1. Input data
        name = registerBinding.nameEt.text.toString().trim()
        email = registerBinding.emailEt.text.toString().trim()
        password = registerBinding.passwordEt.text.toString().trim()
        val cPassword = registerBinding.cpasswordEt.text.toString().trim()
        //2. Validate data
        if (name.isEmpty()){
            //Empty name..
            Toast.makeText(this, "Masukan nama kamu...", Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            //Invalid email addreas
            Toast.makeText(this, "Nama email salah..", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            //Empty password
            Toast.makeText(this, "Masukan Password...", Toast.LENGTH_SHORT).show()
        } else if (cPassword.isEmpty()) {
            //Konfirm password
            Toast.makeText(this, "Konfirmasi Password...", Toast.LENGTH_SHORT).show()
        } else if (password != cPassword) {
            //Password doesn't match
            Toast.makeText(this, "Password tidak sama...", Toast.LENGTH_SHORT).show()
        } else {
            createUserAccount()
        }
    }

    private fun createUserAccount() {
        // 3. Create account - Firebase Auth

        //Show progress
        progressDialog.setMessage("Membuat Account...")
        progressDialog.show()

        //Create user in firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //Account create
                updateUserInfo()
            }
            .addOnFailureListener { e->
                //Failure create account
                progressDialog.dismiss()
                Toast.makeText(this, "Registrasi gagal karena ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserInfo() {
        // Save user info - Firebase Realtime database
        progressDialog.setMessage("Simpan data Pemustaka..")
        //timestamp
        val timestamp = System.currentTimeMillis()
        //get current user uid, since user is register so we can get it now
        val uid = firebaseAuth.uid
        //Setup data to add in db
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"] = "" // add empty, will do in profile edit
        hashMap["userType"] = "user" //posible values are user/admin, will change value admin manually an firebase db
        hashMap["timestamp"] = timestamp

        //set data to db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                //User info saved, open user dashboard
                progressDialog.dismiss()
                Toast.makeText(this, "Berhasil membuat Akun..", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, DashboardUserActivity::class.java))
                finish()
            }
            .addOnFailureListener { e->
                //failed adding data to db
                progressDialog.dismiss()
                Toast.makeText(this, "Gagal Membuat akun karena ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}