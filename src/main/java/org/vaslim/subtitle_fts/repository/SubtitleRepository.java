package org.vaslim.subtitle_fts.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.vaslim.subtitle_fts.model.Subtitle;

import java.util.List;

public interface SubtitleRepository extends ElasticsearchRepository<Subtitle, String> {
    List<Subtitle> findByVideoNameOrText(String query);
}
