package ServerInfo;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Scanner;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

public class ServerSettingsParser {

    private final String helpFilePath = "./src/main/resources/help.txt";
    private Scanner scanner = new Scanner(System.in);
    private ServerInfo serverInfo;
    private String[] inputArr;
    private int port;
    private boolean isVerbose;
    private String filePathToDir;

    public ServerInfo getServerInfo() {
        displayWelcome();
        getInput();
        return serverInfo;
    }

    private void displayWelcome() {
        System.out.println("httpfs");
    }

    private void getInput() {
        System.out.print("> ");
        String input = scanner.nextLine();
        if (input.equalsIgnoreCase("httpfs help")) {
            displayHelp();
            getInput();
        }
        setInputArr(input);
        collectServerInfo();
        scanner.close();
    }
    
    private void displayHelp() {
        try {
            String help = Files.readString(Path.of(helpFilePath));
            System.out.println(help);
        } catch (Exception e) {
            throw new RuntimeException("error reading help file");
        }
    }

    private void setInputArr(String input) {
        inputArr = input.split(" ");
    }

    private void collectServerInfo() {
        verifyFirstArg();
        parseIsVerbose();
        parsePort();
        parseDirectory();
        createDirectoryIfNeeded();
        buildServerInfo();
    }

    private void verifyFirstArg() {
        if (!inputArr[0].equalsIgnoreCase("httpfs")) {
            throw new IllegalArgumentException("first argument not httpfs");
        }
    }

    private void parseIsVerbose() {
        Arrays.stream(inputArr)
                .filter(option -> option.equalsIgnoreCase("-v"))
                .findAny()
                .ifPresentOrElse(option -> this.isVerbose = true, () -> this.isVerbose = false);
    }

    private void parsePort() {
        IntStream.range(0, inputArr.length - 1)
                .filter(indexIsSelectedOption("-p"))
                .findFirst()
                .ifPresentOrElse(setPortToUserInput(), handleNoPortSpecified());
    }

    private IntPredicate indexIsSelectedOption(String option) {
        return index -> inputArr[index].equalsIgnoreCase(option);
    }


    private IntConsumer setPortToUserInput() {
        return index -> {
            try {
                this.port = Integer.parseInt(inputArr[index + 1]);
            } catch (Exception e) {
                this.port = ServerInfo.defaultPort;
            }
        };
    }

    private Runnable handleNoPortSpecified() {
        return () -> this.port = ServerInfo.defaultPort;
    }

    private void parseDirectory() {
        IntStream.range(0, inputArr.length - 1)
                .filter(indexIsSelectedOption("-d"))
                .findFirst()
                .ifPresentOrElse(setFilePathToUserInput(), setFilePathToDefault());
    }

    private IntConsumer setFilePathToUserInput() {
        return index -> filePathToDir = ServerInfo.defaultPath + inputArr[index + 1];
    }

    private Runnable setFilePathToDefault() {
        return () -> filePathToDir = ServerInfo.defaultPath;
    }

    private void createDirectoryIfNeeded() {
        File file = new File(filePathToDir);
        file.mkdirs();
    }

    private void buildServerInfo() {
        this.serverInfo = new ServerInfo(this.isVerbose, this.port, this.filePathToDir);
    }
}
