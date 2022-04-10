package illyan.jay.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.base.RainbowCakeViewModel

/**
 * Same as RainbowCakeFragment, but uses ViewBinding for view inflation.
 */
abstract class RainbowCakeFragmentVB<
        VS : Any,
        VM : RainbowCakeViewModel<VS>,
        VB : ViewBinding
        > : RainbowCakeFragment<VS, VM>() {
    protected lateinit var binding: VB
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = provideViewBinding(inflater, container, savedInstanceState)
        return binding.root
    }

    protected abstract fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): VB
}