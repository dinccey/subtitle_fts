package org.vaslim.subtitle_fts.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MediaRecordDTO {
    private String categoryInfo;
    private String subtitlePath;
    private LocalDateTime videoDate;
    private String author;
    private String title;

    private List<SubtitleDTO> subtitles = new ArrayList<>();

    public String getCategoryInfo() {
        return categoryInfo;
    }

    public void setCategoryInfo(String categoryInfo) {
        this.categoryInfo = categoryInfo;
    }

    public String getSubtitlePath() {
        return subtitlePath;
    }

    public void setSubtitlePath(String subtitlePath) {
        this.subtitlePath = subtitlePath;
    }

    public List<SubtitleDTO> getSubtitles() {
        return subtitles;
    }

    public void setSubtitles(List<SubtitleDTO> subtitles) {
        this.subtitles = subtitles;
    }

    public void addSubtitle(SubtitleDTO subtitleDTO){
        this.subtitles.add(subtitleDTO);
    }

    public LocalDateTime getVideoDate() {
        return videoDate;
    }

    public void setVideoDate(LocalDateTime videoDate) {
        this.videoDate = videoDate;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
