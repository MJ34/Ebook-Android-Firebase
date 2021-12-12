package muji.dev.ebookperpusjateng.dashboard.user

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import muji.dev.ebookperpusjateng.MainActivity
import muji.dev.ebookperpusjateng.dashboard.profile.ProfileActivity
import muji.dev.ebookperpusjateng.dashboard.admin.categories.models.ModelCategory
import muji.dev.ebookperpusjateng.databinding.ActivityDashboardUserBinding

class DashboardUserActivity : AppCompatActivity() {

    private lateinit var dashboardUserBinding: ActivityDashboardUserBinding
    //Firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dashboardUserBinding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(dashboardUserBinding.root)

        //Init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        setupWithViewPagerAdapter(dashboardUserBinding.viewPager)
        dashboardUserBinding.tabLayout.setupWithViewPager(dashboardUserBinding.viewPager)

        //Handle click, logout
        dashboardUserBinding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Handle click, open profile
        dashboardUserBinding.profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupWithViewPagerAdapter(viewPager: ViewPager) {
        viewPagerAdapter = ViewPagerAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            this
        )

        //init list
        categoryArrayList = ArrayList()

        //load categories from db
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list
                categoryArrayList.clear()
                // load some static categories e.g All, Most Viewed, Most Download
                //add data to models
                val modelAll = ModelCategory("01", "All", 1, "")
                val modelMostViewed = ModelCategory("01", "Most Viewed", 1, "")
                val modelMostDownloaded = ModelCategory("01", "Most Downloaded", 1, "")
                //add to list
                categoryArrayList.add(modelAll)
                categoryArrayList.add(modelMostViewed)
                categoryArrayList.add(modelMostDownloaded)
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelAll.id}",
                        "${modelAll.category}",
                        "${modelAll.uid}"
                    ), modelAll.category
                )
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelMostViewed.id}",
                        "${modelMostViewed.category}",
                        "${modelMostViewed.uid}"
                    ), modelMostViewed.category
                )
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelMostDownloaded.id}",
                        "${modelMostDownloaded.category}",
                        "${modelMostDownloaded.uid}"
                    ), modelMostDownloaded.category
                )
                //refres list
                viewPagerAdapter.notifyDataSetChanged()

                //Now load from firebase db
                for (ds in snapshot.children) {
                    //get data in model
                    val model = ds.getValue(ModelCategory::class.java)
                    //add to list
                    categoryArrayList.add(model!!)
                    //add to viewPageradapter
                    viewPagerAdapter.addFragment(
                        BooksUserFragment.newInstance(
                            "${model.id}",
                            "${model.category}",
                            "${model.uid}"
                        ), model.category
                    )
                    //refres list
                    viewPagerAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        //setup adapter view pager
        viewPager.adapter = viewPagerAdapter
    }

    class ViewPagerAdapter(fm: FragmentManager, behavior: Int, context: Context): FragmentPagerAdapter(fm, behavior) {
        //hold list of fragment i.e new instance of some fragment for each category
        private val fragmentList: ArrayList<BooksUserFragment> = ArrayList()
        //list of titles of categories, for tabs
        private val fragmentTitleList: ArrayList<String> = ArrayList()

        private val context: Context

        init {
            this.context = context
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList[position]
        }

        public fun addFragment(fragment: BooksUserFragment, title: String) {
            //add fragment that will be passed as parameter in fragment list
            fragmentList.add(fragment)
            //add title that will be passed as parameter
            fragmentTitleList.add(title)
        }
    }

    //this activity can be opened with or without login, so hide logout and profile button when user not logged in
    private fun checkUser() {
        // Get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            // Not logged in, user can stay in user dashboard without login to
            dashboardUserBinding.subTitleTv.text = "Belum Login"

            //hide profile and logout
            dashboardUserBinding.profileBtn.visibility = View.GONE
            dashboardUserBinding.logoutBtn.visibility = View.GONE
        } else {
            //Logged in, get and show user info
            val email = firebaseUser.email
            // Set to textview of toolbar
            dashboardUserBinding.subTitleTv.text = email

            //show profile and logout
            dashboardUserBinding.profileBtn.visibility = View.VISIBLE
            dashboardUserBinding.logoutBtn.visibility = View.VISIBLE
        }
    }
}