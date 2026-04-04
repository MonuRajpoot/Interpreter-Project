package bloop;

import java.util.HashMap;
import java.util.Map;

public class Environment {

    // this is our variable store — it maps variable names to their current values
    // we use Object as the value type because values can be either a Double or a String
    private final Map<String, Object> variables = new HashMap<>();

    // stores a variable or updates it if it already exists
    // for example: set("x", 10.0) or set("name", "Sitare")
    public void set(String name, Object value) {
        variables.put(name, value);
    }

    // returns the current value of a variable
    // if the variable was never defined, we throw an error with a helpful message
    // we include the variable name in the message so the user knows exactly what went wrong
    public Object get(String name) {
        if (!variables.containsKey(name)) {
            throw new RuntimeException(
                    "Variable not defined: '" + name + "'" +
                            " — make sure you used 'put ... into " + name + "' before using it"
            );
        }
        return variables.get(name);
    }

    // checks if a variable has been defined yet
    // we use this in error checking before trying to get a value
    public boolean has(String name) {
        return variables.containsKey(name);
    }

    // this is useful for debugging — prints everything currently stored in memory
    // we can call this if something is going wrong and we want to see the state
    @Override
    public String toString() {
        return "Environment" + variables.toString();
    }
}
