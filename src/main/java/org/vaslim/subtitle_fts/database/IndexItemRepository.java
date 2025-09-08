package org.vaslim.subtitle_fts.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.vaslim.subtitle_fts.model.indexingdb.IndexItem;

import java.util.Set;

@Repository
public interface IndexItemRepository extends JpaRepository<IndexItem, Long> {
    @Modifying
    @Query("DELETE FROM IndexItem i WHERE i.documentId IN :documentIds")
    void deleteIndexItemsByDocumentId(@Param("documentIds") Set<String> documentIds);

    @Modifying
    @Query(value = "DROP TABLE index_item", nativeQuery = true)
    void deleteAllEntities();

}

