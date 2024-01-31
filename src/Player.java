import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.sound.midi.*;
import javax.swing.JFileChooser;

class Player {
    private static final int MIDI_VOLUME_CONTROL = 7;
    public static final int NOTE_VELOCITY = 50;

    // Lista de notas musicais
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

    // Método para calcular o valor MIDI de uma nota em uma oitava
    public static int midiNote(int octave, String note) {
        return ((octave + 1) * 12) + NOTES.indexOf(note);
    }

    // Define o instrumento padrão no canal
    public void reset() {
        setInstrument(0);
    }

    // Obtém o instrumento atual do canal
    public int getInstrument() {
        return channel.getProgram();
    }

    // Define o instrumento no canal
    public void setInstrument(int instrument) {
        channel.programChange(instrument);
    }

    // Obtém o volume atual do canal
    public int getVolume() {
        return channel.getController(MIDI_VOLUME_CONTROL);
    }

    // Define o volume do canal
    public void setVolume(int volume) {
        channel.controlChange(MIDI_VOLUME_CONTROL, volume);
    }

    // Liga uma nota MIDI no canal
    public void noteOn(int octave, String note) {
        channel.noteOn(midiNote(octave, note), NOTE_VELOCITY);
    }

    // Desliga uma nota MIDI no canal
    public void noteOff(int octave, String note) {
        channel.noteOff(midiNote(octave, note));
    }
}
