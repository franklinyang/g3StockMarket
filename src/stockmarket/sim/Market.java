/**
 * 
 */
package stockmarket.sim;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class holds the stocks, economic indicators and the 
 * player's portfolios. The Market begins by giving the Players the training
 * data and asking them to learn the stocks. Every subsequent round it asks 
 * the player to trade and completes the buy or sell. 
 */
public class Market {

	private ArrayList<Player> players;
	private HashMap <Player, Portfolio> portfolios;
	private static double marketTransactionFee ;
	private static double playerStartingCapital;
	
	public Market(ArrayList<Player> players, double startingCapital, double transactionFee){
		this.players = players;
		portfolios = new HashMap <Player, Portfolio>();
		for (Player player : players){
			portfolios.put(player, new Portfolio(startingCapital, transactionFee));
		}
		marketTransactionFee = transactionFee;
		playerStartingCapital = startingCapital; 
	}	
	
	public void trainPlayers(ArrayList<EconomicIndicator> indicators, ArrayList<Stock> stocks){
		for(Player player : players){
			player.learn(copyIndicaotrs(indicators), copyStocks(stocks));
		}	
	}

	/**
	 * @param 
	 */
	public ArrayList<Trade> newRound(int round, ArrayList<EconomicIndicator> indicators, ArrayList<Stock> stocks) {
		HashMap<String, Stock> stockMap = mapPrices(stocks);
		ArrayList<Trade> allTrades = new ArrayList<Trade>();
		for (Player player : players){
			ArrayList<Trade> trades = player.placeTrade(round, 
										copyIndicaotrs(indicators), copyStocks(stocks), portfolios.get(player).copy());
			if (trades == null){
				continue;
			}
			for (Trade trade : trades){
				if(trade.getAction() == Trade.SELL){
					portfolios.get(player).sellStock(stockMap.get(trade.getStock().getName()), trade.getQuantity());
					allTrades.add(trade);
				}
			}
			for (Trade trade : trades){
				if(trade.getAction() == Trade.BUY){
					portfolios.get(player).buyStock(stockMap.get(trade.getStock().getName()), trade.getQuantity());
					allTrades.add(trade);
				}
			}
		}
		return allTrades;
	}
	

	/**
	 * @param stocks
	 * @return
	 */
	private HashMap<String, Stock> mapPrices(ArrayList<Stock> stocks) {
		HashMap<String, Stock> prices = new HashMap<String, Stock>();
		for (Stock stock : stocks){
			prices.put(stock.getName(), stock);
		}
		return prices;
	}
	
	public static double getTransactionFee() {
		return marketTransactionFee;
	}
	
	public static double getPlayerStartingCapital() {
		return playerStartingCapital;
	}
	
	public Boolean allBankrupt(){
		for (Portfolio portfolio : portfolios.values()){
			if (portfolio.getMonetaryValue() > 0) return false;
		}
		return true;
	}
	
	public static int getMaxRounds(){
		return Simulator.MAX_ROUNDS;
	}

	/**
	 * Prints all portfolios
	 */
	public void printPorfolios() {
		for (Player player : players){
			System.out.println("\n" + player.name );
			System.out.print(portfolios.get(player));
		}
	}
	
	private ArrayList<Stock> copyStocks(ArrayList<Stock> original){
		ArrayList<Stock> copy = new ArrayList<Stock>();
		for (Stock item : original){
			copy.add(item.copy());
		}
		return copy;
	}
	
	private ArrayList<EconomicIndicator> copyIndicaotrs(ArrayList<EconomicIndicator> original){
		ArrayList<EconomicIndicator> copy = new ArrayList<EconomicIndicator>();
		for (EconomicIndicator item : original){
			copy.add(item.copy());
		}
		return copy;
	}

	public Portfolio getPortfolio(Player player) {
		return portfolios.get(player);
	}
}