package com.julianogv.wifihelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.julianogv.wifihelper.listeners.InterstitialAdListener;

public class MainActivity extends Activity {
    WifiManager wifiManager;
    private Handler handler;
    BroadcastFillWifiInfo broadcastWifiInfo;
    TextView txtTolerate;
    SeekBar sbTolerate;
    ToggleButton switchServiceStatus;
    Context ctx;
    CheckBox checkBoxAutoSwitch;
    ListView wifiInfoListView;
    public static long delay = 0;
    SharedPreferences settings;
    SharedPreferences.Editor preferencesEditor;

    private Runnable wifiStartScanRunnable = new Runnable() {
        @Override
        public void run() {
            wifiManager.startScan();
            handler.postDelayed(this, Defines.WIFI_SCAN_RESULTS_DELAY);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = getSharedPreferences(Defines.PREFS_NAME, 0);
        preferencesEditor = settings.edit();

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        ctx = this;
        checkBoxAutoSwitch = (CheckBox) findViewById(R.id.checkAutoSwitch);
        txtTolerate = (TextView) findViewById(R.id.txtTolerate);
        sbTolerate = (SeekBar)findViewById(R.id.seekBarTolerate);
        switchServiceStatus = (ToggleButton)findViewById(R.id.switchServiceStatus);
        wifiInfoListView = (ListView)findViewById(R.id.listWifi);

        restoreSavedPreferences();
        prepareListeners();
        setBroadcastReceivers();
        startWifiScanThread();

        InterstitialAd interstitialAds = new InterstitialAd(this);
        interstitialAds.setAdUnitId("ca-app-pub-1817810316504207/1298383595");
        interstitialAds.setAdListener(new InterstitialAdListener(interstitialAds));
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitialAds.loadAd(adRequest);

        AlertDialog.Builder alertBox = new AlertDialog.Builder(this);
        alertBox.setNeutralButton("OK", null);
        alertBox.setMessage(R.string.working);
        alertBox.setIcon(R.drawable.ic_launcher);
        alertBox.show();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastWifiInfo);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void restoreSavedPreferences(){
        txtTolerate.setText(settings.getInt(Defines.TOLERATE_PREFS_NAME, 0) + "");
        sbTolerate.setProgress(settings.getInt(Defines.TOLERATE_PREFS_NAME, 0));
        switchServiceStatus.setChecked(settings.getBoolean(Defines.SHOULD_RUN, true));
        checkBoxAutoSwitch.setChecked(settings.getBoolean(Defines.AUTO_SWITCH_PREFS_NAME, false));
    }

    public void setBroadcastReceivers(){
        //receiver responsable for filling wifi list at main activity
        broadcastWifiInfo = new BroadcastFillWifiInfo();
        IntentFilter intentFilter = new IntentFilter(Defines.FILL_DATA);
        registerReceiver(broadcastWifiInfo, intentFilter);
    }

    public void startWifiScanThread(){
        //I've heard its better to use handler than timer
        handler = new Handler();
        handler.removeCallbacks(wifiStartScanRunnable);
        handler.postDelayed(wifiStartScanRunnable, 100);
    }

    public void prepareListeners(){
        sbTolerate.setOnSeekBarChangeListener(seekBarTolerateOnSeekListener);
        switchServiceStatus.setOnCheckedChangeListener(switchHandlers);

        checkBoxAutoSwitch.setOnCheckedChangeListener(checkBoxAutoSwitchHandler);
    }

    SeekBar.OnSeekBarChangeListener seekBarTolerateOnSeekListener = new SeekBar.OnSeekBarChangeListener(){
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            txtTolerate.setText(i+"");
            preferencesEditor = settings.edit();
            preferencesEditor.putInt(Defines.TOLERATE_PREFS_NAME, Integer.parseInt(txtTolerate.getText().toString()));
            preferencesEditor.commit();
        }
    };

    CheckBox.OnCheckedChangeListener checkBoxAutoSwitchHandler = new CheckBox.OnCheckedChangeListener(){

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            preferencesEditor.putBoolean(Defines.AUTO_SWITCH_PREFS_NAME, isChecked);
            preferencesEditor.commit();
        }
    };

    ToggleButton.OnCheckedChangeListener switchHandlers = new ToggleButton.OnCheckedChangeListener(){

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if(isChecked){
                Toast.makeText(ctx, "Starting service!", Toast.LENGTH_SHORT).show();
                startWifiScanThread();
            }else{
                handler.removeCallbacks(wifiStartScanRunnable);
            }
            preferencesEditor.putBoolean(Defines.SHOULD_RUN, isChecked);
            preferencesEditor.commit();
        }

    };

    public class BroadcastFillWifiInfo extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ctx,
                    android.R.layout.simple_list_item_1, intent.getStringArrayListExtra("data"));
            wifiInfoListView.setAdapter(arrayAdapter);
        }
    }
}