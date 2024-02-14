package org.vaslim.subtitle_fts.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.ElasticsearchTransport;
import fr.noop.subtitle.model.SubtitleCue;
import fr.noop.subtitle.model.SubtitleParsingException;
import fr.noop.subtitle.vtt.VttObject;
import fr.noop.subtitle.vtt.VttParser;
import net.openhft.hashing.LongHashFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.CRC32;

@Service
public class IndexServiceImpl implements IndexService {

    private static final Logger logger = LoggerFactory.getLogger(IndexServiceImpl.class);
    private final ElasticsearchClient elasticsearchClient;

    private final ElasticsearchOperations elasticsearchOperations;

    private final ElasticsearchTransport elasticsearchTransport;

    private final FileService fileService;

    private final SubtitleRepository subtitleRepository;

    private final CategoryInfoRepository categoryInfoRepository;

    private final VttParser vttParser;

    private final IndexFileRepository indexFileRepository;

    private final IndexFileCategoryRepository indexFileCategoryRepository;

    private final IndexItemRepository indexItemRepository;

    @Value("${files.path.root}")
    private String path;

    @Value("${category_info_index.file.extension}")
    private String categoryInfoIndexFileExtension;

    @Value("${subtitle_index.file.extension}")
    private String subtitleIndexFileExtension;

    public IndexServiceImpl(ElasticsearchClient elasticsearchClient, ElasticsearchOperations elasticsearchOperations, ElasticsearchTransport elasticsearchTransport, FileService fileService, SubtitleRepository subtitleRepository, CategoryInfoRepository categoryInfoRepository, VttParser vttParser, IndexFileRepository indexFileRepository, IndexFileCategoryRepository indexFileCategoryRepository, IndexItemRepository indexItemRepository) {
        this.elasticsearchClient = elasticsearchClient;
        this.elasticsearchOperations = elasticsearchOperations;
        this.elasticsearchTransport = elasticsearchTransport;
        this.fileService = fileService;
        this.subtitleRepository = subtitleRepository;
        this.categoryInfoRepository = categoryInfoRepository;
        this.vttParser = vttParser;
        this.indexFileRepository = indexFileRepository;
        this.indexFileCategoryRepository = indexFileCategoryRepository;
        this.indexItemRepository = indexItemRepository;
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

            indexCategoryInfo();

            long endTime = System.currentTimeMillis();
            logger.info("CategoryInfo indexing time seconds " + (endTime - startTime) / 1000);
            fileService.reset(); //reset iterator
            startTime = System.currentTimeMillis();

            indexSubtitles();
            endTime = System.currentTimeMillis();
            logger.info("Subtitle indexing time seconds: " + (endTime - startTime) / 1000);
        } finally {
            fileService.reset(); //reset iterator
        }

    }



    private void indexCategoryInfo() {
        List<File> files;
        while(!(files = fileService.getNext()).isEmpty()){
            files.forEach(file -> {
                if(file.getAbsolutePath().endsWith(categoryInfoIndexFileExtension)){
                    IndexFileCategory indexFileCategory;
                    try {
                        indexFileCategory = indexFileCategoryRepository.save(getIndexFileCategoryUpdated(file));
                    } catch (IOException | NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                    //currently there is no need to check for this
//                    if(indexFileCategory.isFileChanged()){
//                        indexFileCategory.setProcessed(false);
//                        categoryInfoRepository.delete(indexFileCategoryToCategory(indexFileCategory.getDocumentId()));
//                        indexFileCategoryRepository.save(indexFileCategory);
//                    }
                    if(indexFileCategory.isFileDeleted()){
                        categoryInfoRepository.delete(indexFileCategoryToCategory(indexFileCategory.getDocumentId()));
                        indexFileCategoryRepository.delete(indexFileCategory);
                    }
                }
            });
        }

        indexFileCategoryRepository.flush();
        indexFileCategoryRepository.findIndexFileByProcessedIsFalse().forEach(indexFileCategory -> {
            CategoryInfo categoryInfo = populateCategoryInfo(indexFileCategory.getFilePath());
            indexFileCategory.setDocumentId(categoryInfo.getId());
            categoryInfoRepository.save(categoryInfo);
            indexFileCategory.setProcessed(true);
            indexFileCategory.setDocumentId(categoryInfo.getId());
            indexFileCategoryRepository.save(indexFileCategory);
        });
        indexItemRepository.flush();
    }

    private void indexSubtitles() {
        List<File> files;
        while(!(files = fileService.getNext()).isEmpty()){
            files.forEach(file -> {

                if(file.getAbsolutePath().endsWith(subtitleIndexFileExtension)){
                    IndexFile indexFile;
                    try {
                        indexFile = indexFileRepository.save(getIndexFileUpdated(file));
                    } catch (IOException | NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                    if(indexFile.isFileChanged()){
                        indexFile.setProcessed(false);
                        subtitleRepository.deleteAll(indexItemsToSubtitles(indexFile.getIndexItems()));
                        indexFileRepository.save(indexFile);
                    }
                    if(indexFile.isFileDeleted()){
                        subtitleRepository.deleteAll(indexItemsToSubtitles(indexFile.getIndexItems()));
                        indexFileRepository.delete(indexFile);
                    }

                }
            });
        }
        indexFileRepository.flush();
        indexFileRepository.findIndexFileByProcessedIsFalse().forEach(indexFile -> {
            File file = new File(indexFile.getFilePath());
            Set<IndexItem> indexItems = new HashSet<>();
            VttObject vttObject;
            try {
                vttObject = vttParser.parse(new FileInputStream(file));
            } catch (IOException | SubtitleParsingException e) {
                throw new RuntimeException(e);
            }
            List<SubtitleCue> subtitleCues = vttObject.getCues();
            Set<Subtitle> subtitles = new HashSet<>();
            subtitleCues.forEach(subtitleCue -> {
                Subtitle subtitle = populateSubtitle(subtitleCue, file.getPath());
                IndexItem indexItem = new IndexItem();
                indexItem.setIndexFile(indexFile);
                indexItem.setDocumentId(subtitle.getId());
                indexItems.add(indexItem);
                subtitles.add(subtitle);
            });

            subtitleRepository.saveAll(subtitles);
            indexFile.setIndexItems(indexItems);
            indexFile.setProcessed(true);
            indexFileRepository.save(indexFile);
        });
        indexItemRepository.flush();
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
        indexFile.setFileDeleted(false);
        String oldHash = indexFile.getFileHash();
        indexFile.setFileHash(generateXXH3(file));
        indexFile.setFileChanged(false);
        if(!indexFile.getFileHash().equals(oldHash)){
            indexFile.setFileChanged(true);
        }
        indexFile.setFilePath(file.getAbsolutePath());
        return indexFile;
    }
    private IndexFileCategory getIndexFileCategoryUpdated(File file) throws IOException, NoSuchAlgorithmException {
        IndexFileCategory indexFile = indexFileCategoryRepository.findByFilePath(file.getAbsolutePath()).orElse(new IndexFileCategory());
        indexFile.setFileDeleted(false);
        indexFile.setFilePath(file.getAbsolutePath());
        return indexFile;
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

    private Subtitle populateSubtitle(SubtitleCue subtitleCue, String fileName) {
        Subtitle subtitle = new Subtitle();
        String subtitlePath = getPath(fileName);
        String categoryInfo = getCategoryInfo(subtitlePath);

        subtitle.setCategoryInfo(categoryInfo);
        subtitle.setSubtitlePath(subtitlePath);
        subtitle.setText(subtitleCue.getText());
        subtitle.setTimestamp(convertTimestampToSeconds(subtitleCue.getId().substring(0, subtitleCue.getId().indexOf(" "))));
        subtitle.setId(generateId(subtitle.getSubtitlePath(), subtitle.getText()));

        return subtitle;
    }

    private CategoryInfo populateCategoryInfo(String filename){
        CategoryInfo categoryInfo = new CategoryInfo();
        categoryInfo.setCategoryInfo(getCategoryInfo(getPath(filename)));
        categoryInfo.setSubtitlePath(getPath(filename));
        categoryInfo.setId(generateId(categoryInfo.getCategoryInfo(),categoryInfo.getSubtitlePath()));
        return categoryInfo;
    }

    private String getPath(String fileName) {
        String subtitlePath = fileName.replaceAll(path,"");
        if(subtitlePath.startsWith("/")){
            subtitlePath = subtitlePath.substring(1);
        }
        return subtitlePath;
    }

    private String getCategoryInfo(String subtitlePath) {
        String categoryInfo = subtitlePath
                .replaceAll("/", " ")
                .replaceAll(categoryInfoIndexFileExtension,"")
                .replaceAll("_"," ");
        return categoryInfo;
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


    public String generateId(String title, String text) {
        LongHashFunction xxh3 = LongHashFunction.xx();
        long hash = xxh3.hashChars(title + text);
        return Long.toHexString(hash);
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

        indexFileRepository.deleteAll();
        indexFileCategoryRepository.deleteAll();
        indexItemRepository.deleteAll();
    }

    @Override
    public void cleanupIndex() {
        try {
            long startTime = System.currentTimeMillis();
            indexFileRepository.findAll().forEach(indexFile -> {
                if (!Files.exists(java.nio.file.Paths.get(indexFile.getFilePath()))) {
                    indexFile.setFileDeleted(true);
                }
            });
            long endTime = System.currentTimeMillis();
            logger.info("Subtitle database cleanup time " + (endTime - startTime) / 1000);

            startTime = System.currentTimeMillis();

            indexFileCategoryRepository.findAll().forEach(indexFileCategory -> {
                if (!Files.exists(java.nio.file.Paths.get(indexFileCategory.getFilePath()))) {
                    indexFileCategory.setFileDeleted(true);
                }
            });
            endTime = System.currentTimeMillis();
            logger.info("Category database cleanup time seconds: " + (endTime - startTime) / 1000);
        } finally {
        }
    }

}
