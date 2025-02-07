import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws Exception {
        while (true) {
            System.out.print("$ ");

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            String[] parts = input.split(" ");

            switch (parts[0]) {
                case "echo":
                    String result = Stream.of(parts)
                        .skip(1) // do not repeat "echo"
                        .collect(Collectors.joining(" "));
                    System.out.println(result);
                    break;
                case "exit":
                    if (parts.length == 2 && parts[1].equals("0")) {
                        System.exit(0);
                    }
                    // Fall-through in case the command is different from "exit 0"
                default:
                    System.out.println(input + ": command not found");
            }
        }
    }
}
