import java.util.Arrays;
import java.util.List;
import javax.sound.midi.*;

class Player {
    private static final int MIDI_VOLUME_CONTROL = 7;
    private static final int NOTE_VELOCITY = 50;

    private static final List<String> NOTES = Arrays.asList(
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    );

    private MidiChannel channel;

    public Player() {
        Synthesizer synth;
        try {
            synth = MidiSystem.getSynthesizer();
            synth.open();
        } catch (MidiUnavailableException e) {
            throw new RuntimeException();
        }
        channel = synth.getChannels()[0];
        reset();
    }

    private static int midiNote(int octave, String note) {
        return ((octave + 1) * 12) + NOTES.indexOf(note);
    }

    public void reset() {
        setInstrument(0);
    }

    public int getInstrument() {
        return channel.getProgram();
    }

    public void setInstrument(int instrument) {
        channel.programChange(instrument);
    }

    public int getVolume() {
        return channel.getController(MIDI_VOLUME_CONTROL);
    }

    public void setVolume(int volume) {
        channel.controlChange(MIDI_VOLUME_CONTROL, volume);
    }

    public void noteOn(int octave, String note) {
        channel.noteOn(midiNote(octave, note), NOTE_VELOCITY);
    }

    public void noteOff(int octave, String note) {
        channel.noteOff(midiNote(octave, note));
    }
}
