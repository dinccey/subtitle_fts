package org.vaslim.subtitle_fts.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public interface FileService {
    List<File> getNext();

    void reset();
}
