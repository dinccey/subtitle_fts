package org.vaslim.subtitle_fts.service.impl;

import org.springframework.stereotype.Service;
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
    public List<Subtitle> findVideosByTitleOrSubtitleContentFuzzy(String query) {
        return subtitleRepository.findByTextOrVideoName(query);
    }

    @Override
    public List<Subtitle> findVideosByTitleOrSubtitleContentExact(String query) {
        return subtitleRepository.findByText(query);
    }
}
