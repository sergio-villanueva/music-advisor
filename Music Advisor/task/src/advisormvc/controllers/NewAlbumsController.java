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
import java.util.Arrays;
import java.util.List;

public class NewAlbumsController extends ClientController {

    private List<Album> albums;

    public NewAlbumsController(Authorization authorization, String apiServerPrefix) throws NotAuthorizedException, IOException, InterruptedException {
        super(authorization, apiServerPrefix);
    }

    @Override
    public void invoke() throws NotAuthorizedException, InterruptedException, IOException {
        // renew the access token if necessary
        handleAuthExpiration();

        // fetch new albums
        String albumURI = apiServerPrefix + "/browse/new-releases";
        String result = getMethod(albumURI);
        System.out.println("JSON result: " + result);

        // parse json into list of albums
        JsonObject albumsLevel = JsonParser.parseString(result).getAsJsonObject().get("albums").getAsJsonObject();
        albums = new ArrayList<>();

        JsonArray resultList = albumsLevel.get("items").getAsJsonArray();
        for (JsonElement element : resultList) {
            JsonObject item = element.getAsJsonObject();

            String title = item.get("name").getAsString();
            String link = item.get("external_urls").getAsJsonObject().get("spotify").getAsString();

            JsonArray jsonArtistList = item.get("artists").getAsJsonArray();
            String[] artistList = new String[jsonArtistList.size()];
            int i = 0;

            for (JsonElement artist : jsonArtistList) {
                artistList[i] = artist.getAsJsonObject().get("name").getAsString();
                i++;
            }
            albums.add(new Album(Arrays.toString(artistList), title, link));
        }

    }

    @Override
    public List<String> extractPages(int pageSize) {

        int totalPages = (int) Math.ceil(albums.size() / (double) pageSize);
        System.out.println("total pages: " + totalPages);
        List<String> pageList = new ArrayList<>(totalPages);

        for (int start = 0; start < totalPages; start += pageSize) {
            int end = Math.min(start + pageSize, totalPages);
            List<Album> page = albums.subList(start, end);
            StringBuilder pageString = new StringBuilder();

            for (Album album : page) {
                pageString.append(album.getTitle()).append("\n");
                pageString.append(album.getArtist()).append("\n");
                pageString.append(album.getSpotifyLink()).append("\n\n");
            }
            pageList.add(pageString.toString());
        }

        return pageList;
    }

}
