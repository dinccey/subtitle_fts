package org.vaslim.subtitle_fts.dto;

public class SubtitleDTO {

    private double timestamp;
    private String text;


    public double getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
