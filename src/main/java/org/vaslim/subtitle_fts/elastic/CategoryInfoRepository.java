package org.vaslim.subtitle_fts.elastic;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.vaslim.subtitle_fts.model.CategoryInfo;

import java.util.List;

public interface CategoryInfoRepository extends ElasticsearchRepository<CategoryInfo, String> {

    List<CategoryInfo> findAllByCategoryInfo(String query, Pageable pageable);
}
