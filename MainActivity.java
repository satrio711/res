package com.example.x450j.wifi;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }
                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }
                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }

    private static final String REGISTER_URL = "http://simplifiedcoding.16mb.com/UserRegistration/volleyRegister.php";

    Button bt;
    TextView txt;

    // Response
    String responseServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt = (TextView) findViewById(R.id.raw);
        txt.setText(getMacAddr());

        Intent intent = new Intent();
        int WifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
        if (WifiState == WifiManager.WIFI_STATE_ENABLED) {
            Toast.makeText(MainActivity.this, "Wi-Fi enabled", Toast.LENGTH_LONG).show();
        } else if (WifiState == WifiManager.WIFI_STATE_ENABLING) {
            Toast.makeText(MainActivity.this, "Wi-Fi enabling", Toast.LENGTH_LONG).show();
        } else
            Toast.makeText(MainActivity.this, "Wi-Fi disabled", Toast.LENGTH_LONG).show();

        if (WifiState == WifiManager.WIFI_STATE_DISABLED) {
            Toast.makeText(MainActivity.this, "Wi-Fi disabled", Toast.LENGTH_LONG).show();
        } else if (WifiState == WifiManager.WIFI_STATE_DISABLING) {
            Toast.makeText(MainActivity.this, "Wi-Fi disabling", Toast.LENGTH_LONG).show();
        } else Toast.makeText(MainActivity.this, "Wi-Fi enabled", Toast.LENGTH_LONG).show();

        bt = (Button) findViewById(R.id.sendData);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncT asyncT = new AsyncT();
                asyncT.execute();
            }
        });
    }

    /* Inner class to get response */
    class AsyncT extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://192.168.0.68/monitoring/data.php");
//            HttpPost httppost = new HttpPost("http://localhost/test/test.php");
            URL url = null;

            try {
                url = new URL("http://192.168.0.68/monitoring/data.php");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                JSONObject jsonobj = new JSONObject();

                jsonobj.put("id", "user");
                jsonobj.put("time", "second");
                jsonobj.put("mac", "mac_address");
                jsonobj.put("status", "real time");

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("req", jsonobj.toString()));

                Log.e("mainToPost", "mainToPost" + nameValuePairs.toString());

                // Use UrlEncodedFormEntity to send in proper format which we need
                // httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                InputStream inputStream = (InputStream) response.getEntity().getContent(urlConnection);
                InputStreamToStringExample str = new InputStreamToStringExample();
                responseServer = str.getStringFromInputStream(inputStream);
                Log.e("response", "response -----" + responseServer);


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            txt.setText(responseServer);
        }
    }

    public static class InputStreamToStringExample {

        public static void main(String[] args) throws IOException {

            // intilize an InputStream
            InputStream is = new ByteArrayInputStream("file content..blah blah".getBytes());

            String result = getStringFromInputStream(is);

            System.out.println(result);
            System.out.println("Done");

        }

        // convert InputStream to String
        private static String getStringFromInputStream(InputStream is) {

            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();

            String line;
            try {

                br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return sb.toString();
        }

    }
}
