import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Main {
    // This process builder is used to hold the shell's environment
    private static final ProcessBuilder processBuilder = new ProcessBuilder();

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
            },
            "pwd", arguments -> {
                // Prevent the command from being run with arguments
                if (!arguments.isEmpty()) {
                    commandNotFound("pwd", arguments);
                }
                // Let's implement this by reading $PWD
                String pwd = processBuilder.environment().get("PWD");
                if (pwd != null) {
                    System.out.println(pwd);
                }
            },
            "cd", arguments -> {
                if (arguments.size() == 1) {
                    String path = arguments.getFirst();
                    Path currentPath = Paths.get(processBuilder.environment().get("PWD"));
                    Path resolved = currentPath.resolve(Paths.get(path));
                    if (resolved.toFile().isDirectory()) {
                        Path absolutePath = resolved.toAbsolutePath().normalize();
                        // Let's implement this by setting $PWD
                        processBuilder.environment().put("PWD", absolutePath.toString());
                    } else {
                        System.out.println("cd: " + path + ": No such file or directory");
                    }
                } else {
                    commandNotFound("cd", arguments);
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

    private static Optional<String> findExecutable(String command) {
        return executables.stream()
                .filter(e -> e.endsWith("/" + command))
                .findFirst();
    }

    public static void main(String[] args) throws Exception {
        while (true) {
            System.out.print("$ ");

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            List<String> parts = Arrays.asList(input.split(" "));
            String command = parts.getFirst();
            List<String> arguments = parts.subList(1, parts.size());

            Optional<String> executable = findExecutable(command);

            if (builtins.containsKey(command)) {
                builtins.get(command).accept(arguments);
            } else if (command.equals("type")) {
                String candidate = arguments.getFirst();
                if (builtins.containsKey(candidate) || candidate.equals("type")) {
                    System.out.println(candidate + " is a shell builtin");
                } else if (findExecutable(candidate).isPresent()) {
                    System.out.println(candidate + " is " + findExecutable(candidate).get());
                } else {
                    System.out.println(candidate + ": not found");
                }
            } else if (executable.isPresent()) {
                String normalized = Stream.of(executable.get().split("/")).toList().getLast();
                try {
                    Process process = Runtime.getRuntime().exec(normalized + " " + String.join(" ", arguments));
                    String output = new String(process.getInputStream().readAllBytes());
                    System.out.println(output.trim());
                } catch (IOException e) {
                    // Bad luck
                }
            } else {
                commandNotFound(command, arguments);
            }
        }
    }
}
