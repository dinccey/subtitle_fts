package org.vaslim.subtitle_fts.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vaslim.subtitle_fts.model.MediaRecord;
import org.vaslim.subtitle_fts.model.Subtitle;
import org.vaslim.subtitle_fts.service.SubtitleService;

import java.util.List;

@RestController
@RequestMapping("api/v1/search")
public class SearchController {

    private final SubtitleService subtitleService;

    public SearchController(SubtitleService subtitleService) {
        this.subtitleService = subtitleService;
    }

    @GetMapping("/fuzzy")
    public ResponseEntity<List<MediaRecord>> getVideoByQueryFuzzy(@RequestParam String query){
        return ResponseEntity.ok(subtitleService.findVideosByTitleOrSubtitleContentFuzzy(query));
    }

    @GetMapping("/exact")
    public ResponseEntity<List<MediaRecord>> getVideoByQueryExact(@RequestParam(name = "query") String query, @RequestParam(name = "catName") String catName){
        return ResponseEntity.ok(subtitleService.findVideosByTitleOrSubtitleContentExact(query, catName));
    }
}
