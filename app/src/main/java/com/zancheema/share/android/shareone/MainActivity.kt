package com.zancheema.share.android.shareone

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.zancheema.share.android.shareone.databinding.ActivityMainBinding
import com.zancheema.share.android.shareone.util.slideDown
import com.zancheema.share.android.shareone.util.slideUp

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
                    viewDataBinding.fabOpenReceived.slideUp()
                } else if (!canSelectFiles(currentDestination) && canSelectFiles(previousDestination)) {
                    viewDataBinding.fabOpenReceived.slideDown()
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