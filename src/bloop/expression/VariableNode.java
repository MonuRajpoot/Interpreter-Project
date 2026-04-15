package bloop.expression;
import bloop.Environment;

public class VariableNode implements Expression {
    // stores the variable name — like "x" or "score" or "result"
    // we don't store the value here — we look it up at runtime from the Environment
    private final String name;
    // constructor — parser creates this when it sees an identifier in the source code
    public VariableNode(String name) {
        this.name = name;
    }
    // looks up the variable in the environment and returns its current value
    // if the variable was never defined, Environment.get() will throw a helpful error
    @Override
    public Object evaluate(Environment env) {
        return env.get(name);
    }
    // helpful for debugging
    @Override
    public String toString() {
        return "VariableNode(" + name + ")";
    }
}