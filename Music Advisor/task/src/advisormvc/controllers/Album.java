package advisormvc.controllers;

class Album {
    private final String artist;
    private final String title;
    private final String spotifyLink;

    Album(String artist, String title, String spotifyLink) {
        this.artist = artist;
        this.title = title;
        this.spotifyLink = spotifyLink;
    }

    String getArtist() {
        return artist;
    }

    String getTitle() {
        return title;
    }

    String getSpotifyLink() {
        return spotifyLink;
    }
}
