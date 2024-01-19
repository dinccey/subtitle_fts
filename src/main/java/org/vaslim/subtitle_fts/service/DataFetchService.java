package org.vaslim.subtitle_fts.service;

import org.vaslim.subtitle_fts.model.CategoryInfo;
import org.vaslim.subtitle_fts.model.Subtitle;

import java.util.Set;

public interface DataFetchService {
    Set<Subtitle> getNextSubtitleData();

    Set<CategoryInfo> getNextCategoryInfoData();
}
