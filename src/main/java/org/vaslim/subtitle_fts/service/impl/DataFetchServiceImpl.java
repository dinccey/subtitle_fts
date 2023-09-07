package org.vaslim.subtitle_fts.service.impl;

import org.springframework.stereotype.Service;
import org.vaslim.subtitle_fts.model.Subtitle;
import org.vaslim.subtitle_fts.service.DataFetchService;

import java.util.List;

@Service
public class DataFetchServiceImpl implements DataFetchService {
    @Override
    public List<Subtitle> getNextSubtitleData() {
        return null;
    }
}
