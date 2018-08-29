package com.github.mohaka.example.overlaptoolbartabs

import android.animation.Animator
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_news_feed.*
import kotlinx.android.synthetic.main.layout_app_header.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var menuNewsFeed: MenuItem
    private lateinit var menuArchive: MenuItem

    private var params: CollapsingToolbarLayout.LayoutParams? = null
    private var originalBottom = 0
    private var collapsedTop = 0
    private var pinDistance = 0

    private var isBadgeVisible = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initToolbar()
        initNavDrawer()
        initView()
    }

    override fun onResume() {
        super.onResume()
        appBar.addOnOffsetChangedListener(this::onOffsetChanged)
    }

    override fun onPause() {
        appBar.removeOnOffsetChangedListener(this::onOffsetChanged)
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) return
        if (params != null) return;

        params = tabs.layoutParams as? CollapsingToolbarLayout.LayoutParams
        originalBottom = params?.bottomMargin ?: 0
        collapsedTop = -appBar.totalScrollRange
        pinDistance = -tabs.top + 100
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }


    private fun initToolbar() {
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setHomeButtonEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initNavDrawer() {
        val drawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected)
        menuNewsFeed = navigationView.menu.findItem(R.id.menuNewsFeed);
        menuArchive = navigationView.menu.findItem(R.id.menuArchive);
    }

    private fun initView() {
        val pagerAdapter = PagerAdapter(supportFragmentManager)
        viewPager.adapter = pagerAdapter
        tabs.setupWithViewPager(viewPager)
        tabs.addOnTabSelectedListener(object : TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                super.onTabReselected(tab)

                if (viewPager.currentItem == 0) {
                    // TODO scroll news feed up
                } else {
                    // TODO scroll archive up
                }
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                super.onTabSelected(tab)

                if (viewPager.currentItem == 0) {
                    menuNewsFeed.isChecked = true
                    btnFab.show()
                } else {
                    menuArchive.isChecked = true
                    btnFab.hide()
                }
            }
        })

        btnBadge.alpha = 0f
        btnBadge.translationY = -100f
        btnBadge.visibility = View.GONE
        btnBadge.setOnClickListener(this::onBadgeClicked)

        btnFab.setOnClickListener(this::onFabClicked)
    }

    private fun showBadge(content: String) {
        if (!isBadgeVisible) {
            btnBadge.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .setInterpolator(DecelerateInterpolator())
                    .setStartDelay(0)
                    .setListener(object : AnimatorListener() {
                        override fun onAnimationStart(animation: Animator) {
                            btnBadge.visibility = View.VISIBLE
                            btnBadge.text = content
                        }

                        override fun onAnimationEnd(animation: Animator) {
                            isBadgeVisible = true
                        }
                    })
                    .start()
        } else {
            btnBadge.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(150)
                    .setInterpolator(AccelerateInterpolator())
                    .setStartDelay(0)
                    .setListener(object : AnimatorListener() {
                        override fun onAnimationEnd(animation: Animator) {
                            btnBadge.text = content
                            btnBadge.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(150)
                                    .setInterpolator(DecelerateInterpolator())
                                    .setStartDelay(0)
                                    .setListener(null)
                                    .start()
                        }
                    })
                    .start()
        }
    }

    private fun hideBadge() {
        if (isBadgeVisible) {
            isBadgeVisible = false
            btnBadge.animate()
                    .alpha(0f)
                    .translationY(-100f)
                    .setDuration(300)
                    .setInterpolator(DecelerateInterpolator())
                    .setStartDelay(100)
                    .setListener(object : AnimatorListener() {
                        override fun onAnimationEnd(animation: Animator) {
                            btnBadge.visibility = View.GONE
                        }
                    })
                    .start()
        }
    }


    private fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        if (params == null)
            return

        val oldBottom = params?.bottomMargin
        var newBottom = originalBottom * (collapsedTop - verticalOffset) / pinDistance

        if (newBottom > originalBottom)
            newBottom = originalBottom

        if (newBottom < 0)
            newBottom = 0

        if (newBottom != oldBottom) {
            params?.bottomMargin = newBottom
            tabs.layoutParams = params
        }
    }

    private fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawerLayout.closeDrawer(GravityCompat.START)

        return when (item.itemId) {
            R.id.menuNewsFeed -> {
                viewPager.currentItem = 0
                true
            }
            R.id.menuArchive -> {
                viewPager.currentItem = 1
                true
            }
            else -> false
        }
    }

    private fun onFabClicked(view: View) {
        val random = Random().nextInt(9) + 1
        showBadge("${random} new posts")
    }

    private fun onBadgeClicked(view: View) {
        scrollView.smoothScrollTo(0, 0)
        hideBadge()
    }


    private inner class PagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        private val fragments = ArrayList<Fragment>()

        init {
            val monitor = NewsFeedFragment()
            val archive = ArchiveFragment()
            fragments.add(monitor)
            fragments.add(archive)
        }

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return if (position == 0) "News Feed" else "Archive"
        }

        override fun getCount(): Int {
            return 2
        }
    }
}
