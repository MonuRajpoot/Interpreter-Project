package bloop.expression;
import bloop.Environment;

public class LengthNode implements Expression {

    // the expression whose length we want — like VariableNode("name")
    private final Expression argument;

    // constructor — parser creates this when it sees length(x)
    public LengthNode(Expression argument) {
        this.argument = argument;
    }

    @Override
    public Object evaluate(Environment env) {

        // evaluate the argument to get the actual value
        Object value = argument.evaluate(env);

        // make sure it is a String — length only works on strings
        if (!(value instanceof String)) {
            throw new RuntimeException(
                    "length() only works on strings — got: '" + value + "' instead"
            );
        }

        // return the length as a Double so it works with arithmetic
        return (double) ((String) value).length();
    }

    // helpful for debugging
    @Override
    public String toString() {
        return "LengthNode(" + argument + ")";
    }
}