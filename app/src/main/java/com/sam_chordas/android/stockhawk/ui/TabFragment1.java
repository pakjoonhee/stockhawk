package com.sam_chordas.android.stockhawk.ui;

/**
 * Created by joonheepak on 11/18/16.
 */

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.sam_chordas.android.stockhawk.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class TabFragment1 extends Fragment implements OnChartValueSelectedListener{
    String yesterdayDate = getYesterdayDateString();
    String threeMonthsDate = getThreeMonthsDateString();
    private String BASE_URL = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20%3D%20";
    private String END_URL = "%20and%20startDate%20%3D%20\"" + threeMonthsDate + "\"%20and%20endDate%20%3D%20\"" + yesterdayDate + "\"&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
    private ArrayList<String> stockHistory = new ArrayList<>();
    private ArrayList<String> dateStock = new ArrayList<>();
    private ArrayList<String> retrievedStockHistory = new ArrayList<>();
    private LineChart mpAndroidChart;
    private TextView stockPrice;
    private TextView stockDate;
    private TextView currentPrice;
    private String [] dateValues;
    private String bidPrice;
    private Button button;
    View rootView;

    public TabFragment1() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.tab_practice, container, false);

        Bundle bundle = getActivity().getIntent().getExtras();
        String symbol = bundle.getString("symbol");
        bidPrice = bundle.getString("bid_price");
        mpAndroidChart = (LineChart) rootView.findViewById(R.id.mpandroidchart);
        List<Entry> valsComp1 = new ArrayList<Entry>();
        String historyUrl = BASE_URL + "\"" + symbol + "\"" + END_URL;
        stockPrice = (TextView) rootView.findViewById(R.id.stock_price);
        stockDate = (TextView) rootView.findViewById(R.id.stock_date);
        currentPrice = (TextView) rootView.findViewById(R.id.current_price);

        try {
            retrievedStockHistory = new AsyncHttpTask().execute(historyUrl).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < retrievedStockHistory.size(); i++) {
            dateStock.add(retrievedStockHistory.get(i));
            retrievedStockHistory.remove(i);
        }

        float [] floatValues = new float[retrievedStockHistory.size()];
        for (int i = 0; i < retrievedStockHistory.size(); i++) {
            floatValues[i] = Float.parseFloat(retrievedStockHistory.get(i));
            Entry blah = new Entry((float)i, floatValues[i]);
            valsComp1.add(blah);
        }

        dateValues = new String[dateStock.size()];
        for (int i = 0; i < dateStock.size(); i++) {
            dateValues[i] = (dateStock.get(i));
        }

        IAxisValueFormatter formatter = new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return dateValues[(int) value];
            }

            @Override
            public int getDecimalDigits() {  return 0; }
        };

        XAxis xAxis = mpAndroidChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(formatter);
        xAxis.setDrawLabels(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);

        YAxis yAxis = mpAndroidChart.getAxisLeft();
        yAxis.setDrawLabels(false);
        yAxis.setDrawAxisLine(false);
        yAxis.setDrawGridLines(false);
        yAxis.setDrawZeroLine(true);
        mpAndroidChart.getAxisRight().setEnabled(false);

        LineDataSet setComp1 = new LineDataSet(valsComp1, "Company 1");
        setComp1.setDrawCircles(false);
        setComp1.setLineWidth(2f);
        setComp1.setHighLightColor(Color.RED);
        setComp1.setColor(Color.RED);
        setComp1.setDrawHorizontalHighlightIndicator(true);

//        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
//        mv.setChartView(mpAndroidChart);
//        mpAndroidChart.setMarker(mv);

        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setComp1);

        LineData data = new LineData(dataSets);
        data.setDrawValues(false);
        mpAndroidChart.setDescription(null);
        mpAndroidChart.setData(data);
        mpAndroidChart.invalidate();
        mpAndroidChart.setOnChartValueSelectedListener(this);
        mpAndroidChart.getLegend().setEnabled(false);

        return rootView;
    }


    private String getYesterdayDateString() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }

    private String getThreeMonthsDateString() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -90);
        return dateFormat.format(cal.getTime());
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        stockPrice.setText("" + e.getY());
        stockDate.setText(dateStock.get((int)e.getX()));
        currentPrice.setText(bidPrice);
    }

    @Override
    public void onNothingSelected() {

    }

    public class AsyncHttpTask extends AsyncTask<String, Void, ArrayList<String>> {
        HttpURLConnection connection = null;

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            ArrayList<String> result = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                String response = streamToString(stream);
                result = parseStockClose(response);
            } catch (Exception e) {
                Log.d("FAIL", e.getLocalizedMessage());
            }
            return result;
        }

        private ArrayList<String> parseStockClose(String result) {
            JSONArray resultsArray = null;
            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(result);
                resultsArray = jsonObject.getJSONObject("query").getJSONObject("results").getJSONArray("quote");
                for (int i = 0; i < resultsArray.length(); i++) {
                    jsonObject = resultsArray.optJSONObject(i);
                    String stockDate = jsonObject.optString("Date");
                    stockHistory.add(stockDate);
                    String stockPrice = jsonObject.optString("Close");
                    stockHistory.add(stockPrice);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return stockHistory;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            super.onPostExecute(result);

        }
        public String streamToString(InputStream stream) throws IOException {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
            String line;
            String result = "";
            while ((line = bufferedReader.readLine()) != null) {
                result += line;
            }

            if (null != stream) {
                stream.close();
            }
            return result;
        }


    }

}
