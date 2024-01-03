package org.vaslim.subtitle_fts.service.impl;

import fr.noop.subtitle.model.SubtitleCue;
import fr.noop.subtitle.model.SubtitleParsingException;
import fr.noop.subtitle.vtt.VttObject;
import fr.noop.subtitle.vtt.VttParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.vaslim.subtitle_fts.model.Subtitle;
import org.vaslim.subtitle_fts.service.DataFetchService;
import org.vaslim.subtitle_fts.service.FileService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DataFetchServiceImpl implements DataFetchService {

    private static final Logger logger = LoggerFactory.getLogger(DataFetchServiceImpl.class);
    private final FileService fileService;

    private final VttParser vttParser;

    @Value("${files.path.root}")
    private String path;

    public DataFetchServiceImpl(FileService fileService, VttParser vttParser) {
        this.fileService = fileService;
        this.vttParser = vttParser;
    }

    @Override
    public Set<Subtitle> getNextSubtitleData() {
        List<File> nextFiles = fileService.getNext();
        Set<Subtitle> subtitles = new HashSet<>();
        logger.info("Number of subtitle files for parsing: " + nextFiles.size() + " Sample file name: " + nextFiles.stream().findAny().get().getAbsolutePath());
        nextFiles.forEach(file -> {
            if(file.getAbsolutePath().endsWith(".vtt")){
                VttObject vttObject;
                try {
                    vttObject = vttParser.parse(new FileInputStream(file));
                } catch (IOException | SubtitleParsingException e) {
                    throw new RuntimeException(e);
                }

                List<SubtitleCue> subtitleCues = vttObject.getCues();
                subtitleCues.forEach(subtitleCue -> {
                    subtitles.add(populateSubtitle(subtitleCue, file.getPath()));
                });
                logger.info("Subtitle cues count: " + subtitleCues.size());
            }
        });

        return subtitles;
    }

    private Subtitle populateSubtitle(SubtitleCue subtitleCue, String fileName) {
        Subtitle subtitle = new Subtitle();
        String videoName = fileName.replaceAll(path,"");
        if(videoName.startsWith("/")){
            videoName = videoName.substring(1);
        }
        subtitle.setVideoName(videoName);
        subtitle.setText(subtitleCue.getText());
        subtitle.setTimestamp(subtitleCue.getId().substring(0, subtitleCue.getId().indexOf(" ")));
        subtitle.setId(generateId(subtitle.getVideoName(), subtitle.getText()));

        return subtitle;
    }

    public String generateId(String title, String text) {
        try {
            // Create a MessageDigest instance with MD5 algorithm
            MessageDigest md = MessageDigest.getInstance("MD5");
            // Convert the title and text to bytes and update the digest
            md.update(title.getBytes());
            md.update(text.getBytes());
            // Get the digest bytes
            byte[] digest = md.digest();
            // Convert the bytes to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            // Return the hexadecimal string as the ID
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
