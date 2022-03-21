package advisormvc;

import java.io.IOException;
import java.util.List;

public class AdvisorView {
    private final ClientController clientController;
    private int pageSize;
    private final List<String> pageList;
    private int currentPage;

    public AdvisorView(ClientController clientController, int pageSize) throws NotAuthorizedException, IOException, InterruptedException {
        this.clientController = clientController;
        this.pageSize = pageSize;
        clientController.invoke();
        this.pageList = clientController.extractPages(pageSize);
        this.currentPage = 0;
    }

    private boolean isPageValid(int pageNumber) {
        return pageNumber >= 0 && pageNumber < pageList.size();
    }

    public void displayPage() {

        if (!isPageValid(currentPage)) {
            System.out.println("No more pages.");
            return;
        }

        System.out.println(pageList.get(currentPage));
        System.out.println();
        System.out.println("---PAGE " + (currentPage+1) + " OF " + pageList.size() + "---");
    }

    public void displayNextPage() {

        if (!isPageValid(currentPage+1)) {
            System.out.println("No more pages.");
            return;
        }

        currentPage++;
        displayPage();
    }

    public void displayPrevPage() {
        if (!isPageValid(currentPage-1)) {
            System.out.println("No more pages.");
            return;
        }

        currentPage--;
        displayPage();
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public ClientController getClientController() {
        return clientController;
    }

}
