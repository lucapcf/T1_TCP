class Parser {
    private static final int NOTE_DURATION_MS = 500;

    private String text;
    private int position;

    private boolean quit;
    private boolean pause;

    private App app;
    private Player player;
    private int octave;
    private Character lastNote;

    public Parser(App app, Player player) {
        quit = false;
        pause = true;
        this.app = app;
        this.player = player;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!quit) {
                    while (!pause) {
                        playCommand();
                    }
                    try {
                        synchronized (Parser.this) {
                            Parser.this.wait();
                        }
                    } catch (InterruptedException e) {}
                }
            }
        }).start();
    }

    private static boolean isNote(char command) {
        return ('A' <= command) && (command <= 'G');
    }

    public void reset() {
        pause();
        position = 0;
        lastNote = null;
        octave = 0;
        player.reset();
    }

    public void setTextAndReset(String text) {
        reset();
        this.text = text;
    }

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

    private boolean startCommand(char command) {
        boolean wait = false;
        if (isNote(command)) {
            player.noteOn(octave, command);
            wait = true;
        } else if (command == ' ') {
            // etc.
        } else if (false) {
            // etc.
        } else {
            if (lastNote != null) {
                player.noteOn(octave, lastNote);
            }
            wait = true;
        }
        return wait;
    }

    private void finishCommand(char command) {
        if (isNote(command)) {
            player.noteOff(octave, command);
            lastNote = command;
        } else {
            if (lastNote != null) {
                player.noteOff(octave, lastNote);
            }
            lastNote = null;
        }
    }

    private void playCommand() {
        Character command = nextCommand();
        if (command == null) {
            // Fim.
            app.stop();
        } else {
            app.updateEditorPosition(position);
            Boolean shouldWait = startCommand(command);
            if (shouldWait) {
                try {
                    synchronized (this) {
                        wait(NOTE_DURATION_MS);
                    }
                } catch (InterruptedException e) {}
            }
            finishCommand(command);
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
    }

    public void quit() {
        quit = true;
        pause = true;
        synchronized (this) {
            notify();
        }
    }
}
