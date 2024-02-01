import java.util.Arrays;
import java.util.List;

abstract class BaseMidiPlayer extends Player {
    protected static final int MIDI_VOLUME_CONTROL = 7;
    protected static final int NOTE_VELOCITY = 50;

    private static final List<String> NOTES = Arrays.asList(
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    );

    protected static int midiNote(int octave, String note) {
        return ((octave + 1) * 12) + NOTES.indexOf(note);
    }
}
