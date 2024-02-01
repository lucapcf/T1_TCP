import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class App {
    private final ThreadPlayer player;

    private final Editor editor;
    private final JButton playButton;
    private final JSlider volumeSlider;

    private boolean stopped = true;
    private boolean paused = true;

    public static void main(String args[]) {
        App app = new App();
    }

    public App() {
        player = new ThreadPlayer(this);

        JFrame frame = new JFrame("App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JMenuBar menubar = new JMenuBar();
        JMenu fileMenu = new JMenu("Arquivo");

        JMenuItem openMenuItem = new JMenuItem("Abrir");
        openMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFile();
            }
        });

        JMenuItem saveTxtMenuItem = new JMenuItem("Salvar como .txt");
        saveTxtMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveTxt();
            }
        });

        JMenuItem saveMidiMenuItem = new JMenuItem("Salvar como .midi");
        saveMidiMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveMidi();
            }
        });

        fileMenu.add(openMenuItem);
        fileMenu.add(saveTxtMenuItem);
        fileMenu.add(saveMidiMenuItem);

        menubar.add(fileMenu);

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

        volumeSlider = new JSlider(JSlider.VERTICAL, 0, 127, player.getVolume());
        volumeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                if (!source.getValueIsAdjusting()) {
                    int volume = source.getValue();
                    player.setVolume(volume);
                }
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(stopButton);
        bottomPanel.add(playButton);

        frame.setJMenuBar(menubar);

        frame.getContentPane().add(BorderLayout.CENTER, editor);
        frame.getContentPane().add(BorderLayout.EAST, volumeSlider);
        frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);

        frame.setVisible(true);
    }

    private void setStopped(boolean stopped) {
        if (stopped) {
            player.reset(null);
            setPaused(true);
        }
        editor.setEditable(stopped);
        if (!stopped && this.stopped) {
            player.reset(editor.getText());
        }
        this.stopped = stopped;
    }

    private void setPaused(boolean paused) {
        if (paused) {
            playButton.setText("Play");
            player.pause();
        } else {
            playButton.setText("Pause");
            player.play();
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

    // TODO: executar atualizações de interface na thread principal.

    public void updatePosition(int start, int end) {
        editor.highlight(start, end);
    }

    public void updateVolume(int volume) {
        if (volumeSlider.getValue() != volume) {
            volumeSlider.setValue(volume);
        }
    }

    public void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));

        FileNameExtensionFilter fmt = new FileNameExtensionFilter("Arquivos de texto", "txt");
        fileChooser.setFileFilter(fmt);

        int response = fileChooser.showOpenDialog(null);
        if (response == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                editor.setText(sb.toString());
                bufferedReader.close();
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveTxt() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));

        FileNameExtensionFilter fmt = new FileNameExtensionFilter("Arquivos de texto", "txt");
        fileChooser.setFileFilter(fmt);

        int response = fileChooser.showSaveDialog(null);
        if (response == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().endsWith(".txt")) {
                    file = new File(file.getAbsolutePath() + ".txt");
                }
                FileWriter writer = new FileWriter(file);
                writer.write(editor.getText());
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveMidi() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));

        FileNameExtensionFilter fmt = new FileNameExtensionFilter("Arquivos MIDI", "midi");
        fileChooser.setFileFilter(fmt);

        int response = fileChooser.showSaveDialog(null);
        if (response == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".midi")) {
                file = new File(file.getAbsolutePath() + ".midi");
            }
            MidiWriter writer = new MidiWriter();
            writer.writeFile(editor.getText(), file);
        }
    }
}
