package org.vaslim.subtitle_fts.service.impl;

import org.springdoc.core.converters.models.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.vaslim.subtitle_fts.dto.MediaRecordDTO;
import org.vaslim.subtitle_fts.dto.SubtitleDTO;
import org.vaslim.subtitle_fts.elastic.CategoryInfoRepository;
import org.vaslim.subtitle_fts.model.CategoryInfo;
import org.vaslim.subtitle_fts.model.Subtitle;
import org.vaslim.subtitle_fts.elastic.SubtitleRepository;
import org.vaslim.subtitle_fts.service.SubtitleService;

import java.util.*;

@Service
public class SubtitleServiceImpl implements SubtitleService {

    private final SubtitleRepository subtitleRepository;

    private final CategoryInfoRepository categoryInfoRepository;


    public SubtitleServiceImpl(SubtitleRepository subtitleRepository, CategoryInfoRepository categoryInfoRepository) {
        this.subtitleRepository = subtitleRepository;
        this.categoryInfoRepository = categoryInfoRepository;
    }

    @Override
    public List<MediaRecordDTO> findVideosByTitleOrSubtitleContentFuzzy(String query, Integer maxResults) {
        List<Subtitle> subtitlesResponse = subtitleRepository.findByTextOrVideoName(query);
        return prepareElasticResponse(subtitlesResponse, maxResults);
    }

    @Override
    public List<MediaRecordDTO> findVideosByTitleOrSubtitleContentExact(String query, String categoryInfo, Integer maxResults) {
        if(query.trim().isBlank()){
            return prepareElasticResponseCategory(categoryInfoRepository.findAllByCategoryInfo(categoryInfo, PageRequest.of(0,maxResults)), maxResults);
        }
        if(categoryInfo.trim().isBlank()){
            return prepareElasticResponse(subtitleRepository.findByText(query, PageRequest.of(0,maxResults)), maxResults);
        }
        return prepareElasticResponse(subtitleRepository.findByTextAndCategoryInfo(query, categoryInfo, PageRequest.of(0,maxResults)), maxResults);
    }

    private List<MediaRecordDTO> prepareElasticResponseCategory(List<CategoryInfo> subtitlesResponse, Integer maxResults) {
        Set<MediaRecordDTO> mediaRecordDTOS = new LinkedHashSet<>();
        subtitlesResponse.forEach(categoryInfo -> {
            mediaRecordDTOS.add(populateNewMediaRecordFromCategoryInfo(categoryInfo));
        });

        return new ArrayList<>(mediaRecordDTOS);
    }

    private MediaRecordDTO populateNewMediaRecordFromCategoryInfo(CategoryInfo categoryInfo) {
        MediaRecordDTO mediaRecordDTO = new MediaRecordDTO();
        mediaRecordDTO.setSubtitlePath(categoryInfo.getSubtitlePath());
        mediaRecordDTO.setCategoryInfo(categoryInfo.getCategoryInfo());

        return mediaRecordDTO;
    }

    private List<MediaRecordDTO> prepareElasticResponse(List<Subtitle> subtitlesResponse, Integer maxResults) {
        Set<MediaRecordDTO> mediaRecordDTOS = new LinkedHashSet<>();
        subtitlesResponse.forEach(subtitle -> {
            Optional<MediaRecordDTO> mediaRecordDTO = mediaRecordDTOS.stream().filter(mr -> mr.getSubtitlePath() != null && mr.getSubtitlePath().equals(subtitle.getSubtitlePath())).findFirst();
            mediaRecordDTO.ifPresentOrElse(
                    recordDTO -> recordDTO.addSubtitle(mapToSubtitleDTO(subtitle)), () -> mediaRecordDTOS.add(populateNewMediaRecord(subtitle))
            );
        });

        mediaRecordDTOS.forEach(mediaRecordDTO -> {
            mediaRecordDTO.getSubtitles().sort((a, b) -> (int) (a.getTimestamp() - b.getTimestamp()));
        });

        List<MediaRecordDTO> mediaRecordDTOList = new ArrayList<>(mediaRecordDTOS);
        mediaRecordDTOList.sort((a, b) -> b.getSubtitles().size() - a.getSubtitles().size());

        return mediaRecordDTOList;
    }

    private MediaRecordDTO populateNewMediaRecord(Subtitle subtitle) {
        MediaRecordDTO mediaRecordDTO = new MediaRecordDTO();
        mediaRecordDTO.setCategoryInfo(subtitle.getCategoryInfo());
        mediaRecordDTO.setSubtitlePath(subtitle.getSubtitlePath());
        mediaRecordDTO.addSubtitle(mapToSubtitleDTO(subtitle));

        return mediaRecordDTO;
    }

    private SubtitleDTO mapToSubtitleDTO(Subtitle subtitle) {
        SubtitleDTO subtitleDTO = new SubtitleDTO();
        subtitleDTO.setText(subtitle.getText());
        subtitleDTO.setTimestamp(subtitle.getTimestamp());
        return subtitleDTO;
    }
}
