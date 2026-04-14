package bloop.instruction;
import bloop.Environment;

public interface Instruction {

    // every instruction must implement this method
    // when called, it performs its action using the shared environment
    // for example AssignInstruction stores a value, PrintInstruction prints a value
    void execute(Environment env);
}
