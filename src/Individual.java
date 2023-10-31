import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class Individual implements Comparable<Individual> {
    private final int[] chromosome;
    private final double fitness;
    private final Vehicle[] fleet;
    private boolean isValid = true;
    private final Map<Integer, Customer> clients = Configuration.INSTANCE.customers;
    private int overloaded, late;

    public Individual(int fleetSize) {
        ArrayList<Vehicle> trucks = new ArrayList<>(fleetSize);
        ArrayList<Customer> customers = new ArrayList<>(clients.values());
        customers.remove(0);

        int seats = 0;
        if (Configuration.INSTANCE.numberOfCustomers%fleetSize==0)
            seats = Configuration.INSTANCE.numberOfCustomers/fleetSize;
        else seats = Configuration.INSTANCE.numberOfCustomers/fleetSize + 1;

        while (!customers.isEmpty()) {
            Collections.shuffle(customers, Configuration.INSTANCE.randomGenerator);
            trucks.add(new Vehicle(customers, seats));
        }
        trucks.sort(null);

        this.fleet = trucks.toArray(new Vehicle[0]);
        this.chromosome = setChromosome();
        this.fitness = calculateFitness();
    }

    public Individual(int[] chromosome) {
        int fleetSize = Arrays.stream(chromosome).max().getAsInt() + 1;
        ArrayList<Customer>[] vehicleLoads = new ArrayList[fleetSize];
        for (int i=0; i<fleetSize; i++) vehicleLoads[i] = new ArrayList<>();
        for (int i=0; i<chromosome.length; i++) {
            vehicleLoads[chromosome[i]].add(clients.get(i+1));
        }
        int i = 0;
        fleetSize = (int) Arrays.stream(vehicleLoads).filter(x -> !x.isEmpty()).count();
        this.fleet = new Vehicle[fleetSize];
        for (ArrayList<Customer> load : vehicleLoads) {
            if (load.isEmpty()) continue;
            load.sort(null);
            load.add(0,clients.get(0));
            load.add(clients.get(0));
            fleet[i++] = new Vehicle(load.stream().mapToInt(Customer::getId).toArray());
        }
        this.fitness = calculateFitness();
        this.chromosome = setChromosome();
    }

    private int[] setChromosome() {
        int[] chromosome = new int[Configuration.INSTANCE.numberOfCustomers];
        for (int i=0; i<fleet.length; i++) {
            for (int customer : fleet[i].getLoad()) {
                if (customer != 0) chromosome[customer - 1] = i;
            }
        }
        return chromosome;
    }

    private void repairFleet() {
        ArrayList<Integer> unserved = new ArrayList<>();
        for (Vehicle truck : fleet) {
            unserved.addAll(truck.makeValid());
        }
        for (int i=0; i<unserved.size(); i++) {
            for (Vehicle truck : fleet) {
                if (truck.insert(unserved.get(i))) {
                    unserved.remove(i);
                    i--;
                    break;
                }
            }
        }
        if (!unserved.isEmpty()) {
            for (int i=0; i<unserved.size(); i++) {
                for (Vehicle truck : fleet) {
                    if (truck.forcedInsert(unserved.get(i))) {
                        unserved.remove(i);
                        i--;
                        break;
                    }
                }
            }
        }
        if  (!unserved.isEmpty()) isValid = false;
        Arrays.sort(fleet);
    }

    // fitness
    private double calculateFitness() {
        repairFleet();
        if (!isValid) return 500000;
        double distance = 0;
        int late=0, overloaded = 0;
        for (Vehicle truck : fleet) {
            distance += truck.getDistance();
            late += truck.getLate();
            if (truck.overloaded()) overloaded++;
        }
        this.late = late; this.overloaded = overloaded;
        return distance + 200*late + 25*fleet.length;
    }

    // crossover
    public Individual[] crossover(Individual individual) {
        int pivot = Configuration.INSTANCE.randomGenerator.nextInt(this.chromosome.length);

        int[] child01 = new int[chromosome.length];
        int[] child02 = new int[chromosome.length];

        System.arraycopy(this.getGenes(), 0, child01, 0, pivot);
        System.arraycopy(individual.getGenes(), pivot, child01, pivot, child01.length - pivot);

        System.arraycopy(individual.getGenes(), 0, child02, 0, pivot);
        System.arraycopy(this.chromosome, pivot, child02, pivot, child02.length - pivot);

        return new Individual[]{new Individual(child01),
                new Individual(child02)};
    }

    // mutation
    public Individual mutation() {
        ArrayList<Integer> subsetList = new ArrayList<>(36);
        for (int i=32; i<=66; i++) subsetList.add(chromosome[i]);
        Collections.shuffle(subsetList, Configuration.INSTANCE.randomGenerator);
        int[] mutant = new int[chromosome.length];
        System.arraycopy(chromosome,0,mutant,0,32);
        System.arraycopy(subsetList.stream().mapToInt(i -> i).toArray(),0,mutant,32,35);
        System.arraycopy(chromosome,67,mutant,67,33);
        return new Individual(mutant);
    }

    public int[] getGenes() {
        return chromosome;
    }

    public boolean validity() {
        return isValid;
    }

    public int getFleetSize() {
        return fleet.length;
    }

    public double getFitness() {
        return fitness;
    }

    public double getDistance() {
        double distance = 0;
        for (Vehicle truck : fleet) distance += truck.getDistance();
        return distance;
    }

    @Override
    public int compareTo(Individual other) {
        return Double.compare(this.fitness, other.getFitness());
    }

    public boolean equals(Individual other) {
        return Arrays.equals(this.chromosome, other.getGenes());
    }
}