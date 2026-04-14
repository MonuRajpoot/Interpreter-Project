package bloop.instruction;
import bloop.Environment;
import bloop.expression.Expression;
import java.util.List;

public class IfInstruction implements Instruction {

    private final Expression condition;

    // we use List.copyOf() to make an unmodifiable independent copy of the list
    // this means nobody outside this class can change our body after construction
    private final List<Instruction> thenBody;
    private final List<Instruction> elseBody;

    public IfInstruction(Expression condition,
                         List<Instruction> thenBody,
                         List<Instruction> elseBody) {
        this.condition = condition;

        // List.copyOf() does two things for us:
        // 1. creates a new independent copy so outside changes don't affect us
        // 2. makes the copy unmodifiable so nobody can add/remove from it
        this.thenBody = List.copyOf(thenBody);
        this.elseBody = List.copyOf(elseBody);
    }

    @Override
    public void execute(Environment env) {

        // evaluate the condition — should give us true or false
        Object result = condition.evaluate(env);

        // make sure we actually got a Boolean
        if (!(result instanceof Boolean)) {
            throw new RuntimeException(
                    "Condition in 'if' statement did not produce true or false" +
                            " — got: '" + result + "' instead"
            );
        }

        // run the correct block based on the condition
        if ((Boolean) result) {
            for (Instruction instruction : thenBody) {
                instruction.execute(env);
            }
        } else {
            for (Instruction instruction : elseBody) {
                instruction.execute(env);
            }
        }
    }

    @Override
    public String toString() {
        return "IfInstruction(condition=" + condition +
                ", thenBody=" + thenBody +
                ", elseBody=" + elseBody + ")";
    }
}