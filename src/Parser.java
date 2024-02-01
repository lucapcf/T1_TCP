class Parser {
    private static final int INSTRUMENT_PHONE = 124;

    public abstract class Command {
        public final int startpos;
        public final int endpos;

        public Command(int startpos, int endpos) {
            this.startpos = startpos;
            this.endpos = endpos;
        }
    }

    public class NoteCommand extends Command {
        public final String note;

        public NoteCommand(int startpos, int endpos, String note) {
            super(startpos, endpos);
            this.note = note;
        }
    }

    public class InstrumentCommand extends Command {
        public final int instrument;

        public InstrumentCommand(int startpos, int endpos, int instrument) {
            super(startpos, endpos);
            this.instrument = instrument;
        }
    }

    public abstract class DeltaCommand extends Command {
        public final int delta;

        public DeltaCommand(int startpos, int endpos, int delta) {
            super(startpos, endpos);
            this.delta = delta;
        }
    }

    public class VolumeCommand extends DeltaCommand {
        public VolumeCommand(int startpos, int endpos, int delta) {
            super(startpos, endpos, delta);
        }
    }

    public class OctaveCommand extends DeltaCommand {
        public OctaveCommand(int startpos, int endpos, int delta) {
            super(startpos, endpos, delta);
        }
    }

    public class BpmCommand extends DeltaCommand {
        public BpmCommand(int startpos, int endpos, int delta) {
            super(startpos, endpos, delta);
        }
    }

    public class PauseCommand extends Command {
        public PauseCommand(int startpos, int endpos) {
            super(startpos, endpos);
        }
    }

    public class PhoneCommand extends Command {
        public PhoneCommand(int startpos, int endpos) {
            super(startpos, endpos);
        }
    }

    public class RandomCommand extends Command {
        public RandomCommand(int startpos, int endpos) {
            super(startpos, endpos);
        }
    }

    public class BpmRandomCommand extends Command {
        public BpmRandomCommand(int startpos, int endpos) {
            super(startpos, endpos);
        }
    }

    public class NopCommand extends Command {
        public NopCommand(int startpos, int endpos) {
            super(startpos, endpos);
        }
    }

    private final String text;
    private int position = 0;

    private String lastNote = null;

    public Parser(String text) {
        this.text = text;
    }

    private Character getChar() {
        Character c;
        if (position < text.length()) {
            c = text.charAt(position);
        } else {
            c = null;
        }
        position += 1;
        return c;
    }

    private void rewind(int n) {
        if (position >= n) {
            position -= n;
        }
    }

    public Command nextCommand() {
        Command cmd = null;
        int startpos = position;
        Character c = getChar();

        if (c == null) {
            rewind(1);
            return null;
        }

        if ("IOU".indexOf(Character.toUpperCase(c)) != -1) {
            if (lastNote != null) {
                cmd = new NoteCommand(startpos, position, lastNote);
                lastNote = null;
            } else {
                cmd = new RandomCommand(startpos, position);
                return cmd;
            }
        }

        lastNote = null;

        if (c == 'B') {
            Character[] a1 = {'B', 'P', 'M', '+'};
            Character[] a2 = {c, getChar(), getChar(), getChar()};
            if (a1.equals(a2)) {
                cmd = new BpmCommand(startpos, position, 80);
                return cmd;
            } else {
                rewind(3);
            }
        }

        if (c == 'R') {
            Character delta = getChar();
            if (delta == '+') {
                cmd = new OctaveCommand(startpos, position, 1);
                return cmd;
            } else if (delta == '-') {
                cmd = new OctaveCommand(startpos, position, -1);
                return cmd;
            } else {
                rewind(1);
            }
        }

        System.out.println(c);

        if ("ABCDEFG".indexOf(Character.toUpperCase(c)) != -1) {
            String note = Character.toString(Character.toUpperCase(c));
            cmd = new NoteCommand(startpos, position, note);
            lastNote = note;
        } else if (c == ' ') {
            cmd = new PauseCommand(startpos, position);
        } else if (c == '+' || c == '-') {
            if (c == '+') {
                cmd = new VolumeCommand(startpos, position, 1);
            } else if (c == '-') {
                cmd = new VolumeCommand(startpos, position, -1);
            }
        } else if (c == '?') {
            cmd = new RandomCommand(startpos, position);
        } else if (c == ';') {
            cmd = new BpmRandomCommand(startpos, position);
        } else if (c == '\n') {
            StringBuilder sb = new StringBuilder();
            Character d = getChar();
            while (d != null && Character.isDigit(d)) {
                sb.append(d);
            }
            String s = sb.toString();
            int instrument;
            if (s.isEmpty()) {
                instrument = 0;
            } else {
                instrument = Integer.parseInt(s);
            }
            cmd = new InstrumentCommand(startpos, position, instrument);
        } else {
            cmd = new NopCommand(startpos, position);
        }

        return cmd;
    }
}
