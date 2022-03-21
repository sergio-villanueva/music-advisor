package advisor;

import advisormvc.AdvisorView;
import advisormvc.NotAuthorizedException;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    static final String SPOTIFY_AUTHORIZATION_PREFIX = "https://accounts.spotify.com";
    static final String SPOTIFY_API_PREFIX = "https://api.spotify.com/v1";
    static final int DEFAULT_PAGE_SIZE = 5;

    public static void main(String[] args) {

        String authServerPrefix;
        String apiServerPrefix;
        int pageSize;

        switch (args.length) {
            case 6:
                authServerPrefix = args[0].equals("-access") ? args[1] : SPOTIFY_AUTHORIZATION_PREFIX;
                apiServerPrefix = args[2].equals("-resource") ? args[3] + "/v1" : SPOTIFY_API_PREFIX;
                pageSize = args[4].equals("-page") ? Integer.parseInt(args[5]) : DEFAULT_PAGE_SIZE;
                break;
            case 4:
                authServerPrefix = args[0].equals("-access") ? args[1] : SPOTIFY_AUTHORIZATION_PREFIX;
                apiServerPrefix = args[2].equals("-resource") ? args[3] + "/v1" : SPOTIFY_API_PREFIX;
                pageSize = DEFAULT_PAGE_SIZE;
                break;
            case 2:
                authServerPrefix = args[0].equals("-access") ? args[1] : SPOTIFY_AUTHORIZATION_PREFIX;
                apiServerPrefix = SPOTIFY_API_PREFIX;
                pageSize = DEFAULT_PAGE_SIZE;
                break;
            default:
                authServerPrefix = SPOTIFY_AUTHORIZATION_PREFIX;
                apiServerPrefix = SPOTIFY_API_PREFIX;
                pageSize = DEFAULT_PAGE_SIZE;
        }

        MusicAdvisor advisor = new MusicAdvisor(authServerPrefix, apiServerPrefix, pageSize);

        //boolean exitSystem = false;

        try (Scanner scanner = new Scanner(System.in)) {
            AdvisorView view = null;
            while (true) {
                //displayMainMenu();
                // fetch user input
                String[] mainOption = scanner.nextLine().trim().split(" ");

                try {
                    switch (mainOption[0]) {
                        case "new":

                            view = advisor.newAlbums();
                            view.displayPage();
                            System.out.println();
                            break;

                        case "featured":

                            view = advisor.featured();
                            view.displayPage();
                            System.out.println();
                            break;

                        case "categories":

                            view = advisor.getCategories();
                            view.displayPage();
                            System.out.println();
                            break;

                        case "playlists":

                            String category;
                            switch (mainOption.length) {
                                case 1:
                                    // no category defined
                                    System.out.println("Error: No Category Defined");
                                    continue;
                                case 2:
                                    // category is one word
                                    category = mainOption[1];
                                    break;
                                default:
                                    // combine category into one String
                                    StringBuilder categoryBuilder = new StringBuilder();

                                    for (int i = 1; i < mainOption.length; i++) {
                                        categoryBuilder.append(" " + mainOption[i]);
                                    }
                                    category = categoryBuilder.toString().trim();
                            }


                            // subject for internalization into MusicAdvisor:
                            System.out.println("---" + category.toUpperCase() + " PLAYLISTS---");

                            view = advisor.getPlaylists(category);
                            view.displayPage();
                            System.out.println();
                            break;

                        case "auth":

                            advisor.auth();
                            System.out.println("---SUCCESS---");
                            break;

                        case "exit":

                            System.out.println("---GOODBYE!---");
                            System.exit(0);
                            break;

                        case "next":
                            if (view == null) {
                                throw new NotAuthorizedException("Invalid Command");
                            }
                            view.displayNextPage();
                            break;

                        case "prev":
                            if (view == null) {
                                throw new NotAuthorizedException("Invalid Command");
                            }
                            view.displayPrevPage();
                            break;

                        default:
                            System.out.println("Invalid Keyword: Try Again");
                    }
                } catch (NotAuthorizedException e) {
                    System.out.println(e.getMessage());

                } catch (IOException | InterruptedException e) {
                    System.out.println(e.getMessage());
                    System.out.println("Connection Fail: Please try again");
                }


            }
        }

    }


    static void displayMainMenu() {
        System.out.println("Enter one of the following options:");
        System.out.println("featured - retrieve the latest playlists from Spotify");
        System.out.println("new - retrieve the newest albums");
        System.out.println("categories - view the many music categories on offer");
        System.out.println("exit - shut down");
        System.out.println();
    }
}
