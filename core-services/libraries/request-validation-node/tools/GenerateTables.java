import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.IntPredicate;
import org.jsoup.nodes.Entities;
import org.jsoup.parser.Parser;

/**
 * Writes data/jsoup-entities.json and data/java8-chars.json, and checks data/event-handler-names.txt against the
 * resource loaded from the request-validation jar. Must run on Java 8 with jsoup 1.17.2 and the request-validation
 * jar on the class path (see generate.sh). Output is deterministic: no timestamps, fixed ordering.
 */
public final class GenerateTables {
    private static final String HANDLER_RESOURCE = "/org/egov/requestvalidation/event-handler-names.txt";
    private static final int BASE_SIZE = 106;   // jsoup Entities.EscapeMode.base declared size
    private static final int FULL_SIZE = 2125;  // jsoup Entities.EscapeMode.extended declared size
    private static final int XML_SIZE = 4;      // jsoup Entities.EscapeMode.xhtml declared size

    private GenerateTables() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("usage: GenerateTables <data-dir>");
            System.exit(2);
        }
        Path dataDir = Paths.get(args[0]);
        int handlers = checkEventHandlerResource(dataDir.resolve("event-handler-names.txt"));
        String entities = entitiesJson();
        String chars = charsJson();
        Files.write(dataDir.resolve("jsoup-entities.json"), entities.getBytes(StandardCharsets.UTF_8));
        Files.write(dataDir.resolve("java8-chars.json"), chars.getBytes(StandardCharsets.UTF_8));
        System.out.println("java.version=" + System.getProperty("java.version")
                + " handlers=" + handlers + " jsoup-entities.json=" + entities.length() + "B"
                + " java8-chars.json=" + chars.length() + "B");
    }

    // ---------------------------------------------------------------------------------------------------------
    // event-handler-names.txt: the extracted file must equal the class-path resource byte for byte, and parse with
    // the library's own rules (EventHandlerNameList.load).

    private static int checkEventHandlerResource(Path extracted) throws IOException {
        byte[] fromJar;
        try (InputStream in = GenerateTables.class.getResourceAsStream(HANDLER_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("event-handler resource not on the class path");
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            for (int n; (n = in.read(buffer)) > 0;) {
                out.write(buffer, 0, n);
            }
            fromJar = out.toByteArray();
        }
        byte[] onDisk = Files.readAllBytes(extracted);
        if (!Arrays.equals(fromJar, onDisk)) {
            throw new IllegalStateException("extracted event-handler-names.txt differs from the jar resource");
        }
        Set<String> names = new LinkedHashSet<String>();
        for (String line : new String(onDisk, StandardCharsets.UTF_8).split("\r\n|\r|\n", -1)) {
            String value = line.trim().toLowerCase(Locale.ROOT);
            if (!value.isEmpty() && !value.startsWith("#")) {
                if (!value.matches("on[a-z0-9]+")) {
                    throw new IllegalStateException("invalid handler name");
                }
                names.add(value);
            }
        }
        if (names.size() != 124) {
            throw new IllegalStateException("expected 124 handler names, found " + names.size());
        }
        return names.size();
    }

    // ---------------------------------------------------------------------------------------------------------
    // jsoup entity tables, read from jsoup's own packed EntitiesData strings in its load() format
    // ("name=cp1[,cp2];index&", radix 36), then resolved through jsoup's public lookup methods.

    private static String entitiesJson() throws Exception {
        parsePacked(packed("xmlPoints"), XML_SIZE);
        Map<String, int[]> base = parsePacked(packed("basePoints"), BASE_SIZE);
        Map<String, int[]> full = parsePacked(packed("fullPoints"), FULL_SIZE);

        Map<String, int[]> baseOut = new LinkedHashMap<String, int[]>();
        Map<String, int[]> fullOut = new LinkedHashMap<String, int[]>();
        int multi = 0;
        for (Map.Entry<String, int[]> entry : full.entrySet()) {
            String name = entry.getKey();
            requireAsciiAlphanumeric(name);
            if (!Entities.isNamedEntity(name)) {
                throw new IllegalStateException("jsoup does not find full entity " + name);
            }
            int[] resolved = resolve(name);
            if (resolved.length == 2) {
                multi++;
            }
            fullOut.put(name, resolved);
            String expected = new String(resolved, 0, resolved.length);
            // Whole reference with ';' decodes to the resolved code points.
            check(Parser.unescapeEntities("&" + name + ";", false).equals(expected), "decode &" + name + ";");
            boolean inBase = base.containsKey(name);
            if (Entities.isBaseNamedEntity(name) != inBase) {
                throw new IllegalStateException("base membership differs for " + name);
            }
            // Without ';' only base names decode; the reference ends the input here.
            String bare = Parser.unescapeEntities("&" + name, false);
            check(bare.equals(inBase ? expected : "&" + name), "decode &" + name);
        }
        for (Map.Entry<String, int[]> entry : base.entrySet()) {
            String name = entry.getKey();
            if (!full.containsKey(name)) {
                throw new IllegalStateException("base entity missing from full table: " + name);
            }
            if (!Entities.isBaseNamedEntity(name)) {
                throw new IllegalStateException("jsoup does not find base entity " + name);
            }
            baseOut.put(name, resolve(name));
        }

        StringBuilder json = new StringBuilder(64 * 1024);
        json.append("{\n");
        json.append("\"notice\": \"Generated from jsoup 1.17.2 entity data (MIT). See NOTICE.\",\n");
        json.append("\"jsoup\": \"1.17.2\",\n");
        json.append("\"baseCount\": ").append(baseOut.size()).append(",\n");
        json.append("\"fullCount\": ").append(fullOut.size()).append(",\n");
        json.append("\"fullMultiCodepointCount\": ").append(multi).append(",\n");
        appendEntityMap(json, "base", baseOut);
        json.append(",\n");
        appendEntityMap(json, "full", fullOut);
        json.append("\n}\n");
        return json.toString();
    }

    private static String packed(String field) throws Exception {
        Class<?> type = Class.forName("org.jsoup.nodes.EntitiesData");
        Field value = type.getDeclaredField(field);
        value.setAccessible(true);
        return (String) value.get(null);
    }

    private static Map<String, int[]> parsePacked(String data, int declaredSize) {
        Map<String, int[]> out = new LinkedHashMap<String, int[]>();
        int pos = 0;
        while (pos < data.length()) {
            int eq = data.indexOf('=', pos);
            int semi = data.indexOf(';', eq);
            int amp = data.indexOf('&', semi);
            if (amp < 0) {
                amp = data.length();
            }
            String name = data.substring(pos, eq);
            String[] codes = data.substring(eq + 1, semi).split(",", -1);
            Integer.parseInt(data.substring(semi + 1, amp), 36); // reverse-index slot; parsed only to check format
            if (codes.length < 1 || codes.length > 2) {
                throw new IllegalStateException("bad code list for " + name);
            }
            int[] cps = new int[codes.length];
            for (int i = 0; i < codes.length; i++) {
                cps[i] = Integer.parseInt(codes[i], 36);
            }
            if (out.put(name, cps) != null) {
                throw new IllegalStateException("duplicate entity " + name);
            }
            pos = amp + 1;
        }
        if (out.size() != declaredSize) {
            throw new IllegalStateException("entity count " + out.size() + " != declared " + declaredSize);
        }
        return out;
    }

    /** The code points jsoup emits for a named reference (Entities.codepointsForName, including its quirks). */
    private static int[] resolve(String name) {
        int[] holder = new int[2];
        int count = Entities.codepointsForName(name, holder);
        if (count == 1) {
            return new int[] {holder[0]};
        }
        if (count == 2) {
            return new int[] {holder[0], holder[1]};
        }
        throw new IllegalStateException("no code points for " + name);
    }

    private static void requireAsciiAlphanumeric(String name) {
        if (name.isEmpty()) {
            throw new IllegalStateException("empty entity name");
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            boolean ok = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
            if (!ok) {
                throw new IllegalStateException("unexpected character in entity name " + name);
            }
        }
    }

    private static void appendEntityMap(StringBuilder json, String key, Map<String, int[]> map) {
        json.append('"').append(key).append("\": {");
        boolean first = true;
        for (Map.Entry<String, int[]> entry : map.entrySet()) {
            json.append(first ? "\n" : ",\n");
            first = false;
            json.append('"').append(entry.getKey()).append("\": ");
            appendInts(json, entry.getValue());
        }
        json.append("\n}");
    }

    // ---------------------------------------------------------------------------------------------------------
    // Java 8 character predicates.

    private static String charsJson() throws Exception {
        // Facts the port relies on without a table.
        for (int c = 0; c <= 0xFFFF; c++) {
            char ch = (char) c;
            boolean iso = c <= 0x1F || (c >= 0x7F && c <= 0x9F);
            check(Character.isISOControl(ch) == iso, "isISOControl formula at " + c);
            check(Character.toLowerCase(ch) == (char) Character.toLowerCase((int) c), "toLowerCase(char) at " + c);
        }
        List<Integer> upperX = new ArrayList<Integer>();
        for (int c = 0; c <= 0xFFFF; c++) {
            if (Character.toUpperCase((char) c) == 'X') {
                upperX.add(c);
            }
        }
        check(upperX.equals(Arrays.asList(Integer.valueOf('X'), Integer.valueOf('x'))), "toUpperCase == 'X' set");

        List<int[]> lowerChar = new ArrayList<int[]>();
        for (int c = 0; c <= 0xFFFF; c++) {
            char lower = Character.toLowerCase((char) c);
            if (lower != c) {
                lowerChar.add(new int[] {c, lower});
            }
        }

        List<int[]> lowerRoot = new ArrayList<int[]>();
        for (int cp = 0; cp <= 0x10FFFF; cp++) {
            String single = new String(Character.toChars(cp));
            String lower = single.toLowerCase(Locale.ROOT);
            if (!lower.equals(single)) {
                int[] cps = lower.codePoints().toArray();
                int[] row = new int[cps.length + 1];
                row[0] = cp;
                System.arraycopy(cps, 0, row, 1, cps.length);
                lowerRoot.add(row);
            }
        }

        List<Integer> whitespace = new ArrayList<Integer>();
        for (int c = 0; c <= 0xFFFF; c++) {
            if (Character.isWhitespace((char) c)) {
                whitespace.add(c);
            }
        }
        check(whitespace.size() == 26, "26 whitespace units");

        // U+32FF is defined in this runtime's character data, but its normalizer leaves it unchanged.
        check(Character.isDefined(0x32FF), "U+32FF defined");
        check(Normalizer.normalize("\u32FF", Normalizer.Form.NFKC).equals("\u32FF"), "U+32FF not decomposed");

        // String.toLowerCase(Locale.ROOT) maps U+03A3 by the Final_Cased condition of ConditionalSpecialCasing,
        // which tests isCased on the code points of the surrounding word (word boundaries of BreakIterator).
        final Method isCased = Class.forName("java.lang.ConditionalSpecialCasing")
                .getDeclaredMethod("isCased", int.class);
        isCased.setAccessible(true);
        check("A\u03A3".toLowerCase(Locale.ROOT).equals("a\u03C2"), "final sigma after a cased letter");
        check("\u03A3".toLowerCase(Locale.ROOT).equals("\u03C3"), "sigma alone");

        // General categories (Character.getType) of every code point, as runs [start, type].
        List<int[]> categoryRuns = new ArrayList<int[]>();
        int lastType = -1;
        for (int cp = 0; cp <= 0x10FFFF; cp++) {
            int type = Character.getType(cp);
            if (type != lastType) {
                categoryRuns.add(new int[] {cp, type});
                lastType = type;
            }
        }

        // Character.digit(char, 36) of the non-ASCII BMP units that are digits, as rows [first, last, value of first].
        List<int[]> digitRows = new ArrayList<int[]>();
        for (int c = 0x80; c <= 0xFFFF; c++) {
            int value = Character.digit((char) c, 36);
            if (value < 0) {
                continue;
            }
            int[] lastRow = digitRows.isEmpty() ? null : digitRows.get(digitRows.size() - 1);
            if (lastRow != null && lastRow[1] == c - 1 && lastRow[2] + (c - lastRow[0]) == value) {
                lastRow[1] = c;
            } else {
                digitRows.add(new int[] {c, c, value});
            }
        }
        for (int c = 0; c < 0x80; c++) {
            int expected = c >= '0' && c <= '9' ? c - '0' : c >= 'a' && c <= 'z' ? c - 'a' + 10
                    : c >= 'A' && c <= 'Z' ? c - 'A' + 10 : -1;
            check(Character.digit((char) c, 36) == expected, "ASCII digit at " + c);
        }

        StringBuilder json = new StringBuilder(128 * 1024);
        json.append("{\n");
        json.append("\"notice\": \"Derived from the Unicode Character Database 6.2 as implemented by the Java 8 runtime "
                + "below; an extracted and modified subset. See NOTICE.\",\n");
        json.append("\"javaVersion\": \"").append(System.getProperty("java.version")).append("\",\n");
        json.append("\"javaRuntimeVersion\": \"").append(System.getProperty("java.runtime.version")).append("\",\n");
        json.append("\"letter\": ");
        appendRanges(json, 0xFFFF, new IntPredicate() {
            public boolean test(int c) {
                return Character.isLetter((char) c);
            }
        });
        json.append(",\n\"whitespace\": ");
        appendInts(json, toArray(whitespace));
        json.append(",\n\"javaIdentifierPart\": ");
        appendRanges(json, 0xFFFF, new IntPredicate() {
            public boolean test(int c) {
                return Character.isJavaIdentifierPart((char) c);
            }
        });
        json.append(",\n\"lowerChar\": ");
        appendRows(json, lowerChar);
        json.append(",\n\"lowerRoot\": ");
        appendRows(json, lowerRoot);
        json.append(",\n\"defined\": ");
        appendRanges(json, 0x10FFFF, new IntPredicate() {
            public boolean test(int cp) {
                return Character.isDefined(cp);
            }
        });
        json.append(",\n\"cased\": ");
        appendRanges(json, 0x10FFFF, new IntPredicate() {
            public boolean test(int cp) {
                try {
                    return (Boolean) isCased.invoke(null, cp);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        });
        json.append(",\n\"generalCategory\": ");
        appendRows(json, categoryRuns);
        json.append(",\n\"digit\": ");
        appendRows(json, digitRows);
        json.append(",\n\"nfkcOpaque\": [").append(0x32FF).append(']');
        json.append(",\n\"toUpperCaseIsX\": ");
        appendInts(json, toArray(upperX));
        json.append("\n}\n");
        return json.toString();
    }

    private static void appendRanges(StringBuilder json, int max, IntPredicate predicate) {
        List<int[]> ranges = new ArrayList<int[]>();
        int start = -1;
        for (int c = 0; c <= max + 1; c++) {
            boolean in = c <= max && predicate.test(c);
            if (in && start < 0) {
                start = c;
            } else if (!in && start >= 0) {
                ranges.add(new int[] {start, c - 1});
                start = -1;
            }
        }
        appendRows(json, ranges);
    }

    private static void appendRows(StringBuilder json, List<int[]> rows) {
        json.append('[');
        for (int i = 0; i < rows.size(); i++) {
            json.append(i == 0 ? "\n" : ",\n");
            appendInts(json, rows.get(i));
        }
        json.append("\n]");
    }

    private static void appendInts(StringBuilder json, int[] values) {
        json.append('[');
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append(values[i]);
        }
        json.append(']');
    }

    private static int[] toArray(List<Integer> values) {
        int[] out = new int[values.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = values.get(i);
        }
        return out;
    }

    private static void check(boolean condition, String what) {
        if (!condition) {
            throw new IllegalStateException("check failed: " + what);
        }
    }
}
