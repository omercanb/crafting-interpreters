from typing import Optional, TYPE_CHECKING

if TYPE_CHECKING:
    from plox.lox_token import Token


class Environment:
    def __init__(self, enclosing: Optional["Environment"] = None):
        self.values = {}
        self.enclosing = enclosing

    def define(self, name: str, value) -> None:
        self.values[name] = value

    def get(self, name: "Token"):
        if name.lexeme in self.values:
            return self.values[name.lexeme]
        if self.enclosing is not None:
            return self.enclosing.get(name)
        from plox.runtime_error import RuntimeError
        raise RuntimeError(name, f"Undefined variable '{name.lexeme}'.")

    def assign(self, name: "Token", value) -> None:
        if name.lexeme in self.values:
            self.values[name.lexeme] = value
            return
        if self.enclosing is not None:
            self.enclosing.assign(name, value)
            return
        from plox.runtime_error import RuntimeError
        raise RuntimeError(name, f"Undefined variable '{name.lexeme}'.")
