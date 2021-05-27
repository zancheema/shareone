package com.zancheema.share.android.shareone

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.zancheema.share.android.shareone.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var viewDataBinding: ActivityMainBinding

    // Keeps track of the previous nav destination to nav transitions
    private var previousDestination = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        setUpToolbar()
        setUpViewVisibility()
    }

    private fun setUpViewVisibility() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            run {
                val currentDestination = destination.id
                if (!canSelectFiles(previousDestination) && canSelectFiles(currentDestination)) {
                    val animation =
                        AnimationUtils.loadAnimation(applicationContext, R.anim.fab_slide_up)

                    viewDataBinding.fabSelectFiles.apply {
                        startAnimation(animation)
                        visibility = View.VISIBLE
                    }
                } else if (!canSelectFiles(currentDestination) && canSelectFiles(previousDestination)) {
                    val animation =
                        AnimationUtils.loadAnimation(applicationContext, R.anim.fab_slide_down)

                    viewDataBinding.fabSelectFiles.apply {
                        startAnimation(animation)
                        visibility = View.GONE
                    }
                }
                previousDestination = currentDestination
            }
        }
    }

    private fun canSelectFiles(destinationId: Int): Boolean {
        return destinationId == R.id.homeFragment
    }

    private fun setUpToolbar() {
//        val appBarConfiguration = AppBarConfiguration(navController.graph)
        val appBarConfiguration =
            AppBarConfiguration.Builder(R.id.homeFragment, R.id.selectAvatarFragment)
                .build()
        viewDataBinding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }
}