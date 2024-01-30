import java.util.logging.Logger;

// Classe Parser para interpretar strings de texto e convertê-las em música MIDI
class Parser {
    // Logger para registrar mensagens para depuração
    private static final Logger LOGGER = Logger.getLogger(
        Parser.class.getName()
    );

    // Constantes para definir a duração das notas e pausas, e configurações de oitava
    private static final int NOTE_DURATION_MS = 500;
    private static final int PAUSE_DURATION_MS = 50;
    private static final int DEFAULT_OCTAVE = 4;
    private static final int MAX_OCTAVE = 9;

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
    public Parser(App app, Player player, int defaultVolume) {
        this.app = app;
        this.player = player;
        this.defaultVolume = defaultVolume;

        // Inicia uma nova thread para ler e executar os comandos do texto
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!quit) {
                    playLoop();
                    try {
                        synchronized (Parser.this) {
                            Parser.this.wait();
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

    // Obtém o próximo caractere do texto
    private Character nextCommand() {
        Character c;
        if (position < text.length()) {
            c = text.charAt(position);
            position += 1;
        } else {
            c = null;
        }
        return c;
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
    private void playLoop() {
        boolean finished = false;
        paused = false;
        while (!pause && !finished) {
            Character command = nextCommand();
            if (command == null) {
                // Fim.
                finished = true;
            } else {
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
}