import javax.swing.JTextArea;

public class Editor extends JTextArea {
    public void highlight(int start, int end) {
        requestFocusInWindow();
        if (start < 0) {
            start = 0;
        }
        if (end > getText().length()) {
            end = getText().length();
        }
        setSelectionStart(start);
        setSelectionEnd(end);
    }
}
