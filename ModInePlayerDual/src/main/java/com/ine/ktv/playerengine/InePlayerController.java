package com.ine.ktv.playerengine;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.IneRenderersFactory;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.IneStereoVolumeProcessor;
import com.google.android.exoplayer2.source.IneDataSource;
import com.google.android.exoplayer2.source.IneMediaSource;
import com.google.android.exoplayer2.source.LoadEventInfo;
import com.google.android.exoplayer2.source.MediaLoadData;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Util;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;


public class InePlayerController {

    static public class InePlayerControllerConfigure {
        public Context context;
        public SurfaceView playerView ;
        public int maxCacheCount = 4;
        public int itemCacheSize = 1024*1024*4;
        public int[] cacheBandwidthKBS = new int[] {512,256,128,64};
        public int playingBufferSize = 1024*1024*32;
        public EventListen listener;

        public InePlayerControllerConfigure clone(){
            InePlayerControllerConfigure newConfig = new InePlayerControllerConfigure();
            newConfig.context = this.context;
            newConfig.playerView = this.playerView;

            newConfig.maxCacheCount = this.maxCacheCount;
            newConfig.itemCacheSize = this.itemCacheSize;
            newConfig.cacheBandwidthKBS = new int [this.cacheBandwidthKBS.length];
            newConfig.playingBufferSize = this.playingBufferSize;
            System.arraycopy(this.cacheBandwidthKBS,0, newConfig.cacheBandwidthKBS, 0,this.cacheBandwidthKBS.length);
            newConfig.listener = this.listener;
            return newConfig;
        }
    };
    private class cOrderSongPlayerListener implements SimpleExoPlayer.Listener, MediaSourceEventListener, IneStereoVolumeProcessor.Listener {
        // SimpleExoPlayer.Listener
        @Override
        public void onPlaybackStateChanged(int state) {
            if(state==SimpleExoPlayer.STATE_READY)
                onOrderSongReady();
            if(state==SimpleExoPlayer.STATE_ENDED) {
                if(orderSongClearMediaItems > 0) {
                    orderSongClearMediaItems--;
                    return;
                }
                onOrderSongEnd();
            }

        }

        @Override
        public void onMediaItemTransition(@Nullable MediaItem mediaItem, @Player.MediaItemTransitionReason int reason){
            if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO && currentPlayer == orderSongPlayer)
                stop();
        }
        @Override
        public void onPlayerError(ExoPlaybackException error) {
            int state = orderSongPlayer.getPlaybackState();
            if (state == Player.STATE_IDLE || state == Player.STATE_BUFFERING) {
                if (Looper.myLooper() == Looper.getMainLooper())
                    onOrderSongLoadError();
                else
                    handler.post(() -> onOrderSongLoadError());
            }
            else {
                if (Looper.myLooper() == Looper.getMainLooper())
                    onOrderSongPlayError();
                else
                    handler.post(() ->onOrderSongPlayError());
            }
        }
//  MediaSourceEventListener

        @Override
        public void onLoadError(int windowIndex,
                                @Nullable MediaSource.MediaPeriodId mediaPeriodId,
                                LoadEventInfo loadEventInfo,
                                MediaLoadData mediaLoadData,
                                IOException error,
                                boolean wasCanceled) {
            {
                if(!(error instanceof EOFException)) {
                    error.printStackTrace();
                    int state = orderSongPlayer.getPlaybackState();
                    if (state == Player.STATE_IDLE || state == Player.STATE_BUFFERING) {
                        if (Looper.myLooper() == Looper.getMainLooper())
                            onOrderSongLoadError();
                        else
                            handler.post(() -> onOrderSongLoadError());
                    }
                    else {
                        if (Looper.myLooper() == Looper.getMainLooper())
                            onOrderSongPlayError();
                        else
                            handler.post(() ->onOrderSongPlayError());
                    }
                }

            }
        }
        // IneStereoVolumeProcessor.Listener

        //    // MediaSource.MediaSourceCaller
//    @Override
//    public void onSourceInfoRefreshed(MediaSource source, Timeline timeline) {
//
//    }
        @Override
        public void onFormatError(IneStereoVolumeProcessor process, AudioProcessor.AudioFormat audioformat, String message) {
            final String localMessage = message;
            if (Looper.myLooper() == Looper.getMainLooper())
                onOrderSongPlayMessage(localMessage);
            else
                handler.post(() -> onOrderSongPlayMessage(localMessage));

        }
    };
    private class cPublicVideoPlayerListener implements SimpleExoPlayer.Listener, MediaSourceEventListener{
        // SimpleExoPlayer.Listener
        @Override
        public void onPlaybackStateChanged(int state) {
            if(state==SimpleExoPlayer.STATE_READY)
                onPublicVideoReady();
            if(state==SimpleExoPlayer.STATE_ENDED) {
                if(publicVideoClearMediaItems > 0) {
                    publicVideoClearMediaItems--;
                    return;
                }
                onPublicVideoEnd();
            }

        }

        @Override
        public void onMediaItemTransition(@Nullable MediaItem mediaItem, @Player.MediaItemTransitionReason int reason){
            if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO && currentPlayer == orderSongPlayer)
                stop();
        }
        @Override
        public void onPlayerError(ExoPlaybackException error) {
            int state = publicVideoPlayer.getPlaybackState();
            if (state == Player.STATE_IDLE || state == Player.STATE_BUFFERING) {
                if (Looper.myLooper() == Looper.getMainLooper())
                    onPublicVideoLoadError();
                else
                    handler.post(() -> onPublicVideoLoadError());
            }
            else {
                if (Looper.myLooper() == Looper.getMainLooper())
                    onPublicVideoPlayError();
                else
                    handler.post(() ->onPublicVideoPlayError());
            }
        }
//  MediaSourceEventListener

        @Override
        public void onLoadError(int windowIndex,
                                @Nullable MediaSource.MediaPeriodId mediaPeriodId,
                                LoadEventInfo loadEventInfo,
                                MediaLoadData mediaLoadData,
                                IOException error,
                                boolean wasCanceled) {
            {
                if(!(error instanceof EOFException)) {
                    error.printStackTrace();
                    int state = publicVideoPlayer.getPlaybackState();
                    if (state == Player.STATE_IDLE || state == Player.STATE_BUFFERING) {
                        if (Looper.myLooper() == Looper.getMainLooper())
                            onPublicVideoLoadError();
                        else
                            handler.post(() -> onPublicVideoLoadError());
                    }
                    else {
                        if (Looper.myLooper() == Looper.getMainLooper())
                            onPublicVideoPlayError();
                        else
                            handler.post(() ->onPublicVideoPlayError());
                    }
                }

            }
        }

    };
    private SimpleExoPlayer currentPlayer, orderSongPlayer, publicVideoPlayer;
    private cOrderSongPlayerListener orderSongPlayerListener = new cOrderSongPlayerListener();
    private cPublicVideoPlayerListener publicVideoPlayerListener = new cPublicVideoPlayerListener();
    private ArrayList<IneMediaSource> publicVideos;
    private ArrayList<IneMediaSource> orderSongs;
    private IneDataSource.Factory dataSourceFactory;
    private DefaultTrackSelector trackSelector;
    private boolean endOfOrderSong = true;
    private int cacheCount = 0;
    private int orderSongClearMediaItems = 0;
    private int publicVideoClearMediaItems = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long lastPosition = 0;
    private int idleCount = 0;
    private int nextOrderSongCount = 0;
    private int audioControlOutputMode = IneStereoVolumeProcessor.AudioControlOutput_RightMono;
    private boolean Paused = false;
    private IneStereoVolumeProcessor stereoVolumeProcessor;

    private InePlayerControllerConfigure config;

    public interface EventListen {
        default void onPlayListChange(InePlayerController controller) {};
        default void onOrderSongFinish(InePlayerController controller) {};
        default void onStop(InePlayerController controller, String Name, boolean isPublicVideo) {};
        default void onNext(InePlayerController controller, String Name, boolean isPublicVideo) {};
        default void onNextSongDisplay(InePlayerController controller, String Name) {}
        default void onLoadingError(InePlayerController controller, String Name) {};
        default void onPlayingError(InePlayerController controller, String Name, String Message) {};
        default void onAudioChannelMappingChanged(InePlayerController controller, int type) {};
    }
    public InePlayerController(InePlayerControllerConfigure config) {
        this.config = config.clone();

        stereoVolumeProcessor = new IneStereoVolumeProcessor(orderSongPlayerListener);
        trackSelector = new DefaultTrackSelector(this.config.context);
        IneRenderersFactory factory = new IneRenderersFactory(this.config.context, stereoVolumeProcessor);

        orderSongPlayer = new SimpleExoPlayer.Builder(this.config.context, factory)
                .setTrackSelector(trackSelector)
                .build();
        orderSongPlayer.addListener(orderSongPlayerListener);

        publicVideoPlayer = new SimpleExoPlayer.Builder(this.config.context).build();
        publicVideoPlayer.addListener(publicVideoPlayerListener);

        setCurrentPlayer(publicVideoPlayer);
        publicVideos = new ArrayList<>();
        orderSongs = new ArrayList<>();
        dataSourceFactory = new IneDataSource.Factory().setUserAgent(Util.getUserAgent(this.config.context, "InePlayer"));
    }
    public String[] GetVODServerList(){
        return IneDataSource.VODServerBaseUrl;
    }
    public void SetVODServerList(String[] VODServerList){
        IneDataSource.VODServerBaseUrl = VODServerList;
        IneDataSource.VODServerIndex = 0;
    }
    public void SetVODServerToNext(){
        int idx = IneDataSource.VODServerIndex;
        idx++;
        if(idx >= IneDataSource.VODServerBaseUrl.length)
            idx = 0;
        IneDataSource.VODServerIndex = idx;
    }
    public void AddOrderSong(String url, String name, int playingBufferSize) {
        String targetUrl;
        if(url.startsWith("http"))
            targetUrl = url;
        else
            targetUrl = IneDataSource.VODReplaceSign + url;
        if(Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(()->AddOrderSong(targetUrl, name, playingBufferSize));
            return;
        }


        InsertOrderSong(orderSongs.size(), targetUrl, name, playingBufferSize);
    }
    public void InsertOrderSong(int index, String url, String name, int playingBufferSize) {
        if(Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(()->InsertOrderSong(index, url, name, playingBufferSize));
            return;
        }

        String targetUrl;
        if(url.startsWith("http"))
            targetUrl = url;
        else
            targetUrl = IneDataSource.VODReplaceSign + url;

        IneMediaSource mediaSource = new IneMediaSource.Factory(dataSourceFactory, orderSongPlayerListener)
                .setContinueLoadingCheckIntervalBytes(playingBufferSize)
                .createMediaSource(MediaItem.fromUri(targetUrl));
        mediaSource.SongName = name;
        orderSongs.add(index, mediaSource);
        if(currentPlayer == orderSongPlayer) {
            orderSongPlayer.addMediaSource(index, mediaSource);
            orderSongPlayer.prepare();
        }
        if(cacheCount < config.maxCacheCount) {
            mediaSource.dataSource.startCacheBuffer(config.itemCacheSize, config.cacheBandwidthKBS[cacheCount]);
            cacheCount++;
        }
        PlayListChange();
    }

    public void AddPubVideo(String url, String name) {

        if(Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(()->AddPubVideo(url, name));
            return;
        }

        String targetUrl;
        if(url.startsWith("http"))
            targetUrl = url;
        else
            targetUrl = IneDataSource.VODReplaceSign + url;

        IneMediaSource mediaSource = new IneMediaSource.Factory(dataSourceFactory, publicVideoPlayerListener)
                .setContinueLoadingCheckIntervalBytes(config.playingBufferSize)
                .createMediaSource(MediaItem.fromUri(targetUrl));
        mediaSource.SongName = name;
        publicVideos.add(mediaSource);
        PlayListChange();
    }
    public void SetPubVideos(String urls[],String names[]) {
        if(Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(()->SetPubVideos(urls, names));
            return;
        }

        for(int i=0; i<urls.length; i++) {
            publicVideoPlayer.clearMediaItems();
            String targetUrl;
            if(urls[i].startsWith("http"))
                targetUrl = urls[i];
            else
                targetUrl = IneDataSource.VODReplaceSign + urls[i];
            IneMediaSource mediaSource = new IneMediaSource.Factory(dataSourceFactory, publicVideoPlayerListener)
                    .setContinueLoadingCheckIntervalBytes(config.playingBufferSize)
                    .createMediaSource(MediaItem.fromUri(targetUrl));
            mediaSource.SongName = names[i];
            publicVideos.add(mediaSource);
        }
        PlayListChange();
    }

//    public void SelectPubVideo(int index) {
//    }
    public void DeleteOrderSong(int index) {
        if(Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(()->DeleteOrderSong(index));
            return;
        }
        if(index < orderSongs.size()) {
            IneMediaSource mediaSource = orderSongs.get(index);
            orderSongPlayer.removeMediaItem(index);
            if(mediaSource.dataSource.CacheStatus!=IneDataSource.CacheStatus_NoCache)
                cacheCount--;
            orderSongs.get(index).dataSource.freeCacheBuffer();
            orderSongs.remove(index);
            PlayListChange();
        }
    }
    public String[] GetOrderSongPlayList() {
        ArrayList<String> list = new ArrayList<>();
        for(int i = 0; i < orderSongs.size(); i++)
            list.add(orderSongs.get(i).SongName);

        String[] listArray = new String[list.size()];
        listArray = list.toArray(listArray);
        return listArray;
    }
    public String[] GetPublicVideoPlayList() {
        ArrayList<String> list = new ArrayList<>();
        for(int i = 0; i < orderSongs.size(); i++)
            list.add(orderSongs.get(i).SongName);
        String[] listArray = new String[list.size()];
        listArray = list.toArray(listArray);
        return listArray;
    }
    public void AudioControlOutput(int type) {
        audioControlOutputMode = type;
        if(currentPlayer == orderSongPlayer){
            setAudioOutput(type);
        }
    }
    public boolean isInPublicVideo() {
        return currentPlayer == publicVideoPlayer;
    }
    public void open() {
        for(int i = 0; i < publicVideos.size(); i++) {
            publicVideoPlayer.addMediaSource(publicVideos.get(i));
        }
        setCurrentPlayer(publicVideoPlayer);
        publicVideoPlayer.prepare();
        checkOrderSong();
    }
    public void close() {
        handler.removeCallbacks(checkOrderSongLoop);
        orderSongPlayer.stop();
        orderSongClearMediaItems++;
        orderSongPlayer.clearMediaItems();
        publicVideoPlayer.stop();
        publicVideoClearMediaItems++;
        publicVideoPlayer.clearMediaItems();
        orderSongs.clear();
        publicVideos.clear();
    }
    public boolean isPaused() {
        return Paused;
    }
    public void pause() {
        if(currentPlayer == orderSongPlayer && !Paused) {
            orderSongPlayer.pause();
            Paused = true;
        }
    }
    public void resume() {
        if(currentPlayer == orderSongPlayer && Paused) {
            orderSongPlayer.play();
            Paused = false;
        }
    }
    public void replay() {
        if(currentPlayer == orderSongPlayer) {
            resume();
            orderSongPlayer.seekTo(orderSongPlayer.getCurrentWindowIndex(), C.TIME_UNSET);
        }
    }
    public void play() {
        if(orderSongs.size()>0) {
            if (!Paused)
                PlayListChange();
            else
                Paused = false;
        }
        //else if(publicVideos.size()>0)
    }
    public void stop() {
        if(Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(()->stop());
            return;
        }
        Paused = false;
        idleCount = 0;
        int publicVideoIdx = publicVideoPlayer.getCurrentWindowIndex();
        if(currentPlayer == publicVideoPlayer) {
            config.listener.onStop(this, publicVideos.get(publicVideoIdx).SongName, true);
            if(publicVideoPlayer.hasNext()) {
                publicVideoPlayer.next();
                config.listener.onNext(this, publicVideos.get(publicVideoIdx+1).SongName, true);
            }
            else {
                publicVideoPlayer.seekToDefaultPosition(0);
                config.listener.onNext(this, publicVideos.get(0).SongName, true);
            }
        }
        else {
            config.listener.onStop(this, orderSongs.get(0).SongName, false);


            if(orderSongs.size()>1) {
                nextOrderSongCount = 30;
                IneMediaSource mediaSource = orderSongs.get(0);
                if(mediaSource.dataSource.CacheStatus != IneDataSource.CacheStatus_NoCache)
                    cacheCount--;
                orderSongPlayer.removeMediaItem(0);
                mediaSource.dataSource.freeCacheBuffer();
                orderSongs.remove(0);

                PlayListChange();
                int orderSongIdx;
                for(orderSongIdx = 0; orderSongIdx < orderSongs.size() && (cacheCount < config.maxCacheCount); orderSongIdx++) {
                    mediaSource = orderSongs.get(orderSongIdx);
                    if(mediaSource.dataSource.CacheStatus == IneDataSource.CacheStatus_NoCache) {
                        mediaSource.dataSource.startCacheBuffer(config.itemCacheSize, config.cacheBandwidthKBS[config.maxCacheCount - 1]);
                        cacheCount++;
                    }
                }
                orderSongIdx = 0;
                for(int i = 1; (i < config.maxCacheCount) && (orderSongIdx < orderSongs.size()); i++, orderSongIdx++) {
                    mediaSource = orderSongs.get(orderSongIdx);
                    mediaSource.dataSource.changeCacheSpeed(config.cacheBandwidthKBS[i]);
                }
                config.listener.onNext(this, orderSongs.get(0).SongName, false);
                if(orderSongs.size()>1)
                    config.listener.onNextSongDisplay(this,orderSongs.get(1).SongName);
                setCurrentPlayer(publicVideoPlayer);
            }
            else {
                endOfOrderSong = true;
                config.listener.onOrderSongFinish(this);
                orderSongPlayer.removeMediaItem(0);

                //if(mediaSource.dataSource.CacheStatus != IneDataSource.CacheStatus_NoCache)
                //    CacheCount--;
                cacheCount = 0; //workaround
                //publicVideoClearMediaItems++;
                orderSongs.get(0).dataSource.freeCacheBuffer();
                orderSongs.remove(0);

                PlayListChange();
                //setAudioOutput(IneStereoVolumeProcessor.AudioControlOutput_Stereo);
                int CurrentPublicVideoWindow = publicVideoPlayer.getCurrentWindowIndex();
                //publicVideoPlayer.seekTo(CurrentPublicVideoWindow, CurrentPublicVideoPosition);
                setCurrentPlayer(publicVideoPlayer);
                config.listener.onNext(this, publicVideos.get(CurrentPublicVideoWindow).SongName, true);
            }

        }

    }
    public void cut() {
        if(currentPlayer == orderSongPlayer) {
            resume();
            stop();
        }
    }
    private Runnable checkOrderSongLoop = () -> checkOrderSong();

    private void checkOrderSong() {
         if (endOfOrderSong) {
             if ((currentPlayer == publicVideoPlayer) && orderSongs.size() != 0) {
                 IneMediaSource mediaSource = orderSongs.get(0);
                 if (mediaSource.dataSource.CacheStatus == IneDataSource.CacheStatus_Cached || mediaSource.dataSource.CacheStatus == IneDataSource.CacheStatus_CacheError) {
                     int CurrentPublicVideoWindow = publicVideoPlayer.getCurrentWindowIndex();
                     config.listener.onStop(this, publicVideos.get(CurrentPublicVideoWindow).SongName, true);
                     orderSongClearMediaItems++;
                     orderSongPlayer.clearMediaItems();  // will gen end event
                     for (int i = 0; i < orderSongs.size(); i++) {
                         orderSongPlayer.addMediaSource(orderSongs.get(i));
                     }
                     endOfOrderSong = false;
                     orderSongPlayer.prepare();
                     orderSongPlayer.seekToDefaultPosition(0);
                     setAudioOutput(audioControlOutputMode);
                     setCurrentPlayer(orderSongPlayer);
                     config.listener.onNext(this, orderSongs.get(0).SongName, false);
                     if (orderSongs.size() > 1)
                         config.listener.onNextSongDisplay(this, orderSongs.get(1).SongName);
                 }
             }
         }
         else {
             if (currentPlayer == publicVideoPlayer) {
                 if(nextOrderSongCount > 0)
                     nextOrderSongCount--;
                 else {
                     IneMediaSource mediaSource = orderSongs.get(0);
                     if (mediaSource.dataSource.CacheStatus == IneDataSource.CacheStatus_Cached || mediaSource.dataSource.CacheStatus == IneDataSource.CacheStatus_CacheError)
                         setCurrentPlayer(orderSongPlayer);
                     else {
                         // need handle timeout
                     }
                 }

             }
         }

        //protect player hang
        long position = orderSongPlayer.getCurrentPosition();

        if(!Paused && lastPosition == position )
            idleCount++;
        else
            idleCount = 0;
        if(idleCount >= 150) {
            idleCount = 0;
            if(currentPlayer == publicVideoPlayer) {
                if(publicVideos.size()>0)
                    stop();
            }
            else {
                stop();
            }
        }
        lastPosition = position;
        handler.postDelayed(checkOrderSongLoop , 100);
    }
    public void setAudioTrack(int track) {
        System.out.println("setAudioTrack: "  + track);
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        DefaultTrackSelector.Parameters parameters = trackSelector.getParameters();
        DefaultTrackSelector.ParametersBuilder builder = parameters.buildUpon();
        for (int rendererIndex = 0; rendererIndex < mappedTrackInfo.getRendererCount(); rendererIndex++) {
            int trackType = mappedTrackInfo.getRendererType(rendererIndex);
            if (trackType == C.TRACK_TYPE_AUDIO) {
                builder.clearSelectionOverrides(rendererIndex).setRendererDisabled(rendererIndex, false);
                int groupIndex = track -1;
                int [] tracks = {0};
                DefaultTrackSelector.SelectionOverride override = new DefaultTrackSelector.SelectionOverride(groupIndex,tracks);
                builder.setSelectionOverride(rendererIndex, mappedTrackInfo.getTrackGroups(rendererIndex), override);
            }
        }
        trackSelector.setParameters(builder);
    }
    protected void setCurrentPlayer(SimpleExoPlayer newPlayer) {
        if(currentPlayer == newPlayer)
            return;
        if(newPlayer == orderSongPlayer) {
            publicVideoPlayer.setVideoSurfaceView(null);
            publicVideoPlayer.pause();
            orderSongPlayer.setVideoSurfaceView(config.playerView);
            orderSongPlayer.play();
            currentPlayer = orderSongPlayer;
        }
        if(newPlayer == publicVideoPlayer) {
            orderSongPlayer.setVideoSurfaceView(null);
            orderSongPlayer.pause();
            publicVideoPlayer.setVideoSurfaceView(config.playerView);
            publicVideoPlayer.play();
            currentPlayer = publicVideoPlayer;
        }
    }
    protected void setAudioOutput(int type) {
        int AudioTracks = 0;
        for(int i = 0; i < orderSongPlayer.getCurrentTrackGroups().length; i++){
            String format = orderSongPlayer.getCurrentTrackGroups().get(i).getFormat(0).sampleMimeType;
//            String lang = player.getCurrentTrackGroups().get(i).getFormat(0).language;
//            String id = player.getCurrentTrackGroups().get(i).getFormat(0).id;
//
//            System.out.println(player.getCurrentTrackGroups().get(i).getFormat(0));
//            if(format.contains("audio") && id != null && lang != null)
                //System.out.println(lang + " " + id);
            if(format.contains("audio"))
                AudioTracks++;
        }

        if(AudioTracks > 1) {
            if(type == IneStereoVolumeProcessor.AudioControlOutput_RightMono)
                setAudioTrack(1);
            else
                setAudioTrack(2);
        }
        else {
            stereoVolumeProcessor.setMode(type);
        }
        config.listener.onAudioChannelMappingChanged(this, type);
    }
    protected void PlayListChange() {
        config.listener.onPlayListChange(this);
    }
    protected void onOrderSongReady() {
        //play();
    }
    protected void onPublicVideoReady() {
        //play();
    }
    protected void onOrderSongEnd() {
        Log.d("onOrderSongEnd","End");
        //stop();
    }
    protected void onPublicVideoEnd() {
        Log.d("onPublicVideoEnd","End");
        //stop();
    }
    protected void onOrderSongLoadError() {
        config.listener.onLoadingError(this, orderSongs.get(0).SongName);
        stop();
    }

    protected void onPublicVideoLoadError() {
        config.listener.onLoadingError(this, publicVideos.get(publicVideoPlayer.getCurrentWindowIndex()).SongName);
        stop();
    }
    protected void onOrderSongPlayMessage(String message) {
        config.listener.onPlayingError(this, orderSongs.get(0).SongName, message);
    }
    protected void onPublicVideoPlayMessage(String message) {
        config.listener.onPlayingError(this, publicVideos.get(publicVideoPlayer.getCurrentWindowIndex()).SongName, message);
    }
    protected void onOrderSongPlayError() {
        onOrderSongPlayMessage("無法撥放");
        stop();
    }
    protected void onPublicVideoPlayError() {
        onPublicVideoPlayMessage("無法撥放");
        stop();
    }
}
