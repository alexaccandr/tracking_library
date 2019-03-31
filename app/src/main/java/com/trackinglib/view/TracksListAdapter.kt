package com.trackinglib.view

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.trackinglib.R
import com.trackinglib.viewmodel.TrackViewModel
import kotlinx.android.synthetic.main.list_item_track.view.*
import java.text.SimpleDateFormat
import java.util.*


class TracksListAdapter(private val items: MutableList<TrackViewModel>, val block: (String) -> Unit) :
    RecyclerView.Adapter<TracksListAdapter.MyViewHolder>() {

    var format = SimpleDateFormat("d MMMM, yyyy hh:mm:ss")

    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val titleDate = view.startDateView
        val titleDateEnd = view.endDateView
        val number = view.numberView
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TracksListAdapter.MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_track, null))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val it = items[position]
        val date = Date(it.date)
        holder.titleDate.text = format.format(date)
        if (it.endDate != null) {
            val dateEnd = Date(it.endDate)
            holder.titleDateEnd.text = format.format(dateEnd)
            holder.titleDateEnd.visibility = VISIBLE
        }
        holder.number.text = "#${items.size - (position)}"
        holder.view.setOnClickListener {
            block(items[position].id)
        }
    }

    override fun getItemCount() = items.size

    fun addItem(track: TrackViewModel) {
        items.add(0, track)
    }

    fun updateTrackLocation(id: String, location: String) {
//        if (it.endDate != null) {
//            val dateEnd = Date(it.endDate)
//            holder.titleDateEnd.text = format.format(dateEnd)
//            holder.titleDateEnd.visibility = GONE
//        }
//        items.firstOrNull { it.id == id }?.location = location
        notifyDataSetChanged()
    }
}