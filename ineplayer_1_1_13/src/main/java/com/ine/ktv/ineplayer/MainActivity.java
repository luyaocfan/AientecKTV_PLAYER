package com.ine.ktv.ineplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;
import com.google.android.exoplayer2.audio.IneStereoVolumeProcessor;
import com.google.android.exoplayer2.ui.PlayerView;
import com.ine.ktv.playerengine.InePlayerController;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements InePlayerController.EventListen, VODPlayList.EventListener, IneSocketClient.EventListener {

    public PlayerView orderSongView, publicVideoView;
    public static MainActivity _this;
    ConstraintLayout AbmLayout;
    FrameLayout SettingLayout;
    View rootView;
    View topLeftColorView;
    View topColorView;
    View topRightColorView;
    View bottomLeftColorView;
    View bottomColorView;
    View bottomRightColorView;
    View leftTopColorView;
    View leftColorView;
    View leftBottomColorView;
    View rightTopColorView;
    View rightColorView;
    View rightBottomColorView;
    ImageView smallSnapshotView;


    InePlayerController controller;
    static final int MaxCacheCount = 2;
    static final int MaxCacheSize = 1024*1024*64;  // for start tup
    static final int [] CacheBandwidthKBS = new int[] {65536, 65536};
    static final int OrderSongPlayingBufferSize = 1024*1024*64; // for current playing
    static final int PublicVideoPlayingBufferSize = 1024*1024*32; // for current playing

    //config
    private int BoxId = 14;
    private String[] VODServers = new String[] {"http://106.104.151.145:10011/mtv/"};
    private String APIServer = "http://106.104.151.145:10002/index.php/VOD/";
    private String DataServerIP = "106.104.151.145";
    private int DataServerPort = 40003;
//    private int BoxId = 14;
//    private String VODServers = {"http://61.218.32.23:10003/mtv/"};
//    private String APIServer = "http://61.218.32.23:10002/index.php/VOD/";
//    private String DataServerIP = "61.218.32.23";
//    private int DataServerPort = 40003;
    //end config

    private String configLocalPath;

    private String [] PubPlayList = {
            //"http://192.168.99.152/100011.mp4",
            "00990.mp4",
            "00991.mp4",
            "00992.mp4"
    };
    private String [] PlayList = {
            //"http://192.168.99.152/100011_1m.mp4",
            "http://192.168.99.152/100011_re_h265.mp4",
            "http://192.168.99.152/100011.mp4",


//            "http://192.168.99.152/100003.mp4",
//            "http://192.168.99.152/63029ZH1W2.mp4",
//            "http://192.168.99.152/92005W.mp4",
//            "http://192.168.99.152/92005W.mp4",
//            "http://192.168.99.152/92005W2.mp4",
//            "http://192.168.99.152/52576ZH1 [HEVC 高質量及大小 二次編碼].mp4",
//            "http://192.168.99.152/51961ZH1 [自訂].mp4",
//            "http://106.104.151.145:10003/mtv/4K1.mp4",
            "52297YH4.mp4",
            "40589YH1.mp4",
            "24627ZH5.mp4",
            "53213YH1.mp4",
            "37818ZH1.mp4",
            "45156YH1.mp4",
            "19120M6.mp4",
            "19176Y6.mp4",
            "19280Z6.mp4",
            "19128M6.mp4",
            "24570M6.mp4",
            "19298Y1.mp4",
            "24570M6.mp4",
            "19279Z6.mp4",
            "19651Y3.mp4"
    };

    public static int minFontSize;
    public static int ScreenWidth;
    public static int ScreenHeight;
    Space TopPadding;
    TextView textView;
    EditText editSongId;
    int orderSerialNo = 1;
    int publicSerialNo = 1;
    SampleOSD sampleOSD;
    VODPlayList playList;
    IneSocketClient client;
    AudioManager audio;
    Bitmap smallCaptureBitmap;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _this = this;
        TextView txtVersion = findViewById(R.id.txtVersion);
        try {
            PackageInfo info =  getPackageManager().getPackageInfo(getPackageName(), 0);
            txtVersion.setText("v"+info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        configLocalPath = getExternalFilesDir("setting").toString();
        Load();
        playList = new VODPlayList(this, BoxId, APIServer, this);
        rootView = findViewById(R.id.rootView);
        FrameLayout OsdLayout = findViewById(R.id.OsdLayout);
        sampleOSD = new SampleOSD(OsdLayout ,this);
        SettingLayout = findViewById(R.id.SettingLayout);
        TopPadding = findViewById(R.id.TopPadding);
        orderSongView = findViewById(R.id.orderSongView);
        publicVideoView = findViewById(R.id.publicVideoView);
        textView = findViewById(R.id.textView);
        AbmLayout = findViewById(R.id.AbmLayout);
        topLeftColorView = findViewById(R.id.topLeftColorView);
        topColorView = findViewById(R.id.topColorView);
        topRightColorView = findViewById(R.id.topRightColorView);
        bottomLeftColorView = findViewById(R.id.bottomLeftColorView);
        bottomColorView = findViewById(R.id.bottomColorView);
        bottomRightColorView = findViewById(R.id.bottomRightColorView);
        leftTopColorView = findViewById(R.id.leftTopColorView);
        leftColorView = findViewById(R.id.leftColorView);
        leftBottomColorView = findViewById(R.id.leftBottomColorView);
        rightTopColorView = findViewById(R.id.rightTopColorView);
        rightColorView = findViewById(R.id.rightColorView);
        rightBottomColorView = findViewById(R.id.rightBottomColorView);
        smallSnapshotView = findViewById(R.id.smallSnapshotView);

        InePlayerController.InePlayerControllerConfigure config = new InePlayerController.InePlayerControllerConfigure();
        config.context = this;
//        config.orderSongView = orderSongView;
//        config.publicVideoView = publicVideoView;
        config.publicVideoPlayingBufferSize = PublicVideoPlayingBufferSize;
        config.maxCacheCount = MaxCacheCount;
        config.itemCacheSize = MaxCacheSize;
        config.cacheBandwidthKBS = CacheBandwidthKBS;
        config.listener = this;

        controller = new InePlayerController(config);
        controller.SetVODServerList(VODServers);
        for(int i=0; i<PubPlayList.length; i++)
           controller.AddPubVideo(PubPlayList[i], ""+(publicSerialNo++)+". "+new File(PubPlayList[i]).getName().split("\\.")[0]);
        controller.open();

        smallCaptureBitmap = Bitmap.createBitmap(96, 54, Bitmap.Config.ARGB_8888);
        smallCaptureBitmap.setHasAlpha(false);

        Button btnOrderSong = findViewById(R.id.btnOrderSong);
        btnOrderSong.setOnClickListener( (sender) -> {
            for(int i=0; i<PlayList.length; i++)
                controller.AddOrderSong(PlayList[i], "" + (orderSerialNo++) + ". " + new File(PlayList[i]).getName().split("\\.")[0], OrderSongPlayingBufferSize);
        });
        Button btnCutSong = findViewById(R.id.btnCutSong);
        btnCutSong.setOnClickListener( (sender) -> {
            OnEvent(client, IneSocketClient.Event_Cut);
        });
        Button btnStereo = findViewById(R.id.btnStereo);
        btnStereo.setOnClickListener( (sender) -> {
            OnEvent(client, IneSocketClient.Event_Original);
        });
        Button btnRightMono = findViewById(R.id.btnRightMono);
        btnRightMono.setOnClickListener( (sender) -> {
            OnEvent(client, IneSocketClient.Event_MusicOnly);
        });
        editSongId = findViewById(R.id.editSongId);
        editSongId.setOnClickListener((sender)->{
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm.isAcceptingText()) {
                TopPadding.setVisibility(View.VISIBLE);
            }
        });

        Button btnOrderSongId = findViewById(R.id.btnOrderSongId);
        btnOrderSongId.setOnClickListener( (sender) -> {
            AddSong(editSongId.getText().toString(), false);
        });
        Button btnInsertSongId = findViewById(R.id.btnInsertSongId);
        btnInsertSongId.setOnClickListener( (sender) -> {
            AddSong(editSongId.getText().toString(), true);
        });
        Button btnPause = findViewById(R.id.btnPause);
        btnPause.setOnClickListener( (sender) -> {
            if(controller.isPaused())
                OnEvent(client, IneSocketClient.Event_Play);
            else
                OnEvent(client, IneSocketClient.Event_Pause);
        });
        Button btnReplay = findViewById(R.id.btnReplay);
        btnReplay.setOnClickListener( (sender) -> {
            OnEvent(client, IneSocketClient.Event_Replay);
        });
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        orderSongView.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN) {
                hideSystemUI();
                if(SettingLayout.getVisibility()==View.GONE)
                    SettingLayout.setVisibility(View.VISIBLE);
                else
                    SettingLayout.setVisibility(View.GONE);
                return true;
            }
            return true;
        });
        publicVideoView.setOnTouchListener((v, event) -> {
            if(event.getAction()==MotionEvent.ACTION_DOWN) {
                hideSystemUI();
                if(SettingLayout.getVisibility()==View.GONE)
                    SettingLayout.setVisibility(View.VISIBLE);
                else
                    SettingLayout.setVisibility(View.GONE);
                return true;
            }
            return true;
        });
        hideSystemUI();
        LoadScreenInfo();

        checkOrderSong();

        client = new IneSocketClient(this, DataServerIP, DataServerPort, this);
        client.Open();
        //checkExitPub();
        //mainHandler.postDelayed(()->DemoAmbLight(),500);
//        OrderNextTest();
    }
//    int orderIdx = 0;
//    private Runnable OrderNextTestCut = () -> OnEvent(client, IneSocketClient.Event_Cut);
//    public void OrderNextTest() {
//        controller.AddOrderSong(PlayList[orderIdx], "" + (orderSerialNo++) + ". " + new File(PlayList[orderIdx]).getName().split("\\.")[0], PlayingBufferSize);
//        orderIdx++;
//        if(PlayList.length <= orderIdx) {
//            orderIdx = 0;
//        }
//        mainHandler.postDelayed(OrderNextTestCut , 5000);
//    }
    @Override
    protected void onStop(){
        controller.close();
        client.Close();
        super.onStop();
    }
    public void AddSong(String songId, boolean bInsert) {
        if(songId.endsWith(".txt")){
            new PlayListTextDownload((list, bInsertFlag)->{
                for(String name: list) {
                    AddSong(name, bInsertFlag);
                }
            },bInsert).execute(songId);
            return;
        }
        if(!songId.contains("."))
            songId=songId+".mp4";
        if(bInsert && !controller.isInPublicVideo())
                controller.InsertOrderSong(1, songId , ""+(orderSerialNo++)+". "+songId, OrderSongPlayingBufferSize);
        controller.AddOrderSong(songId, ""+(orderSerialNo++)+". "+songId, OrderSongPlayingBufferSize);
    }

    public void Load() {
        String json = "";
        try {
            File file = new File(configLocalPath+"/setting.json");
            boolean readSuccess = false;
            if(file.exists()) {
                BufferedInputStream in = new BufferedInputStream (new FileInputStream(file));
                BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(in));
                String line;
                while((line = bufferedReader.readLine()) != null)
                    json += line;
                in.close();
                try {
                    JSONObject jObj = new JSONObject(json);
                    int configVersion = jObj.optInt("ConfigVersion", 0);
                    if(configVersion == 2) {
                        readSuccess = true;
                        BoxId = jObj.optInt("BoxId", BoxId);
                        JSONArray VODServerArray = jObj.optJSONArray("VODServers");
                        if (VODServerArray != null) {
                            VODServers = new String[VODServerArray.length()];
                            for (int i = 0; i < VODServerArray.length(); i++) {
                                VODServers[i] = VODServerArray.getString(i);
                            }
                        }

                        APIServer = jObj.optString("APIServer", APIServer);
                        DataServerIP = jObj.optString("DataServerIP", DataServerIP);
                        DataServerPort = jObj.optInt("DataServerPort", DataServerPort);
                    }
                } catch (JSONException e) {
                    Log.e("JSON Parser", "Error parsing data " + e.toString());
                }
            }
            if(!readSuccess) {
                JSONObject jObj = new JSONObject();
                jObj.put("ConfigVersion", 2);
                jObj.put("BoxId", BoxId);
                JSONArray VODServerArray = new JSONArray();
                for(int i = 0; i< VODServers.length; i++)
                        VODServerArray.put(VODServers[i]);
                jObj.put("VODServers", VODServerArray);
                jObj.put("APIServer", APIServer);
                jObj.put("DataServerIP", DataServerIP);
                jObj.put("DataServerPort", DataServerPort);
                json = jObj.toString();
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(configLocalPath+"/setting.json"));
                bos.write(json.getBytes());
                bos.flush();
                bos.close();
            }
        }
        catch (Exception e)
        {
            Log.e("File", "Error on load local ROI config " + e.toString());
        }
//        if (json == "")
//            typeList = defaultTypes();
        // new GetCloudROITask().execute("https://www.hassen.idv.tw/watermeter/roi.json");
    }
    void checkOrderSong() {
        playList.RefreshOrderSong();
        //mainHandler.postDelayed(()->{checkOrderSong();}, 3000);
    }
// MainActivity
//    public void checkExitPub() {
//        if(isPubVideo && player.getPreviousWindowIndex()!=C.INDEX_UNSET) {
//            player.seekToDefaultPosition(0);
//            isPubVideo = false;
//        }
//
//        handler.postDelayed(()->{checkExitPub();}, 50);
//    }
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        TopPadding.setVisibility(View.GONE);
    }
    public void LoadScreenInfo() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        minFontSize = (int) (8f * metrics.density);
        ScreenWidth = metrics.widthPixels;
        ScreenHeight = metrics.heightPixels;
    }
    public void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
        hideKeyboard();
    }

    public void showSystemUI() {
        // Shows the system bars by removing all the flags
        // except for the ones that make the content appear under the system bars.
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (0 != (event.getSource() & InputDevice.SOURCE_CLASS_POINTER)) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_SCROLL:
                    if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f)
                        audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                                AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                    else
                        audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                                AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                    return true;
            }
        }
        return super.onGenericMotionEvent(event);
    }
    //OnPlayerControllerEventListen
    @Override
    public void onPlayListChange(InePlayerController controller,boolean isPublicVideo) {

        String[] NewList = controller.GetOrderSongPlayList();
        StringBuilder sb = new StringBuilder();
        if(isPublicVideo) {
            sb.append("公播\n");
        }
        for(int i=0;i<NewList.length;i++){
            sb.append(NewList[i]);
            sb.append("\n");
        }
        textView.setText(sb.toString());

        SampleOSD.UpdateData data = new SampleOSD.UpdateData();
        data.type = OSDBase.UpdateData.Type_PlayListChange;
        sampleOSD.onUpdate(data);
    }
    @Override
    public void onOrderSongFinish(InePlayerController controller) {
        SampleOSD.UpdateData data = new SampleOSD.UpdateData();
        data.type = OSDBase.UpdateData.Type_OrderSongFinish;
        sampleOSD.onUpdate(data);
    };
    @Override
    public void onStop(InePlayerController controller, String Name, boolean isPublicVideo) {
        SampleOSD.UpdateData data = new SampleOSD.UpdateData();
        data.type = OSDBase.UpdateData.Type_Stop;
        data.message = Name;
        sampleOSD.onUpdate(data);
//        if(!isPublicVideo) {
//            int id = Integer.parseInt(Name.split("\\.")[0]);
//            playList.SetSongPlayStatus(id,VODPlayList.Song_Ended);
//        }

    };
    @Override
    public void onNext(InePlayerController controller, String Name, boolean isPublicVideo) {
        SampleOSD.UpdateData data = new SampleOSD.UpdateData();
        data.type = OSDBase.UpdateData.Type_Next;
        data.message = Name;
        sampleOSD.onUpdate(data);
        if(!isPublicVideo) {
            int id = Integer.parseInt(Name.split("\\.")[0]);
            playList.SetSongPlayStatus(id,VODPlayList.Song_Playing);
            Log.d("播放中", Name);
        }

    };
    @Override
    public void onNextSongDisplay(InePlayerController controller, String Name) {
        int id = Integer.parseInt(Name.split("\\.")[0]);
        playList.SetSongPlayStatus(id,VODPlayList.Song_DisplayNext);
        Log.d("下一首", Name);
    };
    @Override
    public void onLoadingError(InePlayerController controller, String Name, boolean isPublicVideo) {
        SampleOSD.UpdateData data = new SampleOSD.UpdateData();
        data.type = OSDBase.UpdateData.Type_LoadingError;
        data.message = Name;
        sampleOSD.onUpdate(data);
    };
    @Override
    public void onPlayingError(InePlayerController controller, String Name, String message, boolean isPublicVideo) {
        SampleOSD.UpdateData data = new SampleOSD.UpdateData();
        data.type = OSDBase.UpdateData.Type_PlayingError;
        data.message = Name+" "+message;
        sampleOSD.onUpdate(data);
    };
    @Override
    public void onOrderSongAudioChannelMappingChanged(InePlayerController controller, int type) {
        SampleOSD.UpdateData data = new SampleOSD.UpdateData();
        data.type = (!controller.isInPublicVideo() && type == IneStereoVolumeProcessor.AudioControlOutput_LeftMono)?OSDBase.UpdateData.Type_AudioOriginal:OSDBase.UpdateData.Type_AudioMusic;
        sampleOSD.onUpdate(data);
    };

//VODPlayList.EventListen
    @Override
    public void onOrderSongData(VODPlayList sender, VODPlayList.OrderPlayItem[] newItems) {
        ArrayList<VODPlayList.OrderPlayItem> orderSongItems = sender.getOrderSongItems();
        //String[] OldList = controller.GetOrderSongPlayList();
        for(int i=0; i<newItems.length; i++) {
            VODPlayList.OrderPlayItem playItem = newItems[i];
            int status = playItem.getPlayStatus();

            controller.AddOrderSong(playItem.getSongFile(), ""+ (playItem.getId())+". "+playItem.getSongName(), OrderSongPlayingBufferSize);
        }
    }
    @Override
    public void onPublicSongData(VODPlayList sender) {
        ArrayList<VODPlayList.PublicPlayItem> publicSongItems = sender.getPublicSongItems();
        for(int i=0; i<publicSongItems.size(); i++) {
            VODPlayList.PublicPlayItem playItem = publicSongItems.get(i);

            controller.AddPubVideo(playItem.getSongFile(), "" + (playItem.getId()) + ". " +playItem.getSongFile());
        }
    }

    @Override
    public void OnEvent(IneSocketClient client, int eventType) {
        switch(eventType) {
            case IneSocketClient.Event_Original:
                controller.AudioControlOutput(IneStereoVolumeProcessor.AudioControlOutput_LeftMono);

                break;
            case IneSocketClient.Event_MusicOnly:
                controller.AudioControlOutput(IneStereoVolumeProcessor.AudioControlOutput_RightMono);
                break;
            case IneSocketClient.Event_HelpMusic:
                break;
            case IneSocketClient.Event_Play:
                if(controller.isPaused()) {
                    controller.resume();
                    SampleOSD.UpdateData data = new SampleOSD.UpdateData();
                    data.type = OSDBase.UpdateData.Type_Play;
                    data.centerString = "播放";
                    sampleOSD.onUpdate(data);
                }
                break;
            case IneSocketClient.Event_Pause:
                if (!controller.isPaused()) {
                    controller.pause();
                    SampleOSD.UpdateData data = new SampleOSD.UpdateData();
                    data.type = OSDBase.UpdateData.Type_Pause;
                    data.centerString = "暫停";
                    sampleOSD.onUpdate(data);
                }
                break;
            case IneSocketClient.Event_Cut: {
                controller.cut();
                SampleOSD.UpdateData data = new SampleOSD.UpdateData();
                data.type = OSDBase.UpdateData.Type_Cut;
                data.centerString = "切歌";
                sampleOSD.onUpdate(data);
                break;
            }
            case IneSocketClient.Event_Replay: {
                controller.replay();
                SampleOSD.UpdateData data = new SampleOSD.UpdateData();
                data.type = OSDBase.UpdateData.Type_Replay;
                data.centerString = "重唱";
                sampleOSD.onUpdate(data);
                break;
            }
            case IneSocketClient.Event_RefreshList:
                playList.RefreshOrderSong();
                break;
        }

    }
    //AMB Demo code
    public Bitmap screenShot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
    public Bitmap getScreenShot(View view) {
        try {
            View screenView = view.getRootView();
            screenView.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
            screenView.setDrawingCacheEnabled(false);
            return bitmap;
        }
        catch (Exception ex) {
            return null;
        }
    }
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        if(bm == null)
            return null;
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }
    void DemoAmbLight() {

        if(orderSongView.getVisibility()==View.VISIBLE) {
            int w = 96;
            int h = 54;
            SurfaceView playerSurfaceView = (SurfaceView) orderSongView.getVideoSurfaceView();
            PixelCopy.OnPixelCopyFinishedListener listener = copyResult -> {
                topLeftColorView.setBackgroundColor(smallCaptureBitmap.getPixel((int) ((float) w * 0.25), 0));
                topColorView.setBackgroundColor(smallCaptureBitmap.getPixel((int) ((float) w * 0.5), 0));
                topRightColorView.setBackgroundColor(smallCaptureBitmap.getPixel((int) ((float) w * 0.75), 0));
                bottomLeftColorView.setBackgroundColor(smallCaptureBitmap.getPixel((int) ((float) w * 0.25), h - 1));
                bottomColorView.setBackgroundColor(smallCaptureBitmap.getPixel((int) ((float) w * 0.5), h - 1));
                bottomRightColorView.setBackgroundColor(smallCaptureBitmap.getPixel((int) ((float) w * 0.75), h - 1));
                leftTopColorView.setBackgroundColor(smallCaptureBitmap.getPixel(0, (int) ((float) h * 0.25)));
                leftColorView.setBackgroundColor(smallCaptureBitmap.getPixel(0, (int) ((float) h * 0.5)));
                leftBottomColorView.setBackgroundColor(smallCaptureBitmap.getPixel(0, (int) ((float) h * 0.75)));
                rightTopColorView.setBackgroundColor(smallCaptureBitmap.getPixel(w - 1, (int) ((float) h * 0.25)));
                rightColorView.setBackgroundColor(smallCaptureBitmap.getPixel(w - 1, (int) ((float) h * 0.5)));
                rightBottomColorView.setBackgroundColor(smallCaptureBitmap.getPixel(w - 1, (int) ((float) h * 0.75)));
                //smallSnapshotView.setImageBitmap(smallCaptureBitmap);
            };
            try {
                PixelCopy.request(playerSurfaceView, smallCaptureBitmap, listener, mainHandler);
            }
            catch (Exception ex) {

            }
        }

        mainHandler.postDelayed(()->DemoAmbLight(),10);
    }
}