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
import stockmarket.LinearRegression.*;
/**
 * @author Anne
 *
 */
public class OodPlayer extends stockmarket.sim.Player {
	private Random random;
	double[][] regressionOutput;
	
	public OodPlayer(){
		name = "Random Player";
		random = new Random();
	}
	
	@Override
	public void learn(ArrayList<EconomicIndicator> indicators,
			ArrayList<Stock> stocks) {
		ArrayList<Double> indicatorHistory;
		
		regressionOutput = new double[indicators.size()][indicators.get(0).getHistory().size()];
		
		// setting x in LinearRegression.implementation
		double[][] trainingData = new double[indicators.size()][indicators.get(0).getHistory().size()];
		for(int i = 0; i < indicators.size(); i++) {
			indicatorHistory = indicators.get(i).getHistory();
			for(int j = 0; j < trainingData[i].length; j++) {
				trainingData[i][j] = indicatorHistory.get(j);
			}
		}
		
		// setting y in LinearRegression.implementation
		for(int s = 0; s < stocks.size(); s++) {
			ArrayList<Double> stockHistory = stocks.get(s).getHistory();
			double[] stockData = new double[stocks.get(s).getHistory().size()];
			double[] coeffs = new double[indicators.size()];
			for(int i = 0; i < stockData.length; i++) {
				stockData[i] = stockHistory.get(i);
			}
			int m = indicators.size();
			LinearRegression.implemation(trainingData, stockData, indicators.size(), stockData.length, coeffs, new double[4], new double[m]);
			for(int t = 0; t < m; t++) {
				regressionOutput[t][s] = coeffs[t];
			}
		}
		for(int i = 0; i < regressionOutput.length; i++) {
			System.err.println("+++++++++++++++++++++++++++++++++++++++++");
			System.err.println("Stock " + i);
			for(int j = 0; j < regressionOutput[0].length; j++) {
				System.err.print(regressionOutput[i][j] + ", ");
			}
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
