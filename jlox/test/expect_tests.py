"""
Expect Test Framework for plox

An expect test compares actual output against saved expected output.
- First run: generates .expected files
- Subsequent runs: compares actual vs expected, fails if different
- Use --update flag to regenerate expected output
"""

import glob
import os
import subprocess
import sys
from pathlib import Path


class ExpectTest:
    def __init__(self, lox_file: str, expected_file: str):
        self.lox_file = lox_file
        self.expected_file = expected_file
        self.expected: str | None = None
        self.actual: str = ""
        self.passed: bool = False
        self.error: str = ""

    def run(self) -> None:
        """Execute the test and capture output."""
        try:
            result = subprocess.run(
                ["python3", "../plox/lox.py", "--print", self.lox_file],
                capture_output=True,
                text=True,
                timeout=5,
            )

            self.actual = result.stdout
            if result.stderr:
                self.error = result.stderr
        except subprocess.TimeoutExpired:
            self.error = "Test timed out"
        except Exception as e:
            self.error = str(e)

    def load_expected(self) -> bool:
        """Load expected output from file. Returns True if file exists."""
        if os.path.exists(self.expected_file):
            try:
                with open(self.expected_file, "r") as f:
                    self.expected = f.read()
                return True
            except Exception as e:
                self.error = f"Failed to read expected file: {e}"
                return False
        return False

    def compare(self) -> bool:
        """Compare actual output with expected. Returns True if they match."""
        if self.expected is None:
            return False
        self.passed = self.actual == self.expected
        return self.passed

    def save_expected(self) -> None:
        """Save actual output as new expected output."""
        try:
            with open(self.expected_file, "w") as f:
                f.write(self.actual)
        except Exception as e:
            self.error = f"Failed to write expected file: {e}"

    def print_result(self, verbose: bool = False) -> None:
        """Print test result."""
        name = os.path.basename(self.lox_file)

        if self.passed:
            print(f"✓ {name}")
        else:
            print(f"✗ {name}")
            if self.error:
                print(f"  Error: {self.error}")
            if verbose and self.expected is not None:
                print("  Expected:")
                expected_lines = self.expected.split("\n")
                for i, line in enumerate(expected_lines[:5], 1):
                    print(f"    {i}: {line}")
                if len(expected_lines) > 5:
                    total = len(expected_lines)
                    print(f"    ... ({total} total lines)")

                print("  Actual:")
                actual_lines = self.actual.split("\n")
                for i, line in enumerate(actual_lines[:5], 1):
                    print(f"    {i}: {line}")
                if len(actual_lines) > 5:
                    total = len(actual_lines)
                    print(f"    ... ({total} total lines)")


class ExpectTestRunner:
    def __init__(self, test_dir: str, update: bool = False):
        self.test_dir = test_dir
        self.update = update
        self.tests: list[ExpectTest] = []
        self.results: dict[str, bool] = {}

    def discover_tests(self) -> None:
        """Find all .lox files and create ExpectTest instances."""
        lox_files = sorted(glob.glob(os.path.join(self.test_dir, "*.lox")))

        if not lox_files:
            print(f"No .lox files found in {self.test_dir}")
            return

        for lox_file in lox_files:
            expected_file = lox_file.replace(".lox", ".expected")
            test = ExpectTest(lox_file, expected_file)
            self.tests.append(test)

    def run_all(self) -> None:
        """Run all discovered tests."""
        if not self.tests:
            print("No tests to run")
            return

        print(f"Running {len(self.tests)} tests...\n")

        for test in self.tests:
            test.run()

            # Load expected output
            expected_exists = test.load_expected()

            if not expected_exists:
                # First run: generate expected output
                test.save_expected()
                test.passed = True
                self.results[test.lox_file] = True
                print(f"⊙ {os.path.basename(test.lox_file)} (generated)")
            elif self.update:
                # Update mode: regenerate expected output
                test.save_expected()
                test.passed = True
                self.results[test.lox_file] = True
                print(f"⟳ {os.path.basename(test.lox_file)} (updated)")
            else:
                # Normal mode: compare actual vs expected
                test.compare()
                self.results[test.lox_file] = test.passed
                test.print_result(verbose=False)

        self.print_summary()

    def print_summary(self) -> None:
        """Print test summary."""
        passed = sum(1 for v in self.results.values() if v)
        failed = len(self.results) - passed

        print("\n" + "=" * 60)
        print(f"Summary: {passed} passed, {failed} failed out of {len(self.results)}")
        print("=" * 60)

        if failed > 0:
            print("\nFailing tests:")
            for test in self.tests:
                if not self.results.get(test.lox_file, False):
                    print(f"  - {os.path.basename(test.lox_file)}")
            sys.exit(1)

    def show_diff(self, test_name: str) -> None:
        """Show detailed diff for a specific test."""
        test = next(
            (t for t in self.tests if os.path.basename(t.lox_file) == test_name),
            None,
        )
        if not test:
            print(f"Test not found: {test_name}")
            return

        test.run()
        if not test.load_expected():
            print(f"No expected output for {test_name}")
            return

        print(f"\n{'Test':<40} {test_name}")
        print("=" * 80)
        print("\n[EXPECTED]")
        print("-" * 80)
        print(test.expected)
        print("\n[ACTUAL]")
        print("-" * 80)
        print(test.actual)
        print("=" * 80)


def main():
    import argparse

    parser = argparse.ArgumentParser(
        description="Expect test framework for plox",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python expect_tests.py              # Run tests
  python expect_tests.py --update     # Regenerate all expected files
  python expect_tests.py --diff scope.lox  # Show diff for specific test
        """,
    )
    parser.add_argument(
        "--update",
        action="store_true",
        help="Regenerate expected output files",
    )
    parser.add_argument(
        "--diff",
        metavar="TEST",
        help="Show detailed diff for a specific test",
    )

    args = parser.parse_args()

    # Change to test directory
    os.chdir(os.path.dirname(os.path.abspath(__file__)))

    runner = ExpectTestRunner("../testFiles", update=args.update)
    runner.discover_tests()

    if args.diff:
        runner.run_all()
        runner.show_diff(args.diff)
    else:
        runner.run_all()


if __name__ == "__main__":
    main()
