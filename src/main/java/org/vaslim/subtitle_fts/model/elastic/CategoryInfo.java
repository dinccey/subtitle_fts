package org.vaslim.subtitle_fts.model.elastic;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.vaslim.subtitle_fts.constants.Constants;

import java.time.LocalDateTime;

@Document(indexName = Constants.INDEX_CATEGORY_INFO)
public class CategoryInfo {

    @Id
    private String id;

    @Field(type = FieldType.Text, name = "categoryInfo")
    private String categoryInfo;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime videoDate;
    private String author;
    private String title;
    private String videoId;

    private String subtitlePath;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }
}
