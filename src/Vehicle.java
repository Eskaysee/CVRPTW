import java.util.ArrayList;
import java.util.Map;

public class Vehicle implements Comparable<Vehicle>{

    private final int[] load;
    private final Map<Integer, Customer> customers = Configuration.INSTANCE.customers;
    private final double capacity = Configuration.INSTANCE.vehicleCapacity;
    private final double[][] distanceMatrix = Configuration.INSTANCE.distanceMatrix;
    private int seats;

    //Constructor
    public Vehicle(ArrayList<Customer> init, int seats) {
        this.load = generateGene(seats, init);
    }

    public Vehicle(int[] chromosome) {
        this.load = chromosome;
    }

    public double getDistance() {
        double totDist = 0;
        for (int i = 0; i< load.length-1; i++) {
            totDist += distanceMatrix[load[i]][load[i+1]];
        }
        return totDist;
    }

    private int[] generateGene(int seats, ArrayList<Customer> init) {
        ArrayList<Customer> load = new ArrayList<>();
        int i=0;
        while (
                !init.isEmpty()
                && load.size()<seats
        ) {
            load.add(init.remove(0));
        }
        load.sort(null);
        load.add(0,customers.get(0));
        load.add(customers.get(0));
        return load.stream().mapToInt(Customer::getId).toArray();
    }

    public double getDemand() {
        double demand = 0.0D;
        for (int client : load) {
            demand += customers.get(client).getDemand();
        }
        return demand;
    }

    public int getOverlaps() {
        int overlap = 0;
        for (int i=1; i<load.length-2; i++) {
            for (int j=i+1; j<load.length-1; j++) {
                if (customers.get(load[i]).overlaps(customers.get(load[j]))) overlap++;
            }
        }
        return overlap;
    }

    public int[] getLoad() {
        return load;
    }

    //Orders vehicles by ascending distance travelled
    @Override
    public int compareTo(Vehicle other) {
        return Double.compare(this.getDistance(), other.getDistance());
    }
}