package org.vaslim.subtitle_fts.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.vaslim.subtitle_fts.service.FileService;

import java.io.File;
import java.util.*;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Value("${files.iteration.size}")
    private Integer size; // number of items to return

    Iterator<File> firstIterator;
    private Set<Iterator<File>> iterators = new HashSet<>();

    @Value("${files.path.root}")
    private String path;

    public FileServiceImpl(@Value("${files.path.root}") String path) {
        // list of files in the path
        List<File> files = Arrays.asList(Objects.requireNonNull(new File(path).listFiles())); // get all files from the path
        this.iterators.clear();
        this.firstIterator = files.iterator();
        addIterators(iterators, firstIterator);
    }

    public List<File> getNext() {
        List<File> result = new ArrayList<>(); // create a result list
        iterators.forEach(iterator -> {
            addFiles(iterator, result, size);
        });
        logger.info("Get next result size: " + result.size() + " Iterators count: " + iterators.size());
        return result; // return the result
    }

    private void addIterators(Set<Iterator<File>> iterators, Iterator<File> firstIterator) {
        while(firstIterator.hasNext()){
            File nextFile = firstIterator.next();
            if (nextFile.isDirectory()) { // if the file is a directory
                File[] filesInDirectory = nextFile.listFiles(); // get all files in the directory
                assert filesInDirectory != null;
                for (File file : filesInDirectory) { // for each file in the directory
                    if (file.isDirectory()) { // if the file is a directory
                        iterators.add(Arrays.asList(Objects.requireNonNull(file.listFiles())).iterator());
                    }
                }
            }
        }
    }

    private void addFiles(Iterator<File> iterator, List<File> result, int size) {
        while (iterator.hasNext() && result.size() < size) { // loop until iterator is exhausted or size is reached
            File nextFile = iterator.next(); // get the next file
            if (result.size() < size) { // if the file is not a directory and size is not reached
                result.add(nextFile); // add the next file to the result
            }
        }
    }


    @Override
    public void reset() {
        List<File> files = Arrays.asList(Objects.requireNonNull(new File(path).listFiles())); // get all files from the path
        this.iterators.clear();
        this.firstIterator = files.iterator();
        addIterators(iterators, firstIterator);
        logger.info("Reset iterators.");
    }
}