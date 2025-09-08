package org.vaslim.subtitle_fts.scheduledjob;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.vaslim.subtitle_fts.service.IndexService;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
@EnableScheduling
public class IndexingJob {

    private final IndexService indexService;

    // Flags to prevent overlapping runs
    private final AtomicBoolean indexingRunning = new AtomicBoolean(false);
    private final AtomicBoolean cleanupRunning = new AtomicBoolean(false);

    public IndexingJob(IndexService indexService) {
        this.indexService = indexService;
    }

    @Scheduled(cron = "${job.cron}")
    public void run() {
        if (!indexingRunning.compareAndSet(false, true)) {
            // Already running, skip this trigger
            System.out.println("Indexing job skipped — previous run still in progress.");
            return;
        }
        try {
            indexService.runIndexing();
        } finally {
            indexingRunning.set(false);
        }
    }

    @Scheduled(cron = "${job.cleanup.cron}")
    public void runDbCleanup() {
        if (!cleanupRunning.compareAndSet(false, true)) {
            // Already running, skip this trigger
            System.out.println("Cleanup job skipped — previous run still in progress.");
            return;
        }
        try {
            indexService.cleanupIndex();
        } finally {
            cleanupRunning.set(false);
        }
    }
}
