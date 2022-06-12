package com.google.android.exoplayer2.source;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManagerProvider;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManagerProvider;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultLoadErrorHandlingPolicy;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.LoadErrorHandlingPolicy;
import com.google.android.exoplayer2.upstream.TransferListener;

import static com.google.android.exoplayer2.util.Assertions.checkNotNull;

public final class IneMediaSource extends BaseMediaSource
        implements ProgressiveMediaPeriod.Listener {

    /** Factory for {@link IneMediaSource}s. */
    public static final class Factory implements MediaSourceFactory {

        private final IneDataSource.Factory dataSourceFactory;

        private ProgressiveMediaExtractor.Factory progressiveMediaExtractorFactory;
        private boolean usingCustomDrmSessionManagerProvider;
        private DrmSessionManagerProvider drmSessionManagerProvider;
        private LoadErrorHandlingPolicy loadErrorHandlingPolicy;
        private int continueLoadingCheckIntervalBytes;
        private MediaSourceEventListener listener = null;
        @Nullable
        private String customCacheKey;
        @Nullable private Object tag;

        /**
         * Creates a new factory for {@link IneMediaSource}s, using the extractors provided by
         * {@link DefaultExtractorsFactory}.
         *
         * @param dataSourceFactory A factory for {@link DataSource}s to read the media.
         */
        public Factory(IneDataSource.Factory dataSourceFactory) {
            this(dataSourceFactory, new DefaultExtractorsFactory(),null);
        }
        /**
         * Creates a new factory for {@link IneMediaSource}s, using the extractors provided by
         * {@link DefaultExtractorsFactory}.
         *
         * @param dataSourceFactory A factory for {@link DataSource}s to read the media.
         */
        public Factory(IneDataSource.Factory dataSourceFactory, MediaSourceEventListener listener) {
            this(dataSourceFactory, new DefaultExtractorsFactory(), listener);
        }

        /**
         * Equivalent to {@link #Factory(IneDataSource.Factory, ExtractorsFactory) new
         * Factory(dataSourceFactory, () -> new BundledExtractorsAdapter(extractorsFactory)}.
         */
        public Factory(IneDataSource.Factory dataSourceFactory, ExtractorsFactory extractorsFactory) {
            this(dataSourceFactory, () -> new BundledExtractorsAdapter(extractorsFactory), null);
        }
        public Factory(IneDataSource.Factory dataSourceFactory, ExtractorsFactory extractorsFactory, MediaSourceEventListener listener) {
            this(dataSourceFactory, () -> new BundledExtractorsAdapter(extractorsFactory), listener);
        }

        /**
         * Creates a new factory for {@link IneMediaSource}s.
         *
         * @param dataSourceFactory A factory for {@link IneDataSource}s to read the media.
         * @param progressiveMediaExtractorFactory A factory for the {@link ProgressiveMediaExtractor}
         *     to extract media from its container.
         */
        public Factory(
                IneDataSource.Factory dataSourceFactory,
                ProgressiveMediaExtractor.Factory progressiveMediaExtractorFactory,
                MediaSourceEventListener listener) {
            this.dataSourceFactory = dataSourceFactory;
            this.progressiveMediaExtractorFactory = progressiveMediaExtractorFactory;
            this.listener = listener;
            drmSessionManagerProvider = new DefaultDrmSessionManagerProvider();
            loadErrorHandlingPolicy = new DefaultLoadErrorHandlingPolicy();
            continueLoadingCheckIntervalBytes = DEFAULT_LOADING_CHECK_INTERVAL_BYTES;
        }

        /**
         * @deprecated Pass the {@link ExtractorsFactory} via {@link #Factory(IneDataSource.Factory,
         *     ExtractorsFactory)}. This is necessary so that proguard can treat the default extractors
         *     factory as unused.
         */
        @Deprecated
        public Factory setExtractorsFactory(@Nullable ExtractorsFactory extractorsFactory) {
            this.progressiveMediaExtractorFactory =
                    () ->
                            new BundledExtractorsAdapter(
                                    extractorsFactory != null ? extractorsFactory : new DefaultExtractorsFactory());
            return this;
        }

        /**
         * @deprecated Use {@link MediaItem.Builder#setCustomCacheKey(String)} and {@link
         *     #createMediaSource(MediaItem)} instead.
         */
        @Deprecated
        public Factory setCustomCacheKey(@Nullable String customCacheKey) {
            this.customCacheKey = customCacheKey;
            return this;
        }

        /**
         * @deprecated Use {@link MediaItem.Builder#setTag(Object)} and {@link
         *     #createMediaSource(MediaItem)} instead.
         */
        @Deprecated
        public Factory setTag(@Nullable Object tag) {
            this.tag = tag;
            return this;
        }

        /**
         * Sets the {@link LoadErrorHandlingPolicy}. The default value is created by calling {@link
         * DefaultLoadErrorHandlingPolicy#DefaultLoadErrorHandlingPolicy()}.
         *
         * @param loadErrorHandlingPolicy A {@link LoadErrorHandlingPolicy}.
         * @return This factory, for convenience.
         */
        public Factory setLoadErrorHandlingPolicy(
                @Nullable LoadErrorHandlingPolicy loadErrorHandlingPolicy) {
            this.loadErrorHandlingPolicy =
                    loadErrorHandlingPolicy != null
                            ? loadErrorHandlingPolicy
                            : new DefaultLoadErrorHandlingPolicy();
            return this;
        }

        /**
         * Sets the number of bytes that should be loaded between each invocation of {@link
         * MediaPeriod.Callback#onContinueLoadingRequested(SequenceableLoader)}. The default value is
         * {@link #DEFAULT_LOADING_CHECK_INTERVAL_BYTES}.
         *
         * @param continueLoadingCheckIntervalBytes The number of bytes that should be loaded between
         *     each invocation of {@link
         *     MediaPeriod.Callback#onContinueLoadingRequested(SequenceableLoader)}.
         * @return This factory, for convenience.
         */
        public Factory setContinueLoadingCheckIntervalBytes(int continueLoadingCheckIntervalBytes) {
            this.continueLoadingCheckIntervalBytes = continueLoadingCheckIntervalBytes;
            return this;
        }

        @Override
        public Factory setDrmSessionManagerProvider(
                @Nullable DrmSessionManagerProvider drmSessionManagerProvider) {
            if (drmSessionManagerProvider != null) {
                this.drmSessionManagerProvider = drmSessionManagerProvider;
                this.usingCustomDrmSessionManagerProvider = true;
            } else {
                this.drmSessionManagerProvider = new DefaultDrmSessionManagerProvider();
                this.usingCustomDrmSessionManagerProvider = false;
            }
            return this;
        }

        public Factory setDrmSessionManager(@Nullable DrmSessionManager drmSessionManager) {
            if (drmSessionManager == null) {
                setDrmSessionManagerProvider(null);
            } else {
                setDrmSessionManagerProvider(unusedMediaItem -> drmSessionManager);
            }
            return this;
        }

        @Override
        public Factory setDrmHttpDataSourceFactory(
                @Nullable HttpDataSource.Factory drmHttpDataSourceFactory) {
            if (!usingCustomDrmSessionManagerProvider) {
                ((DefaultDrmSessionManagerProvider) drmSessionManagerProvider)
                        .setDrmHttpDataSourceFactory(drmHttpDataSourceFactory);
            }
            return this;
        }

        @Override
        public Factory setDrmUserAgent(@Nullable String userAgent) {
            if (!usingCustomDrmSessionManagerProvider) {
                ((DefaultDrmSessionManagerProvider) drmSessionManagerProvider).setDrmUserAgent(userAgent);
            }
            return this;
        }

        /** @deprecated Use {@link #createMediaSource(MediaItem)} instead. */
        @SuppressWarnings("deprecation")
        @Deprecated
        @Override
        public IneMediaSource createMediaSource(Uri uri) {
            return createMediaSource(new MediaItem.Builder().setUri(uri).build());
        }

        /**
         * Returns a new {@link IneMediaSource} using the current parameters.
         *
         * @param mediaItem The {@link MediaItem}.
         * @return The new {@link IneMediaSource}.
         * @throws NullPointerException if {@link MediaItem#playbackProperties} is {@code null}.
         */
        @Override
        public IneMediaSource createMediaSource(MediaItem mediaItem) {
            checkNotNull(mediaItem.playbackProperties);
            boolean needsTag = mediaItem.playbackProperties.tag == null && tag != null;
            boolean needsCustomCacheKey =
                    mediaItem.playbackProperties.customCacheKey == null && customCacheKey != null;
            if (needsTag && needsCustomCacheKey) {
                mediaItem = mediaItem.buildUpon().setTag(tag).setCustomCacheKey(customCacheKey).build();
            } else if (needsTag) {
                mediaItem = mediaItem.buildUpon().setTag(tag).build();
            } else if (needsCustomCacheKey) {
                mediaItem = mediaItem.buildUpon().setCustomCacheKey(customCacheKey).build();
            }
            return new IneMediaSource(
                    mediaItem,
                    dataSourceFactory,
                    progressiveMediaExtractorFactory,
                    drmSessionManagerProvider.get(mediaItem),
                    loadErrorHandlingPolicy,
                    continueLoadingCheckIntervalBytes,
                    listener);
        }

        @Override
        public int[] getSupportedTypes() {
            return new int[] {C.TYPE_OTHER};
        }
    }

    /**
     * The default number of bytes that should be loaded between each each invocation of {@link
     * MediaPeriod.Callback#onContinueLoadingRequested(SequenceableLoader)}.
     */
    public static final int DEFAULT_LOADING_CHECK_INTERVAL_BYTES = 1024 * 1024;

    private final MediaItem mediaItem;
    private final MediaItem.PlaybackProperties playbackProperties;
    private final IneDataSource.Factory dataSourceFactory;
    private final ProgressiveMediaExtractor.Factory progressiveMediaExtractorFactory;
    private final DrmSessionManager drmSessionManager;
    private final LoadErrorHandlingPolicy loadableLoadErrorHandlingPolicy;
    private final int continueLoadingCheckIntervalBytes;

    private boolean timelineIsPlaceholder;
    private long timelineDurationUs;
    private boolean timelineIsSeekable;
    private boolean timelineIsLive;
    private static final Handler handler = new Handler(Looper.getMainLooper());
    public String SongName;
    public IneDataSource dataSource;
    public ProgressiveMediaPeriod progressiveMediaPeriod = null;
    MediaSourceEventListener.EventDispatcher mediaSourceEventDispatcher = null;
    MediaSourceEventListener listener = null;
    @Nullable private TransferListener transferListener;

    private IneMediaSource(
            MediaItem mediaItem,
            IneDataSource.Factory dataSourceFactory,
            ProgressiveMediaExtractor.Factory progressiveMediaExtractorFactory,
            DrmSessionManager drmSessionManager,
            LoadErrorHandlingPolicy loadableLoadErrorHandlingPolicy,
            int continueLoadingCheckIntervalBytes,
            MediaSourceEventListener listener ) {
        this.playbackProperties = checkNotNull(mediaItem.playbackProperties);
        this.mediaItem = mediaItem;
        this.dataSourceFactory = dataSourceFactory;
        this.progressiveMediaExtractorFactory = progressiveMediaExtractorFactory;
        this.drmSessionManager = drmSessionManager;
        this.loadableLoadErrorHandlingPolicy = loadableLoadErrorHandlingPolicy;
        this.continueLoadingCheckIntervalBytes = continueLoadingCheckIntervalBytes;
        this.timelineIsPlaceholder = true;
        this.timelineDurationUs = C.TIME_UNSET;
        this.listener = listener;
        dataSource = dataSourceFactory.createDataSource();
        dataSource.uri = mediaItem.playbackProperties.uri;
    }

    /**
     * @deprecated Use {@link #getMediaItem()} and {@link MediaItem.PlaybackProperties#tag} instead.
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    @Nullable
    public Object getTag() {
        return playbackProperties.tag;
    }

    @Override
    public MediaItem getMediaItem() {
        return mediaItem;
    }

    @Override
    protected void prepareSourceInternal(@Nullable TransferListener mediaTransferListener) {
        transferListener = mediaTransferListener;
        drmSessionManager.prepare();
        notifySourceInfoRefreshed();
    }
    @Override
    public void maybeThrowSourceInfoRefreshError() {
        // Do nothing.
    }

    @Override
    public MediaPeriod createPeriod(MediaPeriodId id, Allocator allocator, long startPositionUs) {

        if (transferListener != null) {
            dataSource.addTransferListener(transferListener);
        }
        mediaSourceEventDispatcher = createEventDispatcher(id);
        if(listener != null)
            mediaSourceEventDispatcher.addEventListener(handler, listener);
        progressiveMediaPeriod = new ProgressiveMediaPeriod(
                playbackProperties.uri,
                dataSource,
                progressiveMediaExtractorFactory.createProgressiveMediaExtractor(),
                drmSessionManager,
                createDrmEventDispatcher(id),
                loadableLoadErrorHandlingPolicy,
                mediaSourceEventDispatcher,
                this,
                allocator,
                playbackProperties.customCacheKey,
                continueLoadingCheckIntervalBytes);
        return progressiveMediaPeriod;
    }

    @Override
    public void releasePeriod(MediaPeriod mediaPeriod) {
        if(mediaSourceEventDispatcher!=null && listener != null)
            mediaSourceEventDispatcher.removeEventListener(listener);
        ((ProgressiveMediaPeriod) mediaPeriod).release();
    }

    @Override
    protected void releaseSourceInternal() {
        drmSessionManager.release();
    }

    //
    // ProgressiveMediaPeriod.Listener implementation.
    //

    @Override
    public void onSourceInfoRefreshed(long durationUs, boolean isSeekable, boolean isLive) {
        // If we already have the duration from a previous source info refresh, use it.
        durationUs = durationUs == C.TIME_UNSET ? timelineDurationUs : durationUs;
        if (!timelineIsPlaceholder
                && timelineDurationUs == durationUs
                && timelineIsSeekable == isSeekable
                && timelineIsLive == isLive) {
            // Suppress no-op source info changes.
            return;
        }
        timelineDurationUs = durationUs;
        timelineIsSeekable = isSeekable;
        timelineIsLive = isLive;
        timelineIsPlaceholder = false;
        notifySourceInfoRefreshed();
    }
    //
    //MediaSourceEventListener
    //

    // Internal methods.

    private void notifySourceInfoRefreshed() {
        // TODO: Split up isDynamic into multiple fields to indicate which values may change. Then
        // indicate that the duration may change until it's known. See [internal: b/69703223].
        Timeline timeline =
                new SinglePeriodTimeline(
                        timelineDurationUs,
                        timelineIsSeekable,
                        /* isDynamic= */ false,
                        /* useLiveConfiguration= */ timelineIsLive,
                        /* manifest= */ null,
                        mediaItem);
        if (timelineIsPlaceholder) {
            // TODO: Actually prepare the extractors during preparation so that we don't need a
            // placeholder. See https://github.com/google/ExoPlayer/issues/4727.
            timeline =
                    new ForwardingTimeline(timeline) {
                        @Override
                        public Window getWindow(
                                int windowIndex, Window window, long defaultPositionProjectionUs) {
                            super.getWindow(windowIndex, window, defaultPositionProjectionUs);
                            window.isPlaceholder = true;
                            return window;
                        }

                        @Override
                        public Period getPeriod(int periodIndex, Period period, boolean setIds) {
                            super.getPeriod(periodIndex, period, setIds);
                            period.isPlaceholder = true;
                            return period;
                        }
                    };
        }
        refreshSourceInfo(timeline);
    }
}
