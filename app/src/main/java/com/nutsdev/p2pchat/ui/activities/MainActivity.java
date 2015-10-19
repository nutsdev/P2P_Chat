package com.nutsdev.p2pchat.ui.activities;

import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.nutsdev.p2pchat.R;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.SystemService;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    private boolean isWifiP2pEnabled = false;

    private List<WifiP2pDevice> peersList = new ArrayList<>();

    @SystemService
    WifiP2pManager p2pManager;

    private WifiP2pManager.Channel channel;


    /* lifecycle */

    @AfterInject
    void afterInject() {
        channel = p2pManager.initialize(this, getMainLooper(), channelListener);
        p2pManager.discoverPeers(channel, actionListener);
    }


    /* broadcast receiver */

    @Receiver(actions = WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    protected void p2pStateChanged(Intent intent) {
        // Check to see if Wi-Fi is enabled and notify appropriate activity
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(MainActivity.this, "direct mode is enabled", Toast.LENGTH_SHORT).show();
                isWifiP2pEnabled = true;
            } else {
                Toast.makeText(MainActivity.this, "direct mode is disabled", Toast.LENGTH_SHORT).show();
                isWifiP2pEnabled = false;
            }
        }
    }

    @Receiver(actions = WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    protected void p2pPeersChanged(Intent intent) {
        // Call WifiP2pManager.requestPeers() to get a list of current peers
        // request available peers from the wifi p2p manager. This is an
        // asynchronous call and the calling activity is notified with a
        // callback on PeerListListener.onPeersAvailable()
        if (p2pManager != null) {
            p2pManager.requestPeers(channel, peerListListener);
        }
    }

    @Receiver(actions = WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    protected void p2pConnectionChanged(Intent intent) {
        // Respond to new connection or disconnections
        if (p2pManager == null)
            return;

        NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

        if (networkInfo.isConnected()) {
            p2pManager.requestConnectionInfo(channel, connectionInfoListener);
        }
    }

    @Receiver(actions = WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    protected void p2pDeviceDetailsChanged(Intent intent) {
        // Respond to this device's wifi state changing
        toString();
    }


    /* listeners */

    private WifiP2pManager.ChannelListener channelListener = new WifiP2pManager.ChannelListener() {
        @Override
        public void onChannelDisconnected() {
            Toast.makeText(MainActivity.this, "onChannelDisconnected", Toast.LENGTH_SHORT).show();
        }
    };

    private WifiP2pManager.ActionListener actionListener = new WifiP2pManager.ActionListener() {
        @Override
        public void onSuccess() {
            Toast.makeText(MainActivity.this, "onSuccess", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailure(int reason) {
            Toast.makeText(MainActivity.this, "reason " + reason, Toast.LENGTH_SHORT).show();
        }
    };

    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peers) {
            peersList.clear();
            peersList.addAll(peers.getDeviceList());
            Collection<WifiP2pDevice> deviceList = peers.getDeviceList();
        }
    };

    private WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            if (info.groupFormed && info.isGroupOwner) {
                startServerSocket();
            } else if (info.groupFormed) {

            }
        }
    };


    /* background tasks */

    @Background
    protected void startServerSocket() {
        try {
            ServerSocket serverSocket = new ServerSocket(8988);
            Log.d("MainActivity", "Server: Socket opened");
            Socket client = serverSocket.accept();
            Log.d("MainActivity", "Server: connection done");

            // todo accept incoming bytes

            Log.d("MainActivity", "server: accepted message");
            InputStream inputstream = client.getInputStream();
            serverSocket.close();
        } catch (IOException e) {
            Log.e("MainActivity", e.getMessage());
        }
    }

}
