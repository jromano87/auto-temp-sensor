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


/* TreeHacks 2017
 * Author: John Romano
 * -------------------
 * HotDog: An IoT app with the purpose of keeping pets safe from overheating
 * and users informed of the current temperature in their cars. The Android
 * component of the app serves to display current local temperature and
 * sensor temperature through interaction with weather API and Node-RED.
 * In both cases, temperature data is received as a JSON and parsed accordingly.
 * -----------------------------------------------------------------------------
 * HeatGraphsActivity: Displays a graph of extrapolated interior vehicle temperature
 * vs. time elapsed to give the user a better idea of how fast it can get hot based
 * on the current local temperature.
 */
public class HeatGraphsActivity extends AppCompatActivity {

    public double weatherTemp;

    @Override
    /* onCreate
     * --------
     * Receives temperatures from MainStatusActivity to generate graphs from.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_heat_graphs);

        Intent intent = getIntent();
        weatherTemp = intent.getDoubleExtra("weatherTemp", weatherTemp);

        initializeGraph();
    }

    /* initializeGraph
     * ---------------
     * Using the GraphView library, temperature data points are plotted at 10 minute intervals to
     * give the user a visual representation of the rate of temperature increase based on the current
     * local temperature.
     */
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

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(CYAN); // set color of plot line to be blue
        paint.setPathEffect(new DashPathEffect(new float[]{8, 5}, 0));
        series.setCustomPaint(paint);
        graph.addSeries(series);
    }


    /* logRegression
     * -------------
     * Based on the time elapsed, calculate what the extrapolated temperature will likely be.
     * The logarithmic regression function was formed with Wolfram Alpha on data pulled from
     * an avma.org study of interior vehicular temperature increase based on starting temperature.
     *
     * Quartic function regression acquired from WolframAlpha (curve fit command)
     * Car temperature data retrieved from: https://www.avma.org/public/PetCare/Pages/Estimated-Vehicle-Interior-Air-Temperature-v.-Elapsed-Time.aspx
     */
    public double logRegression(double timeElapsed) {
        return 10.6234*Math.log(logFactor()*timeElapsed);
    }


    /* logFactor
     * ---------
     * The multiplier inside the logRegression expression (b in a*log(b*timeElapsed)) kept increasing
     * when observed in Wolfram Alpha. I captured the rate of increase of this factor by mapping the
     * starting temperature to its multiplier so that I could have a general multiplier for any starting temperature.
     *
     * Wolfram Alpha command: exp fit {{70, 613.389w}, {75, 982.068}, {80, 1572.34}, {85, 2517.4}, {90, 4030.49}, {95, 6453.03}}
     */
    public double logFactor() {
        return 0.843443*Math.exp(0.0941323*weatherTemp);
    }


    /* goBackToMain
     * ------------
     * Go back to MainStatusActivity when return button pressed.
     */
    public void goBackToMain(View view) {
        Intent intent = new Intent(this, MainStatusActivity.class);
        startActivity(intent);
    }
}
