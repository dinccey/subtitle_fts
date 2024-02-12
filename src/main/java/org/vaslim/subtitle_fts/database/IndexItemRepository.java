package org.vaslim.subtitle_fts.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vaslim.subtitle_fts.model.indexingdb.IndexFile;
import org.vaslim.subtitle_fts.model.indexingdb.IndexItem;

import java.util.Set;

@Repository
public interface IndexItemRepository extends JpaRepository<IndexItem, Long> {
   
}
