package org.vaslim.subtitle_fts.model.elastic;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.vaslim.subtitle_fts.constants.Constants;

@Document(indexName = Constants.INDEX_CATEGORY_INFO)
public class CategoryInfo {

    @Id
    private String id;
    @Field(type = FieldType.Text, name = "categoryInfo")
    private String categoryInfo;

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
}
