package org.vaslim.subtitle_fts;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/query")
    public ResponseEntity<List<Subtitle>> getVideoByQuery(@RequestParam String query){
        return ResponseEntity.ok(subtitleService.findVideosByTitleOrSubtitleContent(query));
    }
}
