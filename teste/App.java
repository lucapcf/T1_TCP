import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.BufferedReader;
//Emerson mexendo
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class App {
    private final Player player;
    private final Parser parser;

    private final Editor editor;
    private final JButton playButton;
    private final JButton stopButton;

    private boolean stopped = true;
    private boolean paused = true;

    public static void main(String args[]) {
        App app = new App();
    }

    public App() {
        player = new Player();
        parser = new Parser(this, player, player.getVolume());

        JFrame frame = new JFrame("App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600); 

        editor = new Editor();

        stopButton = new JButton("Stop");
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

        //menu de arquivos e deixando tudo visivel no app
        JMenuBar menubar = new JMenuBar();
        JMenu filemenu = new JMenu("File");
        JMenuItem openmenu = new JMenuItem("Open");
        JMenuItem savetxtmenu = new JMenuItem("Save .txt");
        JMenuItem savemidimenu = new JMenuItem("Save .midi");

        openmenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openfile();
            }
        });

        savetxtmenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                savetxt();
            }
        });

        savemidimenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                savemidi();
            }
        });

        filemenu.add(openmenu);
        filemenu.add(savetxtmenu);
        filemenu.add(savemidimenu);
        menubar.add(filemenu);

        frame.setJMenuBar(menubar);
        frame.getContentPane().add(BorderLayout.CENTER, editor);
        frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);
        frame.setVisible(true);
    }

    private void setStopped(boolean stopped) {
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

    private void setPaused(boolean paused) {
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
        editor.highlightCharAt(position);
    }

    public void updateVolume() {
    }

//tentando Fazer o open file 
    public void openfile() {
		JFileChooser fileChooser = new JFileChooser();
        //coloquei para abrir direto no proprio diretorio
		fileChooser.setCurrentDirectory(new File(".")); 

        //para pegar somente .txt(Não sei se sera permitido outros arquivos de texto)
        FileNameExtensionFilter formato = new FileNameExtensionFilter("Arquivos de texto", "txt");
        fileChooser.setFileFilter(formato);
		int response = fileChooser.showOpenDialog(null);
			
		if(response == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
            try {
                //ler o conteudo do arquivo
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);

                StringBuilder content = new StringBuilder();
                String line;
                //ler cada linha do arquivo e adicionala ao string builder
                while ((line = bufferedReader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                editor.setText(content.toString());
                bufferedReader.close();
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
		}
		
	}
    
//tentando fazer o save file
    public void savetxt() {
		JFileChooser fileChooser = new JFileChooser();
        //coloquei para abrir direto no proprio diretorio
		fileChooser.setCurrentDirectory(new File(".")); 
		int response = fileChooser.showSaveDialog(null);
			
		if(response == JFileChooser.APPROVE_OPTION){
            try {
                File file = fileChooser.getSelectedFile();
                //adicionando .txt ao final do arquivo para salvar como texto
                if (!file.getName().endsWith(".txt")){
                    file = new File(file.getAbsolutePath() + ".txt");
                }
                //
                FileWriter writer = new FileWriter(file);
                writer.write(editor.getText());
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

     public void savemidi() {
		JFileChooser fileChooser = new JFileChooser();
        //coloquei para abrir direto no proprio diretorio
		fileChooser.setCurrentDirectory(new File(".")); 
		int response = fileChooser.showSaveDialog(null);
			
		if(response == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            //adicionando .midi 
            if (!file.getName().endsWith(".midi")){
                file = new File(file.getAbsolutePath() + ".midi");
            }
            //Salvando o arquivo midi chamando a função feita em player
        }
    }

}
