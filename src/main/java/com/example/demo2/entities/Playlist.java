package com.example.demo2.entities;

import java.util.Objects;

/**
 * Business entity representing a playlist.
 */
public class Playlist {
    private Integer id; // null until persisted
    private String name;

    public Playlist(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Playlist(String name) {
        this(null, name);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() { return name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Playlist that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
