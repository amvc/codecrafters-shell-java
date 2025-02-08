import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Main {
    // Maps from the command name to the action to perform with the arguments
    private static final Map<String, Consumer<List<String>>> builtins = Map.of(
            "echo", arguments -> {
                String result = String.join(" ", arguments);
                System.out.println(result);
            },
            "exit", arguments -> {
                if (arguments.size() == 1 && arguments.getFirst().equals("0")) {
                    System.exit(0);
                }
                else {
                    commandNotFound("exit", arguments);
                }
            }
    );

    // List of executables found in $PATH
    private static final List<String> executables = new ArrayList<>();

    static {
        // Read each dir in PATH...
        Stream.of(System.getenv("PATH").split(":")).forEach(dir -> {
            try (Stream<Path> files = Files.list(Paths.get(dir))) {
                // ...and add each file to 'executables'
                files.forEach(file -> executables.add(file.toString()));
            } catch (IOException e) {
                // Skip this directory
            }
        });
    }

    private static void commandNotFound(String command, List<String> args) {
        if (args.isEmpty()) {
            System.out.println(command + ": command not found");
        } else {
          System.out.println(command + " " + String.join(" ", args) + ": command not found");
        }
    }

    public static void main(String[] args) throws Exception {
        while (true) {
            System.out.print("$ ");

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            List<String> parts = Arrays.asList(input.split(" "));
            String command = parts.getFirst();
            List<String> arguments = parts.subList(1, parts.size());

            if (builtins.containsKey(command)) {
                builtins.get(command).accept(arguments);
            } else if (command.equals("type")) {
                String candidate = arguments.getFirst();
                Optional<String> executable = executables.stream()
                        .filter(e -> e.endsWith("/" + candidate))
                        .findFirst();
                if (builtins.containsKey(candidate) || candidate.equals("type")) {
                    System.out.println(candidate + " is a shell builtin");
                } else if (executable.isPresent()) {
                    System.out.println(candidate + " is " + executable.get());
                }
                else {
                    System.out.println(candidate + ": not found");
                }
            } else {
                commandNotFound(command, arguments);
            }
        }
    }
}
