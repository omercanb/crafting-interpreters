package lox;

import java.util.List;

class NativeClock implements LoxCallable {
    @Override
    public int arity() {
        return 0;
    }

    public Object call(Interpreter interpreter, List<Object> arguments) {
        return (double) System.currentTimeMillis() / 1000.0;
    }

    @Override
    public String toString() {
        return "<native fn clock>";
    }

}
