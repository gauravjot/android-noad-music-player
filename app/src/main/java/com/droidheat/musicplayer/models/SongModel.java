package com.droidheat.musicplayer.models;

public class SongModel {

    private String Title, Album, Artist, Duration, Path, Name, AlbumID;

    /**
     * ******** Set Methods *****************
     */



    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SongModel)) {
            return false;
        }

        SongModel that = (SongModel) other;

        // Custom equality check here.
        return this.getAlbum().equals(that.getAlbum())
                && this.getAlbumID().equals(that.getAlbumID())
                && this.getArtist().equals(that.getArtist())
                && this.getDuration().equals(that.getDuration())
                && this.getFileName().equals(that.getFileName())
                && this.getPath().equals(that.getPath())
                && this.getTitle().equals(that.getTitle());
    }

    public void setTitle(String a) {
        this.Title = a;
    }

    public void setArtist(String a) {
        this.Artist = a;
    }

    public void setPath(String Url) {
        this.Path = Url;
    }

    public void setDuration(String Duration) {
        this.Duration = Duration;
    }

    public void setFileName(String a) {
        this.Name = a;
    }

    public void setAlbumID(String albumid) {
        this.AlbumID = albumid;
    }

    public void setAlbum(String album) {
        this.Album = album;
    }

    /**
     * ******** Get Methods ***************
     */

    public String getTitle() {
        return this.Title;
    }

    public String getDuration() {
        return this.Duration;
    }

    public String getArtist() {
        return this.Artist;
    }

    public String getPath() {
        return this.Path;
    }

    public String getFileName() {
        return this.Name;
    }

    public String getAlbum() {
        return this.Album;
    }

    public String getAlbumID() {
        return this.AlbumID;
    }

}