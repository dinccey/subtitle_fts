package org.vaslim.subtitle_fts.model.indexingdb;

import jakarta.persistence.*;

@Entity
@Table(name = "index_file_category", indexes = {@Index(name = "indexFileCategpry_filePath", columnList = "filePath")})
public class IndexFileCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fileCatSeq")
    @SequenceGenerator(name = "fileCatSeq", sequenceName = "file_cat_seq", allocationSize = 1)
    @Column(unique = true, nullable = false, updatable = false)
    private Long id;

    @Column(unique = true)
    private String filePath;

    //represents id in elastic document
    @Column(length = 64)
    private String documentId;

    @Column
    private boolean processed = false;

    @Column
    private boolean fileDeleted = false;

    @Column
    private boolean fileChanged;

    @Column(length = 64)
    private String fileHash;

    @Transient
    private boolean objectChanged;

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
        if(filePath != null && !filePath.equals(this.fileHash)){
            setObjectChanged();
        }
        this.filePath = filePath;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        if(processed != this.processed){
            setObjectChanged();
        }
        this.processed = processed;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        if(fileHash != null && !fileHash.equals(this.fileHash)){
            setObjectChanged();
        }
        this.fileHash = fileHash;
    }

    public boolean isFileDeleted() {
        return fileDeleted;
    }

    public void setFileDeleted(boolean fileDeleted) {
        if(fileDeleted != this.fileDeleted){
            this.fileDeleted = fileDeleted;
            setObjectChanged();
        }
    }

    public boolean isFileChanged() {
        return fileChanged;
    }

    public void setFileChanged(boolean fileChanged) {
        this.fileChanged = fileChanged;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String itemOriginalHash) {
        this.documentId = itemOriginalHash;
    }

    private void setObjectChanged(){
        this.objectChanged = true;
    }
    public boolean isObjectChanged() {
        return objectChanged;
    }
}
