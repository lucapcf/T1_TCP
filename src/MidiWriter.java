import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import javax.sound.midi.*;

class MidiWriter extends Player {
    private static final int NOTE_VELOCITY = 50;
    private static final int PPQ = 24;

    private Sequence sequence;
    private Track track;

    public MidiWriter() {
        sequence = new Sequence(Sequence.PPQ, PPQ)
        track = sequence.createTrack();
    }

    // Verifica se um caractere é uma nota musical válida
    private static boolean isNote(char command) {
        return ('A' <= command) && (command <= 'G');
    }

    // Reseta o Parser para o estado inicial
    public void reset() {
        pause();
        lastNote = null;
        position = 0;
        octave = DEFAULT_OCTAVE;
        player.reset();
    }

    // Define o texto a ser interpretado e reseta o Parser
    public void setTextAndReset(String text) {
        reset();
        this.text = text;
    }

    public void writeFile(String text, File file) {
        Sequence sequence = new Sequence(Sequence.PPQ, PPQ);
        Track track = sequence.createTrack();

        ShortMessage onMessage = new ShortMessage();
        onMessage.setMessage(ShortMessage.NOTE_ON, 0, note, NOTE_VELOCITY);
        track.add(new MidiEvent(onMessage, startTick));

        ShortMessage offMessage = new ShortMessage();
        offMessage.setMessage(ShortMessage.NOTE_OFF, 0, note, 0);
        track.add(new MidiEvent(offMessage, startTick + durationTicks));

        int wait = startCommand(cmd);
        finishCommand(cmd);
    }

    public int getVolume() {
        return 100;
    }

    public void setVolume(int volume) {
    }
}
