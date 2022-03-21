package advisormvc;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

public class Authorization {

    private final String authServerPrefix;
    //private final int authPort;
    private boolean consented;
    private String code;
    private Token token;
    private long startTime;

    public Authorization(String authServerPrefix) {
        this.authServerPrefix = authServerPrefix;
        this.consented = false;
        this.code = null;
        this.token = null;
        this.startTime = 0;
    }

    public void setCode(String code) {
        this.code = code;
    }

    private boolean isConsented() {
        return this.consented;
    }

    public boolean isAccessible() {
        return token != null && !isExpired();
    }

    public boolean isExpired() {
        if (token != null) {
            long duration = token.getExpiresIn();
            return startTime + duration < System.nanoTime();
        } else {
            throw new RuntimeException("exchange() was never called");
        }
    }

    private static Token toToken(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Token.class);
    }

    private void printAuthLink() {
        String authLink = authServerPrefix + "/authorize?" + "client_id=" + Responses.CLIENT_ID +
                "&redirect_uri=" + Responses.REDIRECT_URI + //authPort +
                "&response_type=" + Responses.RESPONSE_TYPE;

        System.out.println("use this link to request the access code:");
        System.out.println(URI.create(authLink));
    }

    public boolean consent() {
        if (isConsented()) { return true; }

        HttpServer server;
        try {

            printAuthLink();
            server = HttpServer.create();
            server.bind(new InetSocketAddress(9090), 0);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("exception caught");
            return false;
        }

        server.createContext("/",
                new HttpHandler() {
                    @Override
                    public void handle(HttpExchange httpExchange) throws IOException {

                        String query = httpExchange.getRequestURI().getQuery();

                        String result;
                        if (query != null && query.contains("code")) {
                            setCode(extractQueryParam("code", query));
                            result = Responses.CODE_FOUND;
                        } else {
                            result = Responses.CODE_NOT_FOUND;
                        }


                        httpExchange.sendResponseHeaders(200, result.length());
                        httpExchange.getResponseBody().write(result.getBytes());
                        httpExchange.getResponseBody().close();

                    }
                });


        server.start();


        System.out.println("waiting for code...");



        while (code == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }

        }


        server.stop(0);
        // check if code retrieval was successful
        consented = true;//code != null;

        return true;
    }


    private String extractQueryParam(String key, String query) {
        String[] parameters = query.split("[=&]");
        for (int i = 0; i < parameters.length; i++) {
            if (key.equals(parameters[i])) {
                return parameters[i+1];
            }
        }

        return null;
    }

    public boolean exchange() {

        // consent not granted
        if (!isConsented()) { throw new RuntimeException("consent was not acquired"); }
        // token already exists AND is not expired
        if (isAccessible()) { return true; }

        // exchange auth code for access token
        try {
            token = newToken(code, false);
        } catch (IOException | InterruptedException e) { return false; }

        return true;
    }

    private Token newToken(String code, boolean refresh) throws IOException, InterruptedException {
        String requestBody;

        if (refresh) {
            requestBody = "grant_type=" + Responses.GRANT_TYPE +
                    "&refresh_token=" + code;
        } else {
            requestBody = "grant_type=" + Responses.GRANT_TYPE +
                    "&code=" + code +
                    "&redirect_uri=" + Responses.REDIRECT_URI;
        }

        String jsonResult = postMethod(requestBody);
        Token newToken = toToken(jsonResult);
        startTime = System.nanoTime();

        return newToken;
    }

    private String postMethod(String requestBody) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        var encoder = Base64.getEncoder();
        String keys = Responses.CLIENT_ID + ":" + Responses.CLIENT_SECRET;
        String encodedKeys = encoder.encodeToString(keys.getBytes());

        // set up request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(authServerPrefix + "/api/token"))
                .headers("Authorization", "Basic " + encodedKeys,
                        "Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofMillis(10000L))
                .build();

        // send request and retrieve response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // handle status code
        /* StatusCode statusCode = new StatusCode(response.statusCode());
        if (!statusCode.isSuccessful()) {
            System.out.println("Status Code: " + statusCode.getStatusCode() + " " + statusCode.getStatusDescription());

            throw new IOException("Error: Server Unable To Process Request");
        }
         */
        //System.out.println("Status Code= " + response.statusCode());
        //System.out.println("Printing out the response" + response.body());

        return response.body();
    }

    boolean refresh() throws NotAuthorizedException {
        // consent not granted
        if (!isConsented()) { throw new RuntimeException("consent was not acquired"); }
        // token already exists AND is not expired
        if (isAccessible()) { return true; }

        try {
            token = newToken(token.getRefreshToken(), true);
        } catch (IOException | InterruptedException e) { return false; }

        return true;
    }


    String getTokenType() { return this.token.getTokenType(); }

    String getAccessToken() { return this.token.getAccessToken(); }

    private class Token {
        @SerializedName("access_token")
        private String accessToken;
        @SerializedName("token_type")
        private String tokenType;
        @SerializedName("scope")
        private String scope;
        @SerializedName("expires_in")
        private int expiresIn;
        @SerializedName("refresh_token")
        private String refreshToken;


        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public int getExpiresIn() {
            return expiresIn;
        }

        public void setExpiresIn(int expires_in) {
            this.expiresIn = expiresIn;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refresh_token) {
            this.refreshToken = refreshToken;
        }

    }


}
