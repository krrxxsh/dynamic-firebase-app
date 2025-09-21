package com.example.dynamic_supabase_app;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class SupabaseClient {

    // Your project base URL (no trailing slash)
    private static final String SUPABASE_PROJECT_URL = "https://rgrbnlzqyqktdtpvttbl.supabase.co";
    // Your anon key (safe for public clients, but don't commit secrets)
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJncmJubHpxeXFrdGR0cHZ0dGJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTg0NDI1NzIsImV4cCI6MjA3NDAxODU3Mn0.D64U_fZlaUpk7_BqTyj9xQuqxkyG3RJjBnpQEiTvZXA";

    // Query: latest row by id; change to created_at.desc if you have that column
    private static final String PROMO_QUERY =
            SUPABASE_PROJECT_URL + "/rest/v1/promo?select=message,image_url&order=id.desc&limit=1";

    private final OkHttpClient client = new OkHttpClient();

    public void fetchLatestPromo(Callback callback) {
        Request req = new Request.Builder()
                .url(PROMO_QUERY)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .build();
        Call call = client.newCall(req);
        call.enqueue(callback);
    }
}