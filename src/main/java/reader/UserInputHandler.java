package reader;

import shared.ControlledLogger;
import shared.NodesInfoContainer;
import tournament.Tournament;
import java.util.Scanner;

public class UserInputHandler implements Runnable {
    private Tournament tournament;
    private NodesInfoContainer nodesInfoContainer;

    public UserInputHandler(NodesInfoContainer nodesInfoContainer) {
        this.nodesInfoContainer = nodesInfoContainer;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    private void handleCommand(String command) {
        switch (command.toLowerCase()) {
            case "quit": {
                System.out.println("You typed '" + command + "'");
                if (tournament.checkIfSelfPlayedWithEveryone(nodesInfoContainer)) {
                    tournament.setSelfActiveStatus(false);
                    nodesInfoContainer.setNodeToActivePlayer(tournament.getSelfNode(), false);
                    tournament.printTournamentState();
                } else {
                    System.out.println("You haven't finished the tournament yet.");
                }
                break;
            }

            case "join": {
                System.out.println("You typed '" + command + "'");
                tournament.setSelfActiveStatus(true);
                nodesInfoContainer.setNodeToActivePlayer(tournament.getSelfNode(), true);
                ControlledLogger.allowLogs();
                break;
            }

            case "": {
                if (ControlledLogger.areLogsAllowed()) {
                    ControlledLogger.disableLogs();
                    System.out.println("Please, enter one of commands:");
                    System.out.println("JOIN to join the tournament");
                    System.out.println("QUIT to quit the tournament:");
                    System.out.println("EXIT close the application");
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
        while (true) {
            Scanner keyboard = new Scanner(System.in);
            String input = keyboard.nextLine();
            if (input != null) {
                handleCommand(input);
            }
        }
    }
}
