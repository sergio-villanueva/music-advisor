package advisor;


import advisormvc.AdvisorView;
import advisormvc.Authorization;
import advisormvc.NotAuthorizedException;
import advisormvc.controllers.CategoryController;
import advisormvc.controllers.CategoryPlaylistController;
import advisormvc.controllers.FeaturedPlaylistController;
import advisormvc.controllers.NewAlbumsController;

import java.io.IOException;

public class MusicAdvisor {

    private boolean permissible;
    private final String apiSeverPrefix;
    private final Authorization authorization;
    private final int pageSize;

    public MusicAdvisor(String authServerPrefix, String apiSeverPrefix, int pageSize) {
        this.authorization = new Authorization(authServerPrefix);
        this.apiSeverPrefix = apiSeverPrefix;
        this.permissible = false;
        this.pageSize = pageSize;
    }


    public void auth() throws InterruptedException {
        if (isPermissible()) {
            System.out.println("User already authorized");
            return;
        }
        // receive consent from the resource owner
        boolean authSuccess = authorization.consent();

        // exchange auth code for access token
        boolean exchangeSuccess = authorization.exchange();

        if (exchangeSuccess) {
            setPermissible(true);
        } else {
            setPermissible(false);
            throw new InterruptedException();
        }

    }

    public AdvisorView featured() throws NotAuthorizedException, IOException, InterruptedException {
        // check for authorization
        if (!isPermissible()) {
            throw new NotAuthorizedException();
        }

        return new AdvisorView(new FeaturedPlaylistController(authorization, apiSeverPrefix), pageSize);
    }

    public AdvisorView newAlbums() throws NotAuthorizedException, IOException, InterruptedException {
        // check for authorization
        if (!isPermissible()) {
            throw new NotAuthorizedException();
        }

        return new AdvisorView(new NewAlbumsController(authorization, apiSeverPrefix), pageSize);
    }

    public AdvisorView getCategories() throws NotAuthorizedException, IOException, InterruptedException {
        // check for authorization
        if (!isPermissible()) {
            throw new NotAuthorizedException();
        }

        return new AdvisorView(new CategoryController(authorization, apiSeverPrefix), pageSize);
    }

    public AdvisorView getPlaylists(String category) throws NotAuthorizedException, IOException, InterruptedException {
        // check for authorization
        if (!isPermissible()) {
            throw new NotAuthorizedException();
        }

        return new AdvisorView(new CategoryPlaylistController(authorization, apiSeverPrefix, category), pageSize);
    }


    private void setPermissible(boolean state) {
        permissible = state;
    }

    private boolean isPermissible() {
        return permissible;
    }



}
