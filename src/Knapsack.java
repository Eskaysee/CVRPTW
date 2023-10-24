import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class Knapsack {

    private double[][] table;
    private int capacitySize;
    private int itemsNum;
    private Customer[] items;
    private Map<Integer, Customer> clientMap = new HashMap<>(100);
    private ArrayList<Integer> knap;

    public Knapsack(int capacity, int itemsCount, ArrayList<Customer> customers, Map<Integer, Customer> clientele) {
        this.capacitySize = capacity;
        this.itemsNum = itemsCount;
        table = new double[itemsNum + 1][capacitySize + 1];
        knap = new ArrayList<>();
        this.items = customers.toArray(new Customer[0]);
        initTable();
        sack(itemsNum, capacitySize);
        this.clientMap = clientele;
        pickItems(itemsNum, capacitySize);
        int demand = getDemand();
        if (knap.size()<13 && demand<200) {
            capacitySize -= demand;
            cleanCustomers(customers);
            sack(itemsNum, capacitySize);
            pickItems(itemsNum, capacitySize);
        }
    }

    private int getDemand() {
        int demand = 0;
        for (int i : knap) demand += clientMap.get(i).getDemand();
        return demand;
    }

    private void cleanCustomers(ArrayList<Customer> customers) {
        for (int i : knap) {
            customers.remove(clientMap.get(i));
        }
        items = customers.toArray(new Customer[0]);
        itemsNum = customers.size()-1;
        initTable();
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
//        if (knap.size() == 13) return;
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

    private boolean overlaps(Customer client) {
        for (int i : knap) {
            if (clientMap.get(i).overlaps(client)) { //times may overlap by no more than 2
                if (client.distance(clientMap.get(i))>(5*Math.sqrt(2))) return true; //Distance beyond acceptable range
            }
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
        int demand = 0;
        ArrayList<Customer> custKnapsack =  new ArrayList<>();
        for (int i : knap) {
            demand += clientMap.get(i).getDemand();
            custKnapsack.add(clientMap.get(i));
        }
        custKnapsack.sort(null);
        for (int i=0; i<custKnapsack.size(); i++)
            knap.set(i, custKnapsack.get(i).getId());
        knap.add(0, 0);
        knap.add(0);
        return knap;
    }
}