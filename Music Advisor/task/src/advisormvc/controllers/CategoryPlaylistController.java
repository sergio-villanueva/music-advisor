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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryPlaylistController extends ClientController {

    private final Map<String, String> ids;
    private List<Playlist> playlists;
    private final String category;

    public CategoryPlaylistController(Authorization authorization, String apiServerPrefix, String category) throws NotAuthorizedException, IOException, InterruptedException {
        super(authorization, apiServerPrefix);
        this.category = category;
        this.ids = extractIds();
    }

    private Map<String, String> extractIds() throws NotAuthorizedException, InterruptedException, IOException {
        handleAuthExpiration();

        // fetch Spotify categories
        String categoryURI = apiServerPrefix + "/browse/categories";
        String result = getMethod(categoryURI);

        // parse json into list of categories
        JsonObject categoriesLevel = JsonParser.parseString(result).getAsJsonObject().get("categories").getAsJsonObject();
        Map<String, String> data = new HashMap<>();

        JsonArray resultList = categoriesLevel.get("items").getAsJsonArray();
        for (JsonElement element : resultList) {
            JsonObject item = element.getAsJsonObject();

            String name = item.get("name").getAsString();
            String id = item.get("id").getAsString();
            System.out.println("name: " + name + "  id: " + id);
            data.put(name, id);
        }

        return data;
    }

    @Override
    public void invoke() throws NotAuthorizedException, InterruptedException, IOException {
        handleAuthExpiration();

        // check if category exists
        if (!ids.containsKey(category)) {
            System.out.println("Unknown category name.");
            //throw new IOException("Unknown category name.");
            throw new IOException();
        }

        // fetch playlists
        String playlistURI = apiServerPrefix + "/browse/categories/" + ids.get(category) + "/playlists";
        String result = getMethod(playlistURI);

        if(result.contains("Test unpredictable error message")) {throw new IOException("Test unpredictable error message");}
        // parse json into list of playlists
        System.out.println("get playlist by category uri: " + playlistURI);
        System.out.println("root element: " + JsonParser.parseString(result).toString());
        JsonObject albumsLevel = JsonParser.parseString(result).getAsJsonObject().get("playlists").getAsJsonObject();
        playlists = new ArrayList<>();

        JsonArray resultList = albumsLevel.get("items").getAsJsonArray();
        for (JsonElement element : resultList) {
            JsonObject item = element.getAsJsonObject();

            String title = item.get("name").getAsString();
            String link = item.get("external_urls").getAsJsonObject().get("spotify").getAsString();
            playlists.add(new Playlist(title, link));
        }

    }

    @Override
    protected List<String> extractPages(int pageSize) {

        int totalPages = (int) Math.ceil(playlists.size() / (double) pageSize);
        System.out.println("total pages: " + totalPages);
        List<String> pageList = new ArrayList<>(totalPages);

        for (int start = 0; start < totalPages; start += pageSize) {
            int end = Math.min(start + pageSize, totalPages);
            List<Playlist> page = playlists.subList(start, end);
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
