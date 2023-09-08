package org.vaslim.subtitle_fts.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.vaslim.subtitle_fts.service.FileService;

import java.io.File;
import java.util.*;

@Service
public class FileServiceImpl implements FileService {


    @Value("${files.iteration.size}")
    private Integer size; // number of items to return

    private Iterator<File> iterator; // iterator for the list

    @Value("${files.path.root}")
    private String path;

    public FileServiceImpl(@Value("${files.path.root}") String path) {
        // list of files in the path
        List<File> files = Arrays.asList(Objects.requireNonNull(new File(path).listFiles())); // get all files from the path
        this.iterator = files.iterator(); // create an iterator
    }

    public List<File> getNext() {
        List<File> result = new ArrayList<>(); // create a result list
        int count = 0; // count the number of items added
        while (iterator.hasNext() && count < size) { // loop until iterator is exhausted or size is reached
            result.add(iterator.next()); // add the next file to the result
            count++; // increment the count
        }
        return result; // return the result
    }

    @Override
    public void reset() {
        List<File> files = Arrays.asList(Objects.requireNonNull(new File(path).listFiles())); // get all files from the path
        this.iterator = files.iterator(); // create an iter
    }
}