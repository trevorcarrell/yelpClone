package com.carrtre.yelpclone

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


private const val BASE_URL = "https://api.yelp.com/v3/"
private const val TAG = "MainActivity"
private const val API_KEY = "v1RAlK4UIvX_1MaFvGGhDu6p-vePWZA_ipEbuZ8gvk0VP5BWOz-ugf4whvqn7qMhc193UTco3POPxVVHu6imJPfW2x1sQw3cKDftmGCIFPTK6PMRTiVpMg9epFSQYXYx"
private lateinit var rvRestaurants: RecyclerView
private lateinit var etSearch: EditText

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rvRestaurants = findViewById(R.id.rvRestaurants)
        val restaurants = mutableListOf<YelpRestaurant>()
        rvRestaurants.layoutManager = LinearLayoutManager(this)
        etSearch= findViewById(R.id.etSearch)

        val retrofit = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
        val yelpService = retrofit.create(YelpService::class.java)

        etSearch.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                updateRecyclerView(restaurants, yelpService)
            }

        })
    }

    private fun updateRecyclerView(restaurants: MutableList<YelpRestaurant>, yelpService: YelpService) {
        if (etSearch.text.isEmpty()) {
            displayRecyclerView(restaurants, "Boba", yelpService)
        }
        else {
            displayRecyclerView(restaurants, etSearch.text.toString(), yelpService)
        }
    }

    private fun displayRecyclerView(restaurants: MutableList<YelpRestaurant>, term: String, yelpService: YelpService) {
        val adapter = RestaurantsAdapter(this, restaurants)
        rvRestaurants.adapter = adapter
        yelpService.searchRestaurants("Bearer $API_KEY", term, "Stanford").enqueue(object : Callback<YelpSearchResult> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<YelpSearchResult>, response: Response<YelpSearchResult>) {
                Log.i(TAG, "onReponse $response")
                val body = response.body()
                if (body == null) {
                    Log.w(TAG, "Did not recieve valid response body from Yelp API... exiting")
                    return
                }
                restaurants.clear()
                restaurants.addAll(body.restaurants)
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<YelpSearchResult>, wt: Throwable) {
                Log.i(TAG, "onFailure $wt")
            }
        })
    }
}