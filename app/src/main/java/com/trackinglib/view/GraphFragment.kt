package com.trackinglib.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.jjoe64.graphview.LegendRenderer
import com.jjoe64.graphview.helper.StaticLabelsFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.trackinglib.R
import com.trackinglib.presenter.GraphPresenter
import com.trackinglibrary.model.Track
import kotlinx.android.synthetic.main.fragment_graph.*

class GraphFragment : MvpAppCompatFragment(), GraphView {

    @InjectPresenter
    lateinit var presenter: GraphPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_graph, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.init()
    }


    override fun onTrackLoaded(tracks: Array<Track>) {

        // first series is a line
        val dataPoints = arrayOf(
            DataPoint(0.0, 0.0),
            *(tracks.mapIndexed { index, track ->
                val avSpeed = if (track.averageSpeed.isNaN()) 0.0 else track.averageSpeed * 3.6
                DataPoint((index + 2).toDouble(), avSpeed)
            }.toTypedArray())
        )
        val series = LineGraphSeries(dataPoints)
        series.isDrawBackground = true
        series.setAnimated(true)
        series.isDrawDataPoints = true

        graph.getViewport().setMinX(1.0)
        graph.getViewport().setMaxX((tracks.size + 2).toDouble())
        graph.getViewport().setMinY(0.0)
        graph.getViewport().setMaxY(dataPoints.maxBy { it.y }!!.y * 1.2)

        graph.getViewport().setYAxisBoundsManual(true)
        graph.getViewport().setXAxisBoundsManual(true)

        graph.getGridLabelRenderer().setHorizontalAxisTitle("Номер трека")
        graph.getGridLabelRenderer().setVerticalAxisTitle("Средняя сторость км/ч")

        val staticLabelsFormatter = StaticLabelsFormatter(graph)
//        val xAxis =

        val xAxisTitles = mutableListOf<String>()
        for (i in 0 until (tracks.size + 2)) {
            if (i == 0 || i == (tracks.size + 1)) {
                xAxisTitles.add("")
            } else {
                xAxisTitles.add(i.toString())
            }
        }
        staticLabelsFormatter.setHorizontalLabels(xAxisTitles.toTypedArray())
        graph.getGridLabelRenderer().labelFormatter = staticLabelsFormatter

        graph.addSeries(series)


        graph.legendRenderer.isVisible = true
        graph.legendRenderer.align = LegendRenderer.LegendAlign.TOP
    }
}