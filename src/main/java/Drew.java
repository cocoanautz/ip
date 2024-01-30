import drew.exceptions.InsufficientArgumentsException;
import drew.exceptions.UnknownCommandException;
import drew.tasktypes.Deadline;
import drew.tasktypes.Event;
import drew.tasktypes.Task;
import drew.tasktypes.Todo;

import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
/**
 * Main chatbot class. Contains the logic of the chatbot.
 *
 * @author cocoanautz
 */
public class Drew {
    /**
     * Checks the identity of the command.
     *
     * @param input String containing full user input.
     * @return Command specified by user input.
     */
    public static Command checkCommandIdentity(String input) throws UnknownCommandException,
            InsufficientArgumentsException, IllegalArgumentException {
        int inputLength = input.length();
        Command userCommand;
        if (inputLength == 4 && input.substring(0, 4).equalsIgnoreCase("list")) {
            userCommand = Command.LIST;
        } else if (inputLength >= 4 && input.substring(0, 4).equalsIgnoreCase("mark")) {
            userCommand = Command.MARK;
        } else if (inputLength >= 6 && input.substring(0, 6).equalsIgnoreCase("unmark")) {
            userCommand = Command.UNMARK;
        } else if (inputLength >= 6 && input.substring(0, 6).equalsIgnoreCase("delete")) {
            userCommand = Command.DELETE;
        } else if (inputLength >= 4 && input.substring(0, 4).equalsIgnoreCase("todo")) {
            userCommand = Command.TODO;
        } else if (inputLength >= 8 && input.substring(0, 8).equalsIgnoreCase("deadline")) {
            userCommand = Command.DEADLINE;
        } else if (inputLength >= 5 && input.substring(0, 5).equalsIgnoreCase("event")) {
            userCommand = Command.EVENT;
        } else {
            userCommand = Command.UNKNOWN;
        }

        String inputNoWhitespace = input.replaceAll("\\s", "");
        int inputNoWhitespaceLength = inputNoWhitespace.length();
        int backslashCount = 0;
        for (int i = 0; i < inputNoWhitespaceLength; i++) {
            char c = inputNoWhitespace.charAt(i);
            if (c == '/') {
                backslashCount++;
            }
        }
        switch (userCommand) {
        case UNKNOWN:
            throw new UnknownCommandException("Command not recognized");
        case LIST:
            if (inputNoWhitespaceLength != 4) {
                throw new IllegalArgumentException("No arguments are needed");
            }
            break;
        case TODO:
            if (inputNoWhitespaceLength == 4) {
                throw new InsufficientArgumentsException("'Todo task' cannot be empty");
            }
            break;
        case MARK:
            if (inputNoWhitespaceLength == 4) {
                throw new InsufficientArgumentsException("'Mark index' cannot be empty");
            }
            break;
        //valid integer will be checked later
        case UNMARK:
            if (inputNoWhitespaceLength == 6) {
                throw new InsufficientArgumentsException("'Unmark index' cannot be empty");
            }
            break;
        //valid integer will be checked later
        case DELETE:
            if (inputNoWhitespaceLength == 6) {
                throw new InsufficientArgumentsException("'Delete index' cannot be empty");
            }
            break;
        //valid integer will be checked later
        case DEADLINE:
            if (inputNoWhitespaceLength == 8) {
                throw new InsufficientArgumentsException("'Deadline task' cannot be empty");
            }
            if (backslashCount != 1) {
                throw new IllegalArgumentException("Number of '/' is invalid");
            }
            break;
        case EVENT:
            if (inputNoWhitespaceLength == 5) {
                throw new InsufficientArgumentsException("'Event task' cannot be empty");
            }
            if (backslashCount != 2) {
                throw new IllegalArgumentException("Number of '/' is invalid");
            }
            break;
        }
        return userCommand;
    }
    public static Task parseFromSave(String input) {
        String[] args = input.split("\\|");
        for (int i = 0; i < args.length; i ++) {
            args[i] = args[i].trim();
        }
        Task task = null;
        System.out.println(args[0]);
        switch (args[0]) {
        case "T":
            task = new Todo(args[2]);
            break;
        case "D":
            task = new Deadline(args[2], args[3]);
            break;
        case "E":
            task = new Event(args[2], args[3], args[4]);
            break;
        }

        if (args[1] == "1") {
            task.setDone();
        }
        return task;
    }
    public static int readFile(String filePath, ArrayList<Task> ls) {
        File savedTasks = new File(filePath);
        System.out.println(savedTasks.getAbsolutePath());

        try {
            Scanner fileReader = new Scanner(savedTasks);
            System.out.println("Status: File found");
            while (fileReader.hasNext()) {
                String line = fileReader.nextLine();
                System.out.println(line);
                Task task = parseFromSave(line);
                System.out.println(task.statusString());
                ls.add(task);
            }
            return ls.size();
        } catch (FileNotFoundException e){
            System.out.println("Status: File not found");
            return 0;
        }
    }

    public static boolean saveFile(String filePath, ArrayList<Task> ls) {
        File savedTasks = new File(filePath);
        try {
            boolean mkdirSuccess = savedTasks.getParentFile().mkdirs();
            boolean createFileSuccess = savedTasks.createNewFile();
            System.out.println("mkdir: " + mkdirSuccess);
            System.out.println("File creation: " + createFileSuccess);
            //write to file here
            FileWriter fw = new FileWriter(filePath);
            int listLength = ls.size();
            for (int i = 0; i < listLength; i++) {
                String task = ls.get(i).toSaveFormatString();
                fw.write(task);
            }
            fw.close();
            return true;
        } catch (SecurityException e){
            System.out.println("Error: Privilege");
            return false;
        } catch (IOException e) {
            System.out.println("Error: IO");
            return false;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String DELIMITER = "______________________________________";

        ArrayList<Task> tasks = new ArrayList<>();
        int listLength = 0;

        listLength = readFile("save/tasks.txt", tasks);

        System.out.println(DELIMITER);
        System.out.println("Hello! I'm Drew");
        System.out.println("What can I do for you?");
        System.out.println(DELIMITER);

        String userInput = sc.nextLine();

        while (!userInput.equalsIgnoreCase("bye")) {
            String reply = "";

            try {
                Command userCommand;
                userCommand = checkCommandIdentity(userInput);

                switch (userCommand) {
                case LIST: {
                    reply = reply + "Here are the tasks in your list:" + "\n";
                    for (int i = 0; i < listLength; i++) {
                        reply = reply + Integer.toString(i + 1) + ". " +
                                tasks.get(i).statusString() + "\n";
                    }
                    reply = reply + String.format("Now you have %d task(s) in the list.", listLength) + "\n";
                    break;
                }
                case MARK: {
                    int taskIndex;
                    taskIndex = Integer.parseInt(userInput.substring(5));
                    if (taskIndex > listLength) {
                        throw new IllegalArgumentException("This task does not exist!");
                    }
                    tasks.get(taskIndex - 1).setDone();
                    reply = "Well done! I have marked this task as done:\n" +
                            tasks.get(taskIndex - 1).statusString() + "\n";
                    break;
                }
                case UNMARK: {
                    int taskIndex;
                    taskIndex = Integer.parseInt(userInput.substring(7));
                    if (taskIndex > listLength) {
                        throw new IllegalArgumentException("This task does not exist!");
                    }
                    tasks.get(taskIndex - 1).setNotDone();
                    reply = "Ok. I have marked this task as not done yet:\n" +
                            tasks.get(taskIndex - 1).statusString() + "\n";
                    break;
                }
                case DELETE:
                    int taskIndex;
                    taskIndex = Integer.parseInt(userInput.substring(7));
                    if (taskIndex > listLength) {
                        throw new IllegalArgumentException("This task does not exist!");
                    }
                    reply = "Ok. I have deleted this task :\n" +
                            tasks.get(taskIndex - 1).statusString() + "\n";
                    tasks.remove(taskIndex - 1);
                    listLength--;
                    break;
                case TODO: {
                    String todoDescription = userInput.substring(5);
                    Todo newTask = new Todo(todoDescription);
                    tasks.add(newTask);
                    reply = "Got it. I've added this task:\n";
                    reply = reply + newTask.statusString() + "\n";
                    listLength++;
                    reply = reply + String.format("Now you have %d task(s) in the list.", listLength) + "\n";
                    break;
                }
                case DEADLINE: {
                    int firstBackslashIndex = userInput.indexOf("/");
                    String deadlineDescription = userInput.substring(9, firstBackslashIndex);
                    String date = userInput.substring(firstBackslashIndex + 1);
                    Deadline newTask = new Deadline(deadlineDescription, date);
                    tasks.add(newTask);
                    reply = "Got it. I've added this task:\n";
                    reply = reply + newTask.statusString() + "\n";
                    listLength++;
                    reply = reply + String.format("Now you have %d task(s) in the list.", listLength) + "\n";
                    break;
                }
                case EVENT: {
                    int firstBackslashIndex = userInput.indexOf("/");
                    int secondBackslashIndex = userInput.indexOf("/", firstBackslashIndex + 1);
                    //known bug: event // does not work
                    String eventDescription = userInput.substring(6, firstBackslashIndex);
                    String startDate = userInput.substring(firstBackslashIndex + 1, secondBackslashIndex);
                    String endDate = userInput.substring(secondBackslashIndex + 1);
                    Event newTask = new Event(eventDescription, startDate, endDate);
                    tasks.add(newTask);
                    reply = "Got it. I've added this task:\n";
                    reply = reply + newTask.statusString() + "\n";
                    listLength++;
                    reply = reply + String.format("Now you have %d task(s) in the list.", listLength) + "\n";
                    break;
                }
                }
            } catch (UnknownCommandException e) {
                reply = "That does not seem to be a valid command. Please try again.\n";
            } catch (InsufficientArgumentsException e) {
                reply = e.getMessage() + "\n";
            } catch (NumberFormatException e) {
                reply = "Please enter a valid value.\n";
            } catch (IllegalArgumentException e) {
                reply = e.getMessage() + "\n";
            } finally {
                System.out.println(DELIMITER);
                System.out.print(reply);
                System.out.println(DELIMITER);

                userInput = sc.nextLine();
            }
        }
        saveFile("save/tasks.txt",tasks);
        System.out.println("Bye. Hope to see you again soon!");
    }
}
