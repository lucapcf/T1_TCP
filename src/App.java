import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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

    // Construtor do App
    public App() {
        // Inicializa o Player, Parser e Editor
        player = new Player();
        parser = new Parser(this, player, player.getVolume());
        editor = new Editor();

        // Configuração da janela principal da aplicação
        JFrame frame = new JFrame("App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Inicialização e configuração do botão de parar
        stopButton = new JButton("Stop");
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stop();
            }
        });

        // Inicialização e configuração do botão de reproduzir
        playButton = new JButton("Play");
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playPause();
            }
        });

        // Configuração do controle de volume
        JLabel volumelabel = new JLabel("Volume:");
        JSlider volumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, player.getVolume());
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
        
        // Configuração de layout e adição de componentes à janela do app
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        Box horizontalBox = Box.createHorizontalBox();
        horizontalBox.add(stopButton);
        horizontalBox.add(Box.createHorizontalStrut(10));
        horizontalBox.add(playButton);
        horizontalBox.add(Box.createHorizontalStrut(30));
        horizontalBox.add(volumelabel);
        horizontalBox.add(volumeSlider);
        bottomPanel.add(horizontalBox);

        // Configuração do menu de arquivos e adição à janela
        // menu de arquivos e deixando tudo visível no app
        JMenuBar menubar = new JMenuBar();
        JMenu filemenu = new JMenu("File");
        JMenuItem openmenu = new JMenuItem("Open");
        JMenuItem savetxtmenu = new JMenuItem("Save .txt");
        JMenuItem savemidimenu = new JMenuItem("Save .midi");


        // Configuração dos itens do menu
        openmenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFile();
            }
        });

        savetxtmenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveTxt();
            }
        });

        savemidimenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.saveMidi(editor);
            }
        });

        // Adição dos itens ao menu
        filemenu.add(openmenu);
        filemenu.add(savetxtmenu);
        filemenu.add(savemidimenu);

        // Adição do menu à barra de menu
        menubar.add(filemenu);

        // Configuração final da janela
        frame.setJMenuBar(menubar);
        frame.getContentPane().add(BorderLayout.CENTER, editor);
        frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);
        frame.setVisible(true);
    }

    // Método para configurar o estado "parado" do player
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

    // Método para configurar o estado "pausado" do player
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

    // Método para parar a reprodução
    public void stop() {
        setStopped(true);
    }

    // Método para alternar entre reproduzir e pausar
    private void playPause() {
        setStopped(false);
        setPaused(!this.paused);
    }

    // Método para atualizar a posição do editor
    public void updateEditorPosition(int position) {
        editor.highlightCharAt(position);
    }

    public void updateVolume() {
    }

    public void openFile() {
        // Cria seletor de arquivos
        JFileChooser fileChooser = new JFileChooser();

        // Abre no diretório atual
        fileChooser.setCurrentDirectory(new File("."));

        // Abre somente arquivos .txt
        FileNameExtensionFilter formato = new FileNameExtensionFilter("Arquivos de texto", "txt");
        fileChooser.setFileFilter(formato);

        // Exibe a janela para abrir arquivo e armazena a resposta do usuário
        int response = fileChooser.showOpenDialog(null);

        // Verificando se o usuário escolheu um arquivo para abrir
        if (response == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                // Lê o conteúdo do arquivo
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);

                // Cria stringBuilder
                StringBuilder content = new StringBuilder();

                String line;
                // Lê cada linha do arquivo e a adiciona ao StringBuilder
                while ((line = bufferedReader.readLine()) != null) {
                    content.append(line).append("\n");
                }

                // Define o texto do editor com o conteúdo lido
                editor.setText(content.toString());

                // Fecha os leitores
                bufferedReader.close();
                fileReader.close();

            } catch (IOException e) {
                // Impre a pilha de exceção em caso de erro
                e.printStackTrace();
            }
        }
    }

    public void saveTxt() {
        // Cria um seletor de arquivos
        JFileChooser fileChooser = new JFileChooser();

        // Abre no diretório atual
        fileChooser.setCurrentDirectory(new File("."));

        // Exibe a janela para salvar arquivo e armazena a resposta do usuário
        int response = fileChooser.showSaveDialog(null);

        // Verificando se o usuário escolheu um arquivo para salvar
        if (response == JFileChooser.APPROVE_OPTION) {
            try {
                // Obtendo o arquivo selecionado
                File file = fileChooser.getSelectedFile();

                // Adiciona a extensão .txt ao arquivo se não estiver presente
                if (!file.getName().endsWith(".txt")) {
                    file = new File(file.getAbsolutePath() + ".txt");
                }
                
                // Cria writer para gravar no arquivo
                FileWriter writer = new FileWriter(file);

                // Escre o texto do editor no arquivo
                writer.write(editor.getText());

                // Fechando o escritor
                writer.close();

            } catch (IOException e) {
                // Imprimindo a pilha de exceção em caso de erro
                e.printStackTrace();
            }
        }
    }
}