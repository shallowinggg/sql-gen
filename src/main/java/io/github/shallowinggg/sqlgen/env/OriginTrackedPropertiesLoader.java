package io.github.shallowinggg.sqlgen.env;

import io.github.shallowinggg.sqlgen.io.Resource;
import io.github.shallowinggg.sqlgen.util.Assert;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class to load {@code .properties} files into a map of {@code String} ->
 * {@link String}. Also supports expansion of {@code name[]=a,b,c} list style
 * values.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 * @author Thiago Hirata
 */
class OriginTrackedPropertiesLoader {

    private final Resource resource;

    /**
     * Create a new {@link OriginTrackedPropertiesLoader} instance.
     *
     * @param resource the resource of the {@code .properties} data
     */
    OriginTrackedPropertiesLoader(Resource resource) {
        Assert.notNull(resource, "Resource must not be null");
        this.resource = resource;
    }

    /**
     * Load {@code .properties} data and return a map of {@code String} ->
     * {@link String}.
     *
     * @return the loaded properties
     * @throws IOException on read error
     */
    Map<String, String> load() throws IOException {
        return load(true);
    }

    /**
     * Load {@code .properties} data and return a map of {@code String} ->
     * {@link String}.
     *
     * @param expandLists if list {@code name[]=a,b,c} shortcuts should be expanded
     * @return the loaded properties
     * @throws IOException on read error
     */
    Map<String, String> load(boolean expandLists) throws IOException {
        try (CharacterReader reader = new CharacterReader(this.resource)) {
            Map<String, String> result = new LinkedHashMap<>();
            StringBuilder buffer = new StringBuilder();
            while (reader.read()) {
                String key = loadKey(buffer, reader).trim();
                if (expandLists && key.endsWith("[]")) {
                    key = key.substring(0, key.length() - 2);
                    int index = 0;
                    do {
                        String value = loadValue(buffer, reader, true);
                        put(result, key + "[" + (index++) + "]", value);
                        if (!reader.isEndOfLine()) {
                            reader.read();
                        }
                    }
                    while (!reader.isEndOfLine());
                } else {
                    String value = loadValue(buffer, reader, false);
                    put(result, key, value);
                }
            }
            return result;
        }
    }

    private void put(Map<String, String> result, String key, String value) {
        if (!key.isEmpty()) {
            result.put(key, value);
        }
    }

    private String loadKey(StringBuilder buffer, CharacterReader reader) throws IOException {
        buffer.setLength(0);
        boolean previousWhitespace = false;
        while (!reader.isEndOfLine()) {
            if (reader.isPropertyDelimiter()) {
                reader.read();
                return buffer.toString();
            }
            if (!reader.isWhiteSpace() && previousWhitespace) {
                return buffer.toString();
            }
            previousWhitespace = reader.isWhiteSpace();
            buffer.append(reader.getCharacter());
            reader.read();
        }
        return buffer.toString();
    }

    private String loadValue(StringBuilder buffer, CharacterReader reader, boolean splitLists)
            throws IOException {
        buffer.setLength(0);
        while (reader.isWhiteSpace() && !reader.isEndOfLine()) {
            reader.read();
        }
        while (!reader.isEndOfLine() && !(splitLists && reader.isListDelimiter())) {
            buffer.append(reader.getCharacter());
            reader.read();
        }
        return buffer.toString();
    }

    /**
     * Reads characters from the source resource, taking care of skipping comments,
     * handling multi-line values and tracking {@code '\'} escapes.
     */
    private static class CharacterReader implements Closeable {

        private static final String[] ESCAPES = {"trnf", "\t\r\n\f"};

        private final LineNumberReader reader;

        private int columnNumber = -1;

        private boolean escaped;

        private int character;

        CharacterReader(Resource resource) throws IOException {
            this.reader = new LineNumberReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.ISO_8859_1));
        }

        @Override
        public void close() throws IOException {
            this.reader.close();
        }

        boolean read() throws IOException {
            return read(false);
        }

        boolean read(boolean wrappedLine) throws IOException {
            this.escaped = false;
            this.character = this.reader.read();
            this.columnNumber++;
            if (this.columnNumber == 0) {
                skipLeadingWhitespace();
                if (!wrappedLine) {
                    skipComment();
                }
            }
            if (this.character == '\\') {
                this.escaped = true;
                readEscaped();
            } else if (this.character == '\n') {
                this.columnNumber = -1;
            }
            return !isEndOfFile();
        }

        private void skipLeadingWhitespace() throws IOException {
            while (isWhiteSpace()) {
                this.character = this.reader.read();
                this.columnNumber++;
            }
        }

        private void skipComment() throws IOException {
            if (this.character == '#' || this.character == '!') {
                while (this.character != '\n' && this.character != -1) {
                    this.character = this.reader.read();
                }
                this.columnNumber = -1;
                read();
            }
        }

        private void readEscaped() throws IOException {
            this.character = this.reader.read();
            int escapeIndex = ESCAPES[0].indexOf(this.character);
            if (escapeIndex != -1) {
                this.character = ESCAPES[1].charAt(escapeIndex);
            } else if (this.character == '\n') {
                this.columnNumber = -1;
                read(true);
            } else if (this.character == 'u') {
                readUnicode();
            }
        }

        private void readUnicode() throws IOException {
            this.character = 0;
            for (int i = 0; i < 4; i++) {
                int digit = this.reader.read();
                if (digit >= '0' && digit <= '9') {
                    this.character = (this.character << 4) + digit - '0';
                } else if (digit >= 'a' && digit <= 'f') {
                    this.character = (this.character << 4) + digit - 'a' + 10;
                } else if (digit >= 'A' && digit <= 'F') {
                    this.character = (this.character << 4) + digit - 'A' + 10;
                } else {
                    throw new IllegalStateException("Malformed \\uxxxx encoding.");
                }
            }
        }

        boolean isWhiteSpace() {
            return !this.escaped && (this.character == ' ' || this.character == '\t' || this.character == '\f');
        }

        boolean isEndOfFile() {
            return this.character == -1;
        }

        boolean isEndOfLine() {
            return this.character == -1 || (!this.escaped && this.character == '\n');
        }

        boolean isListDelimiter() {
            return !this.escaped && this.character == ',';
        }

        boolean isPropertyDelimiter() {
            return !this.escaped && (this.character == '=' || this.character == ':');
        }

        char getCharacter() {
            return (char) this.character;
        }

    }

}

