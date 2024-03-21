package gridwars.starter;

import cern.ais.gridwars.api.Coordinates;
import cern.ais.gridwars.api.UniverseView;
import cern.ais.gridwars.api.bot.PlayerBot;
import cern.ais.gridwars.api.command.MovementCommand;


import java.util.ArrayList;
import java.util.List;

public class ThunderBot_Brutus implements PlayerBot {
    private double thresholdForExpansion;
    private boolean distributeEvenly;

    public ThunderBot_Brutus() {
        this.distributeEvenly = true;
        this.thresholdForExpansion = 5.0; // threshold to start expanding to adjacent cell
    }

    public ThunderBot_Brutus(double expansionThreshold, boolean distributeEvenly) {
        this.thresholdForExpansion = expansionThreshold;
        this.distributeEvenly = distributeEvenly;
    }

    @Override
    public void getNextCommands(UniverseView universe, List<MovementCommand> commandQueue) {
        List<Coordinates> controlledCells = universe.getMyCells();
        thresholdForExpansion = Math.min(thresholdForExpansion + 0.15, 24); // Gradually increase the threshold up to a maximum

        for (Coordinates cell : controlledCells) {
            int population = universe.getPopulation(cell);

            if (population > thresholdForExpansion) {
                List<MovementCommand.Direction> expansionDirections = determineExpansionDirections(cell, universe);
                int unitsPerDirection = distributeEvenly ? (population - 5) / expansionDirections.size() : population / (expansionDirections.size() + 4);

                for (MovementCommand.Direction direction : expansionDirections) {
                    commandQueue.add(new MovementCommand(cell, direction, unitsPerDirection));
                }
            }
        }
    }

    private List<MovementCommand.Direction> determineExpansionDirections(Coordinates cell, UniverseView universe) {
        List<MovementCommand.Direction> directions = new ArrayList<>();
        List<MovementCommand.Direction> hostileDirections = new ArrayList<>();
        int shortestEncounterDistance = Integer.MAX_VALUE;
        
        for (MovementCommand.Direction direction : MovementCommand.Direction.values()) {
            int distance = 1;
            while (universe.belongsToMe(cell.getRelative(distance, direction)) && distance < universe.getUniverseSize()) {
                distance++;
                if (!universe.isEmpty(cell.getRelative(distance, direction)) && !universe.belongsToMe(cell.getRelative(distance, direction)) && distance < 17) {
                    hostileDirections.add(direction);
                }
            }
            if (distance < shortestEncounterDistance) {
                shortestEncounterDistance = distance;
                directions.clear();
                directions.add(direction);
                continue;
            } else if (distance == shortestEncounterDistance) {
                continue;
            }
           
        }
        directions.addAll(hostileDirections); // Prioritize directions leading to hostile cells for aggressive expansion
        return directions;
    }
}
