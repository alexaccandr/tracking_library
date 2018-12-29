package com.trackinglib.view

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.trackinglib.R
import com.trackinglib.presenter.TracksListPresenter
import com.trackinglib.viewmodel.TrackViewModel
import com.trackinglibrary.model.Track
import kotlinx.android.synthetic.main.fragment_track_list.*

class TracksListFragment : MvpAppCompatFragment(), TracksListView {

    @InjectPresenter
    lateinit var presenter: TracksListPresenter

    private lateinit var viewAdapter: TracksListAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_track_list, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewManager = LinearLayoutManager(activity)

        recycleView.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(false)

            // use a linear layout manager
            layoutManager = viewManager
        }

        presenter.init()
    }

    override fun updateTracksList(tracks: Array<TrackViewModel>) {
        viewAdapter = TracksListAdapter(tracks.toMutableList())
        recycleView.apply {
            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }
    }

    override fun appendTrack(track: TrackViewModel) {
        viewAdapter.addItem(track)
        viewAdapter.notifyDataSetChanged()
    }

    override fun updateTrackLocation(id: String, location: String) {
        viewAdapter.updateTrackLocation(id, location)
    }
}