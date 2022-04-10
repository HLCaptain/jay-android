package illyan.jay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import co.zsmb.rainbowcake.navigation.NavActivity
import co.zsmb.rainbowcake.navigation.SimpleNavActivity
import dagger.hilt.android.AndroidEntryPoint
import illyan.jay.databinding.ActivityMainBinding
import illyan.jay.ui.login.LoginFragment

@AndroidEntryPoint
class MainActivity : NavActivity<MainViewState, MainViewModel>() {
    override fun provideViewModel() = getViewModelFromFactory()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel.load()
    }

    override fun render(viewState: MainViewState) {
        when(viewState) {
            is MainStart -> {
                // show spalsh screen
            }
            is MainReady -> {
                // navigate to the login fragment if not signed in
                navigator.add(LoginFragment())
            }
        }.exhaustive
    }
}