package com.droidheat.musicplayer;

class SongModel {

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

    void setTitle(String a) {
        this.Title = a;
    }

    void setArtist(String a) {
        this.Artist = a;
    }

    void setPath(String Url) {
        this.Path = Url;
    }

    void setDuration(String Duration) {
        this.Duration = Duration;
    }

    void setFileName(String a) {
        this.Name = a;
    }

    void setAlbumID(String albumid) {
        this.AlbumID = albumid;
    }

    void setAlbum(String album) {
        this.Album = album;
    }

    /**
     * ******** Get Methods ***************
     */

    String getTitle() {
        return this.Title;
    }

    String getDuration() {
        return this.Duration;
    }

    String getArtist() {
        return this.Artist;
    }

    String getPath() {
        return this.Path;
    }

    String getFileName() {
        return this.Name;
    }

    String getAlbum() {
        return this.Album;
    }

    String getAlbumID() {
        return this.AlbumID;
    }

}