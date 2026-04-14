package bloop.expression;

import bloop.Environment;

public interface Expression {

    // every expression must implement this method
    // when called, it calculates and returns the value of this expression
    // it returns Object because the result can be either a Double or a String or a Boolean
    // env is passed in because some expressions (like VariableNode) need to look up values
    Object evaluate(Environment env);
}