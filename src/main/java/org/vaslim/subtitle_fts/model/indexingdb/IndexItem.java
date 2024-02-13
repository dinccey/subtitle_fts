package org.vaslim.subtitle_fts.model.indexingdb;

import jakarta.persistence.*;

@Entity
@Table(name = "index_item")
public class IndexItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "itemSeq")
    @SequenceGenerator(name = "itemSeq", sequenceName = "item_seq", allocationSize = 5)
    @Column(unique = true, nullable = false, updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name="indexFile", nullable=false)
    private IndexFile indexFile;
    @Column(length = 64, unique = true)
    private String itemOriginalHash;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IndexFile getIndexFile() {
        return indexFile;
    }

    public void setIndexFile(IndexFile indexFile) {
        this.indexFile = indexFile;
    }

    public String getItemOriginalHash() {
        return itemOriginalHash;
    }

    public void setItemOriginalHash(String itemOriginalHash) {
        this.itemOriginalHash = itemOriginalHash;
    }
}
