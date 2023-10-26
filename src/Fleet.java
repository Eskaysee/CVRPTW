import java.util.*;

public class Fleet {

    private final int fleetSize;
    private final float elitismRatio;
    private final float mutationRatio;
    private final float crossoverRatio;
    private Vehicle[] fleet;
    private int numberOfCrossoverOperations = 0;
    private int numberOfMutationOperations = 0;
    private final Map<Integer, Customer> clients;
    private Map<Vehicle, Float> wheel;

    public Fleet(int fleetSize, float elitismRatio, float mutationRatio, float crossoverRatio) {
        this.fleetSize = fleetSize;
        this.elitismRatio = elitismRatio;
        this.mutationRatio = mutationRatio;
        this.crossoverRatio = crossoverRatio;
        this.clients = Configuration.INSTANCE.customers;

        int geneLength=0;
        if (Configuration.INSTANCE.numberOfCustomers%fleetSize==0)
            geneLength = Configuration.INSTANCE.numberOfCustomers/fleetSize;
        else geneLength = Configuration.INSTANCE.numberOfCustomers/fleetSize + 1;

        ArrayList<Vehicle> trucks = new ArrayList<>();
        ArrayList<Customer> customers = new ArrayList<>(clients.values());
        customers.remove(0);
        while (customers.size() > 0) {
            Collections.shuffle(customers, new MersenneTwister());
            trucks.add(new Vehicle(customers, geneLength));
        }
        fleet = trucks.toArray(new Vehicle[0]);
        Arrays.sort(fleet);
        System.out.println();
    }

    //Constructor

    public int getNumberOfCrossoverOperations() {
        return numberOfCrossoverOperations;
    }

    public int getNumberOfMutationOperations() {
        return numberOfMutationOperations;
    }

    public void evolve() {
        Vehicle[] trucks = new Vehicle[fleet.length];

        int index = Math.round(fleet.length * elitismRatio);
        System.arraycopy(fleet, 0, trucks, 0, index);
        ArrayList<Vehicle> remainingTrucks = new ArrayList<>(Arrays.asList(Arrays.copyOfRange(fleet, index, fleet.length)));

        while (index < trucks.length) {
            if (Configuration.INSTANCE.randomGenerator.nextFloat() <= crossoverRatio) {
                Vehicle[] parents = selectParents(remainingTrucks);
                Vehicle[] children = parents[0].crossover(parents[1]);
                numberOfCrossoverOperations++;

                if (Configuration.INSTANCE.randomGenerator.nextFloat() <= mutationRatio) {
                    trucks[(index++)] = children[0].mutation();
                    numberOfMutationOperations++;
                } else {
                    trucks[(index++)] = children[0];
                }

                if (index < trucks.length) {
                    if (Configuration.INSTANCE.randomGenerator.nextFloat() <= mutationRatio) {
                        trucks[index] = children[1].mutation();
                        numberOfMutationOperations++;
                    } else {
                        trucks[index] = children[1];
                    }
                }
            } else if (Configuration.INSTANCE.randomGenerator.nextFloat() <= mutationRatio) {
                trucks[index] = remainingTrucks.remove(0).mutation();
                numberOfMutationOperations++;
            } else {
                trucks[index] = remainingTrucks.remove(0);
            }

            index++;
        }
        fleet = trucks;
        Arrays.sort(fleet);
    }

    public Vehicle[] getFleet() {
        Vehicle[] chromosomes = new Vehicle[fleet.length];
        System.arraycopy(fleet, 0, chromosomes, 0, fleet.length);
        return chromosomes;
    }

    public double totalDistance() {
        double sum = 0;
        for (Vehicle vehicle : fleet) {
            sum += vehicle.getDistance();
        }
        return sum;
    }

    private Vehicle[] selectParents(ArrayList<Vehicle> trucks) {
        Vehicle[] parents = new Vehicle[2];
        if (trucks.size()==1) {
            parents[0] = trucks.remove(0);
            parents[1] = parents[0];
            return parents;
        }
        parents[0] = trucks.remove(0);
        updateWheel(trucks); Vehicle previousTruck = null;
        float spin = Configuration.INSTANCE.randomGenerator.nextFloat();
        for (Vehicle vehicle : trucks) {
            if (spin <= wheel.get(vehicle)) {
                if (previousTruck == null) {
                    parents[1] = vehicle;
                    break;
                } else if (wheel.get(previousTruck) < spin) {
                    parents[1] = vehicle;
                    break;
                } else previousTruck = vehicle;
            } else previousTruck = vehicle;
        }
        trucks.remove(parents[1]);
        return parents;
    }

    private void updateWheel(ArrayList<Vehicle> trucks) {
        float totalFitness = 0;
        for (Vehicle truck : trucks)
            totalFitness += truck.getFitness();
        Map<Vehicle, Float> probs = new LinkedHashMap<>();
        float cdf = 0.0F;
        for (Vehicle vehicle : trucks) {
            cdf += (vehicle.getFitness() / totalFitness);
            probs.put(vehicle, cdf);
        }
        this.wheel = probs;
    }
}