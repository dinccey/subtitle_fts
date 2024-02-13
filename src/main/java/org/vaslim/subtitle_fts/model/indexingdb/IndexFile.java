package org.vaslim.subtitle_fts.model.indexingdb;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "index_file")
public class IndexFile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fileSeq")
    @SequenceGenerator(name = "fileSeq", sequenceName = "file_seq", allocationSize = 1)
    @Column(unique = true, nullable = false, updatable = false)
    private Long id;

    @Column(unique = true)
    private String filePath;

    @Column
    private boolean processed = false;

    @Column
    private boolean fileDeleted = false;

    @Column
    private boolean fileChanged;

    @Column(length = 64)
    private String fileHash;

    @OneToMany(mappedBy = "indexFile")
    Set<IndexItem> indexItems = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public Set<IndexItem> getIndexItems() {
        return indexItems;
    }

    public void setIndexItems(Set<IndexItem> indexItems) {
        this.indexItems = indexItems;
    }

    public boolean isFileDeleted() {
        return fileDeleted;
    }

    public void setFileDeleted(boolean fileDeleted) {
        this.fileDeleted = fileDeleted;
    }

    public boolean isFileChanged() {
        return fileChanged;
    }

    public void setFileChanged(boolean fileChanged) {
        this.fileChanged = fileChanged;
    }
}