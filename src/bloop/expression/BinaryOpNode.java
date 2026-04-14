package bloop.expression;
import bloop.Environment;

public class BinaryOpNode implements Expression {

    // left side of the operation — for example in x + y*2, the left is x
    private final Expression left;
    // the operator symbol — can be: +  -  *  /  >  <  ==
    private final String operator;
    // right side of the operation — for example in x + y*2, the right is y*2
    private final Expression right;
    // constructor — parser creates this when it sees an operator between two expressions
    public BinaryOpNode(Expression left, String operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
    @Override
    public Object evaluate(Environment env) {
        // step 1 — evaluate both sides first
        // this is recursive — if left is another BinaryOpNode, it evaluates itself too
        Object leftValue  = left.evaluate(env);
        Object rightValue = right.evaluate(env);
        // step 2 — handle arithmetic operators
        // for these we need both sides to be numbers
        // we cast to Double because all numbers in our language are stored as doubles
        switch (operator) {
            case "+":
                // special case — if either side is a String, we do string joining instead
                // for example: "Hello" + " World" gives "Hello World"
                if (leftValue instanceof String || rightValue instanceof String) {
                    return leftValue.toString() + rightValue.toString();
                }
                return toDouble(leftValue, "+" ) + toDouble(rightValue, "+");

            case "-":
                return toDouble(leftValue, "-") - toDouble(rightValue, "-");

            case "*":
                return toDouble(leftValue, "*") * toDouble(rightValue, "*");

            case "/":
                // extra safety — we check for division by zero and give a helpful message
                double divisor = toDouble(rightValue, "/");
                if (divisor == 0) {
                    throw new RuntimeException(
                            "Cannot divide by zero — right side of '/' evaluated to 0"
                    );
                }
                return toDouble(leftValue, "/") / divisor;

            // step 3 — handle comparison operators
            // these return a Boolean instead of a Double
            case ">":
                return toDouble(leftValue, ">") > toDouble(rightValue, ">");

            case "<":
                return toDouble(leftValue, "<") < toDouble(rightValue, "<");

            case "==":
                // == works for both numbers and strings
                // for numbers: 10.0 == 10.0 → true
                // for strings: "hello" == "hello" → true
                return leftValue.equals(rightValue);

            default:
                // if somehow an unknown operator ends up here, we throw a clear error
                throw new RuntimeException(
                        "Unknown operator: '" + operator + "' — this operator is not supported"
                );
        }
    }

    // helper method — safely converts an Object to a Double
    // if the value is not a number, we throw a helpful error explaining what went wrong
    private double toDouble(Object value, String operator) {
        if (value instanceof Double) {
            return (Double) value;
        }
        throw new RuntimeException(
                "Expected a number for operator '" + operator + "' but got: '" + value + "'"
        );
    }

    // helpful for debugging — shows the full structure of the operation
    @Override
    public String toString() {
        return "BinaryOpNode(" + left + " " + operator + " " + right + ")";
    }
}
