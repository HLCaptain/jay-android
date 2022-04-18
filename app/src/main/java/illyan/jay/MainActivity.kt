package illyan.jay

import android.content.Intent
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import co.zsmb.rainbowcake.base.RainbowCakeActivity
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import dagger.hilt.android.AndroidEntryPoint
import illyan.jay.databinding.ActivityMainBinding
import illyan.jay.service.JayService
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : RainbowCakeActivity<MainViewState, MainViewModel>() {
    override fun provideViewModel() = getViewModelFromFactory()

    private lateinit var binding: ActivityMainBinding
    private lateinit var navControllerDelegate: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }

    override fun render(viewState: MainViewState) {
        val nav = binding.loginNavHostFragment.findNavController()
        when(viewState) {
            is Initial -> {
                // show spalsh screen?
            }
            is LoggedIn -> {
                Timber.d("Logged in!")
            }
            is LoggedOut -> {
                Timber.d("Logged out!")
                stopService(Intent(this, JayService::class.java))
                if (nav.currentDestination?.id != nav.graph.startDestinationId) {
                    nav.popBackStack(nav.graph.startDestinationId, false)
                }
                Unit
            }
        }.exhaustive
    }

    override fun onBackPressed() {
        Timber.d("onBackPressed called")
        val nav = binding.loginNavHostFragment.findNavController()
        if (navControllerDelegate.currentDestination?.id != navControllerDelegate.graph.startDestinationId) {
            super.onBackPressed()
        } else {
            if (nav.previousBackStackEntry?.destination?.id == nav.graph.startDestinationId)  {
                finish()
            }
        }
    }

    override fun onNavigateUp(): Boolean {
        Timber.d("onNavigateUp called")
        val nav = binding.loginNavHostFragment.findNavController()
        if (navControllerDelegate.currentDestination?.id != navControllerDelegate.graph.startDestinationId) {
            return super.onNavigateUp()
        } else {
            if (nav.previousBackStackEntry?.destination?.id == nav.graph.startDestinationId) {
                finish()
            }
        }
        return false
    }

    override fun onSupportNavigateUp(): Boolean {
        Timber.d("onSupportNavigateUp called")
        val nav = binding.loginNavHostFragment.findNavController()
        if (navControllerDelegate.currentDestination?.id != navControllerDelegate.graph.startDestinationId) {
            return navControllerDelegate.navigateUp()
        } else {
            if (nav.previousBackStackEntry?.destination?.id == nav.graph.startDestinationId) {
                finish()
            }
        }
        return false
    }

    fun setNavController(navController: NavController) {
        navControllerDelegate = navController
        setupActionBarWithNavController(navControllerDelegate)
    }
}