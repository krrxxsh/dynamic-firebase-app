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
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView bannerTitle;
    private TextView bannerSubtitle;
    private ImageView bannerImage;

    private final SupabaseClient supabase = new SupabaseClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bannerTitle = findViewById(R.id.bannerTitle);
        bannerSubtitle = findViewById(R.id.bannerSubtitle);
        bannerImage = findViewById(R.id.bannerImage);

        Toast.makeText(this, "MainActivity onCreate()", Toast.LENGTH_SHORT).show();

        fetchPromo();
    }

    private void fetchPromo() {
        supabase.fetchLatestPromo(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    bannerTitle.setText("Network error: " + e.getMessage());
                    bannerSubtitle.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                });
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
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
                try {
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
                                    .centerCrop() // fill the 16:9 hero slot
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
                }
            }
        });
    }
}