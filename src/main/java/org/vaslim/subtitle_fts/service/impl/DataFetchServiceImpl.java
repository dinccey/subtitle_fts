package org.vaslim.subtitle_fts.service.impl;

import org.springframework.stereotype.Service;
import org.vaslim.subtitle_fts.model.Subtitle;
import org.vaslim.subtitle_fts.service.DataFetchService;
import org.vaslim.subtitle_fts.service.FileService;
import org.vaslim.subtitle_fts.srtparsing.SRTParser;
import org.vaslim.subtitle_fts.srtparsing.SubtitleDTO;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataFetchServiceImpl implements DataFetchService {

    private final FileService fileService;

    public DataFetchServiceImpl(FileService fileService) {
        this.fileService = fileService;
    }

    @Override
    public List<Subtitle> getNextSubtitleData() {
        List<File> nextFiles = fileService.getNext();
        List<Subtitle> subtitles = new ArrayList<>();
        nextFiles.forEach(file -> {
            if(file.getName().endsWith(".srt")){
                List<SubtitleDTO> subtitleDTOS = SRTParser.getSubtitlesFromFile(file.getPath());
                subtitleDTOS.forEach(subtitleDTO -> {
                    subtitles.add(populateSubtitle(subtitleDTO, file.getName()));
                });
            }
        });

        return subtitles;
    }

    private Subtitle populateSubtitle(SubtitleDTO subtitleDTO, String fileName) {
        Subtitle subtitle = new Subtitle();
        subtitle.setVideoName(fileName.substring(0, fileName.lastIndexOf(".")));
        subtitle.setText(subtitleDTO.text);
        subtitle.setTimestamp(subtitleDTO.startTime);
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
