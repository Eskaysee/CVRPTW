import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class Knapsack {

    private double[][] table;
    private int capacitySize;
    private int itemsNum;
    private Customer[] items;
    private final Map<Integer, Customer> clientMap = Configuration.INSTANCE.customers;
    private ArrayList<Integer> knap;
    private int seats;

    public Knapsack(int seats, ArrayList<Customer> customers) {
        this.capacitySize = Configuration.INSTANCE.vehicleCapacity;
        this.seats = seats;
        customers.add(0, clientMap.get(0));
        this.itemsNum = customers.size()-1;
        table = new double[itemsNum + 1][capacitySize + 1];
        knap = new ArrayList<>();
        this.items = customers.toArray(new Customer[0]);
        initTable();
        sack(itemsNum, capacitySize);
        pickItems(itemsNum, capacitySize);
        int demand = getDemand();
        int runs=1;
//        while (knap.size()<seats && demand<200 && runs<3) {
        while (demand<200 && runs<3) {
            this.capacitySize = Configuration.INSTANCE.vehicleCapacity - demand;
            cleanCustomers(customers);
            this.itemsNum = customers.size()-1;
            customers.remove(0);
            Collections.shuffle(customers, new MersenneTwister());
            customers.add(0, clientMap.get(0));
            this.items = customers.toArray(new Customer[0]);
            initTable();
            sack(itemsNum, capacitySize);
            pickItems(itemsNum, capacitySize);
            demand = getDemand();
            runs++;
        }
        cleanCustomers(customers);
        customers.remove(0);
    }

    public int getDemand() {
        int demand = 0;
        for (int i : knap) demand += clientMap.get(i).getDemand();
        return demand;
    }

    private void cleanCustomers(ArrayList<Customer> customers) {
        for (int i : knap)
            customers.remove(clientMap.get(i));
    }

    private void initTable() {
        for (int i = 0; i < itemsNum + 1; i++) {
            for (int j = 0; j < capacitySize + 1; j++) {
                if (i == 0 || j == 0) {
                    table[i][j] = 0;
                } else table[i][j] = -1;
            }
        }
    }

    private void pickItems(int i, int j) {
        if (i == 0) return;
//        if (knap.size() == seats) return;
        if (table[i-1][j] != table[i][j]) {
            if (knap.size()==0) {
                knap.add(items[i].getId());
                pickItems(i-1, (int) (j - items[i].getDemand()));
            } else {
                if (!overlaps(items[i])) {
                    knap.add(items[i].getId());
                    pickItems(i-1, (int) (j - items[i].getDemand()));
                } else pickItems(i-1, j);
            }
        } else pickItems(i-1, j);
    }

    public boolean overlaps(Customer client) {
        for (int i : knap) {
            if (clientMap.get(i).overlaps(client)) return true;
        }
        return false;
    }

    private double sack(int i, int j) {
        if (table[i][j]<0) {
            double value;
            if (j < items[i].getDemand()) value = sack(i-1,j);
            else value = Math.max(sack(i-1,j), getValue(i) + sack(i-1, (int) (j - items[i].getDemand())));
            table[i][j] = value;
        }
        return table[i][j];
    }

    private double getValue(int i) {
        double deltaX = items[i].getCoord()[0] - items[0].getCoord()[0];
        double deltaY = items[i].getCoord()[1] - items[0].getCoord()[1];
        double Xsqrd = deltaX * deltaX;
        double Ysqrd = deltaY * deltaY;
        return 1/Math.sqrt(Xsqrd + Ysqrd);
    }

    public ArrayList<Integer> getKnap() {
        ArrayList<Customer> custKnapsack =  new ArrayList<>();
        for (int i : knap)
            custKnapsack.add(clientMap.get(i));
        custKnapsack.sort(null);
        for (int i=0; i<custKnapsack.size(); i++)
            knap.set(i, custKnapsack.get(i).getId());
        return knap;
    }

    public static void main(String[] args) {
        Map<Integer, Customer> clientele = Configuration.INSTANCE.customers;
        double[][] distanceMatrix = Configuration.INSTANCE.distanceMatrix;
        ArrayList<Customer> customers = new ArrayList<>(clientele.values());
        customers.remove(0);
        ArrayList<ArrayList<Integer>> vehicles = new ArrayList<>();
        int vehiclesNum = 0, seats = 10;
        while (!customers.isEmpty()) {
            Collections.shuffle(customers, new MersenneTwister());
            vehicles.add(new Knapsack(seats, customers).getKnap());
            vehicles.get(vehiclesNum).add(0, 0);
            vehicles.get(vehiclesNum).add(0);
            vehiclesNum++;
        }

        double overallDistance = 0;
        for (ArrayList<Integer> car : vehicles) {
            double totDist = 0, demand = 0, overlap = 0;
            System.out.println(car);
            for (int i=0; i<car.size()-1; i++) {
                totDist += distanceMatrix[car.get(i)][car.get(i+1)];
                demand += clientele.get(car.get(i)).getDemand();
            }
            for (int i=1; i<car.size()-2; i++) {
                for (int j=i+1; j<car.size()-1;j++){
                    if (clientele.get(car.get(i)).overlaps(clientele.get(car.get(j)))) {
                        overlap++;
                        System.out.println(clientele.get(car.get(i))+" overlaps "+clientele.get(car.get(j)));
                    }
                }
            }
            System.out.println("Overlaps: "+overlap);
            System.out.println("Carrying "+ (car.size()-2));
            System.out.println("Has Total Demand: "+demand);
            System.out.println("Has Total Distance: "+totDist);
            System.out.println();
            overallDistance += totDist;
        }
        System.out.println("Fleet Size: "+vehiclesNum);
        System.out.println("Distance Covered: "+overallDistance);
        //TOTAL DEMAND 1458
        //maxX 67; maxY 77
        //Minimum Fleet size 15
        //Avg distance: more or less 3700
    }
}