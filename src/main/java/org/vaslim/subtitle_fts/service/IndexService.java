package org.vaslim.subtitle_fts.service;

import org.springframework.stereotype.Service;

@Service
public interface IndexService {
    void runIndexing();

    void deleteIndex();

    void cleanupIndex();
}
