package org.vaslim.subtitle_fts.dto;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MediaRecordDTO {
    private String categoryInfo;
    private String subtitlePath;

    private Set<SubtitleDTO> subtitles = new LinkedHashSet<>();

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

    public Set<SubtitleDTO> getSubtitles() {
        return subtitles;
    }

    public void setSubtitles(Set<SubtitleDTO> subtitles) {
        this.subtitles = subtitles;
    }

    public void addSubtitle(SubtitleDTO subtitleDTO){
        this.subtitles.add(subtitleDTO);
    }
}
