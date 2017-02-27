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

public class MainStatusActivity extends AppCompatActivity {

    public static final int NUM_DOUBLE_PLACES = 2;
    public int weatherTemp;
    public int carTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main_status);

        loadWeatherData();
        loadCarTempData();
    }

    public void loadWeatherData() {
        TextView introMessage = (TextView) findViewById(R.id.weather_info);
        introMessage.setText("Fetching weather...");

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

    public void loadCarTempData() {
        TextView introMessage = (TextView) findViewById(R.id.mytemp_info);
        introMessage.setText("Fetching weather...");

        Ion.with(this)
                .load("http://10.19.190.14:1880/mytemp") 
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        // data has arrived
                        JSONObject json = null;
                        try {
                            json = new JSONObject(result);
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                        processCarTemp(json); 
                    }
                });
    }

    public void goToGraph(View view) {
        Intent intent = new Intent(this, HeatGraphsActivity.class);
        intent.putExtra("weatherTemp", weatherTemp);
        intent.putExtra("carTemp", carTemp);
        startActivity(intent);
    }

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

    public void processCarTemp(JSONObject jsonObject) {
        try {
            int localCarTemp = jsonObject.getInt("temp");
            carTemp = (int) celsiusToFahrenheit((double) localCarTemp); 
            TextView carText = (TextView) findViewById(R.id.mytemp_info);
            carText.setText(String.valueOf(carTemp) + " °F");
        } catch (JSONException jsone) {
            Log.wtf("help", jsone);
        }
    }

    /* temp given in fahrenheit */
    public double getFahrenheit(double temp) {
        double ftemp = (((temp - 273) * 9/5) + 32);
        return ftemp;
    }

    public double celsiusToFahrenheit(double temp) {
        double ftemp = 9 * (temp / 5) + 32;
        return ftemp;
    }

    /* Need to truncate otherwise */
    public String truncate(String value, int places) {
        return new BigDecimal(value)
                .setScale(places, RoundingMode.DOWN)
                .stripTrailingZeros()
                .toString();
    }
}

