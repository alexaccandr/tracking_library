package com.trackinglib.view

import android.content.Intent
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
import com.trackinglib.untils.ViewModelAdapter
import com.trackinglib.viewmodel.TrackViewModel
import com.trackinglibrary.database.TrackRecord
import com.trackinglibrary.model.ModelAdapter
import com.trackinglibrary.utils.DatabaseUtils
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.fragment_track_list.*

class TracksListFragment : MvpAppCompatFragment(), TracksListView {

    @InjectPresenter
    lateinit var presenter: TracksListPresenter

    private lateinit var viewAdapter: TracksListAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var realm: Realm
    private lateinit var listener: RealmChangeListener<RealmResults<TrackRecord>>
    private lateinit var result: RealmResults<TrackRecord>

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
        realm = Realm.getDefaultInstance()
        result = realm.where(TrackRecord::class.java).findAll().sort("startDate", Sort.DESCENDING)
        listener = RealmChangeListener { tracks ->
            val t = ModelAdapter.adaptTracks(tracks)
            val tAdapted= t.map {
                ViewModelAdapter.adaptTrack(it)
            }.toTypedArray()
            updateTracksList(tAdapted)
        }
        result.addChangeListener(listener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        DatabaseUtils.close(realm)
    }

    override fun updateTracksList(tracks: Array<TrackViewModel>) {
        viewAdapter = TracksListAdapter(tracks.toMutableList()) {
            presenter.trackSelected(it)
        }
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

    override fun openMapActivity(id: String) {
        val context = activity
        if (context != null) {
            startActivity(Intent(context, MapsActivity::class.java).putExtra(MapsActivity.EXTRA_TRACK_ID, id))
        }
    }
}