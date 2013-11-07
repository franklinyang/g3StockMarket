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
		name = "Ood Player";
		random = new Random();
	}
	
	private double[][] getRegressionCoefficients(ArrayList<EconomicIndicator> indicators,
			ArrayList<Stock> stocks) {
		
		int m = indicators.size() + 2; //the number of independent variables: indicators + last period price + one constant
		int n = indicators.get(0).getHistory().size() - 1;// the number of training data sets; confused with the last item
		int stockNum = stocks.size();
		double[][] result = new double[stockNum][m]; 
		
		double[][] trainingData = new double[m - 1][n];
		double[][] currentPrice = new double[stockNum][n]; // store dependent variables: each period price
		
		for (int i = 0; i < indicators.size(); i ++) { // push indicators' historical data to trainingData array
			
			EconomicIndicator indicator = indicators.get(i); 
			for (int j = 0; j < n; j ++) {

				trainingData[i][j] = indicator.getHistory().get(j);
			}
		}
		
		int stockIndex = 0;
		for (Stock stock : stocks) { // get the regression coefficients for each stock
			
			trainingData[m - 2][0] = 0; 
			for (int i = 0; i < n; i ++) { // push stocks' historical data to trainingData array
				
				currentPrice[stockIndex][i] = stock.getHistory().get(i);
				
				if (i != 0) {
					trainingData[m - 2][i] = stock.getHistory().get(i - 1);
				}
			}
			
			double[] v = new double[m];
			double[] dt = new double[4];
			
			LinearRegression.implemation(trainingData, currentPrice[stockIndex], m - 1, n, result[stockIndex], dt, v);
			stockIndex ++;
		}
		
//		for(int i = 0; i < stockNum; i++) {
//			System.err.println("+++++++++++++++++++++++++++++++++++++++++");
//			System.err.println("Stock " + i);
//			for(int j = 0; j < m; j++) {
//				System.err.print(result[i][j] + ", ");
//			}
//		}
		
		return result;
	}
	
	@Override
	public void learn(ArrayList<EconomicIndicator> indicators,
			ArrayList<Stock> stocks) {
		ArrayList<Double> indicatorHistory;
		
//		regressionOutput = getRegressionCoefficients(indicators, stocks);

//		for (int i = 0; i < stocks.size(); i++) {
//			System.err.println("+++++++++++++++++++++++++++++++++++++++++");
//			System.err.println("Stock " + i);
//			for (int j = 0; j < indicators.size() + 2; j++) {
//				System.err.print(regressionOutput[i][j] + ", ");
//			}
//		}
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
		
		regressionOutput = getRegressionCoefficients(indicators, stocks);
		
		for (int i = 0; i < myStocks.length; i ++) {
			
			stockToTrade = (Stock) myStocks[i];
			int sharesOwned = portfolioCopy.getSharesOwned(stockToTrade);
			if (sharesOwned <= 0){
				tradeAmount = 0;
			} else{
				tradeAmount = sharesOwned;
			}
			portfolioCopy.sellStock(stockToTrade, tradeAmount);
			trades.add(new Trade(Trade.SELL, stockToTrade, tradeAmount));
		}
		
		double maxSlope = 0;
		int maxIndex = 0;
		for (int i = 0; i < stocks.size(); i++) {
			
			double predictPrice = 0;
			for (int j = 0; j < indicators.size(); j ++) {

				predictPrice += (float)(regressionOutput[i][j]) * (float)(indicators.get(j).currentValue());
			}
			
			int historySize = stocks.get(i).getHistory().size();
			double lastPrice = stocks.get(i).getHistory().get(historySize - 3);
			predictPrice += lastPrice * regressionOutput[i][indicators.size() - 2];
			predictPrice += regressionOutput[i][indicators.size() - 1];
			
			double currentSlope = (predictPrice - lastPrice) / lastPrice;
			if (i == 0) {
				
				maxSlope = currentSlope;
				maxIndex = 0;
			} else {
				
				if (currentSlope > maxSlope) {
					maxSlope = currentSlope;
					maxIndex = i;
				}
			}
		}
		
//		if (maxSlope > 0) {
			
			stockToTrade = stocks.get(maxIndex);
			tradeAmount = (int) (portfolioCopy.getCapital() / stockToTrade.currentPrice());
			trades.add(new Trade(Trade.BUY, stockToTrade, tradeAmount));
//		}

		return trades;
	}

	
}