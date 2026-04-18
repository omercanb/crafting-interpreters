from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from plox.lox_token import Token


class RuntimeError(Exception):
    def __init__(self, token: "Token", message: str):
        self.token = token
        self.message = message
        super().__init__(message)
