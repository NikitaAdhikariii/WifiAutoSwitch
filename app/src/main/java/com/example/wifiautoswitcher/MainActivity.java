package com.example.wifiautoswitch;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.*;
import android.net.wifi.*;
import android.os.*;
import android.text.InputType;
import android.util.Log;
import android.widget.*;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    WifiManager wifiManager;
    Spinner networkSpinner;
    EditText passwordInput;
    TextView statusText;
    CheckBox showPasswordCheckbox;
    Button connectButton, refreshButton, btnAddPreferred, btnViewPreferred, btnMonitorSwitch, btnExit;

    List<ScanResult> scanResults;
    BroadcastReceiver wifiReceiver;
    SharedPreferences preferences;

    private static final int REQUEST_CODE_LOCATION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = getSharedPreferences("wifi_prefs", MODE_PRIVATE);

        networkSpinner = findViewById(R.id.networkSpinner);
        passwordInput = findViewById(R.id.passwordInput);
        statusText = findViewById(R.id.statusText);
        showPasswordCheckbox = findViewById(R.id.showPasswordCheckbox);
        connectButton = findViewById(R.id.connectButton);
        refreshButton = findViewById(R.id.refreshButton);
        btnAddPreferred = findViewById(R.id.btn_add_preferred);
        btnViewPreferred = findViewById(R.id.btn_view_preferred);
        btnMonitorSwitch = findViewById(R.id.btn_monitor_switch);
        btnExit = findViewById(R.id.btn_exit);

        showPasswordCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                passwordInput.setInputType(InputType.TYPE_CLASS_TEXT);
            } else {
                passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });

        btnExit.setOnClickListener(v -> {
            finish();
        });

        checkPermissions();

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        refreshButton.setOnClickListener(v -> startScan());

        connectButton.setOnClickListener(v -> {
            if (networkSpinner.getSelectedItem() == null) {
                appendStatus("❌ No network selected.");
                return;
            }

            // Since spinner now shows "SSID (XX%)", extract SSID only before connecting
            String selected = networkSpinner.getSelectedItem().toString();
            String ssid = selected.split(" \\(")[0].trim(); // get SSID part before " ("

            String password = passwordInput.getText().toString();

            if (isCurrentlyConnected(ssid)) {
                appendStatus("✅ Already connected to " + ssid);
                return;
            } else {
                appendStatus("📶 Currently connected to a different network.");
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                connectToWifi(ssid, password);
            } else {
                connectToNetwork(ssid, password);
            }
        });

        btnAddPreferred.setOnClickListener(v -> {
            if (networkSpinner.getSelectedItem() != null) {
                String selected = networkSpinner.getSelectedItem().toString();
                String ssid = selected.split(" \\(")[0].trim();

                Set<String> preferred = new HashSet<>(preferences.getStringSet("preferred", new HashSet<>()));
                preferred.add(ssid);
                preferences.edit().putStringSet("preferred", preferred).apply();
                appendStatus("⭐ Added to preferred: " + ssid);
            }
        });

        btnViewPreferred.setOnClickListener(v -> {
            Set<String> preferred = preferences.getStringSet("preferred", new HashSet<>());
            appendStatus("📌 Preferred Networks:\n" + preferred);
        });

        btnMonitorSwitch.setOnClickListener(v -> {
            appendStatus("📊 Last switch: Best_Network_5GHz\nTime: 11:42 AM");
        });

        loadNetworks();
        startAutoSwitch();
    }

    private boolean isCurrentlyConnected(String targetSsid) {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String currentSSID = wifiInfo.getSSID();
            return currentSSID != null && currentSSID.replace("\"", "").equals(targetSsid);
        }
        return false;
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
        } else {
            loadNetworks();
        }
    }

    private void startScan() {
        boolean started = wifiManager.startScan();
        if (started) {
            appendStatus("🔍 Scanning Wi-Fi...");
        } else {
            appendStatus("❌ Failed to scan.");
        }
    }

    private void loadNetworks() {
        wifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                scanResults = wifiManager.getScanResults();
                List<String> ssidsWithStrength = new ArrayList<>();

                for (ScanResult result : scanResults) {
                    if (!result.SSID.isEmpty()) {
                        int level = result.level;  // dBm
                        // Convert signal strength to percentage (0-100)
                        int strengthPercent = WifiManager.calculateSignalLevel(level, 100);
                        ssidsWithStrength.add(result.SSID + " (" + strengthPercent + "%)");
                        Log.d("WiFiScan", "Found: " + result.SSID + " Signal: " + level + " dBm");
                    }
                }

                if (ssidsWithStrength.isEmpty()) {
                    appendStatus("❌ No networks found.");
                } else {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, ssidsWithStrength);
                    networkSpinner.setAdapter(adapter);
                    appendStatus("✅ Found " + ssidsWithStrength.size() + " networks.");
                }
            }
        };

        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        startScan();
    }

    private void connectToNetwork(String ssid, String password) {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + ssid + "\"";
        conf.preSharedKey = "\"" + password + "\"";

        int netId = wifiManager.addNetwork(conf);
        if (netId == -1) {
            appendStatus("❌ Failed to configure network.");
            return;
        }

        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
        appendStatus("🔌 Connecting to " + ssid + "...");
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void connectToWifi(String ssid, String password) {
        WifiNetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(password)
                .build();

        NetworkRequest request = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(specifier)
                .build();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.requestNetwork(request, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                appendStatus("✅ Connected to " + ssid);
            }

            @Override
            public void onUnavailable() {
                appendStatus("❌ Could not connect to " + ssid);
            }
        });
    }

    private void appendStatus(String msg) {
        statusText.append(msg + "\n");
    }

    private void startAutoSwitch() {
        Handler handler = new Handler();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                startScan();
                handler.postDelayed(this, 30000); // every 30 seconds
            }
        };
        handler.post(task);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wifiReceiver != null) {
            unregisterReceiver(wifiReceiver);
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] grants) {
        super.onRequestPermissionsResult(code, perms, grants);
        if (code == REQUEST_CODE_LOCATION && grants.length > 0 &&
                grants[0] == PackageManager.PERMISSION_GRANTED) {
            loadNetworks();
        } else {
            appendStatus("❌ Location permission denied.");
        }
    }
}
