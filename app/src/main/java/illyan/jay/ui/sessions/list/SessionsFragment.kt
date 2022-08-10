/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

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

@AndroidEntryPoint
class SessionsFragment : RainbowCakeFragment<SessionsViewState, SessionsViewModel, FragmentSessionsBinding>() {
	override fun provideViewModel() = getViewModelFromFactory()
	override fun provideViewBindingInflater(): (LayoutInflater, ViewGroup?, Boolean) -> FragmentSessionsBinding = FragmentSessionsBinding::inflate

	private var itemEventListener: SessionItemEventListener = SessionItemEventListener()
	private lateinit var adapter: SessionsAdapter

	override fun render(viewState: SessionsViewState) {
		when (viewState) {
			is Initial -> {
				// Show loading of data and animation.
			}
			is Loading -> {
				// Show loading indicator.
			}
			is Ready -> {
				adapter.submitList(viewState.sessions)
			}
		}.exhaustive
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		loadRecycler()
		binding.deleteSessionsButton.setOnClickListener { viewModel.deleteStoppedSessions() }
		viewModel.load()
	}

	private fun loadRecycler() {
		itemEventListener.setOnItemClickListener {
			val action =
				SessionsFragmentDirections.actionSessionsFragmentToSessionInfoFragment(it.id)
			findNavController().navigate(action)
		}
		itemEventListener.setOnMapClickListener {
			val action =
				SessionsFragmentDirections.actionSessionsFragmentToSessionMapFragment(it.id)
			findNavController().navigate(action)
		}
		itemEventListener.setOnItemLongClickListener { true }
		binding.sessionsRecycler.layoutManager = LinearLayoutManager(context)
		adapter = SessionsAdapter(itemEventListener)
		binding.sessionsRecycler.adapter = adapter
	}
}