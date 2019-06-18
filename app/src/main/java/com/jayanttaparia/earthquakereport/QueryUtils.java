package com.jayanttaparia.earthquakereport;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;

public final class QueryUtils {
    private static final String TAG = QueryUtils.class.getSimpleName();

    private QueryUtils(){
    }

    public static ArrayList<Earthquake> fetchEarthquakeArrayList(String stringUrl){

        URL url = createUrl(stringUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        }catch (IOException e){
            Log.e(TAG, "Problem making the HTTP request", e);
        }

        ArrayList<Earthquake> resultEarthquakeList = extractFeatureEarthquakes(jsonResponse);
        return resultEarthquakeList;

    }

    private static URL createUrl(String stringUrl){
        URL url = null;

        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Problem building the url ",e );
            e.printStackTrace();
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException{
        String jsonResponse = "";

        if (url == null){
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }
            else {
                Log.e(TAG, "Error response code: " + urlConnection.getResponseCode());
            }
            
        }
        catch (IOException e ){
            Log.e(TAG, "Problem retrieving the earthquake json results", e);
        }finally {
            if (urlConnection != null){
                urlConnection.disconnect();
            }if (inputStream != null){
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException    {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader =
                    new InputStreamReader(inputStream, Charset.forName("UTF-8"));//inputStreamReader allows us to read only one character at a time so we use bufferedReader which can read one line at a time
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = null;
            line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }


    public static ArrayList<Earthquake> extractFeatureEarthquakes(String jsonString){

        if (TextUtils.isEmpty(jsonString)){
            return null;
        }

        ArrayList<Earthquake> earthquakes = new ArrayList<>();

        try {
            JSONObject baseJsonResponse = new JSONObject(jsonString);
            JSONArray earthquakeArray = baseJsonResponse.getJSONArray("features");

            for(int i=0; i<earthquakeArray.length(); i++){

                JSONObject currentEarthquake = earthquakeArray.getJSONObject(i);
                JSONObject properties = currentEarthquake.getJSONObject("properties");
                    double magnitude = properties.getDouble("mag");
                    String location = properties.getString("place");
                    Long time = properties.getLong("time");
                    String url = properties.getString("url");

                Earthquake earthquake = new Earthquake(magnitude,location,time,url);
                earthquakes.add(earthquake);

            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);

        }
        return earthquakes;
    }

}
