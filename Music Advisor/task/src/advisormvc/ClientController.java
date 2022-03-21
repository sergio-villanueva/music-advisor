package advisormvc;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public abstract class ClientController {
    protected final Authorization authorization;
    protected final String apiServerPrefix;

    public ClientController(Authorization authorization, String apiServerPrefix) {
        this.authorization = authorization;
        this.apiServerPrefix = apiServerPrefix;
    }

    protected void handleAuthExpiration() throws InterruptedException, NotAuthorizedException {
        if (authorization.isExpired()) {
            boolean success = false;
            for (int i = 0; i < 5 && !success; i++) {
                success = authorization.refresh();
            }
            if (!success) {
                throw new InterruptedException("Error: Connection Failed");
            }
        }
    }

    public abstract void invoke() throws NotAuthorizedException, InterruptedException, IOException;

    protected String getMethod(String uri) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        String value = authorization.getTokenType() + " " + authorization.getAccessToken();
        // set up request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Authorization", value)
                .GET()
                .timeout(Duration.ofMillis(10000L))
                .build();

        // send request and retrieve response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Display HTTP Response Error
        if (response.statusCode() >= 300) {
            System.out.println(response.body());
        }
        return response.body();
    }

    protected abstract List<String> extractPages(int pageSize);

}
