import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class App {
    Player player;
    Parser parser;

    Editor editor;
    JButton playButton;
    Boolean stopped = true;
    Boolean paused = true;

    public static void main(String args[]) {
        App app = new App();
    }

    public App() {
        player = new Player();
        parser = new Parser(this, player);

        JFrame frame = new JFrame("App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        editor = new Editor();

        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stop();
            }
        });

        playButton = new JButton("Play");
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playPause();
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(stopButton);
        bottomPanel.add(playButton);

        frame.getContentPane().add(BorderLayout.CENTER, editor);
        frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);

        frame.setVisible(true);
    }

    private void setStopped(Boolean stopped) {
        if (stopped) {
            parser.reset();
            setPaused(true);
        }
        editor.setEditable(stopped);
        if (!stopped && this.stopped) {
            parser.setTextAndReset(editor.getText());
        }
        this.stopped = stopped;
    }

    private void setPaused(Boolean paused) {
        if (paused) {
            playButton.setText("Play");
            parser.pause();
        } else {
            playButton.setText("Pause");
            parser.play();
        }
        this.paused = paused;
    }

    public void stop() {
        setStopped(true);
    }

    private void playPause() {
        setStopped(false);
        setPaused(!this.paused);
    }

    public void updateEditorPosition(int position) {
        editor.requestFocusInWindow();
        editor.setCaretPosition(position);
    }

    public void updateVolume() {
    }
}
