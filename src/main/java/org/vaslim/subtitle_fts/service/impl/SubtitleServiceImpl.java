package org.vaslim.subtitle_fts.service.impl;

import org.springframework.stereotype.Service;
import org.vaslim.subtitle_fts.dto.MediaRecordDTO;
import org.vaslim.subtitle_fts.dto.SubtitleDTO;
import org.vaslim.subtitle_fts.model.Subtitle;
import org.vaslim.subtitle_fts.elastic.SubtitleRepository;
import org.vaslim.subtitle_fts.service.SubtitleService;

import java.util.*;

@Service
public class SubtitleServiceImpl implements SubtitleService {

    private final SubtitleRepository subtitleRepository;

    public SubtitleServiceImpl(SubtitleRepository subtitleRepository) {
        this.subtitleRepository = subtitleRepository;
    }

    @Override
    public List<MediaRecordDTO> findVideosByTitleOrSubtitleContentFuzzy(String query) {
        List<Subtitle> subtitlesResponse = subtitleRepository.findByTextOrVideoName(query);
        return prepareElasticResponse(subtitlesResponse);
    }

    @Override
    public List<MediaRecordDTO> findVideosByTitleOrSubtitleContentExact(String query, String categoryInfo) {
        if(query.isEmpty()){
            return prepareElasticResponse(subtitleRepository.findByCategoryInfo(categoryInfo));
        }
        if(categoryInfo.isEmpty()){
            return prepareElasticResponse(subtitleRepository.findByText(query));
        }
        return prepareElasticResponse(subtitleRepository.findByTextAndCategoryInfo(query, categoryInfo));
    }

    private List<MediaRecordDTO> prepareElasticResponse(List<Subtitle> subtitlesResponse) {
        Set<MediaRecordDTO> mediaRecordDTOS = new LinkedHashSet<>();
        subtitlesResponse.forEach(subtitle -> {
            Optional<MediaRecordDTO> mediaRecordDTO = mediaRecordDTOS.stream().filter(mr -> mr.getSubtitlePath() != null && mr.getSubtitlePath().equals(subtitle.getSubtitlePath())).findFirst();
            mediaRecordDTO.ifPresentOrElse(
                    recordDTO -> recordDTO.addSubtitle(mapToSubtitleDTO(subtitle)), () -> mediaRecordDTOS.add(populateNewMediaRecord(subtitle))
            );
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
