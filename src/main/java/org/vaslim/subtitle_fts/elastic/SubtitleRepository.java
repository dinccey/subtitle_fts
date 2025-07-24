package org.vaslim.subtitle_fts.elastic;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.vaslim.subtitle_fts.model.elastic.Subtitle;

import java.util.List;

public interface SubtitleRepository extends ElasticsearchRepository<Subtitle, String> {

    @Query("{\"bool\": {\"should\": [{\"match\": {\"text\": {\"query\": \"?0\", \"fuzziness\": 1, \"boost\": 2, " +
            "\"operator\": \"and\"}}}, {\"match_phrase\": {\"text\": {\"query\": \"?0\", \"boost\": 3}}}, " +
            "{\"match\": {\"videoName\": {\"query\": \"?0\", \"fuzziness\": 1, \"boost\": 2, \"operator\": \"and\"}}}, " +
            "{\"match_phrase\": {\"videoName\": {\"query\": \"?0\", \"boost\": 3}}}]}}")
    List<Subtitle> findByTextOrVideoName(String query);

    List<Subtitle> findByTextAndCategoryInfo(String query, String categoryInfoQuery, Pageable pageable);
    List<Subtitle> findByText(String query, Pageable pageable);

}

