package com.ine.ktv.playerengine;

import android.content.Context;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.IneRenderersFactory;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.IneStereoVolumeProcessor;
import com.google.android.exoplayer2.ext.ffmpeg.FfmpegLibrary;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.IneDataSource;
import com.google.android.exoplayer2.source.IneMediaSource;
import com.google.android.exoplayer2.source.LoadEventInfo;
import com.google.android.exoplayer2.source.MediaLoadData;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class InePlayerController {
    private static int IdleTimeOut = 150; // 0.1s

    static public class InePlayerControllerConfigure {
        public Context context;
//        public SurfaceView orderSongView, publicVideoView;
        public SurfaceView display;
        public int maxCacheCount = 4;
        public int itemCacheSize = 1024 * 1024 * 2;
        public int[] cacheBandwidthKBS = new int[]{512, 256, 128, 64};
        public int publicVideoPlayingBufferSize = 1024 * 1024 * 32;
        public EventListen listener;

        public InePlayerControllerConfigure clone() {
            InePlayerControllerConfigure newConfig = new InePlayerControllerConfigure();
            newConfig.context = this.context;
//            newConfig.orderSongView = this.orderSongView;
//            newConfig.publicVideoView = this.publicVideoView;
            newConfig.display = this.display;
            newConfig.maxCacheCount = this.maxCacheCount;
            newConfig.itemCacheSize = this.itemCacheSize;
            newConfig.cacheBandwidthKBS = new int[this.cacheBandwidthKBS.length];
            newConfig.publicVideoPlayingBufferSize = this.publicVideoPlayingBufferSize;
            System.arraycopy(this.cacheBandwidthKBS, 0, newConfig.cacheBandwidthKBS, 0, this.cacheBandwidthKBS.length);
            newConfig.listener = this.listener;
            return newConfig;
        }
    }

    ;

    private class cOrderSongPlayerListener implements SimpleExoPlayer.Listener, MediaSourceEventListener, IneStereoVolumeProcessor.Listener, IneDataSource.Listener {
        // Player.Listener
        @Override
        public void onPlaybackStateChanged(int state) {
            if (state == SimpleExoPlayer.STATE_READY) {
                showTracks();
                if (needSwitchSurface) {
                    needSwitchSurface = false;
                    switchToOrderSongPlayer();
                }
                onOrderSongReady();
            }
            if (state == ExoPlayer.STATE_ENDED) {
                if (orderSongClearMediaItems > 0) {
                    orderSongClearMediaItems--;
                    return;
                }
                onOrderSongEnd();
            }

        }

        @Override
        public void onMediaItemTransition(@Nullable MediaItem mediaItem, @Player.MediaItemTransitionReason int reason) {
            if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO)
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
            } else {
                if (Looper.myLooper() == Looper.getMainLooper())
                    onOrderSongPlayError();
                else
                    handler.post(() -> onOrderSongPlayError());
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
                if (!(error instanceof EOFException)) {
                    error.printStackTrace();
                    int state = orderSongPlayer.getPlaybackState();
                    if (state == Player.STATE_IDLE || state == Player.STATE_BUFFERING) {
                        if (Looper.myLooper() == Looper.getMainLooper())
                            onOrderSongLoadError();
                        else
                            handler.post(() -> onOrderSongLoadError());
                    } else {
                        if (Looper.myLooper() == Looper.getMainLooper())
                            onOrderSongPlayError();
                        else
                            handler.post(() -> onOrderSongPlayError());
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

        // IneDataSource.Listener
        @Override
        public void OnIneDataSourceError(int errorType, String url, String message) {

        }
    }

    ;

    private class cPublicVideoPlayerListener implements SimpleExoPlayer.Listener, MediaSourceEventListener, IneDataSource.Listener {
        // SimpleExoPlayer.Listener
        @Override
        public void onPlaybackStateChanged(int state) {
            if (state == SimpleExoPlayer.STATE_READY) {
                if (PublicVideoPlayWhenReady) {
                    PublicVideoPlayWhenReady = false;
                    publicVideoPlayer.play();
                }
                onPublicVideoReady();
            }
            if (state == SimpleExoPlayer.STATE_ENDED) {
            }
        }

        @Override
        public void onMediaItemTransition(@Nullable MediaItem mediaItem, @Player.MediaItemTransitionReason int reason) {
            if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                int publicVideoIdx = publicVideoPlayer.getCurrentWindowIndex();
                if (currentPlayer == publicVideoPlayer) {
                    if (publicVideos.size() - 1 == publicVideoIdx) {
                        if (publicVideoPlayerHadError) {
                            publicVideoPlayerHadError = false;
                            publicVideoPlayer.clearMediaItems();
                            for (int i = 0; i < publicVideos.size(); i++) {
                                publicVideoPlayer.addMediaSource(publicVideos.get(i));
                            }
                        }
                        onPublicVideoEnd();
                    }
                    if (publicVideos.size() > publicVideoIdx)
                        config.listener.onStop(that, publicVideos.get(publicVideoIdx).SongName, true);
                    else
                        config.listener.onStop(that, "Unknown", true);
                }
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            int state = publicVideoPlayer.getPlaybackState();
            if (state == Player.STATE_IDLE || state == Player.STATE_BUFFERING) {
                if (Looper.myLooper() == Looper.getMainLooper())
                    onPublicVideoLoadError();
                else
                    handler.post(() -> onPublicVideoLoadError());
            } else {
                if (Looper.myLooper() == Looper.getMainLooper())
                    onPublicVideoPlayError();
                else
                    handler.post(() -> onPublicVideoPlayError());
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
                if (!(error instanceof EOFException)) {
                    error.printStackTrace();
                    int state = publicVideoPlayer.getPlaybackState();
                    if (state == Player.STATE_IDLE || state == Player.STATE_BUFFERING) {
                        if (Looper.myLooper() == Looper.getMainLooper())
                            onPublicVideoLoadError();
                        else
                            handler.post(() -> onPublicVideoLoadError());
                    } else {
                        if (Looper.myLooper() == Looper.getMainLooper())
                            onPublicVideoPlayError();
                        else
                            handler.post(() -> onPublicVideoPlayError());
                    }
                }

            }
        }

        // IneDataSource.Listener
        @Override
        public void OnIneDataSourceError(int errorType, String url, String message) {

        }
    }

    ;
    private InePlayerController that = this;
    private SimpleExoPlayer currentPlayer = null, orderSongPlayer, publicVideoPlayer;
    private cOrderSongPlayerListener orderSongPlayerListener = new cOrderSongPlayerListener();
    private cPublicVideoPlayerListener publicVideoPlayerListener = new cPublicVideoPlayerListener();
    private ArrayList<IneMediaSource> publicVideos;
    private ArrayList<IneMediaSource> orderSongs;
    private IneDataSource.Factory dataSourceFactory;
    private DefaultTrackSelector trackSelector;
    private boolean endOfOrderSong = true;
    private boolean needSwitchSurface = false;
    private boolean orderSongPlayerHadError = false;
    private boolean publicVideoPlayerHadError = false;
    private int cacheCount = 0;
    private int orderSongClearMediaItems = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long lastOrderSongPosition = 0;
    private long lastPublicVideoPosition = 0;
    private int orderSongPlayerIdleCount = 0;
    private int publicVideoPlayerIdleCount = 0;
    private int nextOrderSongCount = 0;
    private int audioControlOutputMode = IneStereoVolumeProcessor.AudioControlOutput_RightMono;
    private boolean Paused = false;
    private boolean PublicVideoPlayWhenReady = false;
    private IneStereoVolumeProcessor stereoVolumeProcessor;

    private InePlayerControllerConfigure config;

    public interface EventListen {
        default void onPlayListChange(InePlayerController controller) {
        }

        ;

        default void onOrderSongFinish(InePlayerController controller) {
        }

        ;

        default void onStop(InePlayerController controller, String Name, boolean isPublicVideo) {
        }

        ;

        default void onNext(InePlayerController controller, String Name, boolean isPublicVideo) {
        }

        ;

        default void onNextSongDisplay(InePlayerController controller, String Name) {
        }

        default void onLoadingError(InePlayerController controller, String Name) {
        }

        ;

        default void onPlayingError(InePlayerController controller, String Name, String Message) {
        }

        ;

        default void onAudioChannelMappingChanged(InePlayerController controller, int type) {
        }

        ;
    }

    public InePlayerController(InePlayerControllerConfigure config) {
        this.config = config.clone();

        publicVideos = new ArrayList<>();
        orderSongs = new ArrayList<>();

        stereoVolumeProcessor = new IneStereoVolumeProcessor(orderSongPlayerListener);
        trackSelector = new DefaultTrackSelector(this.config.context);
        IneRenderersFactory factory = new IneRenderersFactory(this.config.context, stereoVolumeProcessor);
        factory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON);

        orderSongPlayer = new SimpleExoPlayer.Builder(this.config.context, factory)
                .setTrackSelector(trackSelector)
                .build();
//        showCodecs();
        orderSongPlayer.addListener(orderSongPlayerListener);
//        config.orderSongView.setPlayer(orderSongPlayer);
//        orderSongPlayer.setVideoSurfaceView(config.orderSongView);

        publicVideoPlayer = new SimpleExoPlayer.Builder(this.config.context).build();
        publicVideoPlayer.addListener(publicVideoPlayerListener);
        publicVideoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
//        config.publicVideoView.setPlayer(publicVideoPlayer);
//        publicVideoPlayer.setVideoSurfaceView(config.publicVideoView);

        setCurrentPlayer(publicVideoPlayer);

        dataSourceFactory = new IneDataSource.Factory().setUserAgent(Util.getUserAgent(this.config.context, "InePlayer"));
    }

    public void showCodecs() {
        //com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
        MediaCodecList androidCodecs = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        for (MediaCodecInfo codecInfo : androidCodecs.getCodecInfos()) {
            Log.d("codec", codecInfo.getName() + " " + Arrays.toString(codecInfo.getSupportedTypes()));
        }
        Log.d("codec", "ffmpeg support ac3 : " + ((FfmpegLibrary.supportsFormat(MimeTypes.AUDIO_AC3)) ? "true" : "false"));
    }

    public String[] GetVODServerList() {
        return IneDataSource.VODServerBaseUrl;
    }

    public void SetVODServerList(String[] VODServerList) {
        IneDataSource.VODServerBaseUrl = VODServerList;
        IneDataSource.VODServerIndex = 0;
    }

    public void SetVODServerToNext() {
        int idx = IneDataSource.VODServerIndex;
        idx++;
        if (idx >= IneDataSource.VODServerBaseUrl.length)
            idx = 0;
        IneDataSource.VODServerIndex = idx;
    }

    public void AddOrderSong(String url, String name, int playingBufferSize) {
        String targetUrl;
        if (url.startsWith("http"))
            targetUrl = url;
        else
            targetUrl = IneDataSource.VODReplaceSign + url;
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(() -> AddOrderSong(targetUrl, name, playingBufferSize));
            return;
        }


        InsertOrderSong(orderSongs.size(), targetUrl, name, playingBufferSize);
    }

    public void InsertOrderSong(int index, String url, String name, int playingBufferSize) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(() -> InsertOrderSong(index, url, name, playingBufferSize));
            return;
        }

        String targetUrl;
        if (url.startsWith("http"))
            targetUrl = url;
        else
            targetUrl = IneDataSource.VODReplaceSign + url;

        IneMediaSource mediaSource = new IneMediaSource.Factory(dataSourceFactory, orderSongPlayerListener)
                .setContinueLoadingCheckIntervalBytes(playingBufferSize)
                .createMediaSource(MediaItem.fromUri(targetUrl));
        mediaSource.SongName = name;
        mediaSource.dataSource.listener = orderSongPlayerListener;
        orderSongs.add(index, mediaSource);
        orderSongPlayer.addMediaSource(index, mediaSource);
        //orderSongPlayer.prepare();
        if (cacheCount < config.maxCacheCount) {
            mediaSource.dataSource.startCacheBuffer(config.itemCacheSize, config.cacheBandwidthKBS[cacheCount]);
            cacheCount++;
        }
        PlayListChange();
    }

    public void AddPubVideo(String url, String name) {

        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(() -> AddPubVideo(url, name));
            return;
        }

        String targetUrl;
        if (url.startsWith("http"))
            targetUrl = url;
        else
            targetUrl = IneDataSource.VODReplaceSign + url;

        IneMediaSource mediaSource = new IneMediaSource.Factory(dataSourceFactory, publicVideoPlayerListener)
                .setContinueLoadingCheckIntervalBytes(config.publicVideoPlayingBufferSize)
                .createMediaSource(MediaItem.fromUri(targetUrl));
        mediaSource.dataSource.listener = publicVideoPlayerListener;
        mediaSource.SongName = name;
        publicVideos.add(mediaSource);
        PlayListChange();
    }

    public void SetPubVideos(String urls[], String names[]) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(() -> SetPubVideos(urls, names));
            return;
        }
        publicVideoPlayer.clearMediaItems();
        for (int i = 0; i < urls.length; i++) {

            String targetUrl;
            if (urls[i].startsWith("http"))
                targetUrl = urls[i];
            else
                targetUrl = IneDataSource.VODReplaceSign + urls[i];
            IneMediaSource mediaSource = new IneMediaSource.Factory(dataSourceFactory, publicVideoPlayerListener)
                    .setContinueLoadingCheckIntervalBytes(config.publicVideoPlayingBufferSize)
                    .createMediaSource(MediaItem.fromUri(targetUrl));
            mediaSource.dataSource.listener = publicVideoPlayerListener;
            mediaSource.SongName = names[i];
            publicVideos.add(mediaSource);
        }
        PlayListChange();
    }

    //    public void SelectPubVideo(int index) {
//    }
    public void DeleteOrderSong(int index) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(() -> DeleteOrderSong(index));
            return;
        }
        if (index < orderSongs.size()) {
            IneMediaSource mediaSource = orderSongs.get(index);
            orderSongPlayer.removeMediaItem(index);
            if (mediaSource.dataSource.CacheStatus != IneDataSource.CacheStatus_NoCache)
                cacheCount--;
            orderSongs.get(index).dataSource.freeCacheBuffer();
            orderSongs.remove(index);
            PlayListChange();
        }
    }

    public String[] GetOrderSongPlayList() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < orderSongs.size(); i++)
            list.add(orderSongs.get(i).SongName);

        String[] listArray = new String[list.size()];
        listArray = list.toArray(listArray);
        return listArray;
    }

    public String[] GetPublicVideoPlayList() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < orderSongs.size(); i++)
            list.add(orderSongs.get(i).SongName);
        String[] listArray = new String[list.size()];
        listArray = list.toArray(listArray);
        return listArray;
    }

    public void AudioControlOutput(int type) {
        audioControlOutputMode = type;
        if (currentPlayer == orderSongPlayer) {
            setAudioOutput(type);
        }
    }

    public boolean isInPublicVideo() {
        return (currentPlayer == publicVideoPlayer);
    }

    public void open() {
        for (int i = 0; i < publicVideos.size(); i++) {
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
        publicVideoPlayer.clearMediaItems();
        orderSongs.clear();
        publicVideos.clear();
    }

    public boolean isPaused() {
        return Paused;
    }

    public void pause() {
        if (currentPlayer == orderSongPlayer && !Paused) {
            orderSongPlayer.pause();
            Paused = true;
        }
    }

    public void resume() {
        if (currentPlayer == orderSongPlayer && Paused) {
            setCurrentPlayer(orderSongPlayer);
            orderSongPlayer.play();
            Paused = false;
        }
    }

    public void replay() {
        if (currentPlayer == orderSongPlayer) {
            resume();
            orderSongPlayer.seekTo(orderSongPlayer.getCurrentWindowIndex(), C.TIME_UNSET);
        }
    }

    public void play() {
        if (orderSongs.size() > 0) {
            setCurrentPlayer(orderSongPlayer);
            if (!Paused)
                PlayListChange();
            else
                Paused = false;
        }
        //else if(publicVideos.size()>0)
    }

    public void publicVideoNext() {
        if (currentPlayer == publicVideoPlayer) {
            int publicVideoIdx = publicVideoPlayer.getCurrentWindowIndex();
            publicVideoPlayerIdleCount = 0;
            if (publicVideos.size() > publicVideoIdx)
                config.listener.onStop(this, publicVideos.get(publicVideoIdx).SongName, true);
            else
                config.listener.onStop(this, "Unknown", true);
            if (publicVideoPlayer.hasNext()) {
                publicVideoPlayer.next();
                if (publicVideos.size() > publicVideoIdx + 1)
                    config.listener.onNext(this, publicVideos.get(publicVideoIdx + 1).SongName, true);
                else
                    config.listener.onStop(this, "Unknown", true);
            } else {
                if (publicVideoPlayerHadError) {
                    publicVideoPlayerHadError = false;
                    publicVideoPlayer.clearMediaItems();
                    for (int i = 0; i < publicVideos.size(); i++) {
                        publicVideoPlayer.addMediaSource(publicVideos.get(i));
                    }
                }
                publicVideoPlayer.seekToDefaultPosition(0);
                if (publicVideos.size() > 0)
                    config.listener.onNext(this, publicVideos.get(0).SongName, true);
                else
                    config.listener.onStop(this, "Unknown", true);
            }
        }
    }

    public void stop() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(() -> stop());
            return;
        }
        Paused = false;
        orderSongPlayerIdleCount = 0;

        if (currentPlayer == orderSongPlayer) {
            orderSongPlayer.pause();
            config.listener.onStop(this, orderSongs.get(0).SongName, false);
            if (orderSongs.size() > 1) {

                nextOrderSongCount = 50;
                IneMediaSource mediaSource = orderSongs.get(0);
                if (mediaSource.dataSource.CacheStatus != IneDataSource.CacheStatus_NoCache)
                    cacheCount--;
                orderSongPlayer.removeMediaItem(0);
                mediaSource.dataSource.freeCacheBuffer();
                orderSongs.remove(0);

                PlayListChange();
                int orderSongIdx;
                for (orderSongIdx = 0; orderSongIdx < orderSongs.size() && (cacheCount < config.maxCacheCount); orderSongIdx++) {
                    mediaSource = orderSongs.get(orderSongIdx);
                    if (mediaSource.dataSource.CacheStatus == IneDataSource.CacheStatus_NoCache) {
                        mediaSource.dataSource.startCacheBuffer(config.itemCacheSize, config.cacheBandwidthKBS[config.maxCacheCount - 1]);
                        cacheCount++;
                    }
                }
                orderSongIdx = 0;
                for (int i = 1; (i < config.maxCacheCount) && (orderSongIdx < orderSongs.size()); i++, orderSongIdx++) {
                    mediaSource = orderSongs.get(orderSongIdx);
                    mediaSource.dataSource.changeCacheSpeed(config.cacheBandwidthKBS[i]);
                }
                config.listener.onNext(this, orderSongs.get(0).SongName, false);
                if (orderSongs.size() > 1)
                    config.listener.onNextSongDisplay(this, orderSongs.get(1).SongName);
                setCurrentPlayer(publicVideoPlayer);
            } else {

                endOfOrderSong = true;
                config.listener.onOrderSongFinish(this);
                orderSongPlayer.removeMediaItem(0);

                //if(mediaSource.dataSource.CacheStatus != IneDataSource.CacheStatus_NoCache)
                //    CacheCount--;
                cacheCount = 0; //workaround
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
        if (currentPlayer == orderSongPlayer) {
            resume();
            stop();
        }
    }

    private void ReloadOrderSong() {
        orderSongClearMediaItems++;
        orderSongPlayer.clearMediaItems();  // will gen end event
        for (int i = 0; i < orderSongs.size(); i++) {
            orderSongPlayer.addMediaSource(orderSongs.get(i));
        }
        //orderSongPlayer.prepare();
        orderSongPlayer.seekToDefaultPosition(0);
    }

    private Runnable checkOrderSongLoop = () -> checkOrderSong();

    private void checkOrderSong() {
        if (endOfOrderSong) {
            if (currentPlayer == publicVideoPlayer && orderSongs.size() != 0) {
                IneMediaSource mediaSource = orderSongs.get(0);
                if (mediaSource.dataSource.CacheStatus == IneDataSource.CacheStatus_Cached || mediaSource.dataSource.CacheStatus == IneDataSource.CacheStatus_CacheError) {
                    int CurrentPublicVideoWindow = publicVideoPlayer.getCurrentWindowIndex();
                    config.listener.onStop(this, publicVideos.get(CurrentPublicVideoWindow).SongName, true);
                    //ReloadOrderSong();
                    endOfOrderSong = false;
                    setAudioOutput(audioControlOutputMode);
                    setCurrentPlayer(orderSongPlayer);
                    config.listener.onNext(this, orderSongs.get(0).SongName, false);
                    if (orderSongs.size() > 1)
                        config.listener.onNextSongDisplay(this, orderSongs.get(1).SongName);
                }
            }
        } else {
            if (currentPlayer == publicVideoPlayer) {
                if (orderSongPlayerHadError) {
                    orderSongPlayer.stop(true);
                    ReloadOrderSong();
                    orderSongPlayerHadError = false;
                }
                if (nextOrderSongCount > 0)
                    nextOrderSongCount--;
                else {
                    IneMediaSource mediaSource = orderSongs.get(0);
                    if (mediaSource.dataSource.CacheStatus == IneDataSource.CacheStatus_Cached || mediaSource.dataSource.CacheStatus == IneDataSource.CacheStatus_CacheError) {
                        setCurrentPlayer(orderSongPlayer);
                    } else {
                        // need handle timeout
                    }
                }

            }
        }

        //protect player hang
        if (currentPlayer == orderSongPlayer) {
            long position = orderSongPlayer.getCurrentPosition();

            if (!Paused && lastOrderSongPosition == position)
                orderSongPlayerIdleCount++;
            else
                orderSongPlayerIdleCount = 0;
            if (orderSongPlayerIdleCount >= IdleTimeOut) {
                needSwitchSurface = false;
                orderSongPlayerIdleCount = 0;
                onOrderSongPlayError();
            }
            lastOrderSongPosition = position;
        }
        if (currentPlayer == publicVideoPlayer) {
            long position = publicVideoPlayer.getCurrentPosition();

            if (lastPublicVideoPosition == position)
                publicVideoPlayerIdleCount++;
            else
                publicVideoPlayerIdleCount = 0;
            if (publicVideoPlayerIdleCount >= IdleTimeOut) {
                publicVideoPlayerIdleCount = 0;
                if (publicVideos.size() > 0) {
                    onPublicVideoPlayError();
                }
            }
            lastPublicVideoPosition = position;
        }
        handler.postDelayed(checkOrderSongLoop, 100);
    }

    public void setParameters() {
        DefaultTrackSelector.ParametersBuilder builder = trackSelector.getParameters().buildUpon();
        builder.setMaxVideoBitrate(12000000);
        builder.setMaxVideoFrameRate(30);
        builder.setMaxVideoSize(1920, 1080);
        trackSelector.setParameters(builder);
    }

    public void showTracks() {
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        for (int rendererIndex = 0; rendererIndex < mappedTrackInfo.getRendererCount(); rendererIndex++) {
            int trackType = mappedTrackInfo.getRendererType(rendererIndex);
            TrackGroupArray groups = mappedTrackInfo.getTrackGroups(rendererIndex);
            for (int i = 0; i < groups.length; i++) {
                TrackGroup group = groups.get(i);
                for (int j = 0; j < group.length; j++) {
                    Format format = group.getFormat(j);
                    if (format.sampleMimeType.contains("video"))
                        Log.d("showTracks", "[ Render:" + rendererIndex + " Group:" + i + " Track:" + j + "] " + format.codecs + " " + format.sampleMimeType + " " + format.width + "x" + format.height + " " + format.frameRate);
                    else
                        Log.d("showTracks", "[ Render:" + rendererIndex + " Group:" + i + " Track:" + j + "] " + format.codecs + " " + format.sampleMimeType + " " + format.sampleRate);
                    try {
                        List<com.google.android.exoplayer2.mediacodec.MediaCodecInfo> infos = MediaCodecUtil.getDecoderInfos(format.sampleMimeType, false, false);
                        for (com.google.android.exoplayer2.mediacodec.MediaCodecInfo info : infos) {
                            String profileLevels = "";
                            for (MediaCodecInfo.CodecProfileLevel level : info.getProfileLevels())
                                profileLevels += "[" + level.profile + "," + level.level + "],";
                            Log.d("showTracks", "codec " +
                                    info.vendor + " " +
                                    (info.hardwareAccelerated ? "hardwareAccelerated " : "") +
                                    (info.softwareOnly ? "softwareOnly " : "") +
                                    "ProfileLevels " + profileLevels);
                        }
                    } catch (MediaCodecUtil.DecoderQueryException e) {
                        e.printStackTrace();
                    }

                }

            }
        }
    }

    public void setAudioTrack(int track) {
        System.out.println("setAudioTrack: " + track);
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();

        ArrayList<Integer> AudioTrackers = new ArrayList<Integer>();
        for (int rendererIndex = 0; rendererIndex < mappedTrackInfo.getRendererCount(); rendererIndex++) {
            int trackType = mappedTrackInfo.getRendererType(rendererIndex);
            if (trackType == C.TRACK_TYPE_AUDIO)
                AudioTrackers.add(rendererIndex);
        }
        boolean found = false;
        for (int audioTrackIndex = 0; audioTrackIndex < AudioTrackers.size(); audioTrackIndex++) {
            int rendererIndex = AudioTrackers.get(audioTrackIndex);
            DefaultTrackSelector.Parameters parameters = trackSelector.getParameters();
            DefaultTrackSelector.ParametersBuilder builder = parameters.buildUpon();
            builder.clearSelectionOverrides(rendererIndex).setRendererDisabled(rendererIndex, true);
            trackSelector.setParameters(builder);
        }
        for (int audioTrackIndex = 0; audioTrackIndex < AudioTrackers.size(); audioTrackIndex++) {
            int rendererIndex = AudioTrackers.get(audioTrackIndex);
            DefaultTrackSelector.Parameters parameters = trackSelector.getParameters();
            DefaultTrackSelector.ParametersBuilder builder = parameters.buildUpon();

            TrackGroupArray tg = mappedTrackInfo.getTrackGroups(rendererIndex);
            for (int tgIndex = 0; tgIndex < tg.length; tgIndex++) {
                TrackGroup group = tg.get(tgIndex);
                for (int gIndex = 0; gIndex < group.length; gIndex++) {
                    track--;
                    if (track == 0) {
                        int[] tracks = {tgIndex};
                        DefaultTrackSelector.SelectionOverride override = new DefaultTrackSelector.SelectionOverride(gIndex, tracks);
                        builder.setSelectionOverride(rendererIndex, tg, override);
                        builder.setRendererDisabled(rendererIndex, false);
                        trackSelector.setParameters(builder);
                        Log.d("setAudioTrack", "selected Render:" + rendererIndex + " TrackGroupIndex:" + tgIndex + " groupIndex:" + gIndex);
                        found = true;
                        break;
                    }
                }
                if (found)
                    break;
            }
            if (found)
                break;
        }
    }

    protected void setAudioOutput(int type) {
        int AudioTracks = 0;
        for (int i = 0; i < orderSongPlayer.getCurrentTrackGroups().length; i++) {
            String format = orderSongPlayer.getCurrentTrackGroups().get(i).getFormat(0).sampleMimeType;
//            String lang = player.getCurrentTrackGroups().get(i).getFormat(0).language;
//            String id = player.getCurrentTrackGroups().get(i).getFormat(0).id;
//
//            System.out.println(player.getCurrentTrackGroups().get(i).getFormat(0));
//            if(format.contains("audio") && id != null && lang != null)
            //System.out.println(lang + " " + id);
            if (format.contains("audio"))
                AudioTracks++;
        }

        if (AudioTracks > 1) {
            if (type == IneStereoVolumeProcessor.AudioControlOutput_RightMono)
                setAudioTrack(1);
            else
                setAudioTrack(2);
        } else {
            stereoVolumeProcessor.setMode(type);
        }
        config.listener.onAudioChannelMappingChanged(this, type);
    }

    private void switchToOrderSongPlayer() {
        publicVideoPlayer.stop();

//        config.publicVideoView.setVisibility(View.GONE);
//        config.orderSongView.setVisibility(View.VISIBLE);
        publicVideoPlayer.setVideoSurfaceView(null);
        orderSongPlayer.setVideoSurfaceView(config.display);
        orderSongClearMediaItems++;
        //setParameters();

        //setParameters();
        orderSongPlayer.play();
        currentPlayer = orderSongPlayer;

        publicVideoPlayer.setPlayWhenReady(false);
        publicVideoPlayer.prepare();
    }

    protected void setCurrentPlayer(SimpleExoPlayer newPlayer) {

        if (currentPlayer == newPlayer)
            return;
        if (newPlayer == orderSongPlayer) {
            //CurrentPublicVideoWindow = publicVideoPlayer.getCurrentWindowIndex();
            //CurrentPublicVideoPosition = publicVideoPlayer.getCurrentPosition();
            orderSongPlayer.prepare();
            if (orderSongPlayer.getPlaybackState() == Player.STATE_READY) {
                switchToOrderSongPlayer();
            } else {
                needSwitchSurface = true;
            }

            //MainActivity._this.OrderNextTest();
        }
        if (newPlayer == publicVideoPlayer) {
            orderSongPlayer.stop();

            orderSongPlayer.setVideoSurfaceView(null);
            publicVideoPlayer.setVideoSurfaceView(config.display);
//            config.orderSongView.setVisibility(View.GONE);
//            config.publicVideoView.setVisibility(View.VISIBLE);
            publicVideoPlayer.setPlayWhenReady(false);
            publicVideoPlayer.prepare();
            int state = publicVideoPlayer.getPlaybackState();
            switch (state) {
                case Player.STATE_BUFFERING:
                    PublicVideoPlayWhenReady = true;
                    break;
                case Player.STATE_READY:
                    publicVideoPlayer.play();
                    break;
                case Player.STATE_ENDED:
                    publicVideoPlayer.seekTo(0, 0);
                    publicVideoPlayer.play();
                    break;
            }
            currentPlayer = publicVideoPlayer;
        }
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
        Log.d("onOrderSongEnd", "End");
    }

    protected void onPublicVideoEnd() {
        Log.d("onPublicVideoEnd", "End");
    }

    protected void onOrderSongLoadError() {
        if (orderSongs.size() > 0)
            config.listener.onLoadingError(this, orderSongs.get(0).SongName);
        else
            config.listener.onLoadingError(this, "Unknown");
        stop();
    }

    protected void onPublicVideoLoadError() {
        int idx = publicVideoPlayer.getCurrentWindowIndex();
        String SongName;
        if (publicVideos.size() > idx)
            SongName = "公播:" + publicVideos.get(idx).SongName;
        else
            SongName = "公播:未知歌曲";
        config.listener.onLoadingError(this, SongName);
        publicVideoNext();
    }

    protected void onOrderSongPlayMessage(String message) {
        if (orderSongs.size() > 0)
            config.listener.onPlayingError(this, orderSongs.get(0).SongName, message);
        else
            config.listener.onPlayingError(this, "Unknown", message);
    }

    protected void onPublicVideoPlayMessage(String message) {
        int idx = publicVideoPlayer.getCurrentWindowIndex();
        String SongName;
        if (publicVideos.size() > idx)
            SongName = "公播:" + publicVideos.get(idx).SongName;
        else
            SongName = "公播:未知歌曲";
        config.listener.onPlayingError(this, SongName, message);
    }

    protected void onOrderSongPlayError() {
        onOrderSongPlayMessage("無法撥放");
        orderSongPlayerHadError = true;
        stop();
    }

    protected void onPublicVideoPlayError() {
        onPublicVideoPlayMessage("公播:無法撥放");
        publicVideoPlayerHadError = true;
        publicVideoNext();
    }
}
