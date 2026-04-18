from typing import Any, List, TYPE_CHECKING

from plox.lox_callable import LoxCallable
from plox.environment import Environment

if TYPE_CHECKING:
    from plox.stmt import Function
    from plox.interpreter import Interpreter


class LoxFunction(LoxCallable):
    def __init__(self, declaration: "Function", closure: Environment) -> None:
        self.declaration: "Function" = declaration
        self.closure: Environment = closure

    def arity(self) -> int:
        return len(self.declaration.params)

    def call(self, interpreter: "Interpreter", arguments: List[Any]) -> Any:
        environment = Environment(interpreter.globals)
        for param, arg in zip(self.declaration.params, arguments):
            environment.define(param.lexeme, arg)
        try:
            interpreter.execute_block(self.declaration.body, environment)
        except Exception:
            pass
        return None

    def __str__(self) -> str:
        return f"<fn {self.declaration.name.lexeme}>"
