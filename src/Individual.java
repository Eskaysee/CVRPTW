import java.util.*;

public class Individual implements Comparable<Individual> {
    private final int[] chromosome;
    private final double fitness;
    private final Vehicle[] fleet;
    private boolean isValid = true;
    private final Map<Integer, Customer> clients = Configuration.INSTANCE.customers;
    private int overloaded, late;
    private double distance;

    public Individual() {
        this.fleet = initialise();
        this.fitness = Math.min(6000, calculateFitness());
        if (fitness==6000) isValid=false;
        this.chromosome = setChromosome();
    }

    public Individual(int[] chromosome) {
        this.fleet = loadFleet(chromosome);
        this.fitness = Math.min(6000, calculateFitness());
        if (fitness==6000) isValid=false;
        this.chromosome = setChromosome();
    }

    private Vehicle[] initialise() {
        ArrayList<Vehicle> trucks = new ArrayList<>();
        ArrayList<Customer> customers = new ArrayList<>(clients.values());
        customers.remove(0);

        int initialLength = Configuration.INSTANCE.randomGenerator.nextInt(2, 11);

        while (!customers.isEmpty()) {
            Collections.shuffle(customers, Configuration.INSTANCE.randomGenerator);
            trucks.add(new Vehicle(customers, initialLength));
        }
        trucks.sort(null);
        return trucks.toArray(new Vehicle[0]);
    }

    private Vehicle[] loadFleet(int[] chromosome) {
        List<Vehicle> trucks = new ArrayList<>();
        List<Customer> load = new ArrayList<>();
        load.add(clients.get(chromosome[0]));
        for (int i=0; i<chromosome.length-1; i++) {
            Customer current = clients.get(chromosome[i]);
            Customer next = clients.get(chromosome[i+1]);
            if (current.compareTo(next)>=0 || current.overlaps(next)){
                load.sort(null);
                load.add(0, clients.get(0));
                load.add(clients.get(0));
                trucks.add(new Vehicle(load.stream().mapToInt(Customer::getId).toArray()));
                load.clear();
            }
            load.add(next);
        }
        load.sort(null);
        load.add(0, clients.get(0));
        load.add(clients.get(0));
        trucks.add(new Vehicle(load.stream().mapToInt(Customer::getId).toArray()));
        trucks.sort(null);
        return trucks.toArray(new Vehicle[0]);
    }

    private int[] setChromosome() {
        int[] chromosome = new int[Configuration.INSTANCE.numberOfCustomers];
        int i=0;
        for (Vehicle truck : fleet) {
            for (int customer : truck.getLoad()) {
                if (customer != 0) chromosome[i++] = customer;
            }
        }
        return chromosome;
    }

    private void repairFleet() {
        ArrayList<Integer> unserved = new ArrayList<>();
        for (Vehicle truck : fleet) {
            unserved.addAll(truck.makeValid());
        }
//        if (unserved.isEmpty()) {
//            Arrays.sort(fleet);
//            return 0;
//        }
//        int reassignments = unserved.size();
        for (int i=0; i<unserved.size(); i++) {
            for (Vehicle truck : fleet) {
                if (truck.insert(unserved.get(i))) {
                    unserved.remove(i);
                    i--;
                    break;
                }
            }
        }
        for (int i=0; i<unserved.size(); i++) {
            for (Vehicle truck : fleet) {
                if (truck.forcedInsert(unserved.get(i))) {
                    unserved.remove(i);
                    i--;
                    break;
                }
            }
        }
        if  (!unserved.isEmpty()) {
            isValid = false;
            this.late = unserved.size();
        }
        Arrays.sort(fleet);
//        return reassignments;
    }

    // fitness
    private double calculateFitness() {
//        int reassignments = repairFleet();
        if (!isValid) return 6000;
        double distance = 0;
        int late=0, overloaded = 0;
        for (Vehicle truck : fleet) {
            distance += truck.getDistance();
            late += truck.getLate();
            if (truck.overloaded()) overloaded++;
        }
        this.late = late; this.overloaded = overloaded;
        this.distance = distance;
        return distance + 500*late;
    }

    // crossover
    public Individual[] crossover(Individual individual) {
        int[][] offsprings = PMX(this.chromosome, individual.getGenes());
        return new Individual[]{new Individual(offsprings[0]),
                new Individual(offsprings[1])};
    }

    private int[][] PMX(int[] parent1, int[] parent2) {
        int[] child1 = new int[parent1.length];
        int[] child2 = new int[parent1.length];
        int x1 = Configuration.INSTANCE.randomGenerator.nextInt(0,99);
        int x2 = Configuration.INSTANCE.randomGenerator.nextInt(x1+1,100);
        Map<Integer, Integer> map12 = new HashMap<>();
        Map<Integer, Integer> map21 = new HashMap<>();
        for (int i = x1; i <= x2; i++) {
            child1[i] = parent1[i];
            child2[i] = parent2[i];
            map12.put(parent1[i],parent2[i]);
            map21.put(parent2[i],parent1[i]);
        }
        for (int i = 0; i < parent1.length; i++) {
            if (i==x1) i = x2+1;
            int ch1 = parent2[i];
            int ch2 = parent1[i];
            while (map12.containsKey(ch1)) {
                ch1 = map12.get(ch1);
            }
            while (map21.containsKey(ch2)) {
                ch2 = map21.get(ch2);
            }
            child1[i] = ch1;
            child2[i] = ch2;
        }
        return new int[][]{child1, child2};
    }

    // mutation
    public Individual mutation() {
        ArrayList<Integer> subsetList = new ArrayList<>(35);
        for (int i=32; i<67; i++) subsetList.add(chromosome[i]);
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

    @Override
    public String toString() {
        String s = "Fleet Size: " +fleet.length+"\n" +
                "Unserviced Customers: " +late+"\n" +
                "Distance:" +getDistance()+"\n" +
                "Fleet: " + Arrays.toString(fleet);
        return s;
    }
}