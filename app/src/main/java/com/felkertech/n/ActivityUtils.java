package com.felkertech.n;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.tv.TvContract;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.android.sampletvinput.syncadapter.SyncUtils;
import com.felkertech.n.boilerplate.Utils.SettingsManager;
import com.felkertech.n.cumulustv.ChannelDatabase;
import com.felkertech.n.cumulustv.JSONChannel;
import com.felkertech.n.cumulustv.MainActivity;
import com.felkertech.n.cumulustv.R;
import com.felkertech.n.cumulustv.SamplePlayer;
import com.felkertech.n.plugins.CumulusTvPlugin;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by guest1 on 10/29/2015.
 */
public class ActivityUtils {
    private static final String TAG = "cumulus:ActivityUtils";
    /* SUGGESTED CHANNELS */
    public static JSONChannel[] getSuggestedChannels() {
                /*
                0xx NEWS
                1xx SCIENCE/TECH/NATURE
                2xx HISTORY/EDUCATION
                3xx SPORTS/VIDEO GAMES
                4xx MUSIC
                5xx FICTION
                6xx NONFICTION
                7xx GOVERNMENT/SOCIETY
                9xx MISC
                 */
        final JSONChannel[] channels = { /* Some via http://rgw.ustream.tv/json.php/Ustream.searchBroadcast/ */
                new JSONChannel("100",
                        "NASA Public",
                        "http://iphone-streaming.ustream.tv/uhls/6540154/streams/live/iphone/playlist.m3u8",
                        "http://static-cdn1.ustream.tv/i/channel/live/1_6540154,256x144,b:2015071514.jpg", "",
                        TvContract.Programs.Genres.TECH_SCIENCE),
                new JSONChannel("101",
                        "ISS Stream",
                        "http://iphone-streaming.ustream.tv/uhls/9408562/streams/live/iphone/playlist.m3u8",
                        "http://static-cdn1.ustream.tv/i/channel/picture/9/4/0/8/9408562/9408562_iss_hr_1330361780,256x144,r:1.jpg", "",
                        TvContract.Programs.Genres.TECH_SCIENCE),
                new JSONChannel("133",
                        "TWiT (This Week in Tech)",
                        "http://twit.live-s.cdn.bitgravity.com/cdn-live-s1/_definst_/twit/live/high/playlist.m3u8",
                        "http://wiki.twit.tv/w/images/thumb/TWiT_Logo.svg.png/487px-TWiT_Logo.svg.png",
                        TvContract.Programs.Genres.TECH_SCIENCE, TvContract.Programs.Genres.NEWS),
                new JSONChannel("167",
                        "Montery Bay Aquarium",
                        "http://iphone-streaming.ustream.tv/uhls/9600798/streams/live/iphone/playlist.m3u8",
                        "http://static-cdn1.ustream.tv/i/channel/live/1_9600798,256x144,b:2015071514.jpg", "",
                        TvContract.Programs.Genres.ANIMAL_WILDLIFE),
                new JSONChannel("168",
                        "Audubon Osprey Cam",
                        "http://iphone-streaming.ustream.tv/uhls/11378037/streams/live/iphone/playlist.m3u8",
                        "http://static-cdn1.ustream.tv/i/channel/live/1_11378037,256x144,b:2015071514.jpg", "",
                        TvContract.Programs.Genres.ANIMAL_WILDLIFE),
//                        new JSONChannel("400", "Beats One", "http://stream.connectcast.tv:1935/live/CC-EC1245DB-5C6A-CF57-D13A-BB36B3CBB488-34313/playlist.m3u8", "")
                new JSONChannel("401",
                        "OutOfFocus.TV",
                        "http://pablogott.videocdn.scaleengine.net/pablogott-iphone/play/ooftv1/playlist.m3u8",
                        "http://i.imgur.com/QRCIhN4.png", "",
                        TvContract.Programs.Genres.MUSIC),
                new JSONChannel("900",
                        "Artbeats Demo",
                        "http://cdn-fms.rbs.com.br/hls-vod/sample1_1500kbps.f4v.m3u8", "", "",
                        TvContract.Programs.Genres.ARTS),
/*
                        new JSONChannel("900", "Euronews De", "http://fr-par-iphone-2.cdn.hexaglobe.net/streaming/euronews_ewns/14-live.m3u8", ""),
                        new JSONChannel("901", "TVI (Portugal)", "http://noscdn1.connectedviews.com:1935/live/smil:tvi.smil/playlist.m3u8", ""),
                        new JSONChannel("902", "PHOENIXHD", "http://teleboy.customers.cdn.iptv.ch/1122/index.m3u8", ""),
                        new JSONChannel("903", "Sport 1 Germany", "http://streaming-hub.com/tv/i/sport1_1@97464/index_1300_av-p.m3u8?sd=10&rebase=on", ""),
                        new JSONChannel("904", "RTP International", "http://rtp-pull-live.hls.adaptive.level3.net/liverepeater/rtpi_5ch120h264.stream/livestream.m3u8", "")
*/
        };
        return channels;
    }
    public static void openSuggestedChannels(final Activity mActivity, final GoogleApiClient gapi) {
        final JSONChannel[] channels = getSuggestedChannels();
        ArrayList<String> channeltext = new ArrayList<String>();
        for(JSONChannel j: channels) {
            channeltext.add(j.getName());
        }
        final String[] channelList = channeltext.toArray(new String[channeltext.size()]);
        new MaterialDialog.Builder(mActivity)
                .title(R.string.suggested_channels)
                .items(channelList)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        JSONChannel j = channels[i];
                        addChannel(mActivity, gapi, j, charSequence+"");
                    }
                }).show();
    }
    public static void addChannel(Activity mActivity, GoogleApiClient gapi, JSONChannel j, String name) {
        Log.d(TAG, "I've been told to add "+j.toString());
        ChannelDatabase cd = new ChannelDatabase(mActivity);
        if(cd.channelExists(j)) {
            Toast.makeText(mActivity, R.string.channel_dupe, Toast.LENGTH_SHORT).show();
        } else {
            try {
                if(name != null)
                    Toast.makeText(mActivity, mActivity.getString(R.string.channel_added, name), Toast.LENGTH_SHORT).show();
                cd.add(j);
                ActivityUtils.writeDriveData(mActivity, gapi);
                Log.d(TAG, "Added");
//                SyncUtils.requestSync(info);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public static void editChannel(final Activity mActivity, final String channel) {
        ChannelDatabase cdn = new ChannelDatabase(mActivity);
        JSONChannel jsonChannel = cdn.findChannel(channel); //Find by number
        if(jsonChannel.hasSource()) {
            //Search through all plugins for one of a given source
            PackageManager pm = mActivity.getPackageManager();
            final String pack = jsonChannel.getSource().split(",")[0];
            boolean app_installed = false;
            try {
                pm.getPackageInfo(pack, PackageManager.GET_ACTIVITIES);
                app_installed = true;
                //Open up this particular activity
                Intent intent = new Intent();
                intent.setClassName(pack,
                        jsonChannel.getSource().split(",")[1]);
                intent.putExtra(CumulusTvPlugin.INTENT_EXTRA_ACTION, CumulusTvPlugin.INTENT_EDIT);
                intent.putExtra(CumulusTvPlugin.INTENT_EXTRA_NUMBER, jsonChannel.getNumber());
                intent.putExtra(CumulusTvPlugin.INTENT_EXTRA_NAME, jsonChannel.getName());
                intent.putExtra(CumulusTvPlugin.INTENT_EXTRA_URL, jsonChannel.getUrl());
                intent.putExtra(CumulusTvPlugin.INTENT_EXTRA_ICON, jsonChannel.getLogo());
                intent.putExtra(CumulusTvPlugin.INTENT_EXTRA_SPLASH, jsonChannel.getSplashscreen());
                intent.putExtra(CumulusTvPlugin.INTENT_EXTRA_GENRES, jsonChannel.getGenresString());
                mActivity.startActivity(intent);
            }
            catch (PackageManager.NameNotFoundException e) {
                app_installed = false;
                new MaterialDialog.Builder(mActivity)
                        .title("Plugin " + pack + " not installed")
                        .content("What do you want to do instead?")
                        .positiveText("Download app")
                        .negativeText("Open in another plugin")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse("http://play.google.com/store/apps/details?id=" + pack));
                                mActivity.startActivity(i);
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                                openPluginPicker(false, channel, mActivity);
                            }
                        }).show();
                Toast.makeText(mActivity, "Plugin "+pack+" not installed.", Toast.LENGTH_SHORT).show();
                openPluginPicker(false, channel, mActivity);
            }
        } else {
            Log.d(TAG, "No specified source");
            openPluginPicker(false, channel, mActivity);
        }
    }

    /* DRIVE */
    public static void writeDriveData(Context context, GoogleApiClient gapi) {
        SettingsManager sm = new SettingsManager(context);
        sm.setGoogleDriveSyncable(gapi, new SettingsManager.GoogleDriveListener() {
            @Override
            public void onActionFinished(boolean cloudToLocal) {
                Log.d(TAG, "Action finished " + cloudToLocal);
            }
        });
        try {
            sm.writeToGoogleDrive(DriveId.decodeFromString(sm.getString(R.string.sm_google_drive_id)), new ChannelDatabase(context).toString());

            final String info = TvContract.buildInputId(new ComponentName("com.felkertech.n.cumulustv", ".SampleTvInput"));
            SyncUtils.requestSync(info);
        } catch(Exception e) {
            //Probably invalid drive id. No worries, just let someone know
            Log.e(TAG, e.getMessage() + "");
            Toast.makeText(context, R.string.invalid_file, Toast.LENGTH_SHORT).show();
        }
    }
    public static void readDriveData(Context mContext, GoogleApiClient gapi) {
        if(mContext == null)
            return;
        SettingsManager sm = new SettingsManager(mContext);
        sm.setGoogleDriveSyncable(gapi, null);
        DriveId did;
        try {
            did = DriveId.decodeFromString(sm.getString(R.string.sm_google_drive_id));
        } catch (Exception e) {
            Toast.makeText(mContext, R.string.invalid_file, Toast.LENGTH_SHORT).show();
            return;
        }
        sm.readFromGoogleDrive(did, ChannelDatabase.KEY);

        final String info = TvContract.buildInputId(new ComponentName("com.felkertech.n.cumulustv", ".SampleTvInput"));
        SyncUtils.requestSync(info);
    }
    public static void switchGoogleDrive(Activity mActivity, GoogleApiClient gapi) {
        /*if (gapi.isConnected()) {
            IntentSender intentSender = Drive.DriveApi
                    .newOpenFileActivityBuilder()
                    .setMimeType(new String[]{"application/json", "text*//*"})
                    .build(gapi);
            try {
                mActivity.startIntentSenderForResult(intentSender, REQUEST_CODE_OPENER, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                Log.w(TAG, "Unable to send intent", e);
            }
        } else {
            Toast.makeText(mActivity, "Please wait until Drive Service is active", Toast.LENGTH_SHORT).show();
        }*/
        syncFile(mActivity, gapi);
    }
    public static void deleteChannelData(final Activity mActivity, GoogleApiClient gapi) {
        final SettingsManager sm = new SettingsManager(mActivity);
        sm.setGoogleDriveSyncable(gapi, null);
        new MaterialDialog.Builder(mActivity)
                .title("Delete all your channel data?")
                .positiveText("Yes")
                .negativeText("NO")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        sm.setString(ChannelDatabase.KEY, "{'channels':[]}");
                        try {
                            DriveId did = DriveId.decodeFromString(sm.getString(R.string.sm_google_drive_id));
                            sm.writeToGoogleDrive(did,
                                    sm.getString(ChannelDatabase.KEY));
                        } catch (Exception e) {
                            Toast.makeText(mActivity, "Error: DriveId is invalid", Toast.LENGTH_SHORT).show();
                        }
                        sm.setString(R.string.sm_google_drive_id, "");
                        Toast.makeText(mActivity, "The deed was done", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mActivity, MainActivity.class);
                        mActivity.startActivity(i);
                    }
                })
                .show();
    }

    public static final int RESOLVE_CONNECTION_REQUEST_CODE = 100;
    public static final int REQUEST_CODE_CREATOR = 102;
    public static final int REQUEST_CODE_OPENER = 104;
    public static void syncFile(Activity mActivity, GoogleApiClient gapi) {
        Log.d(TAG, "About to sync a file");
        if (gapi.isConnected()) {
            IntentSender intentSender = Drive.DriveApi
                    .newOpenFileActivityBuilder()
                    .setMimeType(new String[]{"application/json", "text/*"})
                    .build(gapi);
            try {
                Log.d(TAG, "About to start activity");
                mActivity.startIntentSenderForResult(intentSender, REQUEST_CODE_OPENER, null, 0, 0, 0);
                Log.d(TAG, "Activity activated");
            } catch (IntentSender.SendIntentException e) {
                Log.w(TAG, "Unable to send intent", e);
                e.printStackTrace();
            }
        } else {
            Toast.makeText(mActivity, "Please wait until Drive Service is active", Toast.LENGTH_SHORT).show();
        }
    }

    /* LICENSES */
    public static void oslClick(Activity activity) {
        new MaterialDialog.Builder(activity)
                .title("Software Licenses")
                .content(GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(activity))
                .show();
    }

    /* PLUGIN INTERFACES */
    public static void openPluginPicker(final boolean newChannel, Activity activity) {
        openPluginPicker(newChannel, 0, activity);
    }
    public static void openPluginPicker(final boolean newChannel, final int index, final Activity activity) {
        try {
            ChannelDatabase cdn = new ChannelDatabase(activity);
            JSONChannel jsonChannel = new JSONChannel(cdn.getJSONChannels().getJSONObject(index));
            openPluginPicker(newChannel, jsonChannel, activity);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public static void openPluginPicker(final boolean newChannel, final String channel, final Activity activity) {
        ChannelDatabase cdn = new ChannelDatabase(activity);
        openPluginPicker(newChannel, cdn.findChannel(channel), activity);
    }
    public static void openPluginPicker(final boolean newChannel, final JSONChannel queriedChannel, final Activity activity) {
        final PackageManager pm = activity.getPackageManager();
        final Intent plugin_addchannel = new Intent("com.felkertech.cumulustv.ADD_CHANNEL");
        final List<ResolveInfo> plugins = pm.queryIntentActivities(plugin_addchannel, 0);
        ArrayList<String> plugin_names = new ArrayList<String>();
        for (ResolveInfo ri : plugins) {
            plugin_names.add(ri.loadLabel(pm).toString());
        }
        String[] plugin_names2 = plugin_names.toArray(new String[plugin_names.size()]);
        Log.d(TAG, "Load plugins " + plugin_names.toString());
        if(plugin_names.size() == 1) {
            Intent intent = new Intent();
            if (newChannel) {
                Log.d(TAG, "Try to start ");
                ResolveInfo plugin_info = plugins.get(0);
                Log.d(TAG, plugin_info.activityInfo.applicationInfo.packageName + " " +
                        plugin_info.activityInfo.name);

                intent.setClassName(plugin_info.activityInfo.applicationInfo.packageName,
                        plugin_info.activityInfo.name);
                intent.putExtra(CumulusTvPlugin.INTENT_EXTRA_ACTION, CumulusTvPlugin.INTENT_ADD);
            } else {
                ChannelDatabase cdn = new ChannelDatabase(activity);
                JSONChannel jsonChannel = queriedChannel;
                ResolveInfo plugin_info = plugins.get(0);
                Log.d(TAG, plugin_info.activityInfo.applicationInfo.packageName + " " +
                        plugin_info.activityInfo.name);
                intent.setClassName(plugin_info.activityInfo.applicationInfo.packageName,
                        plugin_info.activityInfo.name);
                intent.putExtra(CumulusTvPlugin.INTENT_EXTRA_ACTION, CumulusTvPlugin.INTENT_EDIT);
                intent.putExtra(CumulusTvPlugin.INTENT_EXTRA_NUMBER, jsonChannel.getNumber());
                intent.putExtra(CumulusTvPlugin.INTENT_EXTRA_NAME, jsonChannel.getName());
                intent.putExtra(CumulusTvPlugin.INTENT_EXTRA_URL, jsonChannel.getUrl());
                intent.putExtra(CumulusTvPlugin.INTENT_EXTRA_ICON, jsonChannel.getLogo());
                intent.putExtra(CumulusTvPlugin.INTENT_EXTRA_SPLASH, jsonChannel.getSplashscreen());
                intent.putExtra(CumulusTvPlugin.INTENT_EXTRA_GENRES, jsonChannel.getGenresString());
            }
            activity.startActivity(intent);
        } else {
            new MaterialDialog.Builder(activity)
                    .items(plugin_names2)
                    .title("Choose an app")
                    .content("Yes, if there's only one app, default to that one")
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                            Toast.makeText(activity, "Pick " + i, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent();
                            if (newChannel) {
                                Log.d(TAG, "Try to start ");
                                ResolveInfo plugin_info = plugins.get(i);
                                Log.d(TAG, plugin_info.activityInfo.applicationInfo.packageName + " " +
                                        plugin_info.activityInfo.name);

                                intent.setClassName(plugin_info.activityInfo.applicationInfo.packageName,
                                        plugin_info.activityInfo.name);

                                intent.putExtra(CumulusTvPlugin.INTENT_EXTRA_ACTION, CumulusTvPlugin.INTENT_ADD);
                            } else {
                                ChannelDatabase cdn = new ChannelDatabase(activity);
                                JSONChannel jsonChannel = queriedChannel;
                                ResolveInfo plugin_info = plugins.get(i);
                                intent.setClassName(plugin_info.activityInfo.applicationInfo.packageName,
                                        plugin_info.activityInfo.name);
                                intent.putExtra(CumulusTvPlugin.INTENT_EXTRA_ACTION, CumulusTvPlugin.INTENT_EDIT);
                                intent.putExtra(CumulusTvPlugin.INTENT_EXTRA_NUMBER, jsonChannel.getNumber());
                                intent.putExtra(CumulusTvPlugin.INTENT_EXTRA_NAME, jsonChannel.getName());
                                intent.putExtra(CumulusTvPlugin.INTENT_EXTRA_URL, jsonChannel.getUrl());
                                intent.putExtra(CumulusTvPlugin.INTENT_EXTRA_ICON, jsonChannel.getLogo());
                                intent.putExtra(CumulusTvPlugin.INTENT_EXTRA_SPLASH, jsonChannel.getSplashscreen());
                                intent.putExtra(CumulusTvPlugin.INTENT_EXTRA_GENRES, jsonChannel.getGenresString());
                            }
                            activity.startActivity(intent);
                        }
                    }).show();
        }
    }

    public static void browsePlugins(final Activity mActivity) {
        //Same opening
        final PackageManager pm = mActivity.getPackageManager();
        final Intent plugin_addchannel = new Intent("com.felkertech.cumulustv.ADD_CHANNEL");
        final List<ResolveInfo> plugins = pm.queryIntentActivities(plugin_addchannel, 0);
        ArrayList<String> plugin_names = new ArrayList<String>();
        for (ResolveInfo ri : plugins) {
            plugin_names.add(ri.loadLabel(pm).toString());
        }
        String[] plugin_names2 = plugin_names.toArray(new String[plugin_names.size()]);

        new MaterialDialog.Builder(mActivity)
                .title("Installed Plugins")
                .items(plugin_names2)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        //Load the given plugin with some additional info
                        ChannelDatabase cd = new ChannelDatabase(mActivity);
                        String s = cd.toString();
                        Intent intent = new Intent();
                        Log.d(TAG, "Try to start");
                        ResolveInfo plugin_info = plugins.get(i);
                        Log.d(TAG, plugin_info.activityInfo.applicationInfo.packageName + " " +
                                plugin_info.activityInfo.name);

                        intent.setClassName(plugin_info.activityInfo.applicationInfo.packageName,
                                plugin_info.activityInfo.name);
                        intent.putExtra(CumulusTvPlugin.INTENT_EXTRA_ACTION, CumulusTvPlugin.INTENT_EXTRA_READ_ALL);
                        intent.putExtra(CumulusTvPlugin.INTENT_EXTRA_ALL_CHANNELS, s);
                        mActivity.startActivity(intent);
                    }
                })
                .positiveText("Download More")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse("http://play.google.com/store/search?q=cumulustv&c=apps"));
                        mActivity.startActivity(i);
                    }
                }).show();
    }

    /* ACTIVITY CLONES */
    public static void launchLiveChannels(Activity mActivity) {
        Intent i = mActivity.getPackageManager().getLaunchIntentForPackage("com.google.android.tv");
        if (i == null) {
            Toast.makeText(mActivity, R.string.no_live_channels, Toast.LENGTH_SHORT).show();
        } else {
            mActivity.startActivity(i);
        }
    }
    public static void onActivityResult(final Activity mActivity, final GoogleApiClient gapi, final int requestCode, final int resultCode, final Intent data) {
        SettingsManager sm = new SettingsManager(mActivity);
        switch (requestCode) {
            case RESOLVE_CONNECTION_REQUEST_CODE:
                if (resultCode == mActivity.RESULT_OK) {
                    Log.d(TAG, "App connect +1");
                    gapi.connect();
                } else {
                    Log.d(TAG, "App cannot connect");
                    new MaterialDialog.Builder(mActivity)
                            .title("Connection Issue")
                            .content("Cannot connect to Google Drive at this moment.")
                            .positiveText("OK")
                            .negativeText("Try Again")
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    super.onNegative(dialog);
                                    gapi.connect();
                                }
                            }).show();
                }
                break;
            case REQUEST_CODE_CREATOR:
                if (data == null) //If op was canceled
                    return;
                DriveId driveId = (DriveId) data.getParcelableExtra(
                        OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                Log.d(TAG, driveId.encodeToString() + ", " + driveId.getResourceId() + ", " + driveId.toInvariantString());
                sm.setString(R.string.sm_google_drive_id, driveId.encodeToString());

                DriveFile file = Drive.DriveApi.getFile(gapi, DriveId.decodeFromString(driveId.encodeToString()));
                //Write initial data
                ActivityUtils.writeDriveData(mActivity, gapi);
                break;
            case ActivityUtils.REQUEST_CODE_OPENER:
                if (data == null) //If op was canceled
                    return;
                driveId = (DriveId) data.getParcelableExtra(
                        OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                Log.d(TAG, driveId.encodeToString() + ", " + driveId.getResourceId() + ", " + driveId.toInvariantString());
                sm.setString(R.string.sm_google_drive_id, driveId.encodeToString());
                new MaterialDialog.Builder(mActivity)
                        .title(R.string.sync_initial)
                        .positiveText(R.string.sync_cloud_local)
                        .negativeText(R.string.sync_local_cloud)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                ActivityUtils.readDriveData(mActivity, gapi);
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                                ActivityUtils.writeDriveData(mActivity, gapi);
                            }
                        })
                        .show();
        }
    }

    /* MISC */
    public static void openAbout(final Activity mActivity) {
        try {
            PackageInfo pInfo = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0);
            new MaterialDialog.Builder(mActivity)
                    .title(R.string.app_name)
                    .content(mActivity.getString(R.string.about_app_description, pInfo.versionName))
                    .positiveText("GitHub")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            Intent gi = new Intent(Intent.ACTION_VIEW);
                            gi.setData(Uri.parse("http://github.com/fleker/cumulustv"));
                            mActivity.startActivity(gi);
                        }
                    })
                    .show();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static void openStream(Activity mActivity, String url) {
        Intent i = new Intent(mActivity, SamplePlayer.class);
        i.putExtra(SamplePlayer.KEY_VIDEO_URL, url);
        mActivity.startActivity(i);
    }
}
