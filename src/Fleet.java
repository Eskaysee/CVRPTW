import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class Fleet {

    private final float elitismRatio;
    private final float mutationRatio;
    private final float crossoverRatio;
    private Vehicle[] fleet;
    private int numberOfCrossoverOperations = 0;
    private int numberOfMutationOperations = 0;
    private final Map<Integer, Customer> clients;
    private Vector bestPosition;
    private double bestEvaluation;

    public Fleet() {
        this.elitismRatio = Configuration.INSTANCE.elitismRatio;
        this.mutationRatio = Configuration.INSTANCE.mutationRatio;
        this.crossoverRatio = Configuration.INSTANCE.crossoverRatio;
        this.clients = Configuration.INSTANCE.customers;

        ArrayList<Vehicle> vehicles = new ArrayList<>();
        ArrayList<Customer> customers = new ArrayList<>(clients.values());
        Customer depot = new Customer();
        customers.remove(depot);
        while (customers.size() > 0) {
            Collections.shuffle(customers, new MersenneTwister());
            customers.add(0, depot);
            if (vehicles.isEmpty())
                vehicles.add(new Vehicle(customers,0));
            else vehicles.add(new Vehicle(customers, vehicles.get(0).getGenes().length));
            for (int i : vehicles.get(vehicles.size()-1).getGenes()) {
                if (i != 0)
                    customers.remove(clients.get(i));
            }
            customers.remove(depot);
        }
    }

    //Constructor

    public int getNumberOfCrossoverOperations() {
        return numberOfCrossoverOperations;
    }

    public int getNumberOfMutationOperations() {
        return numberOfMutationOperations;
    }

    public void evolve() {
        Vehicle[] chromosomes = new Vehicle[fleet.length];

        int index = Math.round(fleet.length * elitismRatio);
        System.arraycopy(fleet, 0, chromosomes, 0, index);

        while (index < chromosomes.length) {
            if (Configuration.INSTANCE.randomGenerator.nextFloat() <= crossoverRatio) {
                Vehicle[] parents = selectParents();
                Vehicle[] children = parents[0].crossover(parents[1]);
                numberOfCrossoverOperations++;

                if (Configuration.INSTANCE.randomGenerator.nextFloat() <= mutationRatio) {
                    chromosomes[(index++)] = children[0].mutation();
                    numberOfMutationOperations++;
                } else {
                    chromosomes[(index++)] = children[0];
                }

                if (index < chromosomes.length) {
                    if (Configuration.INSTANCE.randomGenerator.nextFloat() <= mutationRatio) {
                        chromosomes[index] = children[1].mutation();
                        numberOfMutationOperations++;
                    } else {
                        chromosomes[index] = children[1];
                    }
                }
            } else if (Configuration.INSTANCE.randomGenerator.nextFloat() <= mutationRatio) {
                chromosomes[index] = fleet[index].mutation();
                numberOfMutationOperations++;
            } else {
                chromosomes[index] = fleet[index];
            }

            index++;
        }

        Arrays.sort(chromosomes);
        fleet = chromosomes;
    }

    public Vehicle[] getFleet() {
        Vehicle[] chromosomes = new Vehicle[fleet.length];
        System.arraycopy(fleet, 0, chromosomes, 0, fleet.length);
        return chromosomes;
    }

    private Vehicle[] selectParents() {
        Vehicle[] parents = new Vehicle[2];

        for (int i = 0; i < 2; i++) {
            parents[i] = fleet[Configuration.INSTANCE.randomGenerator.nextInt(fleet.length)];
            for (int j = 0; j < 3; j++) {
                int index = Configuration.INSTANCE.randomGenerator.nextInt(fleet.length);
                if (fleet[index].compareTo(parents[i]) < 0) {
                    parents[i] = fleet[index];
                }
            }
        }

        return parents;
    }

    private void updateGlobalBest(Vehicle vehicle) {
        if (vehicle.getBestEvaluation() < bestEvaluation) {
            bestPosition = vehicle.getBestPosition();
            bestEvaluation = vehicle.getBestEvaluation();
        }
    }

    private void updateVelocity(Vehicle vehicle) {
        Vector oldVelocity = vehicle.getVelocity();
        Vector pBest = vehicle.getBestPosition();
        Vector gBest = bestPosition.clone();
        Vector pPosition = vehicle.getPosition();

        double randomNumber01 = Configuration.INSTANCE.randomGenerator.nextDouble();
        double randomNumber02 = Configuration.INSTANCE.randomGenerator.nextDouble();

        Vector newVelocity = oldVelocity.clone();
        newVelocity.multiply(Configuration.INSTANCE.inertia);

        pBest.subtract(pPosition);
        pBest.multiply(Configuration.INSTANCE.cognitiveRatio);
        pBest.multiply(randomNumber01);
        newVelocity.add(pBest);

        gBest.subtract(pPosition);
        gBest.multiply(Configuration.INSTANCE.socialRatio);
        gBest.multiply(randomNumber02);
        newVelocity.add(gBest);

        vehicle.setVelocity(newVelocity);
    }
}