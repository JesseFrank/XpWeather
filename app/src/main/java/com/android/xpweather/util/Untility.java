package com.android.xpweather.util;

import android.text.TextUtils;

import com.android.xpweather.db.City;
import com.android.xpweather.db.Country;
import com.android.xpweather.db.Province;
import com.google.gson.JsonArray;
import com.google.gson.JsonStreamParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Untility {
    /*解析省级*/
    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allProvinces = new JSONArray(response);
            for(int i=0;i<allProvinces.length();i++)
                { JSONObject provinceObject =allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }return false;
    }
    /*解析市级*/
    public static boolean handleCityResponse(String response,int provinceId){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allcities = new JSONArray(response);
                for(int i=0;i<allcities.length();i++)
                { JSONObject cityObject =allcities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }return false;
    }
    /*解析县级*/
    public static boolean handleCountryResponse(String response,int cityId){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCounties = new JSONArray(response);
                for(int i=0;i<allCounties.length();i++)
                { JSONObject countryObject =allCounties.getJSONObject(i);
                    Country country = new Country();
                    country.setCountryName(countryObject.getString("name"));
                    country.setWeatherId(countryObject.getString("weather_id"));
                    country.setCityId(cityId);
                    country.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }return false;
    }
}
