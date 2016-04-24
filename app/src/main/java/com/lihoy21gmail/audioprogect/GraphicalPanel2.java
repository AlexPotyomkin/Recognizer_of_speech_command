package com.lihoy21gmail.audioprogect;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.Observable;
import java.util.Observer;

public class GraphicalPanel2 extends Fragment implements Observer {
    private final String TAG = "myLogs";
    private TimeSeries timeSeries;
    private static XYMultipleSeriesDataset dataset;
    private static XYMultipleSeriesRenderer renderer;
    private static XYSeriesRenderer rendererSeries;
    private static GraphicalView view;
    private Model mModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dataset = new XYMultipleSeriesDataset();
        renderer = new XYMultipleSeriesRenderer();
        rendererSeries = new XYSeriesRenderer();
        timeSeries = new TimeSeries("");
        renderer.addSeriesRenderer(rendererSeries);
        renderer.setBackgroundColor(Color.BLACK);
        renderer.setApplyBackgroundColor(true);
        renderer.setXLabelsColor(Color.WHITE);
        renderer.setYLabelsColor(0, Color.WHITE);
        rendererSeries.setColor(Color.WHITE);
        renderer.setAxesColor(Color.WHITE);
        renderer.setShowLegend(false);
        renderer.setXAxisMin(0);
        dataset.addSeries(timeSeries);
        renderer.setAxisTitleTextSize(40);
        view = ChartFactory.getTimeChartView(getActivity(), dataset, renderer, "");
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        mModel = Model.getInstance();
        Log.d(TAG, "onAttach: addObserver");
        mModel.addObserver(this);
        super.onAttach(activity);
    }

    @Override
    public void update(Observable observable, Object data) {
        if (timeSeries.getItemCount() != 0)
            timeSeries.clear();
        double array_of_mfcc[][] = mModel.getArray_of_MFCC();
        for (int i = 0; i < array_of_mfcc.length; i++) {
            for (int j = 0; j < array_of_mfcc[0].length; j++) {
                timeSeries.add(i, array_of_mfcc[i][j]);
                //Log.d(TAG, "false MFCC" + i + "[" + j + "]= " + array_of_mfcc[i][j]);
            }
        }
        renderer.setXAxisMax(array_of_mfcc.length*array_of_mfcc[0].length);
        view.repaint();
    }
}
