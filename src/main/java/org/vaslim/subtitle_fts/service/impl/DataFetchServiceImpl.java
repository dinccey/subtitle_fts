package org.vaslim.subtitle_fts.service.impl;

import fr.noop.subtitle.model.SubtitleCue;
import fr.noop.subtitle.model.SubtitleParsingException;
import fr.noop.subtitle.vtt.VttObject;
import fr.noop.subtitle.vtt.VttParser;
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

    private final FileService fileService;

    private final VttParser vttParser;

    public DataFetchServiceImpl(FileService fileService, VttParser vttParser) {
        this.fileService = fileService;
        this.vttParser = vttParser;
    }

    @Override
    public Set<Subtitle> getNextSubtitleData() {
        List<File> nextFiles = fileService.getNext();
        Set<Subtitle> subtitles = new HashSet<>();
        nextFiles.forEach(file -> {
            if(file.getName().endsWith(".vtt")){
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
            }
        });

        return subtitles;
    }

    private Subtitle populateSubtitle(SubtitleCue subtitleCue, String fileName) {
        Subtitle subtitle = new Subtitle();
        subtitle.setVideoName(fileName);
        subtitle.setText(subtitleCue.getText());
        subtitle.setTimestamp(subtitleCue.getStartTime().toString());
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
