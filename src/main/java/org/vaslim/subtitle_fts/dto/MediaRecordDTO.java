package org.vaslim.subtitle_fts.dto;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MediaRecordDTO {
    private String categoryInfo;
    private String subtitlePath;

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
}
