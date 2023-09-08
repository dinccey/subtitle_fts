package org.vaslim.subtitle_fts.srtparsing;

public class SubtitleDTO {

    public int id;
    public String startTime;
    public String endTime;
    public String text;
    public long timeIn;
    public long timeOut;
    public SubtitleDTO nextSubtitle;

    @Override
    public String toString() {
        return "Subtitle [id=" + id + ", startTime=" + startTime + ", endTime=" + endTime + ", text=" + text
                + ", timeIn=" + timeIn + ", timeOut=" + timeOut + "]";
    }

}