package Vista;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class DecimalDocumentFilter extends DocumentFilter {

    private final int maxLength;
    private final Double maxValue;

    public DecimalDocumentFilter(int maxLength) {
        this(maxLength, null);
    }

    public DecimalDocumentFilter(int maxLength, Double maxValue) {
        this.maxLength = maxLength;
        this.maxValue = maxValue;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        replace(fb, offset, 0, string, attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        String current = fb.getDocument().getText(0, fb.getDocument().getLength());
        String next = current.substring(0, offset) + (text == null ? "" : text) + current.substring(offset + length);
        if (!next.isEmpty() && maxValue != null && next.matches("\\d{1,2}(\\.\\d{0,1})?")) {
            double valor = Double.parseDouble(next);
            if (valor > maxValue) {
                return;
            }
        }
        if (next.length() <= maxLength && next.matches("\\d{0,2}(\\.\\d{0,1})?")) {
            fb.replace(offset, length, text, attrs);
        }
    }
}
