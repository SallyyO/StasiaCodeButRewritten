package com.example.demo2.entities;

import java.util.Objects;

/**
 * Business entity representing a song/audio file.
 */
public class Song {
    private Integer id; // null until persisted
    private String title;
    private String artist;
    private int durationSeconds; // 0 if unknown
    private String filePath; // absolute path or URL

    public Song(Integer id, String title, String artist, int durationSeconds, String filePath) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.durationSeconds = Math.max(0, durationSeconds);
        this.filePath = filePath;
    }

    public Song(String title, String artist, int durationSeconds, String filePath) {
        this(null, title, artist, durationSeconds, filePath);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = Math.max(0, durationSeconds); }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getDurationDisplay() {
        int s = durationSeconds % 60;
        int m = (durationSeconds / 60) % 60;
        int h = durationSeconds / 3600;
        if (h > 0) {
            return String.format("%d:%02d:%02d", h, m, s);
        }
        return String.format("%d:%02d", m, s);
    }

    @Override
    public String toString() {
        return title + " — " + artist;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Song song)) return false;
        return Objects.equals(id, song.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
