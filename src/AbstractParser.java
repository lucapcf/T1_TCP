import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.sound.midi.*;
import javax.swing.JFileChooser;

// Classe Parser para interpretar strings de texto e convertê-las em música MIDI
class AbstractParser {
    // Logger para registrar mensagens para depuração
    private static final Logger LOGGER = Logger.getLogger(
        AbstractParser.class.getName()
    );

    // Constantes para definir a duração das notas e pausas, e configurações de oitava
    private static final int NOTE_DURATION_MS = 500;
    private static final int PAUSE_DURATION_MS = 50;
    private static final int DEFAULT_OCTAVE = 4;
    private static final int MAX_OCTAVE = 9;
    private static final int PPQ = 24;

    // Enumeração para representar instrumentos MIDI com seus respectivos IDs
    private enum Instrument {
        HARPISCHORD(7),
        TUBULAR_BELLS(15),
        CHURCH_ORGAN(20),
        PAN_FLUTE(76),
        AGOGO(114);

        private final int id;

        Instrument(int value) {
            id = value - 1;
        }

        public int getId() {
            return id;
        }
    }

    // Variáveis para controle do Parser
    private String text;
    private int position = 0;

    // Variáveis de controle de execução
    private boolean quit = false;
    private boolean pause = true;
    private boolean paused = true;

    // Classes e variáveis necessárias
    private final App app;
    private final Player player;

    // Variável para controle de volume
    private final int defaultVolume;

    // Variáveis para controle das notas
    private int octave = DEFAULT_OCTAVE;
    private Character lastNote = null;

    // Construtor do Parser
    public AbstractParser(App app, Player player, int defaultVolume, Editor editor) {
        this.app = app;
        this.player = player;
        this.defaultVolume = defaultVolume;

        // Inicia uma nova thread para ler e executar os comandos do texto
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!quit) {
                    playLoop(editor);
                    try {
                        synchronized (AbstractParser.this) {
                            AbstractParser.this.wait();
                        }
                    } catch (InterruptedException e) {}
                }
            }
        }).start();
    }

    // Verifica se um caractere é uma nota musical válida
    private static boolean isNote(char command) {
        return ('A' <= command) && (command <= 'G');
    }

    // Reseta o Parser para o estado inicial
    public void reset() {
        pause();
        lastNote = null;
        position = 0;
        octave = DEFAULT_OCTAVE;
        player.reset();
    }

    // Define o texto a ser interpretado e reseta o Parser
    public void setTextAndReset(String text) {
        reset();
        this.text = text;
    }

    // Inicia um comando de acordo com o caractere fornecido
    private int startCommand(char c) {
        int wait = 0;
        if (isNote(c)) {
            player.noteOn(octave, Character.toString(c));
            wait = NOTE_DURATION_MS;
        } else if (c == ' ') {
            doubleOrResetVolume();
        } else if (c == '!') {
            player.setInstrument(Instrument.AGOGO.getId());
        } else if ("IOUiou".indexOf(c) != -1) {
            player.setInstrument(Instrument.HARPISCHORD.getId());
        } else if (Character.isDigit(c)) {
            int instrument = player.getInstrument();
            instrument += Character.getNumericValue(c);
            instrument %= 128;
            player.setInstrument(instrument);
        } else if (c == '?' || c == '.') {
            int newOctave = octave + 1;
            if (newOctave > MAX_OCTAVE) {
                newOctave = DEFAULT_OCTAVE;
            }
            octave = newOctave;
        } else if (c == '\n') {
            player.setInstrument(Instrument.TUBULAR_BELLS.getId());
        } else if (c == ';') {
            player.setInstrument(Instrument.PAN_FLUTE.getId());
        } else if (c == ',') {
            player.setInstrument(Instrument.CHURCH_ORGAN.getId());
        } else {
            if (lastNote != null) {
                player.noteOn(octave, Character.toString(lastNote));
                wait = NOTE_DURATION_MS;
            } else {
                wait = PAUSE_DURATION_MS;
            }
        }
        return wait;
    }

    // Finaliza o processamento de um comando
    private void finishCommand(char c) {
        if (isNote(c)) {
            player.noteOff(octave, Character.toString(c));
            lastNote = c;
        } else {
            if (lastNote != null) {
                player.noteOff(octave, Character.toString(lastNote));
            }
            lastNote = null;
        }
    }

    // Loop principal que reproduz o aúdio
    private void playLoop(Editor editor) {
        String textoEditor = editor.getText();
        boolean finished = false;
        paused = false;
        while (!pause && !finished) {
            for (int pos = 0; pos <= textoEditor.length() - 1; pos++) {
                Character command = textoEditor.charAt(pos);
                int wait = startCommand(command);
                if (wait != 0) {
                    long tEnd = System.currentTimeMillis() + wait;
                    while (!pause && System.currentTimeMillis() < tEnd) {
                        try {
                            synchronized (this) {
                                wait(tEnd - System.currentTimeMillis());
                            }
                        } catch (InterruptedException e) {
                        }
                    }
                }
                finishCommand(command);
                app.updateEditorPosition(position);
            }
        }
        paused = true;

        synchronized (this) {
            notify();
        }
        if (finished) {
            app.stop();
        }
    }

    // Duplica o volume ou o reseta
    private void doubleOrResetVolume() {
        int newVolume = player.getVolume() * 2;
        player.setVolume(newVolume);
        if (player.getVolume() != newVolume) {
            player.setVolume(defaultVolume);
        }
    }

    public void play() {
        pause = false;
        synchronized (this) {
            notify();
        }
    }

    public void pause() {
        pause = true;
        synchronized (this) {
            notify();
        }
        while (pause && !paused) {
            synchronized (this) {
                notify();
            }
            try {
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException e) {
            }
        }
    }

    public void quit() {
        quit = true;
        pause();
    }

    public void saveToMidiFile(String textoEditor) {

        // Cria um seletor de arquivos
        JFileChooser fileChooser = new JFileChooser();

        String nomeDiretorio = "MeusArquivosMidi";

        File diretorioAtual = new File(".").getAbsoluteFile();
        File diretorioNovo = diretorioAtual.getParentFile().getParentFile();
        File novoDiretorio = new File(diretorioNovo, nomeDiretorio);

        // Verifica e cria o diretório se ele não existir
        if (!novoDiretorio.exists()) {
            novoDiretorio.mkdirs();
        }

        // Define o novo diretório como o diretório inicial do fileChooser
        fileChooser.setCurrentDirectory(novoDiretorio);

        // Exibe a janela para salvar arquivo e armazena a resposta do usuário
        int response = fileChooser.showSaveDialog(null);

        File midiFile = null;
        
        // Verificando se o usuário escolheu um arquivo para salvar
        if (response == JFileChooser.APPROVE_OPTION) {
            // try {
                // Obtendo o arquivo selecionado
                midiFile = fileChooser.getSelectedFile();                

                // Adiciona a extensão .midi ao arquivo se não estiver presente
                if (!midiFile.getName().endsWith(".midi")) {
                    midiFile = new File(midiFile.getAbsolutePath() + ".midi");
                }
            }

        // Tenta criar a sequência MIDI e escrevê-la no arquivo
        try {
            // Cria uma nova sequência MIDI com resolução PPQ
            Sequence sequence = new Sequence(Sequence.PPQ, PPQ);
            // Cria uma trilha na sequência
            Track track = sequence.createTrack();

            // Define o texto a ser convertido em música MIDI e inicializa variáveis usadas
            this.text = textoEditor;
            this.position = 0;
            this.octave = DEFAULT_OCTAVE;
            int ticks = 0;

            // Processa o texto caractere a caractere adicionando eventos MIDI à trilha
            Character command;
            while ((command = nextCommand()) != null) {
                int durationTicks = processCommand(command, track, ticks);
                ticks += durationTicks;
            }

            // Escreve a sequência MIDI no arquivo selecionado
            MidiSystem.write(sequence, 1, midiFile);
        } catch (InvalidMidiDataException | IOException e) {
            e.printStackTrace();
        }
    }

    private int processCommand(char command, Track track, int startTick) {
        int durationTicks = NOTE_DURATION_MS / PPQ; // Example conversion to ticks

        if (isNote(command)) {
            int note = Player.midiNote(octave, Character.toString(command));
            addNoteToTrack(track, note, startTick, durationTicks);
            return durationTicks;
        }
        // Add other command processing here

        return 0; 
    }

    private void addNoteToTrack(Track track, int note, int startTick, int durationTicks) {
        try {
            ShortMessage onMessage = new ShortMessage();
            onMessage.setMessage(ShortMessage.NOTE_ON, 0, note, Player.NOTE_VELOCITY);
            track.add(new MidiEvent(onMessage, startTick));
    
            ShortMessage offMessage = new ShortMessage();
            offMessage.setMessage(ShortMessage.NOTE_OFF, 0, note, 0);
            track.add(new MidiEvent(offMessage, startTick + durationTicks));
        } catch (InvalidMidiDataException e) {
            e.printStackTrace(); // Or handle the exception in some other way
        }
    }
    

    private Character nextCommand() {
        if (position < text.length()) {
            return text.charAt(position++);
        }
        return null;
    }
}