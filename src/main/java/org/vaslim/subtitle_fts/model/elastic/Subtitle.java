package org.vaslim.subtitle_fts.model.elastic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.vaslim.subtitle_fts.constants.Constants;

@Document(indexName = Constants.INDEX_SUBTITLES)
public class Subtitle {

    @JsonIgnore
    @Id
    private String id;

    @Field(type = FieldType.Text, name = "categoryInfo")
    private String categoryInfo;
    private String subtitlePath;

    private double timestamp;

    @Field(type = FieldType.Text, name = "text")
    private String text;

    @JsonIgnore
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubtitlePath() {
        return subtitlePath;
    }

    public void setSubtitlePath(String subtitlePath) {
        this.subtitlePath = subtitlePath;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCategoryInfo() {
        return categoryInfo;
    }

    public void setCategoryInfo(String categoryInfo) {
        this.categoryInfo = categoryInfo;
    }
}
