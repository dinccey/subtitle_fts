package org.vaslim.subtitle_fts.repository;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.vaslim.subtitle_fts.model.MediaRecord;
import org.vaslim.subtitle_fts.model.Subtitle;

import java.util.List;

public interface SubtitleRepository extends ElasticsearchRepository<MediaRecord, String> {

//    @Query("{\"bool\": {\"should\": [{\"match\": {\"text\": {\"query\": \"?0\", \"fuzziness\": 1, \"boost\": 2, " +
//            "\"operator\": \"and\"}}}, {\"match_phrase\": {\"text\": {\"query\": \"?0\", \"boost\": 3}}}, " +
//            "{\"match\": {\"videoName\": {\"query\": \"?0\", \"fuzziness\": 1, \"boost\": 2, \"operator\": \"and\"}}}, " +
//            "{\"match_phrase\": {\"videoName\": {\"query\": \"?0\", \"boost\": 3}}}]}}")
//    List<MediaRecord> findByTextOrVideoName(String query);

    @Query("{\"nested\": {\"path\": \"subtitles\", \"query\": {\"match\": {\"subtitles.text\": \"?0\"}}, \"score_mode\": \"max\", \"inner_hits\": {}}}")
    List<MediaRecord> findBySubtitlesText(String searchText);

    @Query("{\"bool\": {\"should\": [ {\"match\": {\"categoryData\":\"?0\"}}, {\"nested\": {\"path\":\"subtitles\",\"query\": {\"bool\": {\"must\": [ {\"match\": {\"subtitles.text\":\"?1\"}}]}}, \"inner_hits\": {}}]}}")
    List<MediaRecord> findByCategoryDataAndSubtitleText(String categoryData, String subtitleText);

    List<MediaRecord> findByCategoryData(String categoryData);
}

