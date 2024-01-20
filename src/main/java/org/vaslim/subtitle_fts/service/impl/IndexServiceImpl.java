package org.vaslim.subtitle_fts.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.ElasticsearchTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;
import org.vaslim.subtitle_fts.constants.Constants;
import org.vaslim.subtitle_fts.elastic.CategoryInfoRepository;
import org.vaslim.subtitle_fts.model.CategoryInfo;
import org.vaslim.subtitle_fts.model.Subtitle;
import org.vaslim.subtitle_fts.elastic.SubtitleRepository;
import org.vaslim.subtitle_fts.service.DataFetchService;
import org.vaslim.subtitle_fts.service.FileService;
import org.vaslim.subtitle_fts.service.IndexService;

import java.util.Set;

@Service
public class IndexServiceImpl implements IndexService {

    private static final Logger logger = LoggerFactory.getLogger(IndexServiceImpl.class);
    private final ElasticsearchClient elasticsearchClient;

    private final ElasticsearchTransport elasticsearchTransport;

    private final FileService fileService;

    private final SubtitleRepository subtitleRepository;

    private final CategoryInfoRepository categoryInfoRepository;
    private final DataFetchService dataFetchService;

    public IndexServiceImpl(ElasticsearchClient elasticsearchClient, ElasticsearchTransport elasticsearchTransport, FileService fileService, SubtitleRepository subtitleRepository, CategoryInfoRepository categoryInfoRepository, DataFetchService dataFetchService) {
        this.elasticsearchClient = elasticsearchClient;
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
        try {
            long startTime = System.currentTimeMillis();
            while (!(subtitles = dataFetchService.getNextSubtitleData()).isEmpty()) {
                logger.info("Indexing batch size " + subtitles.size());
                subtitleRepository.saveAll(subtitles);
            }
            long endTime = System.currentTimeMillis();
            logger.info("Subtitle indexing time seconds: " + (endTime - startTime) / 1000);
            fileService.reset(); //reset iterator
            startTime = System.currentTimeMillis();
            while (!(categoryInfos = dataFetchService.getNextCategoryInfoData()).isEmpty()){
                logger.info("Indexing categoryInfo batch size: " + categoryInfos.size());
                categoryInfoRepository.saveAll(categoryInfos);
            }
            endTime = System.currentTimeMillis();
            logger.info("CategoryInfo indexing time seconds: " + (endTime - startTime) / 1000);
        } finally {
            fileService.reset(); //reset iterator
        }

    }

    @Override
    public void deleteIndex() {
        ElasticsearchTemplate elasticsearchTemplate = new ElasticsearchTemplate(elasticsearchClient);
        elasticsearchTemplate.indexOps(IndexCoordinates.of(Constants.INDEX_SUBTITLES)).delete();
        elasticsearchTemplate.indexOps(IndexCoordinates.of(Constants.INDEX_CATEGORY_INFO)).delete();
    }
}
