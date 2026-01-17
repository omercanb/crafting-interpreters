package tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenerateAST {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output_dir>");
            System.exit(64);
        } else {
            String outputDir = args[0];
            defineAst(outputDir, "Expr", Arrays.asList(
                    "Binary : Expr left, Token op, Expr right",
                    "Unary : Token op, Expr right",
                    "Grouping : Expr expr",
                    "Literal : Object value"));
        }
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        // Create path you want to write to and initialize a writer for the file
        String path = outputDir + '/' + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");
        // Define an abstract basename class
        writer.println("package lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("public abstract class Expr {");
        // Extract the class names and fields for each type
        var classNames = new ArrayList<String>();
        var fields = new ArrayList<ArrayList<TypeNamePair>>();
        for (var type : types) {
            String className = type.split(":")[0].trim();
            classNames.add(className);
            String fieldsString = type.split(":")[1].trim();
            String[] pairs = fieldsString.split(", ");
            fields.add(new ArrayList<>());
            for (var pair : pairs) {
                String fieldType = pair.split(" ")[0];
                String fieldName = pair.split(" ")[1];
                var typeNamePair = new TypeNamePair(fieldType, fieldName);
                fields.get(fields.size() - 1).add(typeNamePair);
            }
        }
        // Define the visitor interface
        // with method for every class
        defineVisitor(writer, baseName, classNames);
        // Define the abstract accept method
        writer.println("    abstract <R> R accept(Visitor<R> visitor);");

        for (int i = 0; i < classNames.size(); i++) {
            defineType(writer, baseName, classNames.get(i), fields.get(i));
        }
        // For each type
        // : Extract the class name and fields
        // : Define each type from the name and fields and add it to the expr class

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> typeNames) {
        // Create an interface with the name visitor
        writer.println("    interface Visitor<R> {");
        for (var typeName : typeNames) {
            // Don't know why we currently use the baseName for, will remove when I
            // understand later
            // add visitType(Type t); methods for each classname
            // These visits are generic over a type R as different visitors can need
            // different types
            writer.println(
                    "        R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }
        writer.println("    }");

    }

    private static void defineType(PrintWriter writer, String baseName, String className, List<TypeNamePair> fields) {
        // Define the class that inherits from base name
        writer.println("    static class " + className + " extends " + baseName + " {");
        // Define the fields (as final)
        for (TypeNamePair typeAndName : fields) {
            writer.println("        final " + typeAndName.type + " " + typeAndName.name + ";");
        }
        writer.println();

        // Create the string to write in the constructor parameters by combining all
        // field types and names
        String constructorParameters = "";
        for (TypeNamePair typeAndName : fields) {
            constructorParameters += typeAndName.type + " " + typeAndName.name + ", ";
        }
        // Remove the trailing comma and space
        if (constructorParameters.length() >= 2) {
            int trailingCommaIdx = constructorParameters.length() - 2;
            constructorParameters = constructorParameters.substring(0, trailingCommaIdx);
        }

        // Define the constructor that sets the fields to their values
        writer.println("        " + className + "(" + constructorParameters + ") {");
        for (TypeNamePair typeAndName : fields) {
            writer.println("            this." + typeAndName.name + " = " + typeAndName.name + "; ");
        }
        writer.println("        }");

        // Implement the abstract accept method from the visitor pattern
        writer.println("        @Override");
        writer.println("        <R> R accept(Visitor<R> visitor) {");
        writer.println("            return visitor.visit" + className + baseName + "(this);");
        writer.println("        }");

        writer.println("    }");

    }

}

class TypeNamePair {
    public final String type;
    public final String name;

    TypeNamePair(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String toString() {
        return this.type + " " + this.name;
    }
}
