package com.novoda.noplayer.exoplayer;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import com.novoda.noplayer.ContentType;
import com.novoda.noplayer.Player;
import com.novoda.noplayer.SurfaceHolderRequester;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExoPlayerTwoFacade implements VideoRendererEventListener {

    private int videoWidth;
    private int videoHeight;

    private final SimpleExoPlayer exoPlayer;
    private final DefaultTrackSelector trackSelector;
    private final MediaSourceFactory mediaSourceFactory;
    private SurfaceHolderRequester surfaceHolderRequester;

    private List<Player.CompletionListener> completionListeners = new ArrayList<>();

    public static ExoPlayerTwoFacade newInstance(Context context) {
        DefaultDataSourceFactory defaultDataSourceFactory = new DefaultDataSourceFactory(context, "user-agent");
        Handler handler = new Handler(Looper.getMainLooper());
        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        SimpleExoPlayer exoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector, new DefaultLoadControl());
        MediaSourceFactory mediaSourceFactory = new MediaSourceFactory(defaultDataSourceFactory, handler);
        return new ExoPlayerTwoFacade(exoPlayer, trackSelector, mediaSourceFactory);
    }

    public ExoPlayerTwoFacade(SimpleExoPlayer exoPlayer,
                              DefaultTrackSelector trackSelector,
                              MediaSourceFactory mediaSourceFactory) {
        this.exoPlayer = exoPlayer;
        this.trackSelector = trackSelector;
        this.mediaSourceFactory = mediaSourceFactory;
    }

    public boolean getPlayWhenReady() {
        if (hasPlayer()) {
            return exoPlayer.getPlayWhenReady();
        } else {
            return false;
        }
    }

    public long getPlayheadPosition() {
        if (hasPlayer()) {
            return exoPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    private boolean hasPlayer() {
        return exoPlayer != null;
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public long getMediaDuration() {
        if (hasPlayer()) {
            return exoPlayer.getDuration();
        } else {
            return 0;
        }
    }

    @Override
    public void onVideoEnabled(DecoderCounters counters) {

    }

    @Override
    public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {

    }

    @Override
    public void onVideoInputFormatChanged(Format format) {

    }

    @Override
    public void onDroppedFrames(int count, long elapsedMs) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        videoHeight = height;
        videoWidth = width;
    }

    @Override
    public void onRenderedFirstFrame(Surface surface) {

    }

    @Override
    public void onVideoDisabled(DecoderCounters counters) {

    }

    public int getBufferedPercentage() {
        if (hasPlayer()) {
            return exoPlayer.getBufferedPercentage();
        } else {
            return 0;
        }
    }

    public void setPlayWhenReady(boolean playWhenReady) {
        if (hasPlayer()) {
            if (playWhenReady) {
                pushSurface();
            }
            exoPlayer.setPlayWhenReady(playWhenReady);
        }
    }

    private void pushSurface() {
        if (surfaceHolderRequester == null) {
            Log.w(getClass().getSimpleName(), "Attempt to pushSurface has been ignored because the Player has not been attached to a PlayerView");
            return;
        }

        surfaceHolderRequester.requestSurfaceHolder(new SurfaceHolderRequester.Callback() {
            @Override
            public void onSurfaceHolderReady(SurfaceHolder surfaceHolder) {
                Log.e("!!!", "onSurfaceHolderReady");
                exoPlayer.setVideoSurface(surfaceHolder.getSurface());
            }
        });
    }

    public void seekTo(long positionMillis) {
        if (hasPlayer()) {
            exoPlayer.seekTo(positionMillis);
        }
    }

    public void release() {
        exoPlayer.release();
    }

    public void prepare(Uri uri, ContentType contentType) {
        EventLogger eventLogger = new EventLogger(trackSelector);
        exoPlayer.addListener(eventLogger);
        exoPlayer.setAudioDebugListener(eventLogger);
        exoPlayer.setVideoDebugListener(eventLogger);
        exoPlayer.setMetadataOutput(eventLogger);
        exoPlayer.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == ExoPlayer.STATE_ENDED) {
                    for (Player.CompletionListener completionListener : completionListeners) {
                        completionListener.onCompletion();
                    }
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity() {

            }
        });
        setPlayWhenReady(true);

        MediaSource mediaSource = mediaSourceFactory.create(contentType, uri, eventListener);
        exoPlayer.prepare(mediaSource, true, false);
    }

    private final ExtractorMediaSource.EventListener eventListener = new ExtractorMediaSource.EventListener() {
        @Override
        public void onLoadError(IOException error) {
            Log.e("!!!", "ON LOAD ERROR");
        }
    };

    public void setSurfaceHolderRequester(SurfaceHolderRequester surfaceHolderRequester) {
        this.surfaceHolderRequester = surfaceHolderRequester;
    }

    public void setPlayer(SimpleExoPlayerView simpleExoPlayerView) {
        simpleExoPlayerView.setPlayer(exoPlayer);
    }

    public void addCompletionListener(Player.CompletionListener completionListener) {
        completionListeners.add(completionListener);
    }

}