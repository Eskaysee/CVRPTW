import java.util.ArrayList;
import java.util.Map;

public class Vehicle implements Comparable<Vehicle>{

    private final Vector position;
    private Vector velocity;
    private Vector bestPosition;
    private double bestEvaluation;
    private final int[] genes;
    private final double fitness;
    private final Map<Integer, Customer> customers;

    //Constructor
    public Vehicle(ArrayList<Customer> init, int padding) {
        this.position = new Vector(35.0,35.0,0);
        double totalDemand = init.stream().mapToDouble(Customer::getDemand).sum();
        ArrayList<Integer> chromosome;
        if (totalDemand>200)
            chromosome = generateGene(init);
        else {
            chromosome = new ArrayList<>(init.stream().sorted().map(Customer::getId).toList());
            chromosome.add(0, 0);
            chromosome.add(0);
        }
        genes = pad(padding, chromosome);
        fitness = calculateFitness();
        this.customers = Configuration.INSTANCE.customers;
        this.velocity = new Vector();
    }

    private int[] pad(int padding, ArrayList<Integer> chromosomes) {
        if (padding == 0) return chromosomes.stream().mapToInt(Integer::intValue).toArray();
        else {
            for (int i=chromosomes.size(); i<padding; i++) chromosomes.add(0);
            return chromosomes.stream().mapToInt(Integer::intValue).toArray();
        }
    }

    public Vehicle(int[] chromosome) {
        this.genes = chromosome;
        this.position = new Vector(35.0,35.0,0);
        this.customers = Configuration.INSTANCE.customers;
        this.fitness = calculateFitness();
        this.velocity = new Vector();
    }

    public Vector getPosition() {
        return position.clone();
    }

    public Vector getVelocity() {
        return velocity.clone();
    }

    public void setVelocity(Vector velocity) {
        this.velocity = velocity.clone();
    }

    public Vector getBestPosition() {
        return bestPosition.clone();
    }

    double getBestEvaluation() {
        return bestEvaluation;
    }

    public void updatePosition() {
        position.add(velocity);
    }

    public void updatePersonalBest() {
        double tempEvaluation = evaluate();
        if (tempEvaluation < bestEvaluation) {
            bestPosition = position.clone();
            bestEvaluation = tempEvaluation;
        }
    }

    private double evaluate() {
        return 0.0;
    }

    private double calculateFitness() {
        double totDist = 0;
        for (int i = 0; i< genes.length-1; i++) {
            totDist += customers.get(genes[i]).distance(customers.get(genes[i+1]));
        }
        return totDist;
    }

    protected ArrayList<Integer> generateGene(ArrayList<Customer> init) {
        return new Knapsack(200, init.size()-1, init, customers).getKnap();
    }

    public int[] getGenes() {
        return genes;
    }

    public double getFitness() {
        return fitness;
    }

    //Orders vehicles by ascending distance travelled
    @Override
    public int compareTo(Vehicle other) {
        return Double.compare(fitness, other.getFitness());
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

        return new Vehicle[]{new Vehicle(child01),
                new Vehicle(child02)};
    }

    // mutation
    public Vehicle mutation() {
        //

        return new Vehicle(genes);
    }
}