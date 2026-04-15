package bloop.expression;
import bloop.Environment;

public class StringNode implements Expression {
    // stores the string value — without the surrounding quotes
    // so "hello" in source code becomes just hello stored here
    private final String value;
    // constructor — parser creates this when it sees a quoted string in source code
    public StringNode(String value) {
        this.value = value;
    }
    // just returns the stored string — nothing to calculate
    @Override
    public Object evaluate(Environment env) {
        return value;
    }
    // helpful for debugging
    @Override
    public String toString() {
        return "StringNode(\"" + value + "\")";
    }
}