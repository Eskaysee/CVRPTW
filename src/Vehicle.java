import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class Vehicle implements Comparable<Vehicle>{

    private final int[] genes;
    private final float fitness;
    private final Map<Integer, Customer> customers;
    private final double demand;

    //Constructor
    public Vehicle(ArrayList<Customer> init, int seats) {
        ArrayList<Integer> chromosome;
        this.customers = Configuration.INSTANCE.customers;
        chromosome = generateGene(seats,init);
        if (chromosome.size()<seats+2) genes = pad(seats+2, chromosome);
        else genes = pad(seats, chromosome);
        fitness = (float) (1/getDistance());
        demand = getDemand();
        System.out.println();
    }

    public Vehicle(int[] chromosome) {
        this.genes = chromosome;
        this.customers = Configuration.INSTANCE.customers;
        this.fitness = (float) (1/getDistance());
        this.demand = getDemand();
    }

    private int[] pad(int seats, ArrayList<Integer> chromosomes) {
        for (int i=chromosomes.size(); i<seats; i++) chromosomes.add(0);
        return chromosomes.stream().mapToInt(Integer::intValue).toArray();
    }

    public double getDistance() {
        double totDist = 0;
        for (int i = 0; i< genes.length-1; i++) {
            totDist += customers.get(genes[i]).distance(customers.get(genes[i+1]));
        }
        return totDist;
    }

    private ArrayList<Integer> generateGene(int chromosomeSize, ArrayList<Customer> init) {
        ArrayList<Integer> load = new ArrayList<>();
        while (load.size()<chromosomeSize && init.size() > 0) load.add(init.remove(0).getId());
        load.add(0,0);
        load.add(0);
        return load;
    }

    public double getDemand() {
        double demand = 0.0D;
        for (int i=0; i<genes.length; i++) {
            demand += customers.get(genes[i]).getDemand();
        }
        return demand;
    }

    public int[] getGenes() {
        return genes;
    }

    public float getFitness() {
        return fitness;
    }

    //Orders vehicles by ascending distance travelled
    @Override
    public int compareTo(Vehicle other) {
        return Double.compare(other.getFitness(), this.fitness);
    }

    private void sortGenes(int[] truck) {
        ArrayList<Customer> custKnapsack =  new ArrayList<>();
        for (int i : truck)
            custKnapsack.add(customers.get(i));
        custKnapsack.sort(null);
        truck = custKnapsack.stream().sorted().mapToInt(Customer::getId).toArray();
    }

    // crossover
    public Vehicle[] crossover(Vehicle other) {
        int pivot = Configuration.INSTANCE.randomGenerator.nextInt(genes.length);

        int[] child01 = new int[genes.length];
        int[] child02 = new int[genes.length];

        System.arraycopy(genes, 0, child01, 0, pivot);
        System.arraycopy(other.getGenes(), pivot, child01, pivot, child01.length - pivot);

        System.arraycopy(other.getGenes(), 0, child02, 0, pivot);
        System.arraycopy(genes, pivot, child02, pivot, child02.length - pivot);

//        sortGenes(child01);
//        sortGenes(child02);

        return new Vehicle[]{new Vehicle(child01),
                new Vehicle(child02)};
    }

    // mutation
    public Vehicle mutation() {
        //

        return new Vehicle(genes);
    }
}