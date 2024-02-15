package org.vaslim.subtitle_fts.database;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vaslim.subtitle_fts.model.indexingdb.IndexFile;


import java.util.Optional;
import java.util.Set;

@Repository
public interface IndexFileRepository extends JpaRepository<IndexFile, Long> {
    Set<IndexFile> findAllByFileDeletedIsTrue();

    Page<IndexFile> findIndexFileByProcessedIsFalse(Pageable pageable);

    Optional<IndexFile> findByFilePath(String filePath);
}
