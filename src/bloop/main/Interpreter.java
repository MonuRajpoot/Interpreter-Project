package bloop.main;
import bloop.Environment;
import bloop.instruction.Instruction;
import bloop.tokenizer.Token;
import bloop.parser.Parser;
import bloop.tokenizer.Tokenizer;
import java.util.List;

public class Interpreter {

    // this method is the entire pipeline in three steps
    // it takes raw bloop source code and runs it
    public void run(String sourceCode) {

        try {
            // ---------------------------------------------------
            // STEP 1 — TOKENIZER
            // break the raw source code into a flat list of tokens
            // ---------------------------------------------------
            Tokenizer tokenizer = new Tokenizer(sourceCode);
            List<Token> tokens  = tokenizer.tokenize();

            // ---------------------------------------------------
            // STEP 2 — PARSER
            // read the token list and build a list of instructions
            // each instruction may contain expression trees inside it
            // ---------------------------------------------------
            Parser parser                = new Parser(tokens);
            List<Instruction> instructions = parser.parse();

            // ---------------------------------------------------
            // STEP 3 — EXECUTE
            // create one shared environment for the whole program
            // then execute every instruction one by one
            // ---------------------------------------------------
            Environment env = new Environment();

            for (Instruction instruction : instructions) {
                instruction.execute(env);
            }

        } catch (RuntimeException e) {
            // if anything goes wrong at any stage we catch it here
            // and print a clean error message instead of a scary stack trace
            System.err.println("BLOOP Error: " + e.getMessage());
        }
    }
}
