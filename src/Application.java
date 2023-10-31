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

        Population population = new Population(
                Configuration.INSTANCE.populationSize,
                Configuration.INSTANCE.elitismRatio,
                Configuration.INSTANCE.mutationRatio,
                Configuration.INSTANCE.crossoverRatio
        );

        int i = 0;
        Individual bestIndividual = population.getPopulation()[0];

        try {
            FileHandler fh = new    FileHandler(Configuration.INSTANCE.logFile);
            Logger log = Logger.getLogger(this.getClass().getName());
            log.setLevel(Level.ALL);
            fh.setLevel(Level.ALL);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            log.addHandler(fh);

//            while ((i++ <= Configuration.INSTANCE.maximumNumberOfGenerations) && (bestIndividual.getFitness() != 0)) {
            while (
                    (i++ <= Configuration.INSTANCE.maximumNumberOfIterations)
                    && !optimum(bestIndividual)
                    && !population.isConverged()
            ) {
                System.out.println("generation " + decimalFormat.format(i) + " : " + Arrays.toString(bestIndividual.getGenes()) + ", fitness = " + bestIndividual.getFitness());
                population.evolve();
                bestIndividual = population.getPopulation()[0];
            }

            log.info("generation                  : " + decimalFormat.format(i) + " : " + Arrays.toString(bestIndividual.getGenes()));
            log.info("Best Individual             : " + bestIndividual);
//            log.info("Sum Total Distance          : " + bestIndividual.getDistance());
            log.info("runtime                     : " + (System.nanoTime() - runtimeStart) + " ns");
            log.info("numberOfCrossoverOperations : " + population.getNumberOfCrossoverOperations());
            log.info("numberOfMutationOperations  : " + population.getNumberOfMutationOperations());

            fh.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean optimum(Individual bestIndividual) {
        double min = 1646 * 0.95;
        double max = 1646 * 1.05;
        if (min <= bestIndividual.getDistance()) {
            return bestIndividual.getDistance() <= max;
        }
        return false;
    }
}

