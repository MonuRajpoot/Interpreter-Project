package bloop.expression;
import bloop.Environment;

public class NumberNode implements Expression {
    // stores the number value — we use double so it works for both 10 and 3.14
    // final because this value never changes after the node is created
    private final double value;
    // constructor — the parser creates this node when it sees a number in the source code
    public NumberNode(double value) {
        this.value = value;
    }
    // just returns the stored number — no calculation needed here
    @Override
    public Object evaluate(Environment env) {
        return value;
    }
    // helpful for debugging — shows us what number this node holds
    @Override
    public String toString() {
        return "NumberNode(" + value + ")";
    }
}

