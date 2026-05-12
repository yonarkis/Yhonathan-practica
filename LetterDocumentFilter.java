package Vista;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class LetterDocumentFilter extends DocumentFilter {

    private final int maxLength;

    public LetterDocumentFilter(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (string == null) {
            return;
        }
        String normalized = string.toUpperCase();
        String newValue = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()))
                .insert(offset, normalized)
                .toString();
        if (isValid(newValue)) {
            super.insertString(fb, offset, normalized, attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        String current = fb.getDocument().getText(0, fb.getDocument().getLength());
        String normalized = text == null ? "" : text.toUpperCase();
        String newValue = new StringBuilder(current).replace(offset, offset + length, normalized).toString();
        if (isValid(newValue)) {
            super.replace(fb, offset, length, normalized, attrs);
        }
    }

    private boolean isValid(String value) {
        return value.matches("[A-ZÁÉÍÓÚÑ\\s]*") && value.length() <= maxLength;
    }
}
