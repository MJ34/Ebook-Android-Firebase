package muji.dev.ebookperpusjateng

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import muji.dev.ebookperpusjateng.dashboard.admin.DahboardAdminActivity
import muji.dev.ebookperpusjateng.dashboard.user.DashboardUserActivity
import muji.dev.ebookperpusjateng.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var splashBinding: ActivitySplashBinding

    //Firebase Auth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(splashBinding.root)

        //Init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        Handler(Looper.getMainLooper()).postDelayed( {
            checkUser()
        }, 1000)
    }

    private fun checkUser() {
        // Get current user, if logged in or not
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            // User not logged in, goto main screen
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {

                        //Get user type e.g user or admin
                        val userType = snapshot.child("userType").value
                        if (userType == "user") {
                            //Its simple user, open user dashboard
                            startActivity(Intent(this@SplashActivity, DashboardUserActivity::class.java))
                            finish()
                        } else if (userType == "admin") {
                            // Its admin open admin dashboard
                            startActivity(Intent(this@SplashActivity, DahboardAdminActivity::class.java))
                            finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }

    }
}