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

    public Population(int size, double crossoverRatio, double elitismRatio, double mutationRatio) {
        this.crossoverRatio = crossoverRatio;
        this.elitismRatio = elitismRatio;
        this.mutationRatio = mutationRatio;

        population = new Individual[size];

        int[] fleetSize = {50, 25, 20, 10, 17, 15, 13, 12};

        for (int i = 0; i < size; i++) {
            population[i] = new Individual(fleetSize[i%8]);
        }

        Arrays.sort(population);
        this.wheel = roulette2();
//        Map<Individual, Float> wheel2 = roulette2();
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
        population = individualArray;
        this.wheel = roulette2();
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
            } while (i==1 && parents[0].equals(parents[1]));
        }
        return parents;
    }

    private Map<Individual, Float> roulette() {
        double[] transFitness = scaleFitness();
        double totalFitness = Arrays.stream(transFitness).sum();
        Map<Individual, Float> probs = new LinkedHashMap<>();
        float cdf = 0.0F;
        for (int i=0; i<population.length; i++) {
            cdf += (float) (transFitness[i] / totalFitness);
            probs.put(population[i], cdf);
        }
        return probs;
    }

    private double[] scaleFitness() {
        double[] transformedFitness = new double[population.length];
        double max = Arrays.stream(population).mapToDouble(Individual::getFitness).max().getAsDouble();
        double min = Arrays.stream(population).mapToDouble(Individual::getFitness).min().getAsDouble();
        double scaleFactor = (max-min)/(population.length-1);
        for (int i=0,j=population.length-1; i<population.length && j >=0; i++,j--) {
            double trans1 = max - (scaleFactor * i);
            double trans2 = (min + max) - population[i].getFitness();
            transformedFitness[i] = (trans1 + trans2 + population[j].getFitness())/3;
        }
        return transformedFitness;
    }

    private Map<Individual, Float> roulette2() {
        double[] transFitness = scaleFitness2();
        double totalFitness = Arrays.stream(transFitness).sum();
        Map<Individual, Float> probs = new LinkedHashMap<>();
        float cdf = 0.0F;
        for (int i=0; i<population.length; i++) {
            cdf += (float) (transFitness[i] / totalFitness);
            probs.put(population[i], cdf);
        }
        return probs;
    }

    private double[] scaleFitness2() {
        double[] transformedFitness = new double[population.length];
        for (int i=0,j=population.length-1; i<population.length && j >=0; i++,j--) {
            transformedFitness[i] = population[j].getFitness();
        }
        return transformedFitness;
    }
}