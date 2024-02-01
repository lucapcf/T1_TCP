import javax.sound.midi.*;

class MidiPlayer extends BaseMidiPlayer {
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
