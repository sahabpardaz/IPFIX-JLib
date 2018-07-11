package ir.sahab.ipfix;

public class TemplateNotDefinedException extends IllegalArgumentException {

    public TemplateNotDefinedException() {
        super();
    }

    public TemplateNotDefinedException(String message) {
        super(message);
    }
}
