package org.vaslim.subtitle_fts.database;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.vaslim.subtitle_fts.model.indexingdb.IndexFile;
import org.vaslim.subtitle_fts.model.indexingdb.IndexFileCategory;

import java.util.Optional;
import java.util.Set;

@Repository
public interface IndexFileCategoryRepository extends JpaRepository<IndexFileCategory, Long> {
    Set<IndexFileCategory> findAllByFileDeletedIsTrue();

    Page<IndexFileCategory> findIndexFileByProcessedIsFalse(Pageable pageable);

    Optional<IndexFileCategory> findByFilePath(String filePath);

    @Modifying
    @Query(value = "TRUNCATE TABLE index_file_category", nativeQuery = true)
    void deleteAllEntities();
}
