package advisormvc.controllers;

import advisormvc.Authorization;
import advisormvc.ClientController;
import advisormvc.NotAuthorizedException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FeaturedPlaylistController extends ClientController {

    private List<Playlist> featured;

    public FeaturedPlaylistController(Authorization authorization, String apiServerPrefix) {
        super(authorization, apiServerPrefix);
    }

    @Override
    public void invoke() throws NotAuthorizedException, InterruptedException, IOException {
        // renew the access token if necessary
        handleAuthExpiration();

        // fetch featured playlists
        String playlistURI = apiServerPrefix + "/browse/featured-playlists";
        String result = getMethod(playlistURI);

        // parse json into list of playlist
        System.out.println("root element: " + JsonParser.parseString(result).toString());
        JsonObject albumsLevel = JsonParser.parseString(result).getAsJsonObject().get("playlists").getAsJsonObject();
        featured = new ArrayList<>();

        JsonArray resultList = albumsLevel.get("items").getAsJsonArray();
        for (JsonElement element : resultList) {
            JsonObject item = element.getAsJsonObject();

            String title = item.get("name").getAsString();
            String link = item.get("external_urls").getAsJsonObject().get("spotify").getAsString();
            featured.add(new Playlist(title, link));
        }

    }

    @Override
    protected List<String> extractPages(int pageSize) {

        int totalPages = (int) Math.ceil(featured.size() / (double) pageSize);
        System.out.println("total pages: " + totalPages);
        List<String> pageList = new ArrayList<>(totalPages);

        for (int start = 0; start < totalPages; start += pageSize) {
            int end = Math.min(start + pageSize, totalPages);
            List<Playlist> page = featured.subList(start, end);
            StringBuilder pageString = new StringBuilder();

            for (Playlist playlist : page) {
                pageString.append(playlist.getName()).append("\n");
                pageString.append(playlist.getSpotifyLink()).append("\n\n");
            }
            pageList.add(pageString.toString());
        }

        return pageList;
    }


}
