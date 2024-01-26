package org.vaslim.subtitle_fts.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.ElasticsearchTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;
import org.vaslim.subtitle_fts.constants.Constants;
import org.vaslim.subtitle_fts.elastic.CategoryInfoRepository;
import org.vaslim.subtitle_fts.elastic.SubtitleRepository;
import org.vaslim.subtitle_fts.model.CategoryInfo;
import org.vaslim.subtitle_fts.model.Subtitle;
import org.vaslim.subtitle_fts.service.DataFetchService;
import org.vaslim.subtitle_fts.service.FileService;
import org.vaslim.subtitle_fts.service.IndexService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class IndexServiceImpl implements IndexService {

    private static final Logger logger = LoggerFactory.getLogger(IndexServiceImpl.class);
    private final ElasticsearchClient elasticsearchClient;

    private final ElasticsearchOperations elasticsearchOperations;

    private final ElasticsearchTransport elasticsearchTransport;

    private final FileService fileService;

    private final SubtitleRepository subtitleRepository;

    private final CategoryInfoRepository categoryInfoRepository;
    private final DataFetchService dataFetchService;

    public IndexServiceImpl(ElasticsearchClient elasticsearchClient, ElasticsearchOperations elasticsearchOperations, ElasticsearchTransport elasticsearchTransport, FileService fileService, SubtitleRepository subtitleRepository, CategoryInfoRepository categoryInfoRepository, DataFetchService dataFetchService) {
        this.elasticsearchClient = elasticsearchClient;
        this.elasticsearchOperations = elasticsearchOperations;
        this.elasticsearchTransport = elasticsearchTransport;
        this.fileService = fileService;
        this.subtitleRepository = subtitleRepository;
        this.categoryInfoRepository = categoryInfoRepository;
        this.dataFetchService = dataFetchService;
    }

    @Override
    public void runIndexing() {
        Set<Subtitle> subtitles;
        Set<CategoryInfo> categoryInfos;
        logger.info("Starting indexing...");
        //createIndexIfNotExists(Constants.INDEX_SUBTITLES, getMappingsSubtitle());
        //createIndexIfNotExists(Constants.INDEX_CATEGORY_INFO, getMappingsCategoryInfo());
        try {
            long startTime = System.currentTimeMillis();
            while (!(subtitles = dataFetchService.getNextSubtitleData()).isEmpty()) {
                //logger.info("Indexing batch size " + subtitles.size());
                subtitleRepository.saveAll(subtitles);
            }
            long endTime = System.currentTimeMillis();
            logger.info("Subtitle indexing time seconds: " + (endTime - startTime) / 1000);
            fileService.reset(); //reset iterator
            startTime = System.currentTimeMillis();
            while (!(categoryInfos = dataFetchService.getNextCategoryInfoData()).isEmpty()) {
                //logger.info("Indexing categoryInfo batch size: " + categoryInfos.size());
                categoryInfoRepository.saveAll(categoryInfos);
            }
            endTime = System.currentTimeMillis();
            logger.info("CategoryInfo indexing time seconds: " + (endTime - startTime) / 1000);
        } finally {
            fileService.reset(); //reset iterator
        }

    }

    private void createIndexIfNotExists(String indexName, Map<String,Object> mappings) {
        Map<String, Object> settings = new HashMap<>();
        //settings.put("index.max_result_window", 10);

        if(!elasticsearchOperations.indexOps(IndexCoordinates.of(indexName)).exists()){
            Document settingsDocument = Document.create();
            settingsDocument.append("settings", settings);

            Document mappingsDocument = Document.create();
            mappingsDocument.append("properties", mappings);

            settingsDocument.append("mappings", mappingsDocument);

            elasticsearchOperations.indexOps(IndexCoordinates.of(indexName)).create(settingsDocument);
        }
    }

    private Map<String, Object> getMappingsCategoryInfo() {
        Map<String, Object> mappings = new HashMap<>();

        Map<String, Object> categoryInfo = new HashMap<>();
        categoryInfo.put("type", "keyword");
        mappings.put("categoryInfo", categoryInfo);

        Map<String, Object> subtitlePath = new HashMap<>();
        subtitlePath.put("type", "keyword");
        mappings.put("subtitlePath", subtitlePath);

        return mappings;
    }
    private Map<String, Object> getMappingsSubtitle() {
        Map<String, Object> mappings = new HashMap<>();

        Map<String, Object> categoryInfo = new HashMap<>();
        categoryInfo.put("type", "keyword");
        mappings.put("categoryInfo", categoryInfo);

        Map<String, Object> subtitlePath = new HashMap<>();
        subtitlePath.put("type", "keyword");
        mappings.put("subtitlePath", subtitlePath);

        Map<String, Object> timestamp = new HashMap<>();
        timestamp.put("type", "double");
        mappings.put("timestamp", timestamp);

        Map<String, Object> text = new HashMap<>();
        text.put("type", "text");
        mappings.put("text", text);

        return mappings;
    }


    @Override
    public void deleteIndex() {
        IndexOperations indexOpsSubtitles = elasticsearchOperations.indexOps(IndexCoordinates.of(Constants.INDEX_SUBTITLES));
        if (indexOpsSubtitles.exists()) {
            indexOpsSubtitles.delete();
        }

        IndexOperations indexOpsCategoryInfo = elasticsearchOperations.indexOps(IndexCoordinates.of(Constants.INDEX_CATEGORY_INFO));
        if (indexOpsCategoryInfo.exists()) {
            indexOpsCategoryInfo.delete();
        }
    }

}
