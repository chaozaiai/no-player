package com.novoda.noplayer.exoplayer.mediasource;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.novoda.noplayer.PlayerSubtitleTrack;
import com.novoda.noplayer.exoplayer.RendererTypeRequester;
import com.novoda.utils.NoPlayerLog;

import java.util.ArrayList;
import java.util.List;

import static com.novoda.noplayer.exoplayer.mediasource.TrackType.TEXT;

public class ExoPlayerSubtitleTrackSelector {

    private final ExoPlayerTrackSelector trackSelector;
    private final TrackSelection.Factory trackSelectionFactory;

    public ExoPlayerSubtitleTrackSelector(ExoPlayerTrackSelector trackSelector, TrackSelection.Factory trackSelectionFactory) {
        this.trackSelector = trackSelector;
        this.trackSelectionFactory = trackSelectionFactory;
    }

    public void selectTextTrack(PlayerSubtitleTrack subtitleTrack, RendererTypeRequester rendererTypeRequester) {
        TrackGroupArray trackGroups = trackSelector.trackGroups(TEXT, rendererTypeRequester);

        MappingTrackSelector.SelectionOverride selectionOverride = new MappingTrackSelector.SelectionOverride(
                trackSelectionFactory,
                subtitleTrack.groupIndex(),
                subtitleTrack.formatIndex()
        );
        trackSelector.setSelectionOverride(TEXT, rendererTypeRequester, trackGroups, selectionOverride);
    }

    public List<PlayerSubtitleTrack> getSubtitleTracks(RendererTypeRequester rendererTypeRequester) {
        TrackGroupArray trackGroups = trackSelector.trackGroups(TEXT, rendererTypeRequester);

        List<PlayerSubtitleTrack> subtitleTracks = new ArrayList<>();

        for (int groupIndex = 0; groupIndex < trackGroups.length; groupIndex++) {
            TrackGroup trackGroup = trackGroups.get(groupIndex);

            for (int formatIndex = 0; formatIndex < trackGroup.length; formatIndex++) {
                Format format = trackGroup.getFormat(formatIndex);
                PlayerSubtitleTrack playerSubtitleTrack = new PlayerSubtitleTrack(
                        groupIndex,
                        formatIndex,
                        format.id,
                        format.language,
                        format.sampleMimeType,
                        format.channelCount,
                        format.bitrate
                );
                subtitleTracks.add(playerSubtitleTrack);
            }
        }

        return subtitleTracks;
    }

    public void clearSubtitleTrack(RendererTypeRequester rendererTypeRequester) {
        trackSelector.clearSelectionOverrideFor(TEXT, rendererTypeRequester);
    }

    public void selectFirstTextTrack(RendererTypeRequester rendererTypeRequester) {
        List<PlayerSubtitleTrack> subtitleTracks = getSubtitleTracks(rendererTypeRequester);
        if (subtitleTracks.isEmpty()) {
            NoPlayerLog.e("No subtitles tracks available");
        } else {
            selectTextTrack(subtitleTracks.get(0), rendererTypeRequester);
        }
    }
}
