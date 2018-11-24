package reader;

import shared.ControlledLogger;
import shared.NodesInfoContainer;
import tournament.Tournament;

import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.util.Scanner;

public class UserInputHandler implements Runnable {
    private BufferedReader reader;
    private Tournament tournament;
    private NodesInfoContainer nodesInfoContainer;

    public UserInputHandler(NodesInfoContainer nodesInfoContainer) {
        reader = new BufferedReader(new InputStreamReader(System.in));
        this.nodesInfoContainer = nodesInfoContainer;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public void handleCommand(String command) {
        switch (command.toLowerCase()) {
            case "quit": {
                System.out.println("You typed " + command);
                tournament.setSelfActiveStatus(false);
                break;
            }

            case "join": {
                System.out.println("You typed " + command);
                tournament.setSelfActiveStatus(true);
                nodesInfoContainer.setNodeToActivePlayer(tournament.getSelfNode());
                ControlledLogger.allowLogs();
                break;
            }

            case "": {
                if (ControlledLogger.areLogsAllowed()) {
                    System.out.println("DISABLE LOGS");
                    ControlledLogger.disableLogs();
                    System.out.println("Enter command QUIT to quit the tournament:");
                } else {
                    System.out.println("ENABLE LOGS");
                    ControlledLogger.allowLogs();
                }
                break;
            }

            case "exit": {
                System.exit(0);
            }

            default: {
                System.out.println("Can't recognize the command '" + command + "'");
                break;
            }
        }
    }

    @Override
    public void run() {

        String line;

        while (true) {
            Scanner keyboard = new Scanner(System.in);
            boolean exit = false;
            while (!exit) {
                String input = keyboard.nextLine();

                if (input != null) {
                    handleCommand(input);
                }

            }
//            keyboard.close();
        }
    }
}
