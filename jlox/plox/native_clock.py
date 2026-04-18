import time
from plox.lox_callable import LoxCallable


class NativeClock(LoxCallable):
    def arity(self) -> int:
        return 0

    def call(self, interpreter, arguments: list):
        return time.time()

    def __str__(self) -> str:
        return "<native fn>"
