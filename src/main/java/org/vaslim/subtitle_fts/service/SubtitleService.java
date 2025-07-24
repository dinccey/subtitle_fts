package org.vaslim.subtitle_fts.service;

import org.springframework.stereotype.Service;
import org.vaslim.subtitle_fts.dto.MediaRecordDTO;

import java.util.List;

@Service
public interface SubtitleService {
    List<MediaRecordDTO> findVideosByTitleOrSubtitleContentFuzzy(String query, Integer maxResults);

    List<MediaRecordDTO> findVideosByTitleOrSubtitleContentExact(String query, String categoryInfo, Integer maxResults);
}
