import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Application {
    public static void main(String... args) {
        Application application = new Application();
        application.execute();
    }

    public void execute() {

        DecimalFormat decimalFormat = new DecimalFormat("000000");
        long runtimeStart = System.nanoTime();

        Fleet fleet = new Fleet(
                Configuration.INSTANCE.fleetSize,
                Configuration.INSTANCE.elitismRatio,
                Configuration.INSTANCE.mutationRatio,
                Configuration.INSTANCE.crossoverRatio
        );

        int i = 0;
        Vehicle bestVehicle = fleet.getFleet()[0];

        try {
            FileHandler fh = new    FileHandler(Configuration.INSTANCE.logFile);
            Logger log = Logger.getLogger(this.getClass().getName());
            log.setLevel(Level.ALL);
            fh.setLevel(Level.ALL);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            log.addHandler(fh);

            double initDistance = fleet.totalDistance();

//            while ((i++ <= Configuration.INSTANCE.maximumNumberOfGenerations) && (bestVehicle.getFitness() != 0)) {
            while ((i++ <= Configuration.INSTANCE.maximumNumberOfIterations) && (bestVehicle.getFitness() != 0)) {
                System.out.println("generation " + decimalFormat.format(i) + " : " + Arrays.toString(bestVehicle.getGenes()) + ", fitness = " + bestVehicle.getFitness());
                fleet.evolve();
                bestVehicle = fleet.getFleet()[0];
            }

            log.info("Initial Distance          : " + initDistance);
            log.info("generation                  : " + decimalFormat.format(i) + " : " + Arrays.toString(bestVehicle.getGenes()));
            log.info("Sum Total Distance          : " + fleet.totalDistance());
            log.info("runtime                     : " + (System.nanoTime() - runtimeStart) + " ns");
            log.info("numberOfCrossoverOperations : " + fleet.getNumberOfCrossoverOperations());
            log.info("numberOfMutationOperations  : " + fleet.getNumberOfMutationOperations());

            fh.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

