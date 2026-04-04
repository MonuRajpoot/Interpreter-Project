
public class Token {

    // every token has three things:
    // 1. its type — what kind of token it is (from our TokenType enum)
    // 2. its value — the actual text from the source code, like "put" or "10"
    // 3. its line number — which line it was on, so we can show helpful error messages

    private final TokenType type;
    private final String value;
    private final int line;

    // constructor — we set all three fields here and never change them after
    // we made the fields final because a token should not change once it is created
    public Token(TokenType type, String value, int line) {
        this.type = type;
        this.value = value;
        this.line = line;
    }

    // getter for the token type — tells us what kind of token this is
    public TokenType getType() {
        return type;
    }

    // getter for the value — gives us the actual text from the source code
    public String getValue() {
        return value;
    }

    // getter for the line number — we use this when printing error messages
    public int getLine() {
        return line;
    }

    // this helps us during debugging — we can print any token and see all its details clearly
    @Override
    public String toString() {
        return "Token(" + type + ", \"" + value + "\", line=" + line + ")";
    }
}
