
import java.util.*;

public class Tokenizer {

    // the complete source code as one string
    private final String source;

    // our current position as we walk through the source character by character
    private int pos;

    // tracks which line we are currently on — used for error messages
    private int line;

    // we build up the token list as we go — made unmodifiable before returning
    private final List<Token> tokens;

    // constructor — stores the source code and sets starting position
    public Tokenizer(String source) {
        this.source = source;
        this.pos    = 0;
        this.line   = 1;
        this.tokens = new ArrayList<>();
    }

    // main method — walks through the entire source and returns the complete token list
    public List<Token> tokenize() {

        while (pos < source.length()) {

            // at the start of every line we check indentation first
            // this is how the parser will know which instructions are inside blocks
            if (isStartOfLine()) {
                readIndent();
            }

            // skip any remaining spaces or tabs between tokens on the same line
            // but NOT newlines — those are meaningful tokens
            if (pos < source.length() && source.charAt(pos) == ' ') {
                pos++;
                continue;
            }

            // stop if we have reached the end
            if (pos >= source.length()) break;

            char c = source.charAt(pos);

            if (c == '\n') {
                // newline marks the end of an instruction
                tokens.add(new Token(TokenType.NEWLINE, "\\n", line));
                line++;
                pos++;

            } else if (c == '\r') {
                // windows style line endings — just skip the \r
                // the \n that follows will be handled above
                pos++;

            } else if (c == '#') {
                // comment line — skip everything until end of line
                // BLOOP does not have comments in the spec but this is good to handle
                skipComment();

            } else if (c == '"') {
                // start of a string literal like "hello"
                readString();

            } else if (Character.isDigit(c)) {
                // start of a number like 10 or 3.14
                readNumber();

            } else if (Character.isLetter(c) || c == '_') {
                // start of a keyword or identifier like put, into, result
                readWord();

            } else if (c == '+') {
                tokens.add(new Token(TokenType.PLUS, "+", line));
                pos++;

            } else if (c == '-') {
                tokens.add(new Token(TokenType.MINUS, "-", line));
                pos++;

            } else if (c == '*') {
                tokens.add(new Token(TokenType.STAR, "*", line));
                pos++;

            } else if (c == '/') {
                tokens.add(new Token(TokenType.SLASH, "/", line));
                pos++;

            } else if (c == '>') {
                tokens.add(new Token(TokenType.GREATER, ">", line));
                pos++;

            } else if (c == '<') {
                tokens.add(new Token(TokenType.LESS, "<", line));
                pos++;

            } else if (c == '=') {
                // could be == (equality check) — peek at next character
                if (peek() == '=') {
                    tokens.add(new Token(TokenType.EQUALS, "==", line));
                    pos += 2; // skip both = characters
                } else {
                    // single = is not valid in BLOOP on its own
                    // we throw a helpful error with the line number
                    throw new RuntimeException(
                            "Unexpected character '=' on line " + line +
                                    " — did you mean '==' for equality check?"
                    );
                }

            } else if (c == ':') {
                tokens.add(new Token(TokenType.COLON, ":", line));
                pos++;

            } else if (c == '(') {
                tokens.add(new Token(TokenType.LPAREN, "(", line));
                pos++;

            } else if (c == ')') {
                tokens.add(new Token(TokenType.RPAREN, ")", line));
                pos++;

            } else {
                // completely unknown character — throw a helpful error
                throw new RuntimeException(
                        "Unknown character '" + c + "' on line " + line +
                                " — this character is not part of the BLOOP language"
                );
            }
        }

        // add the EOF token at the very end so the parser knows the file is finished
        tokens.add(new Token(TokenType.EOF, "", line));

        // return an unmodifiable copy — immutability maintained
        // nobody outside should be able to add or remove tokens after tokenizing is done
        return Collections.unmodifiableList(new ArrayList<>(tokens));
    }

    // --- private helper methods below ---

    // checks if we are currently at the very start of a new line
    // we are at the start of a line if we are at position 0
    // or if the previous character was a newline
    private boolean isStartOfLine() {
        return pos == 0 || source.charAt(pos - 1) == '\n';
    }

    // reads the leading spaces at the start of a line
    // produces one INDENT token with the count as its value
    // for example 4 spaces produces Token(INDENT, "4", line)
    private void readIndent() {
        int count = 0;
        while (pos < source.length() && source.charAt(pos) == ' ') {
            count++;
            pos++;
        }
        // we always emit an INDENT token even if count is 0
        // this tells the parser that this line is at the top level
        tokens.add(new Token(TokenType.INDENT, String.valueOf(count), line));
    }

    // reads a string literal — everything between two double quote marks
    // the opening quote has already been seen when this is called
    private void readString() {
        pos++; // skip the opening "
        StringBuilder sb = new StringBuilder();

        while (pos < source.length() && source.charAt(pos) != '"') {
            // handle the case where string runs to end of file without closing quote
            if (source.charAt(pos) == '\n') {
                throw new RuntimeException(
                        "String was not closed on line " + line +
                                " — did you forget the closing quote?"
                );
            }
            sb.append(source.charAt(pos));
            pos++;
        }

        // make sure we found the closing quote
        if (pos >= source.length()) {
            throw new RuntimeException(
                    "String was not closed — reached end of file without finding closing quote"
            );
        }

        pos++; // skip the closing "
        tokens.add(new Token(TokenType.STRING, sb.toString(), line));
    }

    // reads a number — handles both integers like 10 and decimals like 3.14
    private void readNumber() {
        StringBuilder sb = new StringBuilder();
        boolean hasDecimalPoint = false;

        while (pos < source.length() &&
                (Character.isDigit(source.charAt(pos)) || source.charAt(pos) == '.')) {

            if (source.charAt(pos) == '.') {
                // only allow one decimal point in a number
                if (hasDecimalPoint) {
                    throw new RuntimeException(
                            "Invalid number on line " + line +
                                    " — a number cannot have two decimal points"
                    );
                }
                hasDecimalPoint = true;
            }

            sb.append(source.charAt(pos));
            pos++;
        }

        tokens.add(new Token(TokenType.NUMBER, sb.toString(), line));
    }

    // reads a word — could be a keyword like "put" or an identifier like "result"
    // after reading the full word we check if it matches any keyword
    private void readWord() {
        StringBuilder sb = new StringBuilder();

        while (pos < source.length() &&
                (Character.isLetterOrDigit(source.charAt(pos)) || source.charAt(pos) == '_')) {
            sb.append(source.charAt(pos));
            pos++;
        }

        String word = sb.toString();

        // check if the word is a keyword — if not it is an identifier
        // we match against every keyword in the BLOOP language
        switch (word) {
            case "put":    tokens.add(new Token(TokenType.PUT,    word, line)); break;
            case "into":   tokens.add(new Token(TokenType.INTO,   word, line)); break;
            case "print":  tokens.add(new Token(TokenType.PRINT,  word, line)); break;
            case "if":     tokens.add(new Token(TokenType.IF,     word, line)); break;
            case "then":   tokens.add(new Token(TokenType.THEN,   word, line)); break;
            case "repeat": tokens.add(new Token(TokenType.REPEAT, word, line)); break;
            case "times":  tokens.add(new Token(TokenType.TIMES,  word, line)); break;
            case "else":   tokens.add(new Token(TokenType.ELSE,   word, line)); break;
            case "while":  tokens.add(new Token(TokenType.WHILE,  word, line)); break;
            default:
                // not a keyword — must be a variable name like x, score, result
                tokens.add(new Token(TokenType.IDENTIFIER, word, line));
                break;
        }
    }

    // skips everything from current position to end of line
    // used for comment handling
    private void skipComment() {
        while (pos < source.length() && source.charAt(pos) != '\n') {
            pos++;
        }
    }

    // peeks at the next character without moving pos forward
    // returns a null character if we are at the end of the file
    private char peek() {
        if (pos + 1 < source.length()) {
            return source.charAt(pos + 1);
        }
        return '\0'; // null character means nothing is there
    }
}
