package org.vaslim.subtitle_fts.service;

import org.springframework.stereotype.Service;
import org.vaslim.subtitle_fts.model.Subtitle;

import java.util.List;

@Service
public interface SubtitleService {
    List<Subtitle> findVideosByTitleOrSubtitleContent(String query);
}
