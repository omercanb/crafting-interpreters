package lox;

import java.util.List;

class NativePrint implements LoxCallable {
    @Override
    public int arity() {
        return 1;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        System.out.println(interpreter.stringify(arguments.get(0)));
        return null;
    }

    @Override
    public String toString() {
        return "<native fn print>";
    }

}
