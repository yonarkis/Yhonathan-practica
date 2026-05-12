package Vista;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class AlphanumericDocumentFilter extends DocumentFilter {

    private final int maxLength;

    public AlphanumericDocumentFilter(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        replace(fb, offset, 0, string, attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        String current = fb.getDocument().getText(0, fb.getDocument().getLength());
        String next = current.substring(0, offset) + (text == null ? "" : text) + current.substring(offset + length);
        if (next.length() <= maxLength && next.matches("[A-Za-z0-9._-]*")) {
            fb.replace(offset, length, text, attrs);
        }
    }
}
