import java.io.*;
import javax.sound.midi.*;

class MidiWriter extends BaseMidiPlayer {
    private static final int PPQ = 24;

    private int ticks = 0;
    private int instrument = 0;
    private int volume = 100;
    private Sequence sequence;
    private Track track;

    public void writeFile(String text, File file) {
        reset(text);

        try {
            sequence = new Sequence(Sequence.PPQ, PPQ);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
            return;
        }
        track = sequence.createTrack();

        setVolume(volume);

        Parser.Command cmd;
        while ((cmd = nextCommand()) != null) {
            int wait = startCommand(cmd);
            ticks += wait / PPQ;
            finishCommand(cmd);
        }

        try {
            MidiSystem.write(sequence, 0, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.CONTROL_CHANGE, 0, MIDI_VOLUME_CONTROL, volume);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
            return;
        }
        track.add(new MidiEvent(message, ticks));
        this.volume = volume;
    }

    protected void noteOn(int octave, String note) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.NOTE_ON, 0, midiNote(octave, note), NOTE_VELOCITY);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
            return;
        }
        track.add(new MidiEvent(message, ticks));
    }

    protected void noteOff(int octave, String note) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.NOTE_OFF, 0, midiNote(octave, note), 0);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
            return;
        }
        track.add(new MidiEvent(message, ticks));
    }

    protected int getInstrument() {
        return instrument;
    }

    protected void setInstrument(int instrument) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.PROGRAM_CHANGE, 0, instrument, 0);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
            return;
        }
        track.add(new MidiEvent(message, ticks));
        this.instrument = instrument;
    }
}
