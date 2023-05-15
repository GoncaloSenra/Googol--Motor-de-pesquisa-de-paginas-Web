package forms;

public class Tokens {

    private String words;

    public Tokens() {
        this.words = "";
    }

    public String getWords() {
        return words;
    }

    public void setWords(String words) {
        this.words = words;
    }

    @Override
    public String toString() {
        return "Tokens{" +
                "words='" + words + '\'' +
                '}';
    }
}
