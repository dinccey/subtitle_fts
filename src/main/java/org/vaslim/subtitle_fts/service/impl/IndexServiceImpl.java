package org.vaslim.subtitle_fts.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.ElasticsearchTransport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.noop.subtitle.model.SubtitleCue;
import fr.noop.subtitle.model.SubtitleParsingException;
import fr.noop.subtitle.vtt.VttObject;
import fr.noop.subtitle.vtt.VttParser;
import jakarta.persistence.EntityManager;
import net.openhft.hashing.LongHashFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vaslim.subtitle_fts.SubtitleFtsApplication;
import org.vaslim.subtitle_fts.constants.Constants;
import org.vaslim.subtitle_fts.database.IndexFileCategoryRepository;
import org.vaslim.subtitle_fts.database.IndexFileRepository;
import org.vaslim.subtitle_fts.database.IndexItemRepository;
import org.vaslim.subtitle_fts.elastic.CategoryInfoRepository;
import org.vaslim.subtitle_fts.elastic.SubtitleRepository;
import org.vaslim.subtitle_fts.model.elastic.CategoryInfo;
import org.vaslim.subtitle_fts.model.elastic.Subtitle;
import org.vaslim.subtitle_fts.model.indexingdb.IndexFile;
import org.vaslim.subtitle_fts.model.indexingdb.IndexFileCategory;
import org.vaslim.subtitle_fts.model.indexingdb.IndexItem;
import org.vaslim.subtitle_fts.service.FileService;
import org.vaslim.subtitle_fts.service.IndexService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class IndexServiceImpl implements IndexService {

    private static final Logger logger = LoggerFactory.getLogger(IndexServiceImpl.class);
    private final ElasticsearchClient elasticsearchClient;

    private final ElasticsearchOperations elasticsearchOperations;

    private final ElasticsearchTransport elasticsearchTransport;

    private final JdbcTemplate jdbcTemplate;

    private final FileService fileService;

    private final SubtitleRepository subtitleRepository;

    private final CategoryInfoRepository categoryInfoRepository;

    private final VttParser vttParser;

    private final IndexFileRepository indexFileRepository;

    private final IndexFileCategoryRepository indexFileCategoryRepository;

    private final IndexItemRepository indexItemRepository;
    private final EntityManager entityManager;

    private static final AtomicInteger counterCategoryInfoSuccess = new AtomicInteger(0);
    private static final AtomicInteger counterCategoryInfoFailed = new AtomicInteger(0);

    private static final AtomicInteger counterSubtitleSuccess = new AtomicInteger(0);
    private static final AtomicInteger counterSubtitleFailed = new AtomicInteger(0);


    @Value("${files.path.root}")
    private String path;

    @Value("${category_info_index.file.extension}")
    private String categoryInfoIndexFileExtension;

    @Value("${subtitle_index.file.extension}")
    private String subtitleIndexFileExtension;


    public IndexServiceImpl(ElasticsearchClient elasticsearchClient, ElasticsearchOperations elasticsearchOperations, ElasticsearchTransport elasticsearchTransport, JdbcTemplate jdbcTemplate, FileService fileService, SubtitleRepository subtitleRepository, CategoryInfoRepository categoryInfoRepository, VttParser vttParser, IndexFileRepository indexFileRepository, IndexFileCategoryRepository indexFileCategoryRepository, IndexItemRepository indexItemRepository, EntityManager entityManager) {
        this.elasticsearchClient = elasticsearchClient;
        this.elasticsearchOperations = elasticsearchOperations;
        this.elasticsearchTransport = elasticsearchTransport;
        this.jdbcTemplate = jdbcTemplate;
        this.fileService = fileService;
        this.subtitleRepository = subtitleRepository;
        this.categoryInfoRepository = categoryInfoRepository;
        this.vttParser = vttParser;
        this.indexFileRepository = indexFileRepository;
        this.indexFileCategoryRepository = indexFileCategoryRepository;
        this.indexItemRepository = indexItemRepository;
        this.entityManager = entityManager;
    }

    @Override
    public void runIndexing() {
        List<File> files;
        Set<CategoryInfo> categoryInfos;
        logger.info("Starting indexing...");
        //createIndexIfNotExists(Constants.INDEX_SUBTITLES, getMappingsSubtitle());
        //createIndexIfNotExists(Constants.INDEX_CATEGORY_INFO, getMappingsCategoryInfo());
        try {
            long startTime = System.currentTimeMillis();

            try {
                indexCategoryInfo();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }

            long endTime = System.currentTimeMillis();
            logger.info("CategoryInfo indexing time seconds {}", (endTime - startTime) / 1000);
            fileService.reset(); //reset iterator
            startTime = System.currentTimeMillis();

            logger.info("Starting subtitle indexing..");
            try {
                indexSubtitles();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e.getMessage());
            }
            endTime = System.currentTimeMillis();
            logger.info("Subtitle indexing time seconds: {}", (endTime - startTime) / 1000);
            logger.info("CategoryInfo success items: {}", counterCategoryInfoSuccess.get());
            logger.info("CategoryInfo failed items: {}", counterCategoryInfoFailed.get());
            logger.info("Subtitle success items: {}", counterSubtitleSuccess.get());
            logger.info("Subtitle failed items: {}", counterSubtitleFailed.get());

            elasticsearchOperations.indexOps(IndexCoordinates.of(Constants.INDEX_SUBTITLES)).refresh();
            elasticsearchOperations.indexOps(IndexCoordinates.of(Constants.INDEX_CATEGORY_INFO)).refresh();
        } finally {
            fileService.reset();

            counterSubtitleSuccess.set(0); //reset iterator
            counterSubtitleFailed.set(0); //reset iterator
            counterCategoryInfoFailed.set(0); //reset iterator
            counterCategoryInfoSuccess.set(0); //reset iterator
        }

    }


    private void indexCategoryInfo() {
        List<File> files;
        while (!(files = fileService.getNext()).isEmpty()) {
            files.forEach(file -> {
                if (file.getAbsolutePath().endsWith(categoryInfoIndexFileExtension)) {
                    IndexFileCategory indexFileCategory;
                    try {
                        indexFileCategory = getIndexFileCategoryUpdated(file);
                    } catch (IOException | NoSuchAlgorithmException e) {
                        logger.error(e.getMessage());
                    }
                }
            });
        }

        indexFileCategoryRepository.flush();

        int pageNumber = 0;
        int pageSize = 50;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<IndexFileCategory> page;
        do {
            page = indexFileCategoryRepository.findIndexFileByProcessedIsFalse(pageable);
            for (IndexFileCategory indexFileCategory : page) {
                String filePath = indexFileCategory.getFilePath();
                String jsonFileName = filePath.replaceFirst("\\.[^.]+$", ".json");
                ObjectMapper mapper = new ObjectMapper();
                try{
                    JsonNode rootNode = mapper.readTree(new File(jsonFileName));
                    CategoryInfo categoryInfo = populateCategoryInfo(rootNode);
                    indexFileCategory.setDocumentId(categoryInfo.getId());
                    categoryInfoRepository.save(categoryInfo);
                    indexFileCategory.setProcessed(true);
                    indexFileCategory.setDocumentId(categoryInfo.getId());
                    indexFileCategoryRepository.save(indexFileCategory);
                    indexFileCategoryRepository.flush();
                    counterCategoryInfoSuccess.incrementAndGet();
                }catch (Exception e){
                    indexFileCategory.setProcessingError(e.getMessage());
                    indexFileCategory.setProcessed(true);
                    indexFileCategoryRepository.save(indexFileCategory);
                    counterCategoryInfoFailed.incrementAndGet();
                    indexFileCategoryRepository.flush();
                }

            }
        } while (page.hasNext());

    }


    private void indexSubtitles() {
        List<File> files;
        while (!(files = fileService.getNext()).isEmpty()) {
            files.forEach(file -> {

                if (file.getAbsolutePath().endsWith(subtitleIndexFileExtension)) {
                    IndexFile indexFile;
                    try {
                        indexFile = getIndexFileUpdated(file);
                        if (indexFile.isFileChanged()) {
                            indexFile.setProcessed(false);
                            subtitleRepository.deleteAll(indexItemsToSubtitles(indexFile.getIndexItems()));
                            indexItemRepository.deleteAll(indexFile.getIndexItems());
                            indexFile.getIndexItems().clear();
                            indexFile.setFileChanged(false);
                            indexFileRepository.save(indexFile);
                        }
                    } catch (IOException | NoSuchAlgorithmException e) {
                        e.printStackTrace();
                        logger.error(e.getMessage());
                        //counterSubtitleFailed.incrementAndGet();
                    }

                }
            });
        }
        indexFileRepository.flush();

        int pageNumber = 0;
        int pageSize = 50;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<IndexFile> page;
        Set<Subtitle> subtitles = new HashSet<>();
        do {
            page = indexFileRepository.findIndexFileByProcessedIsFalse(pageable);
            for (IndexFile indexFile : page) {
                File file = new File(indexFile.getFilePath());
                Set<IndexItem> indexItems = new HashSet<>();
                VttObject vttObject;
                try {
                    vttObject = vttParser.parse(new FileInputStream(file));

                    List<SubtitleCue> subtitleCues = vttObject.getCues();

                    // 1️⃣ Change extension to .json and read the JSON file
                    String jsonFileName = file.getPath().replaceFirst("\\.[^.]+$", ".json");
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readTree(new File(jsonFileName));

                    subtitleCues.forEach(subtitleCue -> {
                        try {
                            Subtitle subtitle = populateSubtitle(subtitleCue, rootNode);
                            IndexItem indexItem = new IndexItem();
                            indexItem.setIndexFile(indexFile);
                            indexItem.setDocumentId(subtitle.getId());
                            indexItems.add(indexItem);
                            subtitles.add(subtitle);
                        } catch (IOException e) {
                            logger.error("Exception: {}", e.getMessage());
                        }

                    });
                    indexItemRepository.saveAll(indexItems);
                    subtitleRepository.saveAll(subtitles);
                    indexFile.setIndexItems(indexItems);
                    indexFile.setProcessed(true);
                    indexFileRepository.save(indexFile);
                    subtitles.clear();
                    indexItemRepository.flush();
                    entityManager.clear();
                    counterSubtitleSuccess.incrementAndGet();

                } catch (IOException | SubtitleParsingException | IndexOutOfBoundsException e) {
                    counterSubtitleFailed.incrementAndGet();
                    indexFile.setProcessed(true);
                    indexFile.setProcessingError(e.getMessage());
                    indexFileRepository.save(indexFile);
                    indexItemRepository.flush();
                    entityManager.clear();
                }

            }

        } while (page.hasNext());
        indexFileRepository.flush();
    }

    private Set<Subtitle> indexItemsToSubtitles(Set<IndexItem> indexItems) {
        Set<Subtitle> subtitles = new HashSet<>();
        indexItems.forEach(indexItem -> {
            Subtitle subtitle = new Subtitle();
            subtitle.setId(indexItem.getDocumentId());
            subtitles.add(subtitle);
        });
        return subtitles;
    }

    private CategoryInfo indexFileCategoryToCategory(String idHash) {
        CategoryInfo categoryInfo = new CategoryInfo();
        categoryInfo.setId(idHash);
        return categoryInfo;
    }

    private IndexFile getIndexFileUpdated(File file) throws IOException, NoSuchAlgorithmException {
        IndexFile indexFile = indexFileRepository.findByFilePath(file.getAbsolutePath()).orElse(new IndexFile());
        String oldHash = indexFile.getFileHash();
        indexFile.setFileHash(generateXXH3(file));
        indexFile.setFileChanged(false);
        if (oldHash != null && !indexFile.getFileHash().equals(oldHash)) {
            indexFile.setFileChanged(true);
        }
        if (indexFile.getFilePath() == null) {
            indexFile.setFilePath(file.getAbsolutePath());
        }
        if (indexFile.isObjectChanged()) {
            indexFileRepository.save(indexFile);
        }
        return indexFile;
    }

    private IndexFileCategory getIndexFileCategoryUpdated(File file) throws IOException, NoSuchAlgorithmException {
        IndexFileCategory indexFile = indexFileCategoryRepository.findByFilePath(file.getAbsolutePath()).orElse(new IndexFileCategory());
        if (indexFile.getFilePath() == null) {
            indexFile.setFilePath(file.getAbsolutePath());
        }
        if (indexFile.isObjectChanged() || indexFile.getId() == null) {
            indexFileCategoryRepository.save(indexFile);
        }
        return indexFile;
    }

    private void createIndexIfNotExists(String indexName, Map<String, Object> mappings) {
        Map<String, Object> settings = new HashMap<>();
        //settings.put("index.max_result_window", 10);

        if (!elasticsearchOperations.indexOps(IndexCoordinates.of(indexName)).exists()) {
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

    private Subtitle populateSubtitle(SubtitleCue subtitleCue, JsonNode root) throws IOException {
        Subtitle subtitle = new Subtitle();

        // categoryInfo → from sql_params.vid_category
        subtitle.setCategoryInfo(root.path("sql_params").path("search_category").asText() + " " + root.path("sql_params").path("vid_title").asText());

        // subtitlePath → from target_vtt_filename
        subtitle.setSubtitlePath(root.path("target_directory_relative").asText() + "/" + root.path("target_vtt_filename").asText());

        // videoDate → from sql_params.date (parse to DateTime)
        String dateStr = root.path("sql_params").path("date").asText();
        if (dateStr.equals("0000-00-00")) {
            dateStr = root.path("sql_params").path("created_at").asText();
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(dateStr, fmt);

        subtitle.setVideoDate(dateTime);

        // author → from sql_params.search_category
        subtitle.setAuthor(root.path("sql_params").path("search_category").asText());

        // title → from sql_params.vid_title
        subtitle.setTitle(root.path("sql_params").path("vid_title").asText());

        // videoId → from sql_params.video_id
        subtitle.setVideoId(root.path("sql_params").path("video_id").asText());

        subtitle.setText(subtitleCue.getText());
        subtitle.setTimestamp(convertTimestampToSeconds(subtitleCue.getId().substring(0, subtitleCue.getId().indexOf(" "))));
        subtitle.setId(generateId(subtitle.getSubtitlePath(), subtitle.getText(), String.valueOf(subtitle.getTimestamp())));

        return subtitle;
    }

    private CategoryInfo populateCategoryInfo(JsonNode root) {
        CategoryInfo categoryInfo = new CategoryInfo();

        // categoryInfo → from sql_params.vid_category
        categoryInfo.setCategoryInfo(root.path("sql_params").path("vid_category").asText() + " " + root.path("sql_params").path("vid_title").asText());

        // subtitlePath → from target_vtt_filename
        categoryInfo.setSubtitlePath(root.path("target_directory_relative").asText() + "/" + root.path("target_vtt_filename").asText());

        // videoDate → from sql_params.date (parse to DateTime)
        String dateStr = root.path("sql_params").path("date").asText();
        if (dateStr.equals("0000-00-00")) {
            dateStr = root.path("sql_params").path("created_at").asText();
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(dateStr, fmt);
        categoryInfo.setVideoDate(dateTime);

        // author → from sql_params.search_category
        categoryInfo.setAuthor(root.path("sql_params").path("search_category").asText());

        // title → from sql_params.vid_title
        categoryInfo.setTitle(root.path("sql_params").path("vid_title").asText());

        categoryInfo.setId(generateId(categoryInfo.getCategoryInfo(), categoryInfo.getSubtitlePath(), ""));
        return categoryInfo;
    }

    private String getPath(String fileName) {
        String subtitlePath = fileName.replaceAll(path, "");
        if (subtitlePath.startsWith("/")) {
            subtitlePath = subtitlePath.substring(1);
        }
        return subtitlePath;
    }

    private String getCategoryInfo(String subtitlePath) {
        return subtitlePath
                .replaceAll("/", " ")
                .replaceAll(categoryInfoIndexFileExtension, "")
                .replaceAll("_", " ");
    }

    public double convertTimestampToSeconds(String timestamp) {
        String[] parts = timestamp.split(":");
        double hours = 0;
        double minutes = 0;
        double seconds = 0;

        if (parts.length == 3) {
            // Timestamp is in the format HH:MM:SS.sss
            hours = Double.parseDouble(parts[0]);
            minutes = Double.parseDouble(parts[1]);
            seconds = Double.parseDouble(parts[2]);
        } else if (parts.length == 2) {
            // Timestamp is in the format MM:SS.sss
            minutes = Double.parseDouble(parts[0]);
            seconds = Double.parseDouble(parts[1]);
        } else if (parts.length == 1) {
            // Timestamp is in the format SS.sss
            seconds = Double.parseDouble(parts[0]);
        } else {
            throw new IllegalArgumentException("Invalid timestamp format: " + timestamp);
        }

        return hours * 3600 + minutes * 60 + seconds;
    }


    public String generateId(String title, String text, String timestamp) {
        LongHashFunction xxh3 = LongHashFunction.xx(); //extract into a bean?
        long hash1 = xxh3.hashChars(title + text);
        long hash2 = xxh3.hashChars(text + timestamp);
        BigInteger hash = new BigInteger(Long.toBinaryString(hash1) + Long.toBinaryString(hash2), 2);
        return hash.toString(16);
    }


    public static String generateXXH3(File file) throws IOException {
        LongHashFunction xxh3 = LongHashFunction.xx();
        try (InputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            long hash = 0;
            while ((bytesRead = fis.read(buffer)) != -1) {
                hash = xxh3.hashBytes(buffer, 0, bytesRead);
            }
            return Long.toHexString(hash);
        }
    }

    private static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuilder stringBuffer = new StringBuilder();
        for (byte bytes : arrayBytes) {
            stringBuffer.append(Integer.toString((bytes & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
    }

    @Transactional
    @Override
    public void deleteIndex() {

        jdbcTemplate.execute("DROP TABLE IF EXISTS index_item");
        jdbcTemplate.execute("DROP TABLE IF EXISTS index_file");
        jdbcTemplate.execute("DROP TABLE IF EXISTS index_file_category");

        logger.info("Deleting all indexes...");
        IndexOperations indexOpsSubtitles = elasticsearchOperations.indexOps(IndexCoordinates.of(Constants.INDEX_SUBTITLES));
        if (indexOpsSubtitles.exists()) {
            indexOpsSubtitles.delete();
        }

        IndexOperations indexOpsCategoryInfo = elasticsearchOperations.indexOps(IndexCoordinates.of(Constants.INDEX_CATEGORY_INFO));
        if (indexOpsCategoryInfo.exists()) {
            indexOpsCategoryInfo.delete();
        }
        createIndexes();
        logger.info("Done.");

        SubtitleFtsApplication.restart();
    }

    public void createIndexes() {
        logger.info("Creating indexes...");

        IndexOperations indexOpsSubtitles = elasticsearchOperations.indexOps(IndexCoordinates.of(Constants.INDEX_SUBTITLES));
        if (!indexOpsSubtitles.exists()) {
            indexOpsSubtitles.create(); // creates the index
            indexOpsSubtitles.putMapping(indexOpsSubtitles.createMapping(Subtitle.class)); // applies mapping
        }

        IndexOperations indexOpsCategoryInfo = elasticsearchOperations.indexOps(IndexCoordinates.of(Constants.INDEX_CATEGORY_INFO));
        if (!indexOpsCategoryInfo.exists()) {
            indexOpsCategoryInfo.create();
            indexOpsCategoryInfo.putMapping(indexOpsCategoryInfo.createMapping(CategoryInfo.class));
        }

        logger.info("Indexes created.");
    }


    @Override
    public void cleanupIndex() {
        try {
            AtomicInteger count = new AtomicInteger();
            long startTime = System.currentTimeMillis();
            indexFileRepository.findAll().forEach(indexFile -> {
                if (!Files.exists(Paths.get(indexFile.getFilePath()))) {
                    subtitleRepository.deleteAll(indexItemsToSubtitles(indexFile.getIndexItems()));
                    indexItemRepository.deleteAll(indexFile.getIndexItems());
                    indexFileRepository.delete(indexFile);
                    count.getAndIncrement();
                }
            });
            long endTime = System.currentTimeMillis();
            logger.info("Subtitle database cleanup time " + (endTime - startTime) / 1000 + ", Deleted: " + count.get());

            startTime = System.currentTimeMillis();

            count.set(0);
            indexFileCategoryRepository.findAll().forEach(indexFileCategory -> {
                if (!Files.exists(Paths.get(indexFileCategory.getFilePath()))) {
                    categoryInfoRepository.delete(indexFileCategoryToCategory(indexFileCategory.getDocumentId()));
                    indexFileCategoryRepository.delete(indexFileCategory);
                    count.getAndIncrement();
                } else {

                }
            });
            indexItemRepository.flush();
            indexFileRepository.flush();
            indexFileCategoryRepository.flush();
            endTime = System.currentTimeMillis();
            logger.info("Category database cleanup time seconds: " + (endTime - startTime) / 1000 + ", Deleted: " + count.get());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Cleanup failed: {}", e.getMessage());
        }
    }

}
