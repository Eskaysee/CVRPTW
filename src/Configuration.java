import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public enum Configuration {
    INSTANCE;

    public final char[] targetGene = "genetic algorithms are fascinating".toCharArray();
    public final String fileSeparator = System.getProperty("file.separator");
    public final String lineSeparator = System.getProperty("line.separator");
    public final String userDirectory = System.getProperty("user.dir");
    public final String logFile = userDirectory + fileSeparator + "logs" + fileSeparator + "CVRPTW.log";
    public final MersenneTwister randomGenerator = new MersenneTwister(System.currentTimeMillis());
    public final Map<Integer, Customer> customers = clientData();
    public final double[][] distanceMatrix = makeDistanceMatrix();
    public final int populationSize = 75;
    public final int maximumNumberOfIterations = 10000;
    public final int vehicleLifetime = 230;
    public final int maximumNumberOfGenerations = maximumNumberOfIterations/vehicleLifetime;
    public final int numberOfCustomers = 100;
    public final int vehicleCapacity = 200;
    //Genetic Algorithm Parameters
    public final float crossoverRatio = 0.7f;
    public final float elitismRatio = 0.1f;
    public final float mutationRatio = 0.005f;

    private Map<Integer,Customer> clientData() {
        try {
            Scanner data = new Scanner(new File("Data.csv"));
            Map<Integer, Customer> clientele = new HashMap<>(numberOfCustomers+1);
            clientele.put(0, new Customer());
            while (data.hasNextLine()) {
                String[] Customer = data.nextLine().split(",");
                int id = Integer.parseInt(Customer[0])-1;
                double[] pos = {Double.parseDouble(Customer[1]), Double.parseDouble(Customer[2])};
                double demand = Double.parseDouble(Customer[3]);
                double readyTime = Double.parseDouble(Customer[4]);
                double dueTime = Double.parseDouble(Customer[5]);
                double serviceTime = Double.parseDouble(Customer[6]);
                clientele.put(id, new Customer(id, pos, demand, readyTime, dueTime, serviceTime));
            }
            return clientele;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private double distance(int client1, int client2) {
        double deltaX = customers.get(client1).getCoord()[0] - customers.get(client2).getCoord()[0];
        double deltaY = customers.get(client1).getCoord()[1] - customers.get(client2).getCoord()[1];
        double Xsqrd = deltaX * deltaX;
        double Ysqrd = deltaY * deltaY;
        return Math.sqrt(Xsqrd + Ysqrd);
    }

    private double[][] makeDistanceMatrix() {
        double[][] distanceMatrix = new double[numberOfCustomers+1][numberOfCustomers+1];
        for (int i=0; i<=numberOfCustomers; i++) distanceMatrix[i][i] = 0;
        for (int i=0; i<=numberOfCustomers; i++) {
            for (int j=i+1; j<=numberOfCustomers; j++) {
                distanceMatrix[i][j] = distance(i,j);
                distanceMatrix[j][i] = distanceMatrix[i][j];
            }
        }
        return distanceMatrix;
    }
}
