package com.zancheema.share.android.shareone

import android.os.Bundle
import android.view.View
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        setUpToolbar()
        setUpViewVisibility()
    }

    private fun setUpViewVisibility() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            run {
                viewDataBinding.fabSelectFiles.visibility =
                    if (canSelectFiles(destination.id)) View.VISIBLE else View.GONE
            }
        }
    }

    private fun canSelectFiles(destinationId: Int): Boolean {
        return destinationId == R.id.homeFragment
    }

    private fun setUpToolbar() {
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        viewDataBinding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }
}