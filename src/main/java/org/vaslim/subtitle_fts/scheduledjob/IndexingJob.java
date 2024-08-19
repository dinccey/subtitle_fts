package org.vaslim.subtitle_fts.scheduledjob;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.vaslim.subtitle_fts.service.IndexService;


@Component
@EnableScheduling
public class IndexingJob {

    private final IndexService indexService;

    public IndexingJob(IndexService indexService) {
        this.indexService = indexService;
    }

    @Scheduled(cron = "${job.cron}")
    public void run() {
        indexService.runIndexing();
    }

    @Scheduled(cron = "${job.cleanup.cron}")
    public void runDbCleanup() {
        indexService.cleanupIndex();
    }
}
