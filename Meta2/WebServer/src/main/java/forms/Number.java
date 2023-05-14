package forms;

public class Number {

    private int num;

    public Number() {
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return "Number{" +
                "num=" + num +
                '}';
    }
}
