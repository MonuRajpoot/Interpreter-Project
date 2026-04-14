package bloop.instruction;
import bloop.expression.Expression;
import bloop.Environment;
import java.util.List;

public class WhileInstruction implements Instruction {

    // the condition to check before each iteration — like BinaryOpNode(x > 0)
   // we re-evaluate this every iteration because the value of x changes inside the loop
    private final Expression condition;

    // unmodifiable copy — safe from outside modification
    private final List<Instruction> body;

    public WhileInstruction(Expression condition, List<Instruction> body) {
        this.condition = condition;

        // List.copyOf() makes a fixed independent snapshot of the body
        this.body = List.copyOf(body);
    }

    @Override
    public void execute(Environment env) {

        while (true) {

            // re-evaluate condition fresh every iteration
            Object result = condition.evaluate(env);

            // make sure it is a Boolean
            if (!(result instanceof Boolean)) {
                throw new RuntimeException(
                        "Condition in 'while' statement did not produce true or false" +
                                " — got: '" + result + "' instead"
                );
            }

            // stop if condition became false
            if (!(Boolean) result) {
                break;
            }

            // run all body instructions for this iteration
            for (Instruction instruction : body) {
                instruction.execute(env);
            }
        }
    }

    @Override
    public String toString() {
        return "WhileInstruction(condition=" + condition + ", body=" + body + ")";
    }
}

//public class WhileInstruction implements Instruction {
//
//    // the condition to check before each iteration — like BinaryOpNode(x > 0)
//    // we re-evaluate this every iteration because the value of x changes inside the loop
//    private final Expression condition;
//
//    // the instructions to run on each iteration as long as condition stays true
//    // nested blocks work here too for the same reason as RepeatInstruction
//    private final List<Instruction> body;
//
//    // constructor — parser creates this when it sees: while <condition> then:
//    public WhileInstruction(Expression condition, List<Instruction> body) {
//        this.condition = condition;
//        this.body      = body;
//    }
//
//    @Override
//    public void execute(Environment env) {
//
//        // we keep looping as long as the condition evaluates to true
//        // the condition is re-evaluated fresh at the start of every iteration
//        while (true) {
//
//            // step 1 — evaluate the condition
//            Object result = condition.evaluate(env);
//
//            // step 2 — make sure it is actually a Boolean
//            if (!(result instanceof Boolean)) {
//                throw new RuntimeException(
//                        "Condition in 'while' statement did not produce true or false" +
//                                " — got: '" + result + "' instead"
//                );
//            }
//
//            // step 3 — if condition is false, stop the loop
//            if (!(Boolean) result) {
//                break;
//            }
//
//            // step 4 — condition was true so run all body instructions
//            for (Instruction instruction : body) {
//                instruction.execute(env);
//            }
//        }
//    }
//
//    // helpful for debugging
//    @Override
//    public String toString() {
//        return "WhileInstruction(condition=" + condition + ", body=" + body + ")";
//    }
//}

