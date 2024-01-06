package org.vaslim.subtitle_fts.service;

import org.springframework.stereotype.Service;
import org.vaslim.subtitle_fts.dto.MediaRecordDTO;
import org.vaslim.subtitle_fts.model.Subtitle;

import java.util.List;
import java.util.Set;

@Service
public interface SubtitleService {
    List<MediaRecordDTO> findVideosByTitleOrSubtitleContentFuzzy(String query);

    List<MediaRecordDTO> findVideosByTitleOrSubtitleContentExact(String query, String categoryInfo);
}
