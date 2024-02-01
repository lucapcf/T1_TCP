class ThreadPlayer extends MidiPlayer {
    private final App app;

    private boolean quit = false;
    private boolean pause = true;
    private boolean paused = true;

    public ThreadPlayer(App app) {
        this.app = app;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!quit) {
                    playLoop();
                    try {
                        synchronized (ThreadPlayer.this) {
                            ThreadPlayer.this.wait();
                        }
                    } catch (InterruptedException e) {}
                }
            }
        }).start();
    }

    public void reset(String text) {
        pause();
        super.reset(text);
        app.updateVolume(getVolume());
    }

    public void setVolume(int volume) {
        super.setVolume(volume);
        app.updateVolume(volume);
    }

    private void processCommand(Parser.Command cmd) {
        int wait = startCommand(cmd);
        app.updatePosition(cmd.startpos, cmd.endpos);
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
        finishCommand(cmd);
    }

    private void playLoop() {
        boolean finished = false;
        paused = false;
        while (!pause && !finished) {
            Parser.Command cmd = nextCommand();
            if (cmd == null) {
                // Fim.
                finished = true;
            } else {
                processCommand(cmd);
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

    public void play() {
        pause = false;
        synchronized (this) {
            notify();
        }
    }

    /* Ativa a flag de pausa e espera até o loop do parser pausar. */
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
            } catch (InterruptedException e) {}
        }
    }

    public void quit() {
        quit = true;
        pause();
    }
}
