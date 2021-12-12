package muji.dev.ebookperpusjateng

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import muji.dev.ebookperpusjateng.auth.login.LoginActivity
import muji.dev.ebookperpusjateng.dashboard.user.DashboardUserActivity
import muji.dev.ebookperpusjateng.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        //Handle click login app
        mainBinding.loginBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        //hand click, skip and continue to main screen
        mainBinding.skipBtn.setOnClickListener {
           startActivity(Intent(this, DashboardUserActivity::class.java))
        }


    }
}