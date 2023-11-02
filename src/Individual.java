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
    private double distance;

    public Individual() {
        ArrayList<Vehicle> trucks = new ArrayList<>();
        ArrayList<Customer> customers = new ArrayList<>(clients.values());
        customers.remove(0);

        int initialLength = Configuration.INSTANCE.randomGenerator.nextInt(2, 8);

        while (!customers.isEmpty()) {
            Collections.shuffle(customers, Configuration.INSTANCE.randomGenerator);
            trucks.add(new Vehicle(customers, initialLength));
        }
        trucks.sort(null);

        this.fleet = trucks.toArray(new Vehicle[0]);
        this.fitness = Math.min(15000, calculateFitness());
        if (fitness==15000) isValid=false;
        this.chromosome = setChromosome();
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
        Arrays.sort(fleet);
        this.fitness = Math.min(15000, calculateFitness());
        if (fitness==15000) isValid=false;
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
        if (unserved.isEmpty()) {
            Arrays.sort(fleet);
            return;
        } else {
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
            } else {
                Arrays.sort(fleet);
                return;
            }
        }
        if  (!unserved.isEmpty()) {
            isValid = false;
            this.late = unserved.size();
        }
        Arrays.sort(fleet);
    }

    // fitness
    private double calculateFitness() {
        repairFleet();
        if (!isValid) return 15000;
        double distance = 0;
        int late=0, overloaded = 0;
        for (Vehicle truck : fleet) {
            distance += truck.getDistance();
            late += truck.getLate();
            if (truck.overloaded()) overloaded++;
        }
        this.late = late; this.overloaded = overloaded;
        this.distance = distance;
        return distance + 1000*late;
    }

    // two-point crossover
    public Individual[] crossover(Individual individual) {
        int pivot1 = Configuration.INSTANCE.randomGenerator.nextInt(this.chromosome.length-1);
        int pivot2 = Configuration.INSTANCE.randomGenerator.nextInt(pivot1,this.chromosome.length);

        int[] child01 = new int[chromosome.length];
        int[] child02 = new int[chromosome.length];

        System.arraycopy(this.getGenes(), 0, child01, 0, pivot1);
        System.arraycopy(individual.getGenes(), pivot1, child01, pivot1, pivot2-pivot1);
        System.arraycopy(this.getGenes(), pivot2, child01, pivot2, child01.length - pivot2);

        System.arraycopy(individual.getGenes(), 0, child02, 0, pivot1);
        System.arraycopy(this.chromosome, pivot1, child02, pivot1, pivot2 - pivot1);
        System.arraycopy(this.chromosome, pivot2, child02, pivot2, child02.length - pivot2);

        return new Individual[]{new Individual(child01),
                new Individual(child02)};
    }

    // mutation
    public Individual mutation() {
        int point1 = Configuration.INSTANCE.randomGenerator.nextInt(1,98);
        int point2 = Configuration.INSTANCE.randomGenerator.nextInt(point1+1,99);
        ArrayList<Integer> subsetList = new ArrayList<>(point2-point1);
        for (int i=point1; i<point2; i++) subsetList.add(chromosome[i]);
        Collections.shuffle(subsetList, Configuration.INSTANCE.randomGenerator);
        int[] mutant = new int[chromosome.length];
        System.arraycopy(chromosome,0,mutant,0,point1);
        System.arraycopy(subsetList.stream().mapToInt(i -> i).toArray(),0,mutant,point1,point2-point1);
        System.arraycopy(chromosome,point2,mutant,point2,100-point2);
        return new Individual(mutant);
    }

    public int[] getGenes() {
        return chromosome;
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

    @Override
    public String toString() {
        String s = "Fleet Size: " +fleet.length+"\n" +
                "Unserviced Customers: " +late+"\n" +
                "Distance:" +getDistance()+"\n" +
                "Fleet: " + Arrays.toString(fleet);
        return s;
    }
}