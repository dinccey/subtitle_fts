package org.vaslim.subtitle_fts.service;

import org.springframework.stereotype.Service;
import org.vaslim.subtitle_fts.model.MediaRecord;
import org.vaslim.subtitle_fts.model.Subtitle;

import java.util.List;

@Service
public interface SubtitleService {
    List<MediaRecord> findVideosByTitleOrSubtitleContentFuzzy(String query);

    List<MediaRecord> findVideosByTitleOrSubtitleContentExact(String query, String categoryData);
}
