import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.sound.midi.*;
import javax.swing.JFileChooser;

class Player {
    private static final int MIDI_VOLUME_CONTROL = 7;
    private static final int NOTE_VELOCITY = 50;

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
    private static int midiNote(int octave, String note) {
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

    // Salva uma sequência MIDI em um arquivo
    public void saveMidi(Editor editor) {
        // Cria um seletor de arquivos
        JFileChooser fileChooser = new JFileChooser();

        // Abre no diretório atual
        fileChooser.setCurrentDirectory(new File("."));

        // Exibe a janela para salvar arquivo e armazena a resposta do usuário
        int response = fileChooser.showSaveDialog(null);

        
        // Verificando se o usuário escolheu um arquivo para salvar
        if (response == JFileChooser.APPROVE_OPTION) {
            // try {
                // Obtendo o arquivo selecionado
                File file = fileChooser.getSelectedFile();                

                // Adiciona a extensão .midi ao arquivo se não estiver presente
                if (!file.getName().endsWith(".midi")) {
                    file = new File(file.getAbsolutePath() + ".midi");
                }

                String strEditor = editor.getText();

                // Chama a função para criar e salvar a sequência MIDI
                criaMidiSequence(strEditor, file);


            // } catch (IOException e) {
                // Imprimindo a pilha de exceção em caso de erro
                // e.printStackTrace();
            // }
        }
    }

    // Cria uma sequência MIDI
    public void criaMidiSequence(String strEditor, File file) {
        try {
            // Cria uma sequência MIDI
            Sequence sequence = new Sequence(Sequence.PPQ, 24);
    
            // Cria uma faixa na sequência
            Track track = sequence.createTrack();
    
            // Analisa o texto do editor e cria eventos MIDI
            // Exemplo: Suponha que cada linha do strEditor seja "nota duração intensidade"
            String[] linhas = strEditor.split("\n");
            for (String linha : linhas) {
                String[] partes = linha.split(" ");
                int nota = Integer.parseInt(partes[0]);
                int duracao = Integer.parseInt(partes[1]);
                int intensidade = Integer.parseInt(partes[2]);
    
                // Cria e adiciona o evento de nota ligada
                // ShortMessage onMessage = new ShortMessage();
                // onMessage.setMessage(ShortMessage.NOTE_ON, 0, nota, intensidade);
                // track.add(new MidiEvent(onMessage, /* momento de início */));
    
                // // Cria e adiciona o evento de nota desligada
                // ShortMessage offMessage = new ShortMessage();
                // offMessage.setMessage(ShortMessage.NOTE_OFF, 0, nota, 0);
                // track.add(new MidiEvent(offMessage, /* momento de término */));
            }
    
            // Escreve a sequência em um arquivo
            MidiSystem.write(sequence, 1, file);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
