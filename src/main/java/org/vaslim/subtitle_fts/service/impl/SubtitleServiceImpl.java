package org.vaslim.subtitle_fts.service.impl;

import org.springframework.stereotype.Service;
import org.vaslim.subtitle_fts.model.MediaRecord;
import org.vaslim.subtitle_fts.model.Subtitle;
import org.vaslim.subtitle_fts.repository.SubtitleRepository;
import org.vaslim.subtitle_fts.service.SubtitleService;

import java.util.List;

@Service
public class SubtitleServiceImpl implements SubtitleService {

    private final SubtitleRepository subtitleRepository;

    public SubtitleServiceImpl(SubtitleRepository subtitleRepository) {
        this.subtitleRepository = subtitleRepository;
    }

    @Override
    public List<MediaRecord> findVideosByTitleOrSubtitleContentFuzzy(String query) {
        return subtitleRepository.findBySubtitlesText(query);
    }

    @Override
    public List<MediaRecord> findVideosByTitleOrSubtitleContentExact(String searchText, String categoryData) {
        return subtitleRepository.findByCategoryDataAndSubtitleText(categoryData, searchText);
        //return subtitleRepository.findByCategoryData(categoryData);
    }
}
