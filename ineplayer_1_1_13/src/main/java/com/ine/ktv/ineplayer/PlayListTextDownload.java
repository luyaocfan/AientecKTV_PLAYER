package com.ine.ktv.ineplayer;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

class PlayListTextDownload extends AsyncTask<String, Void, ArrayList<String>> {
    public interface EventListener {
        void OnExecuteResult(ArrayList<String> result, boolean bInsert);
    };
    private boolean bInsert;
    private EventListener listener;

    public PlayListTextDownload(EventListener listener,boolean bInsert) {
        this.bInsert = bInsert;
        this.listener = listener;
    }

    @Override
    protected ArrayList<String> doInBackground(String... urls) {
        try {
            URL url = new URL(urls[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(150000); //milliseconds
            conn.setConnectTimeout(15000); // milliseconds
            conn.setRequestMethod("GET");

            conn.connect();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                ArrayList<String>  lines = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(), "UTF-8"));

                String line = null;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);

                }
                return lines;
            } else {

                return null;
            }


        } catch (Exception e) {
            // System.out.println("exception in jsonparser class ........");
            e.printStackTrace();
            return null;
        }
    }

    protected void onPostExecute(ArrayList<String> result) {
        if(result!=null)
            listener.OnExecuteResult(result, bInsert);
    }
}