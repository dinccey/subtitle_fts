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
        addFiles(iterator, result, size); // call the recursive method
        return result; // return the result
    }

    private void addFiles(Iterator<File> iterator, List<File> result, int size) {
        while (iterator.hasNext() && result.size() < size) { // loop until iterator is exhausted or size is reached
            File nextFile = iterator.next(); // get the next file
            if (nextFile.isDirectory()) { // if the file is a directory
                File[] filesInDirectory = nextFile.listFiles(); // get all files in the directory
                for (File file : filesInDirectory) { // for each file in the directory
                    if (file.isDirectory()) { // if the file is a directory
                        addFiles(Arrays.asList(file.listFiles()).iterator(), result, size); // recursively add files from the directory
                    } else if (result.size() < size) { // if the file is not a directory and size is not reached
                        result.add(file); // add the file to the result
                    }
                }
            } else if (result.size() < size) { // if the file is not a directory and size is not reached
                result.add(nextFile); // add the next file to the result
            }
        }
    }


    @Override
    public void reset() {
        List<File> files = Arrays.asList(Objects.requireNonNull(new File(path).listFiles())); // get all files from the path
        this.iterator = files.iterator(); // create an iter
    }
}