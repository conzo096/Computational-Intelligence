package coursework;

import java.util.ArrayList;
import java.util.Collections;

import model.Fitness;
import model.Individual;
import model.LunarParameters.DataSet;
import model.NeuralNetwork;

/**
 * Implements a basic Evolutionary Algorithm to train a Neural Network
 * 
 * You Can Use This Class to implement your EA or implement your own class that extends {@link NeuralNetwork} 
 * 
 */
public class ExampleEvolutionaryAlgorithm extends NeuralNetwork {
	

	/**
	 * The Main Evolutionary Loop
	 */
	@Override
	public void run()
	{		
		//Initialise a population of Individuals with random weights
		population = initialise();

		//Record a copy of the best Individual in the population
		best = getBest();
		System.out.println("Best From Initialisation " + best);

		/**
		 * main EA processing loop
		 */		
		
		while (evaluations < Parameters.maxEvaluations)
		{

			/**
			 * this is a skeleton EA - you need to add the methods.
			 * You can also change the EA if you want 
			 * You must set the best Individual at the end of a run
			 * 
			 */
			
			// Select 2 Individuals from the current population. Currently returns random Individual
			Individual parent1 = FitnessProportionalSelection(); 
			Individual parent2 = FitnessProportionalSelection();

			while(parent1 == parent2)
				parent2 = FitnessProportionalSelection();
			
			// Generate a child by crossover. Not Implemented			
			ArrayList<Individual> children = reproduce(parent1, parent2);			
			
			//mutate the offspring
			mutate(children);
			
			// Evaluate the children
			evaluateIndividuals(children);			

			// Replace children in population
			replace(children);
			
		//	mutate(population);
			// check to see if the best has improved
			best = getBest();
			
			// Implemented in NN class. 
			outputStats();
			
			//Increment number of completed generations	
			//evaluations++;
		}

		//save the trained network to disk
		saveNeuralNetwork();
	}

	

	/**
	 * Sets the fitness of the individuals passed as parameters (whole population)
	 * 
	 */
	private void evaluateIndividuals(ArrayList<Individual> individuals) {
		for (Individual individual : individuals) {
			individual.fitness = Fitness.evaluate(individual, this);
		}
	}


	/**
	 * Generates a randomly initialised population
	 * 
	 */
	private ArrayList<Individual> initialise() {
		population = new ArrayList<>();
		for (int i = 0; i < Parameters.popSize; ++i) {
			//chromosome weights are initialised randomly in the constructor
			Individual individual = new Individual();
			population.add(individual);
		}
		evaluateIndividuals(population);
		return population;
	}

	/**
	 * Selection -- Obtains the best individual from the population.
	 * 
	 */
	private Individual selectFirstParent()
	{		
		Individual parent = getGoodIndividual(30);
		return parent.copy();
	}
	
	private Individual selectSecondParent()
	{
		Individual parent = getAverage();
		return parent.copy();		
	}
	
	private Individual FitnessProportionalSelection()
	{
		Individual parent = population.get(rouletteSelect());
		return parent.copy();
		
	}
	 
	int rouletteSelect()
	{
		// calculate the total weight
		double weight_sum = 0;
		for(int i=0; i<population.size(); i++)
		{
			weight_sum += population.get(i).fitness;
		}
		// get a random value
		double value = randUniformPositive() * weight_sum;	
		// locate the random value based on the weights
		for(int i=0; i<population.size(); i++)
		{		
			value -= population.get(i).fitness;		
			if(value <= 0)
				return i;
		}
		// when rounding errors occur, we return the last item's index 
		return population.size() - 1;
	}

	// Returns a uniformly distributed double value between 0.0 and 1.0
	double randUniformPositive()
	{
		// easiest implementation
		return Parameters.random.nextDouble();
	} 
	
	
	

	/**
	 * Crossover / Reproduction
	 * 
	 * NEEDS REPLACED with proper method this code just returns exact copies of the
	 * parents. 
	 */
	private ArrayList<Individual> reproduce(Individual parent1, Individual parent2)
	{
		// Why is this an array? Reproduce should only generate one child per generation?
		// Could have a percent chance of multiple children
		ArrayList<Individual> children = new ArrayList<>();
		
		children.add(parent1.copy());
		children.add(parent1.copy());
		
		
		for(int i=0; i < children.size();i++)
		{
			Individual child = children.get(i);
			// Uniform crossover operation.
			for (int j = 0; j < child.chromosome.length; j++)
			{	
				if(Parameters.random.nextBoolean())
				{
						child.chromosome[j] = parent2.chromosome[j];
				}
			}
			
			// One point crossover
//			int midPoint = (int)child.chromosome.length/2;
//			for(int j = 0; j < midPoint;j++)
//			{
//					child.chromosome[j] = parent2.chromosome[j];
//			}
			
		}
		
		return children;
	} 
	
	/**
	 * Mutate each individual in the list. 
	 * 
	 * 
	 */
	private void mutate(ArrayList<Individual> individuals)
	{		
		
		for(Individual individual : individuals)
		{
			for (int i = 0; i < individual.chromosome.length; i++)
			{
				if (Parameters.random.nextDouble() < Parameters.mutateRate)
				{
					if (Parameters.random.nextBoolean())
					{
						
						individual.chromosome[i] += Parameters.mutateChange;
					}
					else
						individual.chromosome[i] -= Parameters.mutateChange;
				}
			}
		}		
	}

	
	
	
	/**
	 * 
	 * Replaces the worst member of the population 
	 * (regardless of fitness)
	 * 
	 */
	private void replace(ArrayList<Individual> individuals)
	{
		for(Individual individual : individuals)
		{
			int idx = getWorstIndex();		
			population.set(idx, individual);
		}		
	}

	
	/**
	 * Returns a copy of the best individual in the population
	 * 
	 */
	private Individual getBest() {
		best = null;
		for (Individual individual : population) {
			if (best == null) {
				best = individual.copy();
			} else if (individual.fitness < best.fitness) {
				best = individual.copy();
			}
		}
		return best;
	}
	
	/**
	 * Obtains a random individual from the top x of population
	 * 
	 */
	private Individual getGoodIndividual(int size)
	{
		Individual[] topSet = new Individual[size];
		
		ArrayList<Individual> sortedPop = new ArrayList<Individual>(population.size());
		for(Individual i : population)
		{
			sortedPop.add(i.copy());
		}
		
		Collections.sort(sortedPop, 
                (o1, o2) -> o1.compareTo(o2));
		
		
		// Now that they are all sorted, obtain the top x amount.
		
		for(int i=0; i < size;i++)
		{
			topSet[i] = sortedPop.get(i).copy();		
		}
		// Now clear sortedPop.
		sortedPop.clear();
		
		// Find a random object from the list.
		return topSet[Parameters.random.nextInt(size)].copy();
		
	}
	

	/**
	 * Returns a copy of an average individual in the population.
	 * 
	 */
	
	private Individual getAverage()
	{
		
		double averageFitness = 0;
		
		// Calculate average fitness.
		for (Individual individual : population)
		{
			averageFitness += individual.fitness;
		}
		averageFitness /= population.size();
		
		// Find a member within this average.
		for (Individual individual : population)
		{
			// If the fitness is within 
			if( (averageFitness - individual.fitness) <= 0.2f)
			{
				//System.out.println(averageFitness - individual.fitness);
				return individual.copy();
			}
		}
		
		return population.get(0).copy();
	}
	
	/**
	 * Returns the index of the worst member of the population
	 * @return
	 */
	private int getWorstIndex() {
		Individual worst = null;
		int idx = -1;
		for (int i = 0; i < population.size(); i++) {
			Individual individual = population.get(i);
			if (worst == null) {
				worst = individual;
				idx = i;
			} else if (individual.fitness > worst.fitness) {
				worst = individual;
				idx = i; 
			}
		}
		return idx;
	}	

 	private int getBestIndex() {
		Individual best = null;
		int idx = -1;
		for (int i = 0; i < population.size(); i++) {
			Individual individual = population.get(i);
			if (best == null) {
				best = individual;
				idx = i;
			} else if (individual.fitness < best.fitness) {
				best = individual;
				idx = i; 
			}
		}
		return idx;
	}	
	
	
	@Override
	public double activationFunction(double x) {
		if (x < -20.0) {
			return -1.0;
		} else if (x > 20.0) {
			return 1.0;
		}
		return Math.tanh(x);
	}
}
