package com.clennnzy.textapi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Html;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import android.Manifest;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements HttpHandler {
    private boolean hasStarted = false;
    private Button startButton;
    private final int PORT = 0x1F90;
    private ScrollView scrollView;
    private TextView textViewState;
    private HttpServer httpServer = null;
    private TextView logsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();

        startButton = findViewById(R.id.buttonStart);
        scrollView = findViewById(R.id.scrollView);
        textViewState = findViewById(R.id.textViewState);
        logsTextView = findViewById(R.id.logsTextView);

        startButton.setOnClickListener(view -> {
            if (!hasStarted && httpServer == null) {
                try {
                    httpServer = HttpServer.create(new InetSocketAddress(getMobileIPAddress(), PORT), 0);
                    if(httpServer != null) {
                        httpServer.createContext("/", this::handle);
                        httpServer.start();
                        textViewState.setText(R.string.server_running);
                        String logs = logsTextView.getText().toString();
                        logs += "<br>Running at: http:/" + httpServer.getAddress();
                        logsTextView.setText(Html.fromHtml(logs));
                        hasStarted = true;
                        startButton.setText(R.string.server_stop);
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.cannot_start, Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), R.string.cannot_start_ex, Toast.LENGTH_SHORT).show();
                }
            } else {
                httpServer.stop(1);
                httpServer = null;
                hasStarted = false;
                textViewState.setText(R.string.server_running);
                startButton.setText(R.string.start_server);
                logsTextView.setText("Status: Not running");
            }
        });
    }

    public String getMobileIPAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        return  addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Couldn't resolve IP address.", Toast.LENGTH_SHORT).show();
        }
        return "";
    }

    protected void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        0);
            }
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Map<String, Object> parameters = new HashMap<String, Object>();
        URI requestedUri = exchange.getRequestURI();
        String query = requestedUri.getRawQuery();
        parseQuery(query, parameters);

        // send response
        String response = "{ response: 200, message : \"SMS Sent Successfully\" }";
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(parameters.get("phone").toString(), null, parameters.get("message").toString(), null, null);
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.toString().getBytes());
        os.close();
    }

    public static void parseQuery(String query, Map<String,
            Object> parameters) throws UnsupportedEncodingException {

        if (query != null) {
            String pairs[] = query.split("[&]");
            for (String pair : pairs) {
                String param[] = pair.split("[=]");
                String key = null;
                String value = null;
                if (param.length > 0) {
                    key = URLDecoder.decode(param[0],
                            System.getProperty("file.encoding"));
                }

                if (param.length > 1) {
                    value = URLDecoder.decode(param[1],
                            System.getProperty("file.encoding"));
                }

                if (parameters.containsKey(key)) {
                    Object obj = parameters.get(key);
                    if (obj instanceof List<?>) {
                        List<String> values = (List<String>) obj;
                        values.add(value);

                    } else if (obj instanceof String) {
                        List<String> values = new ArrayList<String>();
                        values.add((String) obj);
                        values.add(value);
                        parameters.put(key, values);
                    }
                } else {
                    parameters.put(key, value);
                }
            }
        }
    }
}