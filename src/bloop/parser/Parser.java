package bloop.parser;

import bloop.tokenizer.Token;
import bloop.tokenizer.TokenType;


import bloop.expression.Expression;
import bloop.expression.NumberNode;
import bloop.expression.StringNode;
import bloop.expression.VariableNode;
import bloop.expression.BinaryOpNode;
import bloop.expression.LengthNode;


import bloop.instruction.Instruction;
import bloop.instruction.AssignInstruction;
import bloop.instruction.PrintInstruction;
import bloop.instruction.IfInstruction;
import bloop.instruction.RepeatInstruction;
import bloop.instruction.WhileInstruction;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    // the flat list of tokens produced by the tokenizer
    // we made it unmodifiable to maintain immutability
    private final List<Token> tokens;

    // tracks which token we are currently looking at
    // this is the only mutable state in the parser — it moves forward as we parse
    private int pos;

    // constructor — stores the token list
    public Parser(List<Token> tokens) {
        // defensive copy for immutability
        this.tokens = List.copyOf(tokens);
        this.pos    = 0;
    }

    // main method — parses all instructions at the top level (indent 0)
    // returns the complete list of instructions ready for execution
    public List<Instruction> parse() {
        // top level instructions have indent level 0
        return parseBlock(0);
    }

    // -------------------------------------------------------------------
    // BLOCK PARSING
    // parses a list of instructions that all share the same indent level
    // this method calls itself recursively for nested blocks
    // -------------------------------------------------------------------

    private List<Instruction> parseBlock(int expectedIndent) {
        List<Instruction> instructions = new ArrayList<>();

        // keep parsing instructions as long as:
        // 1. we have not reached end of file
        // 2. the next line has the correct indentation level
        while (!isAtEnd()) {

            // skip any blank lines
            skipBlankLines();

            if (isAtEnd()) break;

            // peek at the indent of the next line
            int indent = peekIndent();

            // if indent is less than expected we have left this block
            // so we stop and return what we have collected so far
            if (indent < expectedIndent) break;

            // if indent is more than expected something is wrong
            if (indent > expectedIndent) {
                Token t = currentToken();
                throw new RuntimeException(
                        "Unexpected indentation on line " + t.getLine() +
                                " — expected " + expectedIndent + " spaces but got " + indent
                );
            }

            // consume the INDENT token now that we have checked it
            consumeIndent();

            // parse one instruction at this level
            Instruction instruction = parseInstruction(expectedIndent);

            // instruction can be null if the line was empty after consuming indent
            if (instruction != null) {
                instructions.add(instruction);
            }
        }

        return instructions;
    }

    // -------------------------------------------------------------------
    // INSTRUCTION PARSING
    // looks at the current token and decides which instruction to parse
    // -------------------------------------------------------------------

    private Instruction parseInstruction(int currentIndent) {

        // skip any newlines before the instruction
        skipNewlines();

        if (isAtEnd()) return null;

        Token token = currentToken();

        // decide which instruction to parse based on current token
        switch (token.getType()) {

            case PUT:
                return parseAssign();

            case PRINT:
                return parsePrint();

            case IF:
                return parseIf(currentIndent);

            case REPEAT:
                return parseRepeat(currentIndent);

            case WHILE:
                return parseWhile(currentIndent);

            case NEWLINE:
                // empty line — skip it
                advance();
                return null;

            case EOF:
                return null;

            default:
                throw new RuntimeException(
                        "Unexpected token '" + token.getValue() +
                                "' on line " + token.getLine() +
                                " — expected an instruction like put, print, if, repeat, or while"
                );
        }
    }

    // -------------------------------------------------------------------
    // ASSIGN INSTRUCTION
    // handles: put <expression> into <variableName>
    // -------------------------------------------------------------------

    private AssignInstruction parseAssign() {

        // consume the PUT token
        Token putToken = consume(TokenType.PUT,
                "Expected 'put' at start of assignment");

        // parse the expression — everything between put and into
        Expression expression = parseExpression();

        // consume the INTO token
        consume(TokenType.INTO,
                "Expected 'into' after expression in assignment on line "
                        + putToken.getLine());

        // next token must be the variable name
        Token nameToken = consume(TokenType.IDENTIFIER,
                "Expected variable name after 'into' on line "
                        + putToken.getLine());

        // consume the newline that ends this instruction
        consumeNewline();

        return new AssignInstruction(nameToken.getValue(), expression);
    }

    // -------------------------------------------------------------------
    // PRINT INSTRUCTION
    // handles: print <expression>
    // -------------------------------------------------------------------

    private PrintInstruction parsePrint() {

        // consume the PRINT token
        Token printToken = consume(TokenType.PRINT,
                "Expected 'print' at start of print statement");

        // parse the expression to print
        Expression expression = parseExpression();

        // consume the newline
        consumeNewline();

        return new PrintInstruction(expression);
    }

    // -------------------------------------------------------------------
    // IF INSTRUCTION
    // handles: if <condition> then:
    //              <body>
    //          else:          ← optional
    //              <elseBody> ← optional
    // -------------------------------------------------------------------

    private IfInstruction parseIf(int currentIndent) {

        // consume the IF token
        Token ifToken = consume(TokenType.IF,
                "Expected 'if' at start of if statement");

        // parse the condition expression — like score > 50
        Expression condition = parseExpression();

        // consume the THEN token
        consume(TokenType.THEN,
                "Expected 'then' after condition on line "
                        + ifToken.getLine());

        // consume the COLON token
        consume(TokenType.COLON,
                "Expected ':' after 'then' on line "
                        + ifToken.getLine());

        // consume the newline that ends the if header line
        consumeNewline();

        // parse the body — all instructions indented more than current level
        // we expect body to be indented by 4 more spaces
        List<Instruction> thenBody = parseBlock(currentIndent + 4);

        // check if there is an else block following
        List<Instruction> elseBody = new ArrayList<>();

        if (peekTokenType() == TokenType.INDENT && peekIndent() == currentIndent) {
            // save position in case this is not an else
            int savedPos = pos;
            consumeIndent();

            if (peekTokenType() == TokenType.ELSE) {
                // consume ELSE
                advance();

                // consume COLON
                consume(TokenType.COLON,
                        "Expected ':' after 'else' on line "
                                + currentToken().getLine());

                // consume newline
                consumeNewline();

                // parse else body
                elseBody = parseBlock(currentIndent + 4);

            } else {
                // not an else — restore position so this line gets parsed normally
                pos = savedPos;
            }
        }

        return new IfInstruction(condition, thenBody, elseBody);
    }

    // -------------------------------------------------------------------
    // REPEAT INSTRUCTION
    // handles: repeat <count> times:
    //              <body>
    // -------------------------------------------------------------------

    private RepeatInstruction parseRepeat(int currentIndent) {

        // consume REPEAT token
        Token repeatToken = consume(TokenType.REPEAT,
                "Expected 'repeat' at start of repeat statement");

        // parse the count expression — like 4 or n
        Expression count = parseExpression();

        // consume TIMES token
        consume(TokenType.TIMES,
                "Expected 'times' after count on line "
                        + repeatToken.getLine());

        // consume COLON token
        consume(TokenType.COLON,
                "Expected ':' after 'times' on line "
                        + repeatToken.getLine());

        // consume newline
        consumeNewline();

        // parse the body — indented 4 more spaces than current level
        List<Instruction> body = parseBlock(currentIndent + 4);

        return new RepeatInstruction(count, body);
    }

    // -------------------------------------------------------------------
    // WHILE INSTRUCTION
    // handles: while <condition> then:
    //              <body>
    // -------------------------------------------------------------------

    private WhileInstruction parseWhile(int currentIndent) {

        // consume WHILE token
        Token whileToken = consume(TokenType.WHILE,
                "Expected 'while' at start of while statement");

        // parse the condition expression
        Expression condition = parseExpression();

        // consume THEN token
        consume(TokenType.THEN,
                "Expected 'then' after condition on line "
                        + whileToken.getLine());

        // consume COLON token
        consume(TokenType.COLON,
                "Expected ':' after 'then' on line "
                        + whileToken.getLine());

        // consume newline
        consumeNewline();

        // parse the body
        List<Instruction> body = parseBlock(currentIndent + 4);

        return new WhileInstruction(condition, body);
    }

    // -------------------------------------------------------------------
    // EXPRESSION PARSING — three method chain for operator precedence
    // -------------------------------------------------------------------

    // handles + and - (lowest priority)
    // calls parseTerm() which handles higher priority operators first
    private Expression parseExpression() {

        // start by parsing the left side as a term
        Expression left = parseTerm();

        // keep looping as long as we see + or -
        while (peekTokenType() == TokenType.PLUS ||
                peekTokenType() == TokenType.MINUS) {

            // consume the operator token
            Token op = advance();
            String operator = op.getValue();

            // parse the right side as another term
            Expression right = parseTerm();

            // wrap both sides in a BinaryOpNode
            // this becomes the new left for the next iteration
            left = new BinaryOpNode(left, operator, right);
        }

        // check for comparison operators after the expression
        // comparisons have lower priority than arithmetic
        if (peekTokenType() == TokenType.GREATER ||
                peekTokenType() == TokenType.LESS    ||
                peekTokenType() == TokenType.EQUALS) {

            Token op = advance();
            String operator = op.getValue();
            Expression right = parseTerm();
            left = new BinaryOpNode(left, operator, right);
        }

        return left;
    }

    // handles * and / (medium priority)
    // calls parsePrimary() which handles single values
    private Expression parseTerm() {

        // start by parsing the left side as a primary
        Expression left = parsePrimary();

        // keep looping as long as we see * or /
        while (peekTokenType() == TokenType.STAR ||
                peekTokenType() == TokenType.SLASH) {

            Token op = advance();
            String operator = op.getValue();
            Expression right = parsePrimary();

            left = new BinaryOpNode(left, operator, right);
        }

        return left;
    }

    // handles single values — numbers, strings, variables, parentheses
    // this is the bottom of the precedence chain
    private Expression parsePrimary() {

        Token token = currentToken();

        switch (token.getType()) {

            case NUMBER:
                advance();
                // parse the number string into a double
                return new NumberNode(Double.parseDouble(token.getValue()));

            case STRING:
                advance();
                // value already has quotes stripped by tokenizer
                return new StringNode(token.getValue());

            case IDENTIFIER:
                advance();
                // check if this is a built-in function call like length(x)
                if (peekTokenType() == TokenType.LPAREN) {
                    return parseBuiltinCall(token);
                }
                return new VariableNode(token.getValue());

            case LPAREN:
                // handle grouped expressions like (x + y) * 2
                advance(); // consume (
                Expression inner = parseExpression();
                consume(TokenType.RPAREN,
                        "Expected ')' to close expression on line "
                                + token.getLine());
                return inner;

            default:
                throw new RuntimeException(
                        "Expected a value (number, string, or variable) on line "
                                + token.getLine() +
                                " but got '" + token.getValue() + "'"
                );
        }
    }

    // -------------------------------------------------------------------
    // BUILT-IN FUNCTION CALL
    // handles: length(x) — string length extension
    // -------------------------------------------------------------------

    private Expression parseBuiltinCall(Token nameToken) {

        // only "length" is supported right now
        if (!nameToken.getValue().equals("length")) {
            throw new RuntimeException(
                    "Unknown built-in function '" + nameToken.getValue() +
                            "' on line " + nameToken.getLine() +
                            " — the only supported built-in is length()"
            );
        }

        // consume the (
        consume(TokenType.LPAREN,
                "Expected '(' after 'length' on line " + nameToken.getLine());

        // parse the argument inside the parentheses
        Expression argument = parseExpression();

        // consume the )
        consume(TokenType.RPAREN,
                "Expected ')' after argument in length() on line "
                        + nameToken.getLine());

        // we return a special node for this
        // LengthNode is a small extra class we will create below
        return new LengthNode(argument);
    }

    // -------------------------------------------------------------------
    // UTILITY METHODS
    // -------------------------------------------------------------------

    // returns the current token without moving forward
    private Token currentToken() {
        return tokens.get(pos);
    }

    // moves forward one token and returns the token we just passed
    private Token advance() {
        Token t = tokens.get(pos);
        pos++;
        return t;
    }

    // checks what type the current token is without consuming it
    private TokenType peekTokenType() {
        if (isAtEnd()) return TokenType.EOF;
        return tokens.get(pos).getType();
    }

    // checks the indentation level of the current line
    // only call this when you know the current token is INDENT
    private int peekIndent() {
        if (isAtEnd()) return 0;
        if (tokens.get(pos).getType() == TokenType.INDENT) {
            return Integer.parseInt(tokens.get(pos).getValue());
        }
        return 0;
    }

    // consumes the INDENT token — call this after checking the indent level
    private void consumeIndent() {
        if (peekTokenType() == TokenType.INDENT) {
            advance();
        }
    }

    // consumes the next token and verifies it is the expected type
    // throws a helpful error if the token is not what we expected
    private Token consume(TokenType expected, String errorMessage) {
        if (peekTokenType() != expected) {
            Token current = currentToken();
            throw new RuntimeException(
                    errorMessage +
                            " — found '" + current.getValue() +
                            "' on line " + current.getLine() + " instead"
            );
        }
        return advance();
    }

    // consumes a newline token if present — does nothing if not present
    // we use this at the end of every instruction
    private void consumeNewline() {
        if (peekTokenType() == TokenType.NEWLINE) {
            advance();
        }
    }

    // skips over any consecutive newline tokens
    private void skipNewlines() {
        while (peekTokenType() == TokenType.NEWLINE) {
            advance();
        }
    }

    // skips blank lines — lines that have only an INDENT followed by a NEWLINE
    private void skipBlankLines() {
        while (!isAtEnd()) {
            int savedPos = pos;

            if (peekTokenType() == TokenType.INDENT) {
                advance(); // consume indent
                if (peekTokenType() == TokenType.NEWLINE) {
                    advance(); // consume newline — this was a blank line
                    continue;
                }
            }
            // not a blank line — restore position and stop
            pos = savedPos;
            break;
        }
    }

    // checks if we have reached the end of the token list
    private boolean isAtEnd() {
        return pos >= tokens.size() ||
                tokens.get(pos).getType() == TokenType.EOF;
    }
}
