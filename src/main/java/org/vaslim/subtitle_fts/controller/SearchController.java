package org.vaslim.subtitle_fts.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vaslim.subtitle_fts.dto.MediaRecordDTO;
import org.vaslim.subtitle_fts.model.Subtitle;
import org.vaslim.subtitle_fts.service.SubtitleService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("api/v1/search")
public class SearchController {

    private final SubtitleService subtitleService;

    public SearchController(SubtitleService subtitleService) {
        this.subtitleService = subtitleService;
    }

    @GetMapping("/fuzzy")
    public ResponseEntity<List<MediaRecordDTO>> getVideoByQueryFuzzy(@RequestParam String query){
        return ResponseEntity.ok(subtitleService.findVideosByTitleOrSubtitleContentFuzzy(query));
    }

    @GetMapping("/exact")
    public ResponseEntity<List<MediaRecordDTO>> getVideoByQueryExact(@RequestParam(name = "query") String query, @RequestParam("categoryInfo") String categoryInfo){
        return ResponseEntity.ok(subtitleService.findVideosByTitleOrSubtitleContentExact(query, categoryInfo));
    }
}
