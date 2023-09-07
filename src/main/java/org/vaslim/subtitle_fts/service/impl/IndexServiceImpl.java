package org.vaslim.subtitle_fts.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.transport.ElasticsearchTransport;
import org.springframework.stereotype.Service;
import org.vaslim.subtitle_fts.model.Subtitle;
import org.vaslim.subtitle_fts.service.DataFetchService;
import org.vaslim.subtitle_fts.service.IndexService;

import java.io.IOException;
import java.util.List;

@Service
public class IndexServiceImpl implements IndexService {

    private final ElasticsearchClient elasticsearchClient;

    private final ElasticsearchTransport elasticsearchTransport;

    private final DataFetchService dataFetchService;

    public IndexServiceImpl(ElasticsearchClient elasticsearchClient, ElasticsearchTransport elasticsearchTransport, DataFetchService dataFetchService) {
        this.elasticsearchClient = elasticsearchClient;
        this.elasticsearchTransport = elasticsearchTransport;
        this.dataFetchService = dataFetchService;
    }

    @Override
    public void runIndexing() {
        List<Subtitle> subtitles;

        while (!(subtitles = dataFetchService.getNextSubtitleData()).isEmpty()) {
            subtitles.forEach(subtitle -> {
                try {
                    IndexResponse response = elasticsearchClient.index(i -> i
                            .index("video")
                            .id(subtitle.getVideoName())
                            .document(subtitle));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
