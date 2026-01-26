# Lox Test Framework

A simple snapshot-based test framework for the Lox interpreter.

## How it works

1. **Test Discovery**: The framework finds all `.lox` files in the test directory
2. **Execution**: Each test file is run through the Lox interpreter
3. **Comparison**: Output is compared against `.expected` files
4. **Generation**: If no `.expected` file exists, one is generated automatically

## Running the tests

The easiest way is to use the included shell script:

```bash
cd test
./run_tests.sh
```

This will:
- Compile the Lox interpreter (if not already compiled)
- Compile the test framework
- Run all tests using relative paths

### Manual compilation and run

```bash
cd test
javac -d bin src/*.java
java -cp bin test.LoxTestFramework ../testFiles ../lox/lox
```

Or with default relative paths:

```bash
cd test
java -cp bin test.LoxTestFramework
```

## Directory structure

```
jlox/
├── testFiles/
│   ├── for.lox              # Test file
│   ├── for.expected         # Expected output
│   ├── while.lox
│   ├── while.expected
│   └── ...
├── test/
│   ├── run_tests.sh         # Easy test runner
│   ├── README.md
│   ├── src/
│   │   ├── LoxTestFramework.java   # package test
│   │   ├── TestRunner.java         # package test
│   │   └── TestResult.java         # package test
│   └── bin/                 # Compiled classes (generated)
└── lox/lox/
    └── (compiled Lox classes)
```

## Updating expected outputs

When you change interpreter behavior intentionally, simply delete the `.expected` files and rerun the tests. New snapshots will be generated automatically.

```bash
rm ../testFiles/*.expected
./run_tests.sh  # Generates fresh snapshots
```

## Test results

The framework prints:
- ✓ for passing tests
- ✗ for failing tests with a diff showing expected vs actual output
- A summary of pass/fail counts

Exit code 1 if any tests fail, 0 if all pass.
