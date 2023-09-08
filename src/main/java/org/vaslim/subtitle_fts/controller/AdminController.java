package org.vaslim.subtitle_fts.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.vaslim.subtitle_fts.model.Subtitle;
import org.vaslim.subtitle_fts.service.IndexService;

import java.util.List;

@Profile({"local","dev"})
@RestController
@RequestMapping("api/v1/admin")
public class AdminController {

    private final IndexService indexService;

    public AdminController(IndexService indexService) {
        this.indexService = indexService;
    }

    @GetMapping("/index")
    public ResponseEntity<List<Subtitle>> getVideoByQuery(){
        indexService.runIndexing();
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
