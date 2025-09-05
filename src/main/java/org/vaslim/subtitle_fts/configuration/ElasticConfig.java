package org.vaslim.subtitle_fts.configuration;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.vaslim.subtitle_fts.constants.Constants;
import org.vaslim.subtitle_fts.model.elastic.CategoryInfo;
import org.vaslim.subtitle_fts.model.elastic.Subtitle;

@Configuration
public class ElasticConfig extends ElasticsearchConfiguration {

    @Value("${elasticsearch.address}")
    private String elasticAddress;

    @Value("${spring.elasticsearch.username}")
    private String username;

    @Value("${spring.elasticsearch.password}")
    private String password;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlAuto;

    private ElasticsearchOperations elasticsearchOperations;

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(elasticAddress)
                .withBasicAuth(username, password)
                .build();
    }

    @PostConstruct
    public void initIndexes() {
        if(ddlAuto.equals("create-drop")){
            IndexOperations indexOpsSubtitles = elasticsearchOperations.indexOps(IndexCoordinates.of(Constants.INDEX_SUBTITLES));
            if (indexOpsSubtitles.exists()) {
                indexOpsSubtitles.delete();
            }
            indexOpsSubtitles.create();
            indexOpsSubtitles.putMapping(indexOpsSubtitles.createMapping(Subtitle.class));

            IndexOperations indexOpsCategoryInfo = elasticsearchOperations.indexOps(IndexCoordinates.of(Constants.INDEX_CATEGORY_INFO));
            if (indexOpsCategoryInfo.exists()) {
                indexOpsCategoryInfo.delete();
            }
            indexOpsCategoryInfo.create();
            indexOpsCategoryInfo.putMapping(indexOpsCategoryInfo.createMapping(CategoryInfo.class));
        }
    }
}
