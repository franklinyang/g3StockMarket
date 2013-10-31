/**
 * 
 */
package stockmarket.sim;

import java.util.ArrayList;

/**
 * has two methods, Learn and Trade. The Learn method takes in the
 *   starting historical data of the economic indicators and the stocks for the 
 *   Player to build a model. The Trade method applies the stock model to the current
 *   Economic Indicators and decides to buy or sell stocks and how much. It then tells
 *   the market to place the trade by returning a list of trades for the market to 
 *   process. 
 *
 */
public abstract class Player {
	public String name;

	public abstract void learn(ArrayList<EconomicIndicator> indicators,
			ArrayList<Stock> stocks);

	public abstract ArrayList<Trade> placeTrade(int currentRound, ArrayList<EconomicIndicator> indicators,
			ArrayList<Stock> stocks, Portfolio porfolioCopy);
	
	public String getName(){
		return name;
	}

}
