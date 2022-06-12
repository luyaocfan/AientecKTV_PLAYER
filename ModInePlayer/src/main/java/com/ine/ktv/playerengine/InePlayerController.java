package com.ine.ktv.playerengine;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;

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
import com.google.android.exoplayer2.util.Util;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;


public class InePlayerController implements SimpleExoPlayer.Listener, MediaSourceEventListener, IneStereoVolumeProcessor.Listener {
    private boolean inPublicVideo = false;

    static public class InePlayerControllerConfigure {
        public Context context;
        public SurfaceView surfaceView;
        public int maxCacheCount = 4;
        public int itemCacheSize = 1024 * 1024 * 4;
        public int[] cacheBandwidthKBS = new int[]{512, 256, 128, 64};
        public int playingBufferSize = 1024 * 1024 * 32;
        public EventListen listener;

        public InePlayerControllerConfigure clone() {
            InePlayerControllerConfigure newConfig = new InePlayerControllerConfigure();
            newConfig.context = this.context;
            newConfig.surfaceView = this.surfaceView;
            newConfig.maxCacheCount = this.maxCacheCount;
            newConfig.itemCacheSize = this.itemCacheSize;
            newConfig.cacheBandwidthKBS = new int[this.cacheBandwidthKBS.length];
            newConfig.playingBufferSize = this.playingBufferSize;
            System.arraycopy(this.cacheBandwidthKBS, 0, newConfig.cacheBandwidthKBS, 0, this.cacheBandwidthKBS.length);
            newConfig.listener = this.listener;
            return newConfig;
        }
    }

    ;
    private SimpleExoPlayer player;
    private ArrayList<IneMediaSource> publicVideos;
    private ArrayList<IneMediaSource> orderSongs;
    private IneDataSource.Factory dataSourceFactory;
    private DefaultTrackSelector trackSelector;

    private int CacheCount = 0;

    private int OrderSongClearMediaItems = 0;
    private int PublicVideoClearMediaItems = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long lastPosition = 0;
    private int IdleCount = 0;
    private int audioControlOutputMode = IneStereoVolumeProcessor.AudioControlOutput_RightMono;
    private boolean Paused = false;
    private IneStereoVolumeProcessor stereoVolumeProcessor;
    private int CurrentPublicVideoWindow = 0;
    private long CurrentPublicVideoPosition = 0;

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
        stereoVolumeProcessor = new IneStereoVolumeProcessor(this);
        trackSelector = new DefaultTrackSelector(this.config.context);
        IneRenderersFactory factory = new IneRenderersFactory(this.config.context, stereoVolumeProcessor);

        player = new SimpleExoPlayer.Builder(this.config.context, factory)
                .setTrackSelector(trackSelector)
                .build();
        player.addListener(this);
//        this.config.surfaceView.setPlayer(player);
        player.setVideoSurfaceView(config.surfaceView);
        publicVideos = new ArrayList<>();
        orderSongs = new ArrayList<>();
        dataSourceFactory = new IneDataSource.Factory().setUserAgent(Util.getUserAgent(this.config.context, "InePlayer"));
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
        String targetUrl;
        if (url.startsWith("http"))
            targetUrl = url;
        else
            targetUrl = IneDataSource.VODReplaceSign + url;
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(() -> InsertOrderSong(index, targetUrl, name, playingBufferSize));
            return;
        }

        IneMediaSource mediaSource = new IneMediaSource.Factory(dataSourceFactory, this)
                .setContinueLoadingCheckIntervalBytes(playingBufferSize)
                .createMediaSource(MediaItem.fromUri(url));
        mediaSource.SongName = name;
        orderSongs.add(index, mediaSource);
        if (!inPublicVideo) {
            player.addMediaSource(index, mediaSource);
            player.prepare();
        }
        if (CacheCount < config.maxCacheCount) {
            mediaSource.dataSource.startCacheBuffer(config.itemCacheSize, config.cacheBandwidthKBS[CacheCount]);
            CacheCount++;
        }
        PlayListChange();
    }

    public void AddPubVideo(String url, String name) {
        String targetUrl;
        if (url.startsWith("http"))
            targetUrl = url;
        else
            targetUrl = IneDataSource.VODReplaceSign + url;
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(() -> AddPubVideo(targetUrl, name));
            return;
        }

        IneMediaSource mediaSource = new IneMediaSource.Factory(dataSourceFactory, this)
                .setContinueLoadingCheckIntervalBytes(config.playingBufferSize)
                .createMediaSource(MediaItem.fromUri(targetUrl));
        mediaSource.SongName = name;
        publicVideos.add(mediaSource);
        PlayListChange();
    }

    public void SetPubVideos(String urls[], String names[]) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(() -> SetPubVideos(urls, names));
            return;
        }

        CurrentPublicVideoWindow = 0;
        CurrentPublicVideoPosition = 0;
        for (int i = 0; i < urls.length; i++) {
            if (inPublicVideo)
                player.clearMediaItems();
            String targetUrl;
            if (urls[i].startsWith("http"))
                targetUrl = urls[i];
            else
                targetUrl = IneDataSource.VODReplaceSign + urls[i];
            IneMediaSource mediaSource = new IneMediaSource.Factory(dataSourceFactory, this)
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
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(() -> DeleteOrderSong(index));
            return;
        }
        if (index < orderSongs.size()) {
            IneMediaSource mediaSource = orderSongs.get(index);
            player.removeMediaItem(index);
            if (mediaSource.dataSource.CacheStatus != IneDataSource.CacheStatus_NoCache)
                CacheCount--;
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
        if (!inPublicVideo) {
            setAudioOutput(type);
        }
    }

    public boolean isInPublicVideo() {
        return inPublicVideo;
    }

    public void open() {
        for (int i = 0; i < publicVideos.size(); i++) {
            player.addMediaSource(publicVideos.get(i));
        }
        inPublicVideo = true;
        player.prepare();
        checkOrderSong();
    }

    public void close() {
        handler.removeCallbacks(checkOrderSongLoop);
        player.stop();
        PublicVideoClearMediaItems++;
        player.clearMediaItems();
        orderSongs.clear();
        publicVideos.clear();
    }

    public boolean isPaused() {
        return Paused;
    }

    public void pause() {
        if (!inPublicVideo && !Paused) {
            player.pause();
            Paused = true;
        }
    }

    public void resume() {
        if (!inPublicVideo && Paused) {
            player.play();
            Paused = false;
        }
    }

    public void replay() {
        if (!inPublicVideo) {
            resume();
            player.seekTo(player.getCurrentWindowIndex(), C.TIME_UNSET);
        }
    }

    public void play() {
        player.play();
        if (!Paused)
            PlayListChange();
        else
            Paused = false;
    }

    public void stop() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(() -> stop());
            return;
        }
        Paused = false;
        IdleCount = 0;
        int idx = player.getCurrentWindowIndex();
        if (inPublicVideo) {
            config.listener.onStop(this, publicVideos.get(idx).SongName, true);
            if (player.hasNext()) {
                player.next();
                config.listener.onNext(this, publicVideos.get(idx + 1).SongName, true);
            } else {
                player.seekToDefaultPosition(0);
                config.listener.onNext(this, publicVideos.get(0).SongName, true);
            }
        } else {
            config.listener.onStop(this, orderSongs.get(0).SongName, false);
            if (orderSongs.size() > 1) {
                if (idx == 0)
                    player.next();
                IneMediaSource mediaSource = orderSongs.get(0);
                if (mediaSource.dataSource.CacheStatus != IneDataSource.CacheStatus_NoCache)
                    CacheCount--;
                player.removeMediaItem(0);
                mediaSource.dataSource.freeCacheBuffer();
                orderSongs.remove(mediaSource);
                PlayListChange();
                for (idx = 1; idx < orderSongs.size() && (CacheCount < config.maxCacheCount); idx++) {
                    mediaSource = orderSongs.get(idx);
                    if (mediaSource.dataSource.CacheStatus == IneDataSource.CacheStatus_NoCache) {
                        mediaSource.dataSource.startCacheBuffer(config.itemCacheSize, config.cacheBandwidthKBS[config.maxCacheCount - 1]);
                        CacheCount++;
                    }
                }
                idx = 1;
                for (int i = 1; (i < config.maxCacheCount) && (idx < orderSongs.size()); i++, idx++) {
                    mediaSource = orderSongs.get(idx);
                    mediaSource.dataSource.changeCacheSpeed(config.cacheBandwidthKBS[i]);
                }
                config.listener.onNext(this, orderSongs.get(0).SongName, false);
                if (orderSongs.size() > 1)
                    config.listener.onNextSongDisplay(this, orderSongs.get(1).SongName);
            } else {
                config.listener.onOrderSongFinish(this);
                IneMediaSource mediaSource = orderSongs.get(0);
                player.removeMediaItem(0);
                //if(mediaSource.dataSource.CacheStatus != IneDataSource.CacheStatus_NoCache)
                //    CacheCount--;
                CacheCount = 0; //workaround
                orderSongs.get(0).dataSource.freeCacheBuffer();
                orderSongs.remove(0);
                PublicVideoClearMediaItems++;
                player.clearMediaItems();
                for (int i = 0; i < publicVideos.size(); i++) {
                    player.addMediaSource(publicVideos.get(i));
                }
                inPublicVideo = true;
                PlayListChange();
                setAudioOutput(IneStereoVolumeProcessor.AudioControlOutput_Stereo);
                player.seekTo(CurrentPublicVideoWindow, CurrentPublicVideoPosition);
                config.listener.onNext(this, publicVideos.get(CurrentPublicVideoWindow).SongName, true);
            }

        }

    }

    public void cut() {
        if (!inPublicVideo) {
            resume();
            stop();
        }
    }

    private Runnable checkOrderSongLoop = () -> checkOrderSong();

    private void checkOrderSong() {
        if (inPublicVideo && orderSongs.size() != 0) {
            IneMediaSource mediaSource = orderSongs.get(0);
            if (mediaSource.dataSource.CacheStatus == IneDataSource.CacheStatus_Cached || mediaSource.dataSource.CacheStatus == IneDataSource.CacheStatus_CacheError) {
                CurrentPublicVideoWindow = player.getCurrentWindowIndex();
                CurrentPublicVideoPosition = player.getCurrentPosition();
                if (publicVideos.size() > 0)
                    config.listener.onStop(this, publicVideos.get(CurrentPublicVideoWindow).SongName, true);
                inPublicVideo = false;
                OrderSongClearMediaItems++;
                player.clearMediaItems();  // will gen end event
                for (int i = 0; i < orderSongs.size(); i++) {
                    player.addMediaSource(orderSongs.get(i));
                }
                player.seekToDefaultPosition(0);
                setAudioOutput(audioControlOutputMode);
                config.listener.onNext(this, orderSongs.get(0).SongName, false);
                if (orderSongs.size() > 1)
                    config.listener.onNextSongDisplay(this, orderSongs.get(1).SongName);
            }
        }

        long position = player.getCurrentPosition();

        if (!Paused && lastPosition == position)
            IdleCount++;
        else
            IdleCount = 0;
        if (IdleCount >= 150) {
            IdleCount = 0;
            stop();
        }
        lastPosition = position;
        handler.postDelayed(checkOrderSongLoop, 100);
    }

    public void setAudioTrack(int track) {
        System.out.println("setAudioTrack: " + track);
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        DefaultTrackSelector.Parameters parameters = trackSelector.getParameters();
        DefaultTrackSelector.ParametersBuilder builder = parameters.buildUpon();
        for (int rendererIndex = 0; rendererIndex < mappedTrackInfo.getRendererCount(); rendererIndex++) {
            int trackType = mappedTrackInfo.getRendererType(rendererIndex);
            if (trackType == C.TRACK_TYPE_AUDIO) {
                builder.clearSelectionOverrides(rendererIndex).setRendererDisabled(rendererIndex, false);
                int groupIndex = track - 1;
                int[] tracks = {0};
                DefaultTrackSelector.SelectionOverride override = new DefaultTrackSelector.SelectionOverride(groupIndex, tracks);
                builder.setSelectionOverride(rendererIndex, mappedTrackInfo.getTrackGroups(rendererIndex), override);
            }
        }
        trackSelector.setParameters(builder);
    }

    protected void setAudioOutput(int type) {
        int AudioTracks = 0;
        for (int i = 0; i < player.getCurrentTrackGroups().length; i++) {
            String format = player.getCurrentTrackGroups().get(i).getFormat(0).sampleMimeType;
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

    protected void PlayListChange() {
        config.listener.onPlayListChange(this);
    }

    protected void onReady() {
        play();
    }

    protected void onEnd() {
        Log.d("onEnd", "End");
        //stop();
    }

    protected void onLoadError() {
        if (inPublicVideo)
            config.listener.onLoadingError(this, publicVideos.get(player.getCurrentWindowIndex()).SongName);
        else
            config.listener.onLoadingError(this, orderSongs.get(0).SongName);
        stop();
    }

    protected void onPlayMessage(String message) {
        if (inPublicVideo)
            config.listener.onPlayingError(this, publicVideos.get(player.getCurrentWindowIndex()).SongName, message);
        else
            config.listener.onPlayingError(this, orderSongs.get(0).SongName, message);
    }

    protected void onPlayError() {
        onPlayMessage("無法撥放");
        stop();
    }

    // SimpleExoPlayer.Listener
    @Override
    public void onPlaybackStateChanged(int state) {
        if (state == SimpleExoPlayer.STATE_READY)
            onReady();
        if (state == SimpleExoPlayer.STATE_ENDED) {
            if (OrderSongClearMediaItems > 0) {
                OrderSongClearMediaItems--;
                return;
            }
            if (PublicVideoClearMediaItems > 0) {
                PublicVideoClearMediaItems--;
                return;
            }
            onEnd();
        }

    }

    @Override
    public void onMediaItemTransition(@Nullable MediaItem mediaItem, @Player.MediaItemTransitionReason int reason) {
        if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO && !inPublicVideo)
            stop();
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        int state = player.getPlaybackState();
        if (state == Player.STATE_IDLE || state == Player.STATE_BUFFERING) {
            if (Looper.myLooper() == Looper.getMainLooper())
                onLoadError();
            else
                handler.post(() -> onLoadError());
        } else {
            if (Looper.myLooper() == Looper.getMainLooper())
                onPlayError();
            else
                handler.post(() -> onPlayError());
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
                int state = player.getPlaybackState();
                if (state == Player.STATE_IDLE || state == Player.STATE_BUFFERING) {
                    if (Looper.myLooper() == Looper.getMainLooper())
                        onLoadError();
                    else
                        handler.post(() -> onLoadError());
                } else {
                    if (Looper.myLooper() == Looper.getMainLooper())
                        onPlayError();
                    else
                        handler.post(() -> onPlayError());
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
            onPlayMessage(localMessage);
        else
            handler.post(() -> onPlayMessage(localMessage));

    }
}
