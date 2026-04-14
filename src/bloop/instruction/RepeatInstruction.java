package bloop.instruction;

import bloop.Environment;
import bloop.expression.Expression;
import java.util.List;

public class RepeatInstruction implements Instruction {

    private final Expression count;

    // unmodifiable copy — safe from outside modification
    private final List<Instruction> body;

    public RepeatInstruction(Expression count, List<Instruction> body) {
        this.count = count;

        // List.copyOf() makes a fixed independent snapshot of the body
        this.body = List.copyOf(body);
    }

    @Override
    public void execute(Environment env) {

        // evaluate the count expression
        Object countValue = count.evaluate(env);

        // make sure it is a number
        if (!(countValue instanceof Double)) {
            throw new RuntimeException(
                    "Repeat count must be a number — got: '" + countValue + "' instead"
            );
        }

        int times = (int) Math.floor((Double) countValue);

        // make sure count is not negative
        if (times < 0) {
            throw new RuntimeException(
                    "Repeat count cannot be negative — got: " + times
            );
        }

        // run all body instructions exactly 'times' number of times
        for (int i = 0; i < times; i++) {
            for (Instruction instruction : body) {
                instruction.execute(env);
            }
        }
    }

    @Override
    public String toString() {
        return "RepeatInstruction(count=" + count + ", body=" + body + ")";
    }
}

//public class RepeatInstruction implements Instruction {
//
//    // how many times to repeat the body
//    // we store it as an Expression because the count could be a variable like: repeat n times:
//    private final Expression count;
//
//    // the instructions to run on each iteration
//    // this list can contain ANY instruction — so nested blocks work automatically
//    private final List<Instruction> body;
//
//    // constructor — parser creates this when it sees: repeat <count> times:
//    public RepeatInstruction(Expression count, List<Instruction> body) {
//        this.count = count;
//        this.body  = body;
//    }
//
//    @Override
//    public void execute(Environment env) {
//
//        // step 1 — evaluate the count expression to get the actual number
//        Object countValue = count.evaluate(env);
//
//        // step 2 — make sure the count is actually a number
//        if (!(countValue instanceof Double)) {
//            throw new RuntimeException(
//                    "Repeat count must be a number — got: '" + countValue + "' instead"
//            );
//        }
//
//        // step 3 — convert to an integer so we can use it in a for loop
//        int times = (int) Math.floor((Double) countValue);
//
//        // step 4 — make sure the count is not negative
//        if (times < 0) {
//            throw new RuntimeException(
//                    "Repeat count cannot be negative — got: " + times
//            );
//        }
//
//        // step 5 — run all body instructions exactly 'times' number of times
//        // notice each instruction in the body has access to the same environment
//        // so variables set inside the loop are visible outside too
//        for (int i = 0; i < times; i++) {
//            for (Instruction instruction : body) {
//                instruction.execute(env);
//            }
//        }
//    }
//
//    // helpful for debugging
//    @Override
//    public String toString() {
//        return "RepeatInstruction(count=" + count + ", body=" + body + ")";
//    }
//}
