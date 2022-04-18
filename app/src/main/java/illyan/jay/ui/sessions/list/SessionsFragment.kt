package illyan.jay.ui.sessions.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import dagger.hilt.android.AndroidEntryPoint
import illyan.jay.databinding.FragmentSessionsBinding
import illyan.jay.ui.custom.RainbowCakeFragment
import illyan.jay.ui.sessions.list.model.UiSession
import illyan.jay.util.ItemEventListener
import javax.inject.Inject

@AndroidEntryPoint
class SessionsFragment : RainbowCakeFragment<SessionsViewState, SessionsViewModel, FragmentSessionsBinding>() {
    override fun provideViewModel() = getViewModelFromFactory()
    override fun provideViewBindingInflater(): (LayoutInflater, ViewGroup?, Boolean) -> FragmentSessionsBinding = FragmentSessionsBinding::inflate

    // TODO: make another class xyz : ItemEventListener which has an onMapClickListener function
    private var itemEventListener: SessionItemEventListener = SessionItemEventListener()
    private lateinit var adapter: SessionsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadRecycler()
        viewModel.load()
    }

    override fun render(viewState: SessionsViewState) {
        when(viewState) {
            is Initial -> {

            }
            is Loading -> {

            }
            is Ready -> {
                adapter.submitList(viewState.sessions)
            }
        }.exhaustive
    }

    private fun loadRecycler() {
        itemEventListener.setOnItemClickListener {
            val action = SessionsFragmentDirections.actionSessionsFragmentToSessionInfoFragment(it.id)
            findNavController().navigate(action)
        }
        itemEventListener.setOnMapClickListener {
            val action = SessionsFragmentDirections.actionSessionsFragmentToSessionMapFragment(it.id)
            findNavController().navigate(action)
        }
        itemEventListener.setOnItemLongClickListener { true }
        binding.sessionsRecycler.layoutManager = LinearLayoutManager(context)
        adapter = SessionsAdapter(itemEventListener)
        binding.sessionsRecycler.adapter = adapter
    }
}