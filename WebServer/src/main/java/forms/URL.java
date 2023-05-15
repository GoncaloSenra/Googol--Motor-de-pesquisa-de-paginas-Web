package forms;

public class URL {

    private String link;

    public URL() {
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return "URL{" +
                "link='" + link + '\'' +
                '}';
    }
}
