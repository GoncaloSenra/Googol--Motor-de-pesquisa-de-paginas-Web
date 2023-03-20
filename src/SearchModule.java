import java.io.IOException;
import java.util.StringTokenizer;

public class SearchModule extends Thread {

    public static void main(String[] args) {
        SearchModule s = new SearchModule();
        s.start();
    }

    public void run() {
        ServerUrlList UrlList = new ServerUrlList();
        UrlList.start();


    }



}
