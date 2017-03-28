package com.example.paraghedawoo.checkweather;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    EditText cityName;
    TextView city;
    TextView state;
    TextView condition;
    TextView temp;
    TextView feels;
    TextView humidity;
    TextView wind;
    TextView lastUpdate;
    ImageView weatherIcon;
    RelativeLayout layout;
    Button button;

    public class ImageDownload extends AsyncTask<String, Void, Bitmap>{
        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL imageUrl = new URL(urls[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) imageUrl.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                Bitmap icon = BitmapFactory.decodeStream(inputStream);
                return icon;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public class WeatherDownload extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params) {
            String result = "";
            URL url;
            HttpURLConnection connection;
            try {
                url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                InputStream in = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1){
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;
            } catch (IOException e) {
                e.printStackTrace();
                return "Failed !";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject object = new JSONObject(result);
                String locationData = object.getString("location");
                Pattern p = Pattern.compile(":\"//(.*?)\",\"c");
                Matcher m = p.matcher(result);
                while(m.find()){
                    ImageDownload setIcon = new ImageDownload();
                    try {
                        Bitmap icon = setIcon.execute("https://"+m.group(1)).get();
                        weatherIcon.setImageBitmap(icon);
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                JSONObject location = new JSONObject(locationData);
                city.setText(location.getString("name"));
                state.setText(location.getString("region")+", "+location.getString("country"));
                String currentData = object.getString("current");
                JSONObject current = new JSONObject(currentData);
                lastUpdate.setText("Last Updated : "+current.getString("last_updated"));
                temp.setText("Temperature : "+ current.getString("temp_c")+"°C");
                wind.setText("Wind(KPH) : "+current.getString("wind_kph")+"("+current.getString("wind_degree")+current.getString("wind_dir")+")");
                humidity.setText("Humidity : "+ current.getString("humidity")+"%");
                feels.setText("Feels Like : "+ current.getString("feelslike_c")+"°C");
                String conditionData = current.getString("condition");
                JSONObject conditionDat = new JSONObject(conditionData);
                condition.setText(conditionDat.getString("text"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void click(View view){
        String city = cityName.getText().toString();
        if(city.length()==0){
            Toast.makeText(getApplicationContext(), "enter a city", Toast.LENGTH_SHORT).show();
        }else {
            try {
                String encodedName = URLEncoder.encode(city, "UTF-8");
                WeatherDownload weather = new WeatherDownload();
                weather.execute("http://api.apixu.com/v1/current.json?key=3591b6d3b1ee4134a2d52620170601&q=" +encodedName);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            layout.setVisibility(View.VISIBLE);
            cityName.setEnabled(false);
            button.setEnabled(false);
            cityName.setVisibility(View.INVISIBLE);
            button.setVisibility(View.INVISIBLE);
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference reference = database.getReference().child("City Name");
            reference.push().setValue(city);
        }
    }

    public void retry (View view){
        layout.setVisibility(View.INVISIBLE);
        cityName.setEnabled(true);
        button.setEnabled(true);
        cityName.setVisibility(View.VISIBLE);
        button.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cityName = (EditText) findViewById(R.id.editText);
        city = (TextView) findViewById(R.id.cityText);
        state = (TextView) findViewById(R.id.stateText);
        condition = (TextView)findViewById(R.id.condition);
        temp = (TextView)findViewById(R.id.currTemp);
        feels = (TextView)findViewById(R.id.feelsLike);
        humidity = (TextView)findViewById(R.id.humidity);
        wind = (TextView)findViewById(R.id.wind);
        lastUpdate = (TextView)findViewById(R.id.lastUpdate);
        button = (Button)findViewById(R.id.button);
        layout = (RelativeLayout) findViewById(R.id.resultLayout);
        weatherIcon = (ImageView)findViewById(R.id.weatherIcon);
    }
}
