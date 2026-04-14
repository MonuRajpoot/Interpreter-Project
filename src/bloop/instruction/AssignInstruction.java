package bloop.instruction;
import bloop.Environment;
import bloop.expression.Expression;


public class AssignInstruction implements Instruction {

    // the name of the variable we are storing into — like "x" or "result"
    private final String variableName;

    // the expression whose result will be stored — like NumberNode(10) or BinaryOpNode
    // we store the expression object, not the value yet
    // the value is only calculated when execute() is called at runtime
    private final Expression expression;

    // constructor — parser creates this when it sees: put <expression> into <name>
    public AssignInstruction(String variableName, Expression expression) {
        this.variableName = variableName;
        this.expression = expression;
    }

    @Override
    public void execute(Environment env) {

        // step 1 — evaluate the expression to get the actual value
        // for example BinaryOpNode(x + y * 2) becomes 16.0
        Object value = ((Expression) expression).evaluate(env);

        // step 2 — store the result in the environment under the variable name
        // after this, any instruction that reads "result" will get 16.0
        env.set(variableName, value);
    }

    // helpful for debugging
    @Override
    public String toString() {
        return "AssignInstruction(" + variableName + " = " + expression + ")";
    }
}
