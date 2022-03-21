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

public class CategoryController extends ClientController {

    private List<String> categories;

    public CategoryController(Authorization authorization, String apiServerPrefix) {
        super(authorization, apiServerPrefix);
    }

    @Override
    public void invoke() throws NotAuthorizedException, InterruptedException, IOException {

        handleAuthExpiration();

        // fetch Spotify categories
        String categoryURI = apiServerPrefix + "/browse/categories";
        String result = getMethod(categoryURI);

        // parse json into list of categories
        JsonObject categoriesLevel = JsonParser.parseString(result).getAsJsonObject().get("categories").getAsJsonObject();
        categories = new ArrayList<>();

        JsonArray resultList = categoriesLevel.get("items").getAsJsonArray();
        for (JsonElement element : resultList) {
            JsonObject item = element.getAsJsonObject();

            String name = item.get("name").getAsString();
            categories.add(name);

        }

    }

    @Override
    protected List<String> extractPages(int pageSize) {

        int totalPages = (int) Math.ceil(categories.size() / (double) pageSize);
        System.out.println("total pages: " + totalPages);
        List<String> pageList = new ArrayList<>(totalPages);

        for (int start = 0; start < totalPages; start += pageSize) {
            int end = Math.min(start + pageSize, totalPages);
            List<String> page = categories.subList(start, end);
            StringBuilder pageString = new StringBuilder();

            for (String category : page) {
                pageString.append(category).append("\n\n");
            }
            pageList.add(pageString.toString());
        }

        return pageList;
    }

}
