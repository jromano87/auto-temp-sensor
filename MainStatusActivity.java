package com.example.john.tempsense;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import org.json.*;
import java.math.BigDecimal;
import java.math.RoundingMode;


/* TreeHacks 2017
 * Author: John Romano
 * -------------------
 * HotDog: An IoT app with the purpose of keeping pets safe from overheating
 * and users informed of the current temperature in their cars. The Android
 * component of the app serves to display current local temperature and
 * sensor temperature through interaction with weather API and Node-RED.
 * In both cases, temperature data is received as a JSON and parsed accordingly.
 * -----------------------------------------------------------------------------
 * MainStatusActivity: The main dashboard that displays current local temperature
 * and sensor temperature (current temperature in the car).
 */
public class MainStatusActivity extends AppCompatActivity {

    public static final int NUM_DOUBLE_PLACES = 2;
    public int weatherTemp;
    public int carTemp;

    @Override
    /* onCreate
     * --------
     * Set main view of app and call both helper functions.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main_status);

        loadWeatherData();
        loadCarTempData();
    }


    /* loadWeatherData
     * ---------------
     * Used the Ion library to load the JSON data from the weather API. Calls the processWeather
     * function to edit the TextView widget to show the current local temperature.
     */
    public void loadWeatherData() {
        TextView introMessage = (TextView) findViewById(R.id.weather_info);
        introMessage.setText("Fetching weather..."); // shows in case data isn't loading

        Ion.with(this)
                .load("http://api.worldweatheronline.com/premium/v1/weather.ashx?key=e85efc8567284f2d837194233171902&q=94309&num_of_days=1&format=json")
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        // data has arrived
                        processWeather(result);
                    }
                });
    }


    /* loadCarTempData
     * ---------------
     * Uses the Ion library to obtain JSON data from Node-RED
     */
    public void loadCarTempData() {
        TextView introMessage = (TextView) findViewById(R.id.mytemp_info);
        introMessage.setText("Fetching weather...");

        Ion.with(this)
                .load("http://10.19.190.14:1880/mytemp") // was the URL of the Node-RED
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        // data has arrived
                        JSONObject json = null;
                        try {
                            json = new JSONObject(result); // giving null object because not connected to Node-RED; had to give hardware back at treehacks
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                        processCarTemp(json);
                    }
                });
    }


    /* goToGraph
     * ---------
     * Called when button pressed; goes to graph activity using an intent, passing in the current
     * local temperature to generate graphs from.
     */
    public void goToGraph(View view) {
        Intent intent = new Intent(this, HeatGraphsActivity.class);
        intent.putExtra("weatherTemp", weatherTemp);
        startActivity(intent);
    }


    /* processWeather
     * --------------
     * Updates the appropriate TextView to show the correct local weather temperature, obtaining
     * data from the JSON object.
     */
    public void processWeather(String result) {
        try {
            JSONObject json = new JSONObject(result);
            JSONObject weatherData = json.getJSONObject("data");
            JSONArray tempArray = weatherData.getJSONArray("current_condition");
            JSONObject temp = tempArray.getJSONObject(0);
            int localWeather = temp.getInt("temp_F");
            weatherTemp = localWeather;
            TextView weatherText = (TextView) findViewById(R.id.weather_info);
            weatherText.setText(String.valueOf(weatherTemp) + " °F");
        } catch (JSONException jsone) {
            Log.wtf("help", jsone);
        }
    }


    /* processCarTemp
     * --------------
     * Updates the appropriate TextView to show the correct sensor temperature, obtaining data from
     * the JSON object.
     */
    public void processCarTemp(JSONObject jsonObject) {
        try {
            int localCarTemp = jsonObject.getInt("temp");
            carTemp = (int) celsiusToFahrenheit((double) localCarTemp); // issue is casting
            TextView carText = (TextView) findViewById(R.id.mytemp_info);
            carText.setText(String.valueOf(carTemp) + " °F");
        } catch (JSONException jsone) {
            Log.wtf("help", jsone);
        }
    }


    /* celsiusToFahrenheit
     * -------------------
     * Sensor temperature arrives in Celsius; must convert to Fahrenheit
     */
    public double celsiusToFahrenheit(double temp) {
        double ftemp = 9 * (temp / 5) + 32;
        return ftemp;
    }

}

