package x.funny.co.model;

public class Difference {
    private final DifferenceType type;
    private String text;

    private Difference(DifferenceType type, String text) {
        this.type = type;
        this.text = text;
    }

    public static Difference of(DifferenceType type, String text) {
        return new Difference(type, text);
    }

    public DifferenceType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public void appendText(String substr) {
        this.text += substr;
    }

    public boolean isEmpty() {
        return this.text.isEmpty();
    }

    public void setText(String text) {
        this.text = text;
    }

    public int length() {
        return this.text.length();
    }

    public String substring(int begging, int end) {
        return this.text.substring(begging, end);
    }

    public String substring(int begging) {
        return this.text.substring(begging);
    }

    public boolean endsWith(Difference diff) {
        return this.text.endsWith(diff.text);
    }

    public boolean startsWith(Difference diff) {
        return this.text.startsWith(diff.text);
    }
}
