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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_graph, null)
    }

    override fun onTrackLoaded(tracks: Array<Track>) {

        // first series is a line
        val dataPoints = arrayOf(
            DataPoint(0.0, 0.0),
            *(tracks.mapIndexed { index, track ->
                val avSpeed = if (track.averageSpeed.isNaN()) 0.0 else track.averageSpeed * 3.6
                DataPoint((index + 1).toDouble(), avSpeed)
            }.toTypedArray())
        )
        val series = LineGraphSeries(dataPoints)
        series.isDrawBackground = true

        val showAnimation = !presenter.isInRestoreState(this)
        series.setAnimated(showAnimation)

        series.isDrawDataPoints = true

        graph.viewport.setMinX(0.0)
        graph.viewport.setMaxX((tracks.size + 1).toDouble())
        graph.viewport.setMinY(0.0)
        graph.viewport.setMaxY(dataPoints.maxBy { it.y }!!.y * 1.2)

        graph.viewport.isYAxisBoundsManual = true
        graph.viewport.isXAxisBoundsManual = true

        graph.gridLabelRenderer.horizontalAxisTitle = "Номер трека"
        graph.gridLabelRenderer.verticalAxisTitle = "Средняя сторость км/ч"

        val staticLabelsFormatter = StaticLabelsFormatter(graph)

        val xAxisTitles = mutableListOf<String>()
        for (i in 0 until (tracks.size + 2)) {
            if (i == 0 || i == (tracks.size + 1)) {
                xAxisTitles.add("")
            } else {
                xAxisTitles.add(i.toString())
            }
        }
        staticLabelsFormatter.setHorizontalLabels(xAxisTitles.toTypedArray())
        graph.gridLabelRenderer.labelFormatter = staticLabelsFormatter

        graph.addSeries(series)


        graph.legendRenderer.isVisible = true
        graph.legendRenderer.align = LegendRenderer.LegendAlign.TOP
    }

    override fun onDestroyView() {
        graph.removeAllSeries()
        super.onDestroyView()
    }
}