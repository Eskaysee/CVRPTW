import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class Population {
    private final double elitismRatio;
    private final double mutationRatio;
    private final double crossoverRatio;
    private Individual[] population;
    private int numberOfCrossoverOperations = 0;
    private int numberOfMutationOperations = 0;

    private Map<Individual, Float> wheel;

    private boolean converged = false;

    public Population(int size, double crossoverRatio, double elitismRatio, double mutationRatio) {
        this.crossoverRatio = crossoverRatio;
        this.elitismRatio = elitismRatio;
        this.mutationRatio = mutationRatio;

        population = new Individual[size];

        for (int i = 0; i < size; i++) {
            population[i] = new Individual();
        }

        Arrays.sort(population);
        this.wheel = roulette();
    }

    public int getNumberOfCrossoverOperations() {
        return numberOfCrossoverOperations;
    }

    public int getNumberOfMutationOperations() {
        return numberOfMutationOperations;
    }

    public void evolve() {
        Individual[] individualArray = new Individual[population.length];
        int index = (int) Math.round(population.length * elitismRatio);
        System.arraycopy(population, 0, individualArray, 0, index);

        while (index < individualArray.length) {
            if (Configuration.INSTANCE.randomGenerator.nextFloat() <= crossoverRatio) {
                Individual[] parents = selectParents();
                Individual[] children = parents[0].crossover(parents[1]);
                numberOfCrossoverOperations++;

                if (Configuration.INSTANCE.randomGenerator.nextFloat() <= mutationRatio) {
                    individualArray[(index++)] = children[0].mutation();
                    numberOfMutationOperations++;
                } else {
                    individualArray[(index++)] = children[0];
                }

                if (index < individualArray.length) {
                    if (Configuration.INSTANCE.randomGenerator.nextFloat() <= mutationRatio) {
                        individualArray[index] = children[1].mutation();
                        numberOfMutationOperations++;
                    } else {
                        individualArray[index] = children[1];
                    }
                }
            } else if (Configuration.INSTANCE.randomGenerator.nextFloat() <= mutationRatio) {
                individualArray[index] = population[index].mutation();
                numberOfMutationOperations++;
            } else {
                individualArray[index] = population[index];
            }
            index++;
        }

        Arrays.sort(individualArray);
        this.population = individualArray;
        this.wheel = roulette();

        int count = (int) Arrays.stream(population).filter(Individual::validity).count();
        if (count == 1)
            converged = true;
    }

    public Individual[] getPopulation() {
        Individual[] individualArray = new Individual[population.length];
        System.arraycopy(population, 0, individualArray, 0, population.length);
        return individualArray;
    }

    private Individual[] selectParents() {
        Individual[] parents = new Individual[2];
        for (int i=0; i<2; i++) {
            do {
                Individual previousIndiv = null;
                float spin = Configuration.INSTANCE.randomGenerator.nextFloat();
                for (Individual indiv : population) {
                    if (spin <= wheel.get(indiv)) {
                        if (previousIndiv == null) {
                            parents[i] = indiv;
                            break;
                        } else if (wheel.get(previousIndiv) < spin) {
                            parents[i] = indiv;
                            break;
                        } else previousIndiv = indiv;
                    } else previousIndiv = indiv;
                }
//                System.out.println("iteration: " + i + " spin: "+ spin + " fleet: " + population.length);
            } while (i==1 && parents[0].equals(parents[1]));
        }
        return parents;
    }

    private Map<Individual, Float> roulette() {
        double[] scaledFitness = scaleFitness();
        double totalFitness = Arrays.stream(scaledFitness).sum();
        Map<Individual, Float> probs = new LinkedHashMap<>();
        float cdf = 0.0F;
        for (int i=0; i<population.length; i++) {
            cdf += (float) (scaledFitness[i] / totalFitness);
            probs.put(population[i], cdf);
        }
        return probs;
    }

    private double[] scaleFitness() {
        double[] transformedFitness = new double[population.length];
        for (int i=0; i<population.length; i++) {
            transformedFitness[i] = 6000 - population[i].getFitness();
        }
        return transformedFitness;
    }

    public boolean isConverged() {
        return converged;
    }
}