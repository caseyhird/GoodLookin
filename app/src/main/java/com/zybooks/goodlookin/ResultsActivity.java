package com.zybooks.goodlookin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

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

public class ResultsActivity extends AppCompatActivity implements SearchValueAdapter.ItemClickListener {
    public static final String EXTRA_SEARCH_VAL = "search_string";
    private static final String TAG = "bingSearch";
    private static final String HEADER_SUB_KEY = "Ocp-Apim-Subscription-Key";
    private static final String PRIVATE_KEY_1 = "474c1e670b084ad0bafd268a056602ab";
    private String searchVal;
    private String url = "https://api.bing.microsoft.com/v7.0/search?q=";
    private ArrayList<ResultValue> info = new ArrayList<>();
    SearchValueAdapter adapter;

    private String encodeVal(String input){

        String encodedVal;

        try{
            encodedVal = URLEncoder.encode(input, StandardCharsets.UTF_8.toString());
        }catch(Exception UnSupportedEncodingException){
            System.out.println("Failed to encode search parameter, using un-encoded value");
            return input;
        }
        return encodedVal;
    }

    @Override
    public void onItemClick(View view, int position){
        //add functionality to go to url
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // When searching by text, get search string from intent
        Intent intent = getIntent();
        if (intent.getExtras().containsKey(EXTRA_SEARCH_VAL))
            searchVal = intent.getStringExtra(EXTRA_SEARCH_VAL);
        //TextView t = findViewById(R.id.searchString); // FIXME Need to implement an actual UI for results

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        //Add the search value to the query
        String qParam = encodeVal(searchVal);
        url += qParam;

        //Create adapter for data set to be populated by JSON data
//        SearchValueAdapter adapter = new SearchValueAdapter(this, info);

        // Create a new JsonObjectRequest that requests available weather info
        JsonObjectRequest requestObj = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "JSON response: " + response.toString());
                        info = parseJson(response);
                        adapter.notifyDataSetChanged();
                        //Test print
//                        t.setText(info.get(0).getSnippet());
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

        RecyclerView recyclerView = findViewById(R.id.result_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchValueAdapter(this, info);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

//        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
//                layoutManager.getOrientation());
//        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    private ArrayList<ResultValue> parseJson (JSONObject json){
        ArrayList<ResultValue> infoList = new ArrayList<>();

        try{
            JSONObject tempObj = json.getJSONObject("webPages");

            JSONArray values = tempObj.getJSONArray("value");
            for (int i = 0; i < 3; ++i){
                String name = values.getJSONObject(i).getString("name");
                String url = values.getJSONObject(i).getString("url");
                String snippet = values.getJSONObject(i).getString("snippet");

                ResultValue item = new ResultValue(name, url, snippet);
                infoList.add(item);
            }
        }
        catch (Exception e){
            Log.e(TAG, "One or more fields not found in JSON data");
        }

        return infoList;
    }
}