package org.vaslim.subtitle_fts.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vaslim.subtitle_fts.model.indexingdb.IndexFile;
import org.vaslim.subtitle_fts.model.indexingdb.IndexFileCategory;

import java.util.Optional;
import java.util.Set;

@Repository
public interface IndexFileCategoryRepository extends JpaRepository<IndexFileCategory, Long> {
    Set<IndexFileCategory> findAllByFileDeletedIsTrue();

    Set<IndexFileCategory> findIndexFileByProcessedIsFalse();

    Optional<IndexFileCategory> findByFilePath(String filePath);
}
