abstract class Player {
    private static final int NOTE_DURATION_MS = 500;
    private static final int PAUSE_DURATION_MS = 50;

    private static final int DEFAULT_OCTAVE = 4;
    private static final int MAX_OCTAVE = 9;

    private static final int PHONE_INSTRUMENT = 124;
    private static final int PHONE_OCTAVE = 4;
    private static final String PHONE_NOTE = "A";
    private static final int PHONE_DURATION_MS = 500;

    private int octave = DEFAULT_OCTAVE;
    private Parser parser;

    private int previousInstrument;
    private String randomNote;

    public void reset(String text) {
        octave = DEFAULT_OCTAVE;
        // midi.reset();
        if (text != null) {
            parser = new Parser(text);
        } else {
            parser = null;
        }
    }

    public abstract int getVolume();
    public abstract void setVolume(int volume);
    protected abstract void noteOn(int octave, String note);
    protected abstract void noteOff(int octave, String note);
    protected abstract int getInstrument();
    protected abstract void setInstrument(int instrument);

    protected Parser.Command nextCommand() {
        return parser.nextCommand();
    }

    protected int startCommand(Parser.Command cmd) {
        int wait = 0;
        if (cmd instanceof Parser.NoteCommand) {
            noteOn(octave, ((Parser.NoteCommand) cmd).note);
            wait = NOTE_DURATION_MS;
        } else if (cmd instanceof Parser.VolumeCommand) {
            setVolume(getVolume() + ((Parser.VolumeCommand) cmd).delta);
        } else if (cmd instanceof Parser.OctaveCommand) {
            int newOctave = octave + ((Parser.OctaveCommand) cmd).delta;
            if (0 <= newOctave && newOctave <= MAX_OCTAVE) {
                octave = newOctave;
            }
        } else if (cmd instanceof Parser.InstrumentCommand) {
            setInstrument(((Parser.InstrumentCommand) cmd).instrument);
        } else if (cmd instanceof Parser.BpmCommand) {
            //bpm += cmd.delta;
        } else if (cmd instanceof Parser.PauseCommand) {
            wait = PAUSE_DURATION_MS;
        } else if (cmd instanceof Parser.PhoneCommand) {
            previousInstrument = getInstrument();
            setInstrument(PHONE_INSTRUMENT);
            noteOn(PHONE_OCTAVE, PHONE_NOTE);
            wait = PHONE_DURATION_MS;
        } else if (cmd instanceof Parser.RandomCommand) {
            randomNote = "A";
            noteOn(octave, randomNote);
            wait = NOTE_DURATION_MS;
        } else if (cmd instanceof Parser.BpmRandomCommand) {
            //bpm = 80;
        }
        return wait;
    }

    protected void finishCommand(Parser.Command cmd) {
        if (cmd instanceof Parser.NoteCommand) {
            noteOff(octave, ((Parser.NoteCommand) cmd).note);
        } else if (cmd instanceof Parser.PhoneCommand) {
            noteOff(PHONE_OCTAVE, PHONE_NOTE);
        } else if (cmd instanceof Parser.RandomCommand) {
            noteOff(octave, randomNote);
            randomNote = null;
        }
    }
}
