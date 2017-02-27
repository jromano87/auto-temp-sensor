package com.example.john.tempsense;

import android.content.Intent;
import android.graphics.DashPathEffect;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import static android.graphics.Color.BLUE;
import static android.graphics.Color.CYAN;
import static android.graphics.Color.GREEN;
import static android.graphics.Color.WHITE;

public class HeatGraphsActivity extends AppCompatActivity {

    public double weatherTemp;
    public double carTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_heat_graphs);

        Intent intent = getIntent();
        weatherTemp = intent.getDoubleExtra("weatherTemp", weatherTemp);
        carTemp = intent.getDoubleExtra("carTemp", carTemp);

        initializeGraph();
    }

    public void initializeGraph() {
        GraphView graph = (GraphView) findViewById(R.id.heat_graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, logRegression(1)), // can't do log of 0, so 1 (decimal logs are negative)
                new DataPoint(10, logRegression(10.0)),
                new DataPoint(20, logRegression(20.0)),
                new DataPoint(30, logRegression(30.0)),
                new DataPoint(40, logRegression(40.0)),
                new DataPoint(50, logRegression(50.0)),
                new DataPoint(60, logRegression(60.0)),
        });

        GridLabelRenderer grid = graph.getGridLabelRenderer();
        grid.setGridColor(WHITE);

        // styling series
        series.setTitle("Car Temperature vs. Time Elapsed");
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(10);
        series.setThickness(8);
        series.setBackgroundColor(WHITE);
        series.setColor(WHITE);

// custom paint to make a dotted line
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(CYAN);
        paint.setPathEffect(new DashPathEffect(new float[]{8, 5}, 0));
        series.setCustomPaint(paint);
        graph.addSeries(series);
    }

    /* Quartic function regression acquired from WolframAlpha (curve fit command)
     *  Car temperature data retrieved from: https://www.avma.org/public/PetCare/Pages/Estimated-Vehicle-Interior-Air-Temperature-v.-Elapsed-Time.aspx
     *  Function from WA, data from https://www.avma.org/public/PetCare/Pages/Estimated-Vehicle-Interior-Air-Temperature-v.-Elapsed-Time.aspx
     */
    public double logRegression(double timeElapsed) {
        return 10.6234*Math.log(logFactor()*timeElapsed);
        //return 10.6234*Math.log(timeElapsed); // I KNOW WHY it's crashing; log 0 gives garbage value
    }

    /* Getting the logarithmic multiplication factors from an exponential function; they kept increasing at an increasing rate as the starting temperature increased,
    * so I captured that rate of increase as an exponential function so that I could have a general factor for any starting temp */
    // Wolfram Alpha command: exp fit {{70, 613.389w}, {75, 982.068}, {80, 1572.34}, {85, 2517.4}, {90, 4030.49}, {95, 6453.03}}
    public double logFactor() {
        return 0.843443*Math.exp(0.0941323*weatherTemp);
    }

    public void goBackToMain(View view) {
        Intent intent = new Intent(this, MainStatusActivity.class);
        startActivity(intent);
    }

    // todo: can have message that displays threat level based on temp
    // straight horizontal line on graph that shows carTemp
}
