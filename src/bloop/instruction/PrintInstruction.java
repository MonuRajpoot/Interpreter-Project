package bloop.instruction;

import bloop.Environment;
import bloop.expression.Expression;

public class PrintInstruction implements Instruction {

    // the expression to print — could be a variable, a number, a string, or a calculation
    private final Expression expression;

    // constructor — parser creates this when it sees: print <expression>
    public PrintInstruction(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void execute(Environment env) {

        // step 1 — evaluate the expression to get the actual value
        Object value = expression.evaluate(env);

        // step 2 — print the value to the screen
        // we need special handling for numbers — by default Java prints 16.0 but we want 16
        // so if the value is a whole number like 16.0 we print it as 16 instead
        if (value instanceof Double) {
            double number = (Double) value;

            // check if the number has no decimal part
            // for example 16.0 has no decimal part so we print 16
            // but 3.14 has a decimal part so we print 3.14
            if (number == Math.floor(number) && !Double.isInfinite(number)) {
                System.out.println((int) number);
            } else {
                System.out.println(number);
            }
        } else {
            // for strings and booleans just print as they are
            System.out.println(value);
        }
    }

    // helpful for debugging
    @Override
    public String toString() {
        return "PrintInstruction(" + expression + ")";
    }
}
