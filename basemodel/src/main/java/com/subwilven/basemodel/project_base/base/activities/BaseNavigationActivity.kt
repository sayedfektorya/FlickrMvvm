package com.subwilven.basemodel.project_base.base.activities

import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.subwilven.basemodel.R
import com.subwilven.basemodel.project_base.POJO.NavigationType
import com.subwilven.basemodel.project_base.base.fragments.BaseFragment
import com.subwilven.basemodel.project_base.base.fragments.TagedFragment
import com.subwilven.basemodel.project_base.utils.FragmentManagerUtil

public abstract class BaseNavigationActivity : com.subwilven.basemodel.project_base.base.activities.BaseActivity() {


    abstract val navigationType: NavigationType
    private var drawer: DrawerLayout? = null
    private var toggle: ActionBarDrawerToggle? = null
    protected var drawerNavigationView: NavigationView? = null
    private var bottomNavigationView: BottomNavigationView? = null
    abstract val menuIdsListWithFragment :List<Pair<Int,Class<*>>>
    private var currentFragmentIndex = -1
    private var firstFragmentTag: String? = null


    internal var closeDrawerHandler = Handler()
    internal val r: Runnable = Runnable { drawer!!.closeDrawer(GravityCompat.START) }

    internal var onDrawerNavigationItemSelectedListener = NavigationView.OnNavigationItemSelectedListener { this.onItemSelected(it) }

    internal var onBottomNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { this.onItemSelected(it) }

    private val isDrawerFragment: Boolean
        get() = FragmentManagerUtil.getBackStackCount(supportFragmentManager) == 0 &&
                !FragmentManagerUtil.isFragmentActive(supportFragmentManager, firstFragmentTag!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //should init toolbar before set up navigation drawer
        //so we wont wait until the fragment call it we call it now
        initToolbar()

        if (savedInstanceState != null) {
            currentFragmentIndex = savedInstanceState.getInt("currentFragmentIndex")
            firstFragmentTag = savedInstanceState.getString("firstFragmentTag")
        }

        if (navigationType == NavigationType.BottomNavigation)
            setUpBottomNavigation(savedInstanceState)
        else if (navigationType == NavigationType.DrawerNavigation)
            setUpNavigationDrawer(savedInstanceState)

    }

    private fun setUpBottomNavigation(savedInstanceState: Bundle?) {
        bottomNavigationView = findViewById(R.id.nav_view)
        bottomNavigationView!!.setOnNavigationItemSelectedListener(onBottomNavigationItemSelectedListener)

        //launch the frist fragment for the frist time
        if (savedInstanceState == null) {
            bottomNavigationView!!.selectedItemId = menuIdsListWithFragment[0].first
            onBottomNavigationItemSelectedListener
                    .onNavigationItemSelected(bottomNavigationView!!.menu.getItem(0))
        }
    }




    private fun setUpNavigationDrawer(savedInstanceState: Bundle?) {

        drawer = findViewById(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer!!.addDrawerListener(toggle!!)
        toggle!!.syncState()

        drawerNavigationView = findViewById(R.id.nav_view)
        drawerNavigationView!!.setNavigationItemSelectedListener(onDrawerNavigationItemSelectedListener)

        //launch the frist fragment for the frist time
        if (savedInstanceState == null) {
            navigateToDrawerFragmentByIndex(0)
        }
    }

    protected fun navigateToDrawerFragmentById(@IdRes menuId :Int){
        val index = menuIdsListWithFragment.indexOfFirst { it.first == menuId }
        drawerNavigationView!!.setCheckedItem(menuId)
        onDrawerNavigationItemSelectedListener
            .onNavigationItemSelected(drawerNavigationView!!.menu.getItem(index))
    }

    protected fun navigateToDrawerFragmentByIndex(index :Int){
        onDrawerNavigationItemSelectedListener
            .onNavigationItemSelected(drawerNavigationView!!.menu.getItem(index))
        drawerNavigationView!!.setCheckedItem(menuIdsListWithFragment[index].first)
    }

    override fun enableBackButton(enableBackButton: Boolean) {

        if (navigationType == NavigationType.DrawerNavigation)
            if (enableBackButton) {
                toggle!!.isDrawerIndicatorEnabled = false
                supportActionBar!!.setDisplayHomeAsUpEnabled(true)
                supportActionBar!!.setDisplayShowHomeEnabled(true)
                toggle!!.setToolbarNavigationClickListener { onBackPressed() }
            } else {
                supportActionBar!!.setDisplayHomeAsUpEnabled(false)
                toggle!!.isDrawerIndicatorEnabled = true
                toggle!!.toolbarNavigationClickListener = null
                toggle!!.syncState()
            }

    }

    open fun onItemSelected(item: MenuItem): Boolean {

        var fragmentClass: Class<*>? = null
        var newFragmentIndex = -1

        //get the fragment class of clicked item to create new instance of it
        for (i in menuIdsListWithFragment.indices) {
            if (item.itemId == menuIdsListWithFragment[i].first) {
                fragmentClass = menuIdsListWithFragment[i].second
                newFragmentIndex = i
            }
        }

        //if user click the same running fragment do nothing and close the drawer
        if (fragmentClass == null || currentFragmentIndex == newFragmentIndex) {
            if (navigationType == NavigationType.DrawerNavigation)
                drawer!!.closeDrawer(GravityCompat.START)
            return true
        }

        currentFragmentIndex = newFragmentIndex
        try {
            val fragment = fragmentClass.newInstance() as BaseFragment<*>
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            navigate(supportFragmentManager, fragment)
            if (newFragmentIndex == 0)
                firstFragmentTag = fragment.fragmentTag
        } catch (e: ClassCastException) {
            // if we cannot inhert our fragment from base fragment we must use TagedFragment interface
            val fragment = fragmentClass.newInstance() as Fragment
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            navigate(supportFragmentManager, fragment,(fragment as TagedFragment).fragmentTag)
            if (newFragmentIndex == 0)
                firstFragmentTag = fragment.fragmentTag

        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        }

        if (navigationType == NavigationType.DrawerNavigation)
            closeDrawerHandler.postDelayed(r, 200)

        return true

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("firstFragmentTag", firstFragmentTag)
        outState.putInt("currentFragmentIndex", currentFragmentIndex)
        super.onSaveInstanceState(outState)
    }
    fun closeDrawer(){
        drawer!!.closeDrawer(GravityCompat.START)
    }

    override fun onBackPressed() {

        if (drawer != null && drawer!!.isDrawerOpen(GravityCompat.START)) {
            drawer!!.closeDrawer(GravityCompat.START)
        } else if (isDrawerFragment) {// if any fragment of the drawer fragment active make the frist one active
            if (navigationType == NavigationType.DrawerNavigation) {
                drawerNavigationView!!.setCheckedItem(menuIdsListWithFragment[0].first)
                onDrawerNavigationItemSelectedListener
                        .onNavigationItemSelected(drawerNavigationView!!.menu.getItem(0))
            } else if (navigationType == NavigationType.BottomNavigation) {
                bottomNavigationView!!.selectedItemId = menuIdsListWithFragment[0].first
                onBottomNavigationItemSelectedListener
                        .onNavigationItemSelected(bottomNavigationView!!.menu.getItem(0))
            }
        } else {
            super.onBackPressed()
        }
    }

//    fun setElevation(f: Float) {
//        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//        //            appBarLayout.setElevation(f);
//        //        }
//    }
}
