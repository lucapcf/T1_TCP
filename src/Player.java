import javax.sound.midi.*;

class Player {
    private static final int MIDI_VOLUME_CONTROL = 7;
    private static final int NOTE_VELOCITY = 100;

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

    private static int noteId(int octave, char note) {
        return 0;
    }

    public void reset() {
        setInstrument(0);
        setVolume(100);
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

    public void noteOn(int octave, char note) {
        channel.noteOn(noteId(octave, note), NOTE_VELOCITY);
    }

    public void noteOff(int octave, char note) {
        channel.noteOff(noteId(octave, note));
    }
}
