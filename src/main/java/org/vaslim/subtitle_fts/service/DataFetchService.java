package org.vaslim.subtitle_fts.service;

import org.vaslim.subtitle_fts.model.Subtitle;

import java.util.List;

public interface DataFetchService {
    List<Subtitle> getNextSubtitleData();
}
