package Vista;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class NumericDocumentFilter extends DocumentFilter {

    private final int maxLength;

    public NumericDocumentFilter(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (string == null) {
            return;
        }
        String newValue = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()))
                .insert(offset, string)
                .toString();
        if (isValid(newValue)) {
            super.insertString(fb, offset, string, attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        String current = fb.getDocument().getText(0, fb.getDocument().getLength());
        String newValue = new StringBuilder(current).replace(offset, offset + length, text == null ? "" : text).toString();
        if (isValid(newValue)) {
            super.replace(fb, offset, length, text, attrs);
        }
    }

    private boolean isValid(String value) {
        return value.matches("\\d*") && value.length() <= maxLength;
    }
}
