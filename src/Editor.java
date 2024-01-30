import javax.swing.JTextArea;

public class Editor extends JTextArea {
    public void highlightCharAt(int position) {
        requestFocusInWindow();
        if (getText().length() >= position) {
            setSelectionStart(position);
            setSelectionEnd(position + 1);
        }
    }
}
