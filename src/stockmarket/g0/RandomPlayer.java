/**
 * 
 */
package stockmarket.g0;

import java.util.ArrayList;
import java.util.Random;

import stockmarket.sim.EconomicIndicator;
import stockmarket.sim.Portfolio;
import stockmarket.sim.Stock;
import stockmarket.sim.Trade;

/**
 * @author Anne
 *
 */
public class RandomPlayer extends stockmarket.sim.Player {
	private Random random;
	
	public RandomPlayer(){
		name = "Random Player";
		random = new Random();
	}
	
	@Override
	public void learn(ArrayList<EconomicIndicator> indicators,
			ArrayList<Stock> stocks) {
		System.out.println("Indicators");
		for (EconomicIndicator indicator : indicators){
			System.out.println(indicator);
		}
		System.out.println("Stocks");
		for (Stock stock : stocks){
			System.out.println(stock);
		}
	}

	/*
	@Override
	public ArrayList<Trade> placeTrade(int currentRound,
			ArrayList<EconomicIndicator> indicators, ArrayList<Stock> stocks, Portfolio portfolioCopy) {
		System.out.println("\nRound " + currentRound + "\n" + portfolioCopy);
		Stock stockToTrade = stocks.get(Math.abs(random.nextInt()%10));
		int tradeAmount = Math.abs(random.nextInt()%100);
		int type = Trade.BUY;
		if(Math.abs(random.nextInt() %2) > 0){
			type = Trade.SELL;
		}
		ArrayList<Trade> trades = new ArrayList<Trade>();
		trades.add(new Trade(type, stockToTrade, tradeAmount));
		System.out.println(trades.get(0));
		return trades;
	}
	*/
	@Override
	public ArrayList<Trade> placeTrade(int currentRound,
			ArrayList<EconomicIndicator> indicators, ArrayList<Stock> stocks, Portfolio portfolioCopy) {
		System.out.println("\nRound " + currentRound + "\n" + portfolioCopy);
		ArrayList<Trade> trades = new ArrayList<Trade>();
		int type;
		int tradeAmount;
		Stock stockToTrade;
		Object[] myStocks = portfolioCopy.getAllStocks().toArray();
		
		if(Math.abs(random.nextInt() %2) > 0 && myStocks.length > 0){
			type = Trade.SELL;
			int pickedStock = Math.abs(random.nextInt()%(myStocks.length));
			stockToTrade = (Stock) myStocks[pickedStock];
			int sharesOwned = portfolioCopy.getSharesOwned(stockToTrade);
			if (sharesOwned <= 0){
				tradeAmount = 0;
			}
			else{
				tradeAmount = Math.abs(random.nextInt()%(portfolioCopy.getSharesOwned(stockToTrade)));
			}
			
		}
		else{
			stockToTrade = stocks.get(Math.abs(random.nextInt()%10));
			double amountCanBuy = portfolioCopy.getCapital() / stockToTrade.currentPrice();
			if ((int) amountCanBuy <= 0){
				tradeAmount = 0;
			}
			else{
				tradeAmount = Math.abs(random.nextInt()%((int)amountCanBuy));
			}
			
			type = Trade.BUY;
			
		}
		
		trades.add(new Trade(type, stockToTrade, tradeAmount));
		System.out.println(trades.get(0));
		return trades;
	}

	
}
