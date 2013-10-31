/**
 * 
 */
package stockmarket.sim;

import java.util.HashMap;
import java.util.Set;

/**
 * This class holds a player's current capital and their stocks
 *
 */
public class Portfolio {
	private double capital;
	private HashMap <Stock, Integer> stocks;
	private double startingMoney; 
	private double transactionFee; 
	
	public Portfolio (double startingCapital, double transactionFee){
		capital = startingCapital;
		startingMoney = startingCapital;
		stocks = new HashMap <Stock, Integer>();
		this.transactionFee = transactionFee;
	}
	
	public Portfolio (double startingCapital, double currentCapital, 
			     HashMap <Stock, Integer> Stocks, double transactionFee){
		capital = currentCapital;
		startingMoney = startingCapital;
		stocks = Stocks;// new HashMap <Stock, Integer>();
		this.transactionFee = transactionFee;
	}
	
	/**
	 * Calculates the monetary value of the portfolio
	 * @return the monetary value of the portfolio
	 */
	public double getMonetaryValue(){
		double value = capital;
		for (Stock stock : stocks.keySet()){
			value += stock.currentPrice() * stocks.get(stock);
		}
		return value;
	}

	/**
	 * Buys a certain amount of of the selected stock
	 * if there is sufficient capital to buy it
	 * @param stock The stock to buy
	 * @param amount The amount of the stock to buy
	 * @return If the buy went through
	 */
	public boolean buyStock(Stock stock, int amount){
		if(capital < (stock.currentPrice() * amount) + transactionFee) {
			System.out.println("Trade to Buy " + stock.getName() + " failed: You do not have enough money");
			return false;
		}
		capital -= (stock.currentPrice() * amount) + transactionFee;
		if (stocks.containsKey(stock)){
			int oldAmount = stocks.get(stock);
			stocks.put(stock, amount+oldAmount);
		}
		else{
			stocks.put(stock, amount);
		}
		return true;
	}
	
	/**
	 * Sells a certain amount of the stock and updates the amount of capital
	 * if the portfolio contains the stock and has enough to sell. 
	 * @param stock Stock to sell
	 * @param amount Amount of stock to sell
	 * @return whether the stock has been sold
	 */
	public boolean sellStock(Stock stock, int amount){
		if (!stocks.containsKey(stock)){
			System.out.println("Trade to Sell " + stock.getName() + " failed: You do not own any stock in this company");
			return false;
		}
		int holding = stocks.get(stock);
		if (holding < amount){
			System.out.println("Trade to Sell " + stock.getName() + " failed: You do not own enough stock");
			return false;
		}
		int remaining = holding - amount;
		if (remaining == 0){
			stocks.remove(stock);
		}
		else {
			stocks.put(stock, holding - amount);	
		}
		capital += (stock.currentPrice() * amount) - transactionFee;
		return true;
	}
	
	/**
	 * Returns the amount of stock owned by the portfolio
	 * or NUll if portfolio does not contain stock
	 * @param stock to check
	 * @return the number of stocks owned by the portfolio
	 */
	public int getSharesOwned(Stock stock){
		return stocks.get(stock);
	}
	
	public Set<Stock> getAllStocks (){
		return stocks.keySet();
	}
	
	public double getProfit(){
		return getMonetaryValue() - startingMoney; 
	}
	
	public double getCapital(){
		return capital; 
	}
	
	
	@Override
	public String toString(){
		String portfolioString =  
				"Capital: " + capital + "\n" 
			  + "Stocks:\n";
		for (Stock stock : stocks.keySet()){
			portfolioString += stock.getName() + ", " + stocks.get(stock) + " Shares\n";
		}
		portfolioString += "Monetary Value: " + getMonetaryValue();
		portfolioString += "\nProfit / Loss: " + getProfit() + "\n";
		return portfolioString;
	}
	
	public Portfolio copy(){
		HashMap <Stock, Integer> copyStocks = new HashMap <Stock, Integer>();
		for (Stock stock : stocks.keySet()){
			copyStocks.put(stock.copy(), stocks.get(stock));
		}
		return new Portfolio(startingMoney, capital, copyStocks, transactionFee);
	}
	
}
	

