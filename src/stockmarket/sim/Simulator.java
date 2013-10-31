/**
 * 
 */
package stockmarket.sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.lang.Math;


public class Simulator {

    private static Random           random;
	private double 					STARTING_CAPITAL				= 100000;
	private double					TRANSACTIONFEE					= 5;
	private int 					NUM_STOCKS						= 10;
    public static final int        	MAX_ROUNDS     					= 525;
	private static final int		TRAINING_ROUNDS					= 25;
	private static final double		PROB_NO_INFLUANCE	  		 	= .3;
	private static final double		STRENGTH_OF_TRENDS	  		 	= .5;
	private static final double		RANDOM_STOCK_VARIATIONLIMIT 	= .2;
	private static final int		STOCK_MAX_PRICE					= 800;
	private static final int		STOCK_MIN_PRICE					= 50;
	
	private static final String HEADER_STRING = "CIS 700 Fall 2013 -- Stock Market simulation";
	private static final String VERSION = "v1.0";

	private GameConfig config;
	private HashMap <Stock, ArrayList<Double>> calculateStocks; 
	private ArrayList<EconomicIndicator> indicators;
	private ArrayList<Double> indicatorsMax;
	private ArrayList<Double> indicatorsMin;
	private ArrayList<Stock> stocks;
	private ArrayList<Player> players;
	private ArrayList<String> stockNames;
	private Market market;
	
	public Simulator(String filename){
		random = new Random();
		stockNames = new ArrayList<String>();
		config = new GameConfig(filename);
		
		indicators = new ArrayList<EconomicIndicator> ();
		stocks = new ArrayList<Stock>();
		players = new ArrayList<Player>();
		calculateStocks = new HashMap <Stock, ArrayList<Double>>();
		indicatorsMin = new ArrayList<Double> ();
		indicatorsMax = new ArrayList<Double> ();
		
		
		createIndicators();
		createStocks();
		initializePlayers();
		market = new Market(players, STARTING_CAPITAL, TRANSACTIONFEE);
		
		run();
	}
	
	public static final void main(String[] args)
	{
		if (args.length < 1)
		{
			System.out.println("Quitting, no players given");
			System.exit(-1);
		}
		System.out.println(HEADER_STRING + " "+ VERSION + "\n");
		Simulator game = new Simulator(args[0]);
	}
	
	
	public void run(){
		
		int round = 0;

		for (round = 0; round < TRAINING_ROUNDS; round++){
			updateIndicatorsTrend(round, indicators);
			updateStockPrices(round, new ArrayList<Trade>());
		}
		
		market.trainPlayers(indicators, stocks);

		while (!market.allBankrupt() && round <= MAX_ROUNDS){
			updateIndicatorsTrend(round, indicators);
			ArrayList<Trade> marketForces = market.newRound(round, indicators, stocks);
			updateStockPrices(round, marketForces);
			//printPortfolios();
			round++;
		}
		
		System.out.println("\n====================================================");
		System.out.println("\nFINAL PORTFOLIOS: \n");
		printPortfolios();
		
	}

	/**
	 * 
	 */
	private void printPortfolios() {
		market.printPorfolios();
	}
	
	/**
	 * 
	 */
	private void initializePlayers() {
		try {
			for(int i = 0; i < config.num_players; i ++){
			ArrayList<Class<Player>> configPlayers = config.getPlayers();
			players.add(configPlayers.get(i).newInstance());
			}
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}	
	}

	public void createIndicators(){
		indicators.add(new EconomicIndicator("Unemployment", 5));
		indicatorsMin.add((double) 2);
		indicatorsMax.add((double) 10);
		indicators.add(new EconomicIndicator("GDP", 13.263));
		indicatorsMin.add((double) 5);
		indicatorsMax.add((double) 21);
		indicators.add(new EconomicIndicator("Inflation", 2.01));
		indicatorsMin.add((double) -2);
		indicatorsMax.add((double) 10);
		indicators.add(new EconomicIndicator("Exports", 3.52));
		indicatorsMin.add((double) .5);
		indicatorsMax.add((double) 8);
		indicators.add(new EconomicIndicator("Imports", 6.93));
		indicatorsMin.add((double) 2);
		indicatorsMax.add((double) 10);
	}

	/**
	 * Creates the formula that determines each stocks's price
	 * 
	 */
	private void createStocks(){
		//does not check case where all coefficients are 0 
		for (int i = 0; i < NUM_STOCKS; i++){
			Stock stock = new Stock (createStockName(), random.nextInt(STOCK_MAX_PRICE-(STOCK_MIN_PRICE*5))+(STOCK_MIN_PRICE*5));
			stocks.add(stock);
			ArrayList<Double> formula;
			do{
				formula = new ArrayList<Double>();
				for (int k = 0; k < indicators.size(); k++){
					formula.add(generateCoefficient());
				}	
			} while (!formulaOK(stock.copy(), copyIndicaotrs(indicators), formula));
			calculateStocks.put(stocks.get(i), formula);
		}
	}
	
	/**
	 * @param i
	 * @return
	 */
	private boolean formulaOK(Stock stock, ArrayList<EconomicIndicator> indicatorsTest,
			ArrayList<Double> formula) {
		
		 //Will all indicators very high result in an unusable price?
        double calcPrice = 0;
        for (int i = 0; i < indicators.size(); i++){
                calcPrice += indicatorsMin.get(i) * formula.get(i);
        }
        if(calcPrice > STOCK_MAX_PRICE || calcPrice < STOCK_MIN_PRICE) return false;
        
        //Will all indicators very low result in an unusable price?
        calcPrice = 0;
        for (int i = 0; i < indicators.size(); i++){
                calcPrice += indicatorsMax.get(i) * formula.get(i);
        }
        if(calcPrice > STOCK_MAX_PRICE || calcPrice < STOCK_MIN_PRICE) return false;
        
		//run through the formula and see if at any point the price goes outside the limits
		ArrayList <EconomicIndicator> testindicators = copyIndicaotrs(indicators);
		for (int i = 1; i < MAX_ROUNDS*2; i++){
			updateIndicatorsTrend(i, testindicators);
			stock.updatePrice(i, calculateBasicStockPrice(stock, testindicators, formula));
			if(stock.currentPrice() >= STOCK_MAX_PRICE || stock.currentPrice() <= (STOCK_MIN_PRICE)){
				return false;
			}
		}	
		return true;
	}

	/**
	 * Generates 3 letter stock names and checks for duplicate names 
	 */
	private String createStockName(){
		String name = "" + (char)('A' + Math.abs(random.nextInt() % 25)) +					
						   (char)('A' + Math.abs(random.nextInt() % 25)) + 
						   (char)('A' + Math.abs(random.nextInt() % 25));
		if(!stockNames.contains(name)){
			stockNames.add(name);
			return name;
		}
		else return createStockName();
	}
	
	/**
	 * Creates the coefficient for a stock equation
	 * All coefficients are between -10 and 12 and have a
	 * certain percentage change of being 0
	 * @return
	 */
	private double generateCoefficient(){
		if (random.nextDouble() < PROB_NO_INFLUANCE) return 0.0; 
		int coefficient = -1; 
		if(random.nextBoolean()) coefficient = 1;
		return (coefficient * (random.nextInt(10) + random.nextDouble())); 
	}
	
	public void updateIndicatorsTrend (int round){
		updateIndicatorsTrend(round, indicators);
	}
		
	/**
	 * Economic indicators need to have trends (if going up, more likely to go up)
                - if previous was negative, then a large % chance the next one is negative
                    - if the amount going up is small a smaller chance of staying the same direction
	 * @param round
	 */
	private void updateIndicatorsTrend (int round, ArrayList<EconomicIndicator> indicators){
		for(int i = 0; i < indicators.size(); i++){
			int direction = 1;
			double change = .5;
			double incriment = (indicatorsMax.get(i) - indicatorsMin.get(i)) / 100;
			
			if (round > 2){
				 change = (1 - (indicators.get(i).getHistory().get(round-1) 
							/ indicators.get(i).getHistory().get(round-2)));
				
				 if (change < 0){
					direction = -1;
				}
			 
				//The random double from the last round
				double percentageChange = normalizeRandom(0, incriment, Math.abs(change));
//				System.out.println("normalized " + percentageChange + " total:  " + (percentageChange + STRENGTH_OF_TRENDS) + " direction " + direction);
				if (random.nextDouble() > (percentageChange + STRENGTH_OF_TRENDS)){
					direction *= -1;
				}
			}
			
				//to stay in range
				if (indicators.get(i).currentValue() >= indicatorsMax.get(i)*(1-incriment)){
					direction = -1;
				}
				if (indicators.get(i).currentValue() <= indicatorsMin.get(i)*(1+incriment)){
					direction = 1;
				}	
				
			double update = 1 + (direction*getRandomBetween(0, incriment));
			double newPrice = indicators.get(i).currentValue()* update;
			indicators.get(i).updateValue(round, (newPrice));
		}
	}

	private void updateStockPrices(int round, ArrayList<Trade> marketTrades){
		for (Stock stock : stocks){
			updateStockPrice(round, stock, marketTrades, calculateStocks.get(stock));
		}
	}
	
	/**
	 * Updates each stock price based on the economic indicators and the popularity of the 
	 * stock
	 * @param round The current round
	 */
	private void updateStockPrice(int round, Stock stock, ArrayList<Trade> marketTrades, ArrayList<Double> formula){
		int popularity = 0;
		for(Trade trade : marketTrades){
			if (trade.getStock() == stock){
				popularity += trade.getQuantity();
			}
		}
		stock.updatePrice(round, calculateStockPrice(stock, popularity, formula));
	}
	
	/**
	 * Calculates the new stock price given the current indicators, the formula, and the popularity 
	 * Goes through the stock's formula to get the new base price, plus a small random amount.
	 * The stock will also increase based on how much of the stock was bought or sold as a percentage
	 * of the total stock on the market, calculated by how many shares would be in circulation if 
	 * every player bought all of the stocks they could at the beginning. 
	 * @param stock To update
	 * @return the new price of the stock
	 */
	private double calculateStockPrice(Stock stock, int popularity, ArrayList<Double> formula){
		double newPrice = 0;
		newPrice += RANDOM_STOCK_VARIATIONLIMIT * random.nextDouble();
		
		//change from indicators
		for (int i = 0; i < indicators.size(); i++){
			newPrice += indicators.get(i).currentValue() * formula.get(i);
		}
		//market adjustment based on how much the stock has been bought or sold
		double sharesAvabilble = getMarketSize() /stock.currentPrice();
		newPrice *= 1 + (popularity / sharesAvabilble);
		return newPrice;
	}
	
	private double calculateBasicStockPrice(Stock stock, ArrayList<EconomicIndicator> indicators, ArrayList<Double> formula){
		double newPrice = 0;
		newPrice += RANDOM_STOCK_VARIATIONLIMIT * random.nextDouble();
		
		//change from indicators
		for (int i = 0; i < indicators.size(); i++){
			newPrice += indicators.get(i).currentValue() * formula.get(i);
		}
		return newPrice;
	}

	private double getMarketSize(){
		double size =0;
		for (Player player : players){
			size += market.getPortfolio(player).getMonetaryValue();
		}
		return size;
	}
	
	
	private double getRandomBetween(double lowerBound, double upperBound){
		return (random.nextDouble()*(upperBound-lowerBound)) + lowerBound; 
	}
	
	private double normalizeRandom(double lowerBound, double upperBound, double value){
		return (value / (upperBound-lowerBound)) - lowerBound;
	}
	
	
	private ArrayList<EconomicIndicator> copyIndicaotrs(ArrayList<EconomicIndicator> original){
		ArrayList<EconomicIndicator> copy = new ArrayList<EconomicIndicator>();
		for (EconomicIndicator item : original){
			copy.add(item.copy());
		}
		return copy;
	}
		
}
