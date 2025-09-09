package org.vaslim.subtitle_fts.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vaslim.subtitle_fts.database.IndexFileRepository;
import org.vaslim.subtitle_fts.database.IndexItemRepository;
import org.vaslim.subtitle_fts.elastic.SubtitleRepository;
import org.vaslim.subtitle_fts.model.elastic.Subtitle;
import org.vaslim.subtitle_fts.model.indexingdb.IndexFile;
import org.vaslim.subtitle_fts.model.indexingdb.IndexItem;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import static org.vaslim.subtitle_fts.service.impl.IndexServiceImpl.generateXXH3;

@Service
public class IndexServiceMethodsForProxy {

    private final IndexFileRepository indexFileRepository;
    private final IndexItemRepository indexItemRepository;
    private final SubtitleRepository subtitleRepository;

    public IndexServiceMethodsForProxy(IndexFileRepository indexFileRepository, IndexItemRepository indexItemRepository, SubtitleRepository subtitleRepository) {
        this.indexFileRepository = indexFileRepository;
        this.indexItemRepository = indexItemRepository;
        this.subtitleRepository = subtitleRepository;
    }

    @Transactional
    public void processIndexFileWIthItems(File file) throws IOException, NoSuchAlgorithmException {
        IndexFile indexFile = getIndexFileUpdated(file);
        if (indexFile.isFileChanged()) {
            indexFile.setProcessed(false);
            subtitleRepository.deleteAll(indexItemsToSubtitles(indexFile.getIndexItems()));
            indexItemRepository.deleteAll(indexFile.getIndexItems());
            indexFile.getIndexItems().clear();
            indexFile.setFileChanged(false);
            indexFileRepository.save(indexFile);
        }
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

    public Set<Subtitle> indexItemsToSubtitles(Set<IndexItem> indexItems) {
        Set<Subtitle> subtitles = new HashSet<>();
        indexItems.forEach(indexItem -> {
            Subtitle subtitle = new Subtitle();
            subtitle.setId(indexItem.getDocumentId());
            subtitles.add(subtitle);
        });
        return subtitles;
    }
}
