package com.nutsdev.p2pchat.ui.activities;

import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.nutsdev.p2pchat.R;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.SystemService;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    @SystemService
    WifiP2pManager p2pManager;

    private WifiP2pManager.Channel channel;

    @AfterInject
    void afterInject() {
        channel = p2pManager.initialize(this, getMainLooper(), null);
        p2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "onSuccess", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "reason " + reason, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Receiver(actions = WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    protected void p2pStateChanged(Intent intent) {
        // Check to see if Wi-Fi is enabled and notify appropriate activity
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
            } else {
                // Wi-Fi P2P is not enabled
            }
        }
    }

    @Receiver(actions = WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    protected void p2pPeersChanged(Intent intent) {
        // Call WifiP2pManager.requestPeers() to get a list of current peers
        toString();
    }

    @Receiver(actions = WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    protected void p2pConnectionChanged(Intent intent) {
        // Respond to new connection or disconnections
        toString();
    }

    @Receiver(actions = WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    protected void p2pDeviceDetailsChanged(Intent intent) {
        // Respond to this device's wifi state changing
        toString();
    }

}
