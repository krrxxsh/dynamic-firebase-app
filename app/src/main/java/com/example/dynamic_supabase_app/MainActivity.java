package com.example.dynamic_supabase_app;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

// NEW imports for polling
import android.os.Handler;
import android.os.Looper;

public class MainActivity extends AppCompatActivity {

    private TextView bannerTitle;
    private TextView bannerSubtitle;
    private ImageView bannerImage;

    private final SupabaseClient supabase = new SupabaseClient();

    // Polling support
    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean inFlight = new AtomicBoolean(false);
    private final Runnable pollTask = new Runnable() {
        @Override public void run() {
            if (!inFlight.get()) {
                fetchPromo();
            }
            // Re-run every 5 seconds (adjust interval as you like)
            pollHandler.postDelayed(this, 5000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bannerTitle = findViewById(R.id.bannerTitle);
        bannerSubtitle = findViewById(R.id.bannerSubtitle);
        bannerImage = findViewById(R.id.bannerImage);

        Toast.makeText(this, "MainActivity onCreate()", Toast.LENGTH_SHORT).show();

        // Initial load
        fetchPromo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start polling loop
        pollHandler.post(pollTask);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop polling when not visible
        pollHandler.removeCallbacks(pollTask);
    }

    private void fetchPromo() {
        // Prevent overlapping requests
        if (!inFlight.compareAndSet(false, true)) return;

        supabase.fetchLatestPromo(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                inFlight.set(false);
                runOnUiThread(() -> {
                    bannerTitle.setText("Network error: " + e.getMessage());
                    bannerSubtitle.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                });
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                try {
                    String body = response.body() != null ? response.body().string() : "";
                    if (!response.isSuccessful()) {
                        Log.e("PROMO", "HTTP " + response.code() + " " + body);
                        runOnUiThread(() -> {
                            bannerTitle.setText("HTTP " + response.code());
                            bannerSubtitle.setText(body);
                            bannerSubtitle.setVisibility(View.VISIBLE);
                        });
                        return;
                    }

                    JSONArray arr = new JSONArray(body);
                    if (arr.length() == 0) {
                        runOnUiThread(() -> {
                            bannerTitle.setText("No promos yet");
                            bannerSubtitle.setVisibility(View.GONE);
                            bannerImage.setImageDrawable(null);
                        });
                        return;
                    }

                    JSONObject obj = arr.getJSONObject(0);
                    final String message = obj.optString("message", "—");
                    final String subtitle = obj.optString("subtitle", "");
                    final String imageUrl = obj.optString("image_url", "");

                    runOnUiThread(() -> {
                        bannerTitle.setText(message);
                        if (subtitle == null || subtitle.trim().isEmpty()) {
                            bannerSubtitle.setVisibility(View.GONE);
                        } else {
                            bannerSubtitle.setText(subtitle);
                            bannerSubtitle.setVisibility(View.VISIBLE);
                        }

                        if (!imageUrl.isEmpty()) {
                            Glide.with(MainActivity.this)
                                    .load(imageUrl)
                                    .placeholder(android.R.color.darker_gray)
                                    .centerCrop() // fills the 16:9 hero slot in the layout
                                    .into(bannerImage);
                        } else {
                            bannerImage.setImageDrawable(null);
                        }
                    });
                } catch (Exception ex) {
                    Log.e("PROMO", "Parse error", ex);
                    runOnUiThread(() -> {
                        bannerTitle.setText("Parse error");
                        bannerSubtitle.setVisibility(View.GONE);
                    });
                } finally {
                    inFlight.set(false);
                }
            }
        });
    }
}