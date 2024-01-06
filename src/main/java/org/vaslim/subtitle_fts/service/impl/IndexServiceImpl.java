package org.vaslim.subtitle_fts.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.transport.ElasticsearchTransport;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.aot.AbstractAotProcessor;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;
import org.vaslim.subtitle_fts.model.MediaRecord;
import org.vaslim.subtitle_fts.model.Subtitle;
import org.vaslim.subtitle_fts.repository.SubtitleRepository;
import org.vaslim.subtitle_fts.service.DataFetchService;
import org.vaslim.subtitle_fts.service.FileService;
import org.vaslim.subtitle_fts.service.IndexService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;

@Service
public class IndexServiceImpl implements IndexService {

    private static final Logger logger = LoggerFactory.getLogger(IndexServiceImpl.class);
    private final ElasticsearchClient elasticsearchClient;

    private final ElasticsearchTransport elasticsearchTransport;

    private final FileService fileService;

    private final SubtitleRepository subtitleRepository;
    private final DataFetchService dataFetchService;

    public IndexServiceImpl(ElasticsearchClient elasticsearchClient, ElasticsearchTransport elasticsearchTransport, FileService fileService, SubtitleRepository subtitleRepository, DataFetchService dataFetchService) {
        this.elasticsearchClient = elasticsearchClient;
        this.elasticsearchTransport = elasticsearchTransport;
        this.fileService = fileService;
        this.subtitleRepository = subtitleRepository;
        this.dataFetchService = dataFetchService;
    }

//    @Override
//    public void runIndexing() {
//        Set<MediaRecord> subtitles;
//        try {
//            while (!(subtitles = dataFetchService.getNextSubtitleData()).isEmpty()) {
//                logger.info("Indexing batch size " + subtitles.size());
//                subtitleRepository.saveAll(subtitles);
//            }
//        } finally {
//            fileService.reset(); //reset iterator
//        }
//
//    }

    @Override
    public void runIndexing() {
        try {
            Reader input = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/elastic/index_def.json")), StandardCharsets.UTF_8));
            // Create the index
            CreateIndexRequest request =
                    CreateIndexRequest.of(reqBuilder -> reqBuilder.index("video").withJson(input));
            elasticsearchClient.indices().create(request);

            // Index the documents
            Set<MediaRecord> subtitles;
            while (!(subtitles = dataFetchService.getNextSubtitleData()).isEmpty()) {
                logger.info("Indexing batch size " + subtitles.size());
                subtitleRepository.saveAll(subtitles);
            }
        } catch (Exception e) {
            // Handle exception
            e.printStackTrace();
        } finally {
            fileService.reset(); //reset iterator
        }
    }

    @Override
    public void deleteIndex() {
        ElasticsearchTemplate elasticsearchTemplate = new ElasticsearchTemplate(elasticsearchClient);
        elasticsearchTemplate.indexOps(IndexCoordinates.of("video")).delete();
    }
}
