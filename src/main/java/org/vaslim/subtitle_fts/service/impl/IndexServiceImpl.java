package org.vaslim.subtitle_fts.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.ElasticsearchTransport;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;
import org.vaslim.subtitle_fts.model.Subtitle;
import org.vaslim.subtitle_fts.repository.SubtitleRepository;
import org.vaslim.subtitle_fts.service.DataFetchService;
import org.vaslim.subtitle_fts.service.IndexService;

import java.util.List;

@Service
public class IndexServiceImpl implements IndexService {

    private final ElasticsearchClient elasticsearchClient;

    private final ElasticsearchTransport elasticsearchTransport;

    private final SubtitleRepository subtitleRepository;
    private final DataFetchService dataFetchService;

    public IndexServiceImpl(ElasticsearchClient elasticsearchClient, ElasticsearchTransport elasticsearchTransport, SubtitleRepository subtitleRepository, DataFetchService dataFetchService) {
        this.elasticsearchClient = elasticsearchClient;
        this.elasticsearchTransport = elasticsearchTransport;
        this.subtitleRepository = subtitleRepository;
        this.dataFetchService = dataFetchService;
    }

    @Override
    public void runIndexing() {
        List<Subtitle> subtitles;
        while (!(subtitles = dataFetchService.getNextSubtitleData()).isEmpty()) {
           subtitleRepository.saveAll(subtitles);
        }
    }

    @Override
    public void deleteIndex() {
        ElasticsearchTemplate elasticsearchTemplate = new ElasticsearchTemplate(elasticsearchClient);
        elasticsearchTemplate.indexOps(IndexCoordinates.of("video")).delete();
    }
}
