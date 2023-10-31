import java.util.ArrayList;
import java.util.Map;

public class Vehicle implements Comparable<Vehicle>{

    private int[] load;
    private final Map<Integer, Customer> customers = Configuration.INSTANCE.customers;
    private final double capacity = Configuration.INSTANCE.vehicleCapacity;
    private final double[][] distanceMatrix = Configuration.INSTANCE.distanceMatrix;
    private double demand;
    private double distance;

    //Constructor
    public Vehicle(ArrayList<Customer> init, int trunkSpace) {
        this.load = assignClients(trunkSpace, init);
        this.demand = calcDemand();
        this.distance = calcDistance();
    }

    public Vehicle(int[] clients) {
        this.load = clients;
        this.demand = calcDemand();
        this.distance = calcDistance();
    }

    private double calcDistance() {
        double totDist = 0;
        for (int i = 0; i< load.length-1; i++) {
            totDist += distanceMatrix[load[i]][load[i+1]];
        }
        return totDist;
    }

    private int[] assignClients(int initTrunkSpace, ArrayList<Customer> init) {
        ArrayList<Customer> load = new ArrayList<>();
        int i=0;
        while (
                !init.isEmpty()
                && load.size()<initTrunkSpace
        ) {
            load.add(init.remove(0));
        }
        load.sort(null);
        load.add(0,customers.get(0));
        load.add(customers.get(0));
        return load.stream().mapToInt(Customer::getId).toArray();
    }

    private double calcDemand() {
        double demand = 0.0D;
        for (int client : load) {
            demand += customers.get(client).getDemand();
        }
        return demand;
    }

    public ArrayList<Integer> makeValid() {
        double time = 0, currentDemand = 0;
        ArrayList<Integer> served = new ArrayList<>();
        ArrayList<Integer> unserved = new ArrayList<>();
        served.add(0);
        for (int j=1, i=0; j<load.length; j++) {
            time += distanceMatrix[served.get(i)][load[j]];
            if (currentDemand + customers.get(load[j]).getDemand() > capacity) {
                unserved.add(load[j]);
                time -= distanceMatrix[served.get(i)][load[j]];
            } else if (time < customers.get(load[j]).getDueTime()) {
                if (time < customers.get(load[j]).getReadyTime())
                    time += customers.get(load[j]).getReadyTime() - time;
                time += customers.get(load[j]).getServiceTime();
                currentDemand += customers.get(load[j]).getDemand();
                served.add(load[j]);
                i++;
            } else {
                unserved.add(load[j]);
                time -= distanceMatrix[served.get(i)][load[j]];
            }
        }
        this.load = served.stream().mapToInt(i -> i).toArray();
        this.demand = calcDemand();
        this.distance = calcDistance();
        return unserved;
    }

    public boolean insert(int client) {
        double newDemand = this.demand + customers.get(client).getDemand();
        if (newDemand > capacity) return false;
        ArrayList<Customer> newLoad = new ArrayList<>();
        for (int customer : load) {
            if (customer == 0) continue;
            if (customers.get(customer).overlaps(customers.get(client)))
                return false;
            if (customers.get(client).compareTo(customers.get(customer))<0) {

            }
            newLoad.add(customers.get(customer));
        }
        newLoad.add(customers.get(client));
        newLoad.sort(null);
        newLoad.add(0, customers.get(0));
        newLoad.add(customers.get(0));
        int index  = newLoad.indexOf(customers.get(client));
        if (onTime(newLoad, index)) {
            this.load = newLoad.stream().mapToInt(Customer::getId).toArray();
            this.demand = calcDemand();
            this.distance = calcDistance();
            return true;
        } else return false;
    }

    public boolean onTime(ArrayList<Customer> currentLoad, int index) {
        double time = 0;
        for (int i=0; i<=index; i++) {
            time += distanceMatrix[currentLoad.get(i).getId()][currentLoad.get(i+1).getId()];
            if (time < currentLoad.get(i+1).getDueTime()) {
                if (currentLoad.get(i+1).getId() != 0 && time < currentLoad.get(i+1).getReadyTime())
                    time += currentLoad.get(i+1).getReadyTime() - time;
                time += currentLoad.get(i+1).getServiceTime();
            } else return false;
        }
        return true;
    }

    public boolean forcedInsert(int client) {
        double newDemand = this.demand + customers.get(client).getDemand();
        if (newDemand > capacity) return false;
        ArrayList<Customer> newLoad = new ArrayList<>();
        for (int customer : load) {
            if (customer == 0) continue;
            if (customers.get(customer).overlaps(customers.get(client)))
                return false;
            newLoad.add(customers.get(customer));
        }
        newLoad.add(customers.get(client));
        newLoad.sort(null);
        newLoad.add(0, customers.get(0));
        newLoad.add(customers.get(0));
        this.load = newLoad.stream().mapToInt(Customer::getId).toArray();
        this.demand = calcDemand();
        this.distance = calcDistance();
        return true;
    }

    public int getLate() {
        int late = 0;
        double time = 0;
        for (int i=0; i<load.length-1; i++) {
            time += distanceMatrix[load[i]][load[i+1]];
            if (time < customers.get(load[i+1]).getDueTime()) {
                if (load[i+1] != 0 && time < customers.get(load[i+1]).getReadyTime())
                    time += customers.get(load[i+1]).getReadyTime() - time;
                time += customers.get(load[i+1]).getServiceTime();
            } else late++;
        }
        return late;
    }

    public int[] getLoad() {
        return load;
    }

    public boolean overloaded() {
        return demand>capacity;
    }

    public double getDistance() {
        return distance;
    }

    //Orders vehicles by ascending distance travelled
    @Override
    public int compareTo(Vehicle other) {
        return Double.compare(this.distance, other.getDistance());
    }
}