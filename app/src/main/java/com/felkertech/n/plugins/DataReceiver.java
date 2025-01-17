package com.felkertech.n.plugins;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.tv.TvContract;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.example.android.sampletvinput.syncadapter.SyncUtils;
import com.felkertech.n.ActivityUtils;
import com.felkertech.n.boilerplate.Utils.SettingsManager;
import com.felkertech.n.cumulustv.ChannelDatabase;
import com.felkertech.n.cumulustv.JSONChannel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import org.json.JSONException;
import org.json.JSONObject;

public class DataReceiver extends BroadcastReceiver
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static String INTENT_EXTRA_JSON = "JSON";
    public static String INTENT_EXTRA_ORIGINAL_JSON = "OGJSON";
    public static String INTENT_EXTRA_ACTION = "Dowhat";
    public static String INTENT_EXTRA_ACTION_WRITE = "Write";
    public static String INTENT_EXTRA_ACTION_DELETE = "Delete";
    public static String INTENT_EXTRA_SOURCE = "Source";
    public static String TAG = "cumulus:DataReceiver";

    private GoogleApiClient gapi;
    private Context mContext;
    public DataReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        gapi = new GoogleApiClient.Builder(context)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        Log.d(TAG, "Heard");
        if(intent != null) {
            String jsonString = intent.getStringExtra(INTENT_EXTRA_JSON);
            String action = intent.getStringExtra(INTENT_EXTRA_ACTION);
            if(action.equals(INTENT_EXTRA_ACTION_WRITE)) {
                Log.d(TAG, "Received " + jsonString);
                SettingsManager sm = new SettingsManager(context);
                ChannelDatabase cdn = new ChannelDatabase(context);
                try {
                    JSONObject jo = new JSONObject(jsonString);
                    JSONChannel jsonChannel = new JSONChannel(jo);
                    jsonChannel.setSource(intent.getStringExtra(INTENT_EXTRA_SOURCE));
                    if(intent.hasExtra(INTENT_EXTRA_ORIGINAL_JSON)) {
                        //Clearly edited a stream
                        JSONChannel original = new JSONChannel(new JSONObject(intent.getStringExtra(INTENT_EXTRA_ORIGINAL_JSON)));
                        for(int i = 0; i < cdn.getJSONChannels().length(); i++) {
                            JSONChannel item = new JSONChannel(cdn.getJSONChannels().getJSONObject(i));
                            if(original.equals(item)) {
                                Log.d(TAG, "Found a match");

                            }
                        }
                    } else {

                    }
                    if (cdn.channelExists(jsonChannel)) {
                        //Channel exists, so let's update
                        cdn.update(jsonChannel);
                        Log.d(TAG, "Channel updated");
                    } else {
                        cdn.add(jsonChannel);
                        Log.d(TAG, "Channel added");
                    }
                    gapi.connect();
                    //Now sync
                    final String info = TvContract.buildInputId(new ComponentName("com.felkertech.n.cumulustv", ".SampleTvInput"));
                    SyncUtils.requestSync(info);
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage() + "; Error while adding");
                    e.printStackTrace();
                }
            } else if(action.equals(INTENT_EXTRA_ACTION_DELETE)) {
                ChannelDatabase cdn = new ChannelDatabase(context);
                try {
                    JSONObject jo = new JSONObject(jsonString);
                    JSONChannel jsonChannel = new JSONChannel(jo);
                    cdn.delete(jsonChannel);
                    Log.d(TAG, "Channel successfully deleted");
                    //Now sync
                    gapi.connect();
                    final String info = TvContract.buildInputId(new ComponentName("com.felkertech.n.cumulustv", ".SampleTvInput"));
                    SyncUtils.requestSync(info);
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage() + "; Error while adding");
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Connected. Now what?");
        //Let's sync with GDrive
        ActivityUtils.writeDriveData(mContext, gapi);
        Log.d(TAG, "Sync w/ drive");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
