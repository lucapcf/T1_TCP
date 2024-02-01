import java.util.Arrays;
import java.util.List;
import javax.sound.midi.*;

class MidiPlayer extends Player {
    private static final int MIDI_VOLUME_CONTROL = 7;
    private static final int NOTE_VELOCITY = 50;

    private static final List<String> NOTES = Arrays.asList(
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    );

    private MidiChannel channel;

    public MidiPlayer() {
        Synthesizer synth;
        try {
            synth = MidiSystem.getSynthesizer();
            synth.open();
        } catch (MidiUnavailableException e) {
            throw new RuntimeException();
        }
        channel = synth.getChannels()[0];
        setInstrument(0);
    }

    public void reset(String text) {
        super.reset(text);
        setInstrument(0);
    }

    private static int midiNote(int octave, String note) {
        return ((octave + 1) * 12) + NOTES.indexOf(note);
    }

    public int getVolume() {
        return channel.getController(MIDI_VOLUME_CONTROL);
    }

    public void setVolume(int volume) {
        channel.controlChange(MIDI_VOLUME_CONTROL, volume);
    }

    protected void noteOn(int octave, String note) {
        channel.noteOn(midiNote(octave, note), NOTE_VELOCITY);
    }

    protected void noteOff(int octave, String note) {
        channel.noteOff(midiNote(octave, note));
    }

    protected int getInstrument() {
        return channel.getProgram();
    }

    protected void setInstrument(int instrument) {
        channel.programChange(instrument);
    }
}
