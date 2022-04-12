package illyan.jay

import android.os.Bundle
import androidx.navigation.findNavController
import co.zsmb.rainbowcake.base.RainbowCakeActivity
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import dagger.hilt.android.AndroidEntryPoint
import illyan.jay.databinding.ActivityMainBinding
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : RainbowCakeActivity<MainViewState, MainViewModel>() {
    override fun provideViewModel() = getViewModelFromFactory()

    private lateinit var binding: ActivityMainBinding

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
        when(viewState) {
            is MainStart -> {
                // show spalsh screen?
            }
            is MainReady -> {
                val nav = binding.loginNavHostFragment.findNavController()
                if (viewState.isLoggedIn) {
                    Timber.d("Logged in!")
                } else {
                    Timber.d("Logged out!")
                    if (nav.currentDestination?.id != nav.graph.startDestinationId) {
                        nav.popBackStack(nav.graph.startDestinationId, false)
                    }
                    Unit
                }
            }
        }.exhaustive
    }

    override fun onBackPressed() {
        Timber.d("Back pressed")
        val nav = binding.loginNavHostFragment.findNavController()
        if (nav.previousBackStackEntry?.destination?.id == nav.graph.startDestinationId) {
            Timber.d("Closing app on onBackPressed")
            finish()
        }
        super.onBackPressed()
    }

    override fun onNavigateUp(): Boolean {
        Timber.d("Navigating up")
        val nav = binding.loginNavHostFragment.findNavController()
        if (nav.previousBackStackEntry?.destination?.id != nav.graph.startDestinationId) {
            return super.onNavigateUp()
        } else {
            Timber.d("Closing app on onNavigateUp")
            finish()
        }
        return false
    }
}