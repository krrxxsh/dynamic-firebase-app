package com.example.dynamic_supabase_app;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView promoMessage;
    private ImageView promoImage;
    private OkHttpClient client;
    private Handler handler;

    // Change these to your Supabase project details
    private final String SUPABASE_URL = "https://rgrbnlzqyqktdtpvttbl.supabase.co/rest/v1/promo?select=message,image_url&order=id.desc&limit=1";
    private final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJncmJubHpxeXFrdGR0cHZ0dGJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTg0NDI1NzIsImV4cCI6MjA3NDAxODU3Mn0.D64U_fZlaUpk7_BqTyj9xQuqxkyG3RJjBnpQEiTvZXA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        promoMessage = findViewById(R.id.promoMessage);
        promoImage = findViewById(R.id.promoImage);
        client = new OkHttpClient();
        handler = new Handler();

        fetchPromo();

        // Poll every 10 seconds for real-time effect (simple polling)
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchPromo();
                handler.postDelayed(this, 10000); // 10 seconds
            }
        }, 10000);
    }

    private void fetchPromo() {
        Request request = new Request.Builder()
                .url(SUPABASE_URL)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    try {
                        JSONArray jsonArray = new JSONArray(responseBody);
                        if (jsonArray.length() > 0) {
                            JSONObject promo = jsonArray.getJSONObject(0);
                            final String message = promo.getString("message");
                            final String imageUrl = promo.getString("image_url");

                            runOnUiThread(() -> {
                                promoMessage.setText(message);
                                Glide.with(MainActivity.this).load(imageUrl).into(promoImage);
                            });
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error parsing promo data", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error fetching promo data", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Network error", Toast.LENGTH_SHORT).show());
            }
        });
    }
}