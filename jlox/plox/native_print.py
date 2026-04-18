from plox.lox_callable import LoxCallable


class NativePrint(LoxCallable):
    def arity(self) -> int:
        return 1

    def call(self, interpreter, arguments: list):
        print(interpreter.stringify(arguments[0]))
        return None

    def __str__(self) -> str:
        return "<native fn>"
