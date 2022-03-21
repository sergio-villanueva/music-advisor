package advisormvc.controllers;

class Playlist {

    private final String name;
    private final String spotifyLink;

    Playlist(String name, String spotifyLink) {
        this.name = name;
        this.spotifyLink = spotifyLink;
    }

    String getName() {
        return name;
    }

    String getSpotifyLink() {
        return spotifyLink;
    }
}
