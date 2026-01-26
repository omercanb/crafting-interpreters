package lox;

import java.util.List;

// Function calling needs to happen through functions objects to store the many states a function needs
// We create this interface to unify calling functions and class constructors
interface LoxCallable {
    int arity();

    Object call(Interpreter interpreter, List<Object> arguments);
}
