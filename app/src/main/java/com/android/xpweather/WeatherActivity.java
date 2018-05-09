package com.android.xpweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.xpweather.gson.Forecast;
import com.android.xpweather.gson.Weather;
import com.android.xpweather.util.HttpUntil;
import com.android.xpweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.android.xpweather.R.*;

public class WeatherActivity extends AppCompatActivity {
    public DrawerLayout drawerLayout;

    public SwipeRefreshLayout swipeRefresh;
    private ScrollView weatherLayout;
    private TextView titleUpdateTime;
    private TextView titleCity;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        drawerLayout =(DrawerLayout) findViewById(id.drawer_layout);
        //必应每日一图
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(layout.activity_weather);
        //初始化各控件
        weatherLayout = (ScrollView) findViewById(id.weather_layout);
        titleCity = (TextView) findViewById(id.title_city);
        titleUpdateTime = (TextView) findViewById(id.title_update_time);
        degreeText = (TextView) findViewById(id.title_update_time);
        weatherInfoText = (TextView) findViewById(id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(id.forecast_layout);
        aqiText = (TextView) findViewById(id.aqi_text);
        pm25Text = (TextView) findViewById(id.pm25_text);
        comfortText = (TextView) findViewById(id.comfort_text);
        carWashText = (TextView) findViewById(id.car_wash_text);
        sportText = (TextView) findViewById(id.sport_text);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        final String weatherId;
        if (weatherString != null){
            //有缓存时直接解析天气数据
            Weather weather = Utility.hanleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }else{
            //无缓存时在服务器查询天气
            //String weatherId = getIntent().getStringExtra("weather_id");
            weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
    }
    //根据天气id请求城市天气信息
    public  void requestWeather(final String weatherId){
        String weatherUrl = "http://guolin.tech/api/weather?cityid="+ weatherId+ "&key=9550d109e7194d3192e9475d88879190";
        HttpUntil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException{
                final String responseText = response.body().string();
                final Weather weather = Utility.hanleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather != null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);

                        }else {
                            Toast.makeText(WeatherActivity.this,"获取天气信息时报",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e){
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                 swipeRefresh.setRefreshing(false);
            }


                });

            }
        });
    }

//处理并展示Weather实体类中的数据
private void showWeatherInfo(Weather weather){
    String cityName = weather.basic.cityName;
    String updateTime = weather.basic.update.updatetime.split(" ")[1];
    String degree = weather.now.temperature + "°C";
    String weatherInfo = weather.now.more.info;
    titleCity.setText(cityName);
    titleUpdateTime.setText(updateTime);
    degreeText.setText(degree);
    weatherInfoText.setText(weatherInfo);
    forecastLayout.removeAllViews();
    Intent intent = new Intent(this,AutoUpdateService.class);
    startService(intent);
    for(Forecast forecast : weather.forecastList){
        View view = LayoutInflater.from(this).inflate(layout.forecast_item,forecastLayout,false);
    TextView dateText =(TextView) view.findViewById(id.date_text);
    TextView infoText =(TextView) view.findViewById(id.info_text);
    TextView maxText =(TextView) view.findViewById(id.max_text);
    TextView minText =(TextView) view.findViewById(id.min_text);
    dateText.setText(forecast.date);
    infoText.setText(forecast.more.info);
    maxText.setText(forecast.temperature.max);
    minText.setText(forecast.temperature.min);
    forecastLayout.addView(view);
    }
    if (weather.aqi != null){
        aqiText.setText(weather.aqi.city.aqi);
        pm25Text.setText(weather.aqi.city.pm25);
    }
    String comfort = "舒适度" + weather.suggestion.comnfort.info;
    String carwash = "洗车指数" + weather.suggestion.carWash.info;
    String sport = "运动建议" + weather.suggestion.sport.info;
    comfortText.setText(comfort);
    carWashText.setText(carwash);
    sportText.setText(sport);
    weatherLayout.setVisibility(View.VISIBLE);
}
}