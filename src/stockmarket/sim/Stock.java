/**
 * 
 */
package stockmarket.sim;

import java.util.ArrayList;


/**
 * This class holds the stock price and historical prices based on round
 *
 */
public class Stock {
	private String name;
	private double price;
	private ArrayList<Double> history;

	public Stock (String _name, double initialPrice){
		name = _name;
		history = new ArrayList<Double>(); 
		price = initialPrice;
		history.add(initialPrice);
	}
	
	public Stock (String _name, double currentPrice, ArrayList<Double> history){
		name = _name;
		this.history = history; 
		price = currentPrice;
	}
	
	public void updatePrice (Integer round, double newPrice){
		price = newPrice;
		history.add(round, newPrice);
	}
	
	public double currentPrice(){
		return price;
	}
	
	public double getPriceAtRound(int round){
		return history.get(round);
	}
	
	public ArrayList<Double> getHistory(){
		return history;
	}
	
	public String getName (){
		return name;
	}
	
	@Override
	public String toString(){
		return "Stock " + name + ", Current Price: " + price; 
	}
	
	public Stock copy(){
		ArrayList<Double> copyHistory = new ArrayList<Double>();
		for (int i = 0; i < history.size(); i++){
			copyHistory.add(history.get(i));
		}
		return new Stock(name, price, copyHistory);
	}
}
