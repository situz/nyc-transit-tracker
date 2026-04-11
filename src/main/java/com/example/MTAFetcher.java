package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class MTAFetcher{
    private String apiKey;

    /**
     * API key: environment variable MTA_API_KEY first, then mta.api.key in config.properties
     * (see config.properties.example).
     */
    public MTAFetcher(){
        String fromEnv = System.getenv("MTA_API_KEY");
        if (fromEnv != null && !fromEnv.trim().isEmpty()) {
            apiKey = fromEnv.trim();
            return;
        }

        Properties props = new Properties();
        try (InputStream configInput = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (configInput == null) {
                throw new IllegalStateException(
                        "MTA API key not set. Set environment variable MTA_API_KEY, or add src/main/resources/config.properties "
                                + "(copy from config.properties.example).");
            }
            props.load(configInput);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load config.properties", e);
        }
        apiKey = props.getProperty("mta.api.key");
        if (apiKey != null) {
            apiKey = apiKey.trim();
        }
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException(
                    "MTA API key not set. Set MTA_API_KEY or mta.api.key in config.properties (see config.properties.example).");
        }
    }
    public String getBusTime(String busStop){
        HttpURLConnection conn = null;
        try{
            String urlString = "https://bustime.mta.info/api/siri/stop-monitoring.json" + "?key=" + apiKey + "&MonitoringRef="+ busStop +"&StopMonitoringDetailLevel=normal";
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            //conn.setRequestProperty("x-api-key", apiKey);

            int status = conn.getResponseCode();
            if (status != 200){
                throw new IOException("HTTP Error:" + status);
            }

            InputStreamReader apiInput = new InputStreamReader(conn.getInputStream());

            try(BufferedReader buffer = new BufferedReader(apiInput)){

            String line = buffer.readLine();
            StringBuilder response = new StringBuilder();
            while (line != null){
                response.append(line);
                line = buffer.readLine();
            }
            return response.toString();
            }
            
        }
        catch(IOException e){
            e.printStackTrace();
            return null;
        }
        finally {
                if (conn != null) {
                    conn.disconnect();
                }
        }
    }
}