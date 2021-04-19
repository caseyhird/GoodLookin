package com.zybooks.goodlookin;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ResultsActivity extends AppCompatActivity
        implements SearchValueAdapter.ItemClickListener,
        ResetDialogFragment.OnResetSelectedListener, SensorEventListener {
    public static final String EXTRA_SEARCH_VAL = "search_string";
    public static final String EXTRA_LOC_VAL = "loc_string";
    private static final String TAG = "bingSearch";
    private static final String HEADER_SUB_KEY = "Ocp-Apim-Subscription-Key";
    private static final String PRIVATE_KEY_1 = "474c1e670b084ad0bafd268a056602ab";
    private String searchVal;
    private String location;
    private String url = "https://api.bing.microsoft.com/v7.0/search?q=";
    private String editURL;
    private ArrayList<ResultValue> info = new ArrayList<>();
    SearchValueAdapter adapter;
    EditText refineET;
    RequestQueue queue;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

    private String encodeVal(String input){

        String encodedVal;

        try{
            encodedVal = URLEncoder.encode(input, StandardCharsets.UTF_8.toString());
        }catch(Exception UnSupportedEncodingException){
            Log.d("Encoding: ", "Failed to encode search parameter, using un-encoded value");
            return input;
        }
        return encodedVal;
    }

    @Override
    public void onItemClick(View view, int position){
        //add functionality to go to url
        String URL = adapter.getItem(position).getUrl();
        Uri uri = Uri.parse(URL);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // Set up accelerometer shake detection
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // When searching by text, get search string from intent
        Intent intent = getIntent();
        if (intent.getExtras().containsKey(EXTRA_SEARCH_VAL))
            searchVal = intent.getStringExtra(EXTRA_SEARCH_VAL);
        if (intent.getExtras().containsKey(EXTRA_LOC_VAL))
            location = intent.getStringExtra(EXTRA_LOC_VAL);

        queue = Volley.newRequestQueue(getApplicationContext());

        //Add the search value to the query
        if(!(location == null)){
            searchVal += " " + location;
        }

        String qParam = encodeVal(searchVal);
        editURL = url + qParam;

        // Create a new JsonObjectRequest that requests available search info
        JsonObjectRequest requestObj = new JsonObjectRequest
                (Request.Method.GET, editURL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "JSON response: " + response.toString());
                        info = parseJson(response);

                        RecyclerView recyclerView = findViewById(R.id.result_recycler_view);
                        recyclerView.setLayoutManager(new LinearLayoutManager(ResultsActivity.this));
                        adapter = new SearchValueAdapter(ResultsActivity.this, info);
                        adapter.setClickListener(ResultsActivity.this);
                        recyclerView.setAdapter(adapter);

                        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
                        recyclerView.addItemDecoration(dividerItemDecoration);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Error: " + error.toString());
                    }
                })
        {
            //Pass request headers
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(HEADER_SUB_KEY, PRIVATE_KEY_1);
                return headers;
            }
        };
        // Add the request to the RequestQueue
        queue.add(requestObj);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
    }

    @Override
    public void onResetSelectedClick(Boolean reset) {
        if (reset) super.onBackPressed();
    }

    public void askToReset() {
        FragmentManager manager = getSupportFragmentManager();
        ResetDialogFragment dialog = new ResetDialogFragment();
        dialog.show(manager, "resetDialog");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        mAccelLast = mAccelCurrent;
        mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
        float delta = mAccelCurrent - mAccelLast;
        mAccel = mAccel * 0.9f + delta;
        if (mAccel > 12) {
            Log.d("SHAKE", "IN SHAKE CALL");
            askToReset();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public void refineClick(View view){
        refineET = findViewById(R.id.refineEditText);
        String refineText = refineET.getText().toString();
        if(!refineText.equals("")) {
            refineSearch(refineText);
        }
    }

    private void refineSearch(String newStr){

        String qParam = encodeVal(searchVal + " " + newStr);
        String editURL = url + qParam;

        // Create a new JsonObjectRequest that requests available search info
        JsonObjectRequest requestObj = new JsonObjectRequest
                (Request.Method.GET, editURL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "JSON response: " + response.toString());

                        clearAdapterData();

                        info = parseJson(response);

                        RecyclerView recyclerView = findViewById(R.id.result_recycler_view);
                        recyclerView.setLayoutManager(new LinearLayoutManager(ResultsActivity.this));
                        adapter = new SearchValueAdapter(ResultsActivity.this, info);
                        adapter.setClickListener(ResultsActivity.this);
                        recyclerView.setAdapter(adapter);

                        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
                        recyclerView.addItemDecoration(dividerItemDecoration);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Error: " + error.toString());
                    }
                })
        {
            //Pass request headers
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(HEADER_SUB_KEY, PRIVATE_KEY_1);
                return headers;
            }
        };
        // Add the request to the RequestQueue
        queue.add(requestObj);
    }

    private ArrayList<ResultValue> parseJson (JSONObject json){
        ArrayList<ResultValue> infoList = new ArrayList<>();

        try{
            JSONObject tempObj = json.getJSONObject("webPages");

            JSONArray values = tempObj.getJSONArray("value");
            for (int i = 0; i < 10; ++i){
                String nameVal = values.getJSONObject(i).getString("name");
                String urlVal = values.getJSONObject(i).getString("url");
                String snippetVal = values.getJSONObject(i).getString("snippet");
                snippetVal = snippetVal.substring(0,50) + "...";
                //For some reason editing the nameVal with substring causes an invalid JSON parse
//                nameVal = nameVal.substring(0,45) + "...";

                ResultValue item = new ResultValue(nameVal, urlVal, snippetVal);
                infoList.add(item);
            }
        }
        catch (Exception e){
            Log.e(TAG, "One or more fields not found in JSON data");
        }

        return infoList;
    }

    private void clearAdapterData(){

        int size = info.size();

        if(info.size() > 0) {
            for (int i = 0; i < size; ++i) {
                info.remove(0);
            }
            adapter.notifyItemRangeRemoved(0, size);
        }
    }
}