package com.zybooks.goodlookin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
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

        // When searching by text, get search string from intent
        Intent intent = getIntent();
        if (intent.getExtras().containsKey(EXTRA_SEARCH_VAL))
            searchVal = intent.getStringExtra(EXTRA_SEARCH_VAL);

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        //Add the search value to the query
        String qParam = encodeVal(searchVal);
        url += qParam;

        // Create a new JsonObjectRequest that requests available weather info
        JsonObjectRequest requestObj = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "JSON response: " + response.toString());
                        info = parseJson(response);
                        System.out.println("PARSE JSON FUNCTION CALLED");
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
}