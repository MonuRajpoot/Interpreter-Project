import java.util.*;

public class Tokenizer {

    private String input;
    private int pos;
    private int line;

    private static final Map<String, TokenType> keywords = new HashMap<>();

    static {
        keywords.put("put", TokenType.PUT);
        keywords.put("into", TokenType.INTO);
        keywords.put("print", TokenType.PRINT);
        keywords.put("if", TokenType.IF);
        keywords.put("then", TokenType.THEN);
        keywords.put("repeat", TokenType.REPEAT);
        keywords.put("times", TokenType.TIMES);
    }

    public Tokenizer(String input) {
        this.input = input;
        this.pos = 0;
        this.line = 1;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (pos < input.length()) {
            char current = input.charAt(pos);

            // 🔹 Skip whitespace
            if (Character.isWhitespace(current)) {
                if (current == '\n') line++;
                pos++;
                continue;
            }

            // 🔹 Numbers
            if (Character.isDigit(current)) {
                tokens.add(readNumber());
                continue;
            }

            // 🔹 Identifiers / Keywords
            if (Character.isLetter(current)) {
                tokens.add(readIdentifier());
                continue;
            }

            // 🔹 Strings
            if (current == '"') {
                tokens.add(readString());
                continue;
            }

            // 🔹 Operators & Symbols
            switch (current) {
                case '+':
                    tokens.add(new Token(TokenType.PLUS, "+", line));
                    break;
                case '-':
                    tokens.add(new Token(TokenType.MINUS, "-", line));
                    break;
                case '*':
                    tokens.add(new Token(TokenType.MULTIPLY, "*", line));
                    break;
                case '/':
                    tokens.add(new Token(TokenType.DIVIDE, "/", line));
                    break;
                case '>':
                    tokens.add(new Token(TokenType.GREATER, ">", line));
                    break;
                case '<':
                    tokens.add(new Token(TokenType.LESS, "<", line));
                    break;
                case '=':
                    tokens.add(new Token(TokenType.EQUAL, "=", line));
                    break;
                case '(':
                    tokens.add(new Token(TokenType.LPAREN, "(", line));
                    break;
                case ')':
                    tokens.add(new Token(TokenType.RPAREN, ")", line));
                    break;
                default:
                    throw new RuntimeException(
                        "Unexpected character: '" + current + "' at line " + line
                    );
            }

            pos++;
        }

        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    // 🔹 Read Number
    private Token readNumber() {
        StringBuilder sb = new StringBuilder();

        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            pos++;
        }

        return new Token(TokenType.NUMBER, sb.toString(), line);
    }

    // 🔹 Read Identifier / Keyword
    private Token readIdentifier() {
        StringBuilder sb = new StringBuilder();

        while (pos < input.length() &&
               Character.isLetterOrDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            pos++;
        }

        String word = sb.toString();

        if (keywords.containsKey(word)) {
            return new Token(keywords.get(word), word, line);
        }

        return new Token(TokenType.IDENTIFIER, word, line);
    }

    // 🔹 Read String
    private Token readString() {
        pos++; // skip opening quote

        StringBuilder sb = new StringBuilder();

        while (pos < input.length() && input.charAt(pos) != '"') {
            sb.append(input.charAt(pos));
            pos++;
        }

        pos++; // skip closing quote

        return new Token(TokenType.STRING, sb.toString(), line);
    }
}