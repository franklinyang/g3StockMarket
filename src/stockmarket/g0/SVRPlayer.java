package stockmarket.g0;

import java.io.PrintStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Collections;
import java.util.Set;

import stockmarket.sim.EconomicIndicator;
import stockmarket.sim.Market;
import stockmarket.sim.Portfolio;
import stockmarket.sim.Stock;
import stockmarket.sim.Trade;
import stockmarket.libsvm.*;
import stockmarket.libsvm.libsvm.*;

/**
 * @author Markus
 *
 */
//SVM Library Usage:
//	public static svm_model svm_train(svm_problem prob, svm_parameter param);
//	public static void svm_cross_validation(svm_problem prob, svm_parameter param, int nr_fold, double[] target);
//	public static int svm_get_svm_type(svm_model model);
//	public static int svm_get_nr_class(svm_model model);
//	public static void svm_get_labels(svm_model model, int[] label);
//	public static void svm_get_sv_indices(svm_model model, int[] indices);
//	public static int svm_get_nr_sv(svm_model model);
//	public static double svm_get_svr_probability(svm_model model);
//	public static double svm_predict_values(svm_model model, svm_node[] x, double[] dec_values);
//	public static double svm_predict(svm_model model, svm_node[] x);
//	public static double svm_predict_probability(svm_model model, svm_node[] x, double[] prob_estimates);
//	public static void svm_save_model(String model_file_name, svm_model model) throws IOException
//	public static svm_model svm_load_model(String model_file_name) throws IOException
//	public static String svm_check_parameter(svm_problem prob, svm_parameter param);
//	public static int svm_check_probability_model(svm_model model);
//	public static void svm_set_print_string_function(svm_print_interface print_func);

public class SVRPlayer extends stockmarket.sim.Player {
	private Random random;
	private double transactionFee = 5;
	private ArrayList<Double> indicatorMax;
	private ArrayList<Double> indicatorMin;
	private HashMap<Stock,Double> stockMax = new HashMap<Stock,Double>();
	private HashMap<Stock,Double> stockMin = new HashMap<Stock,Double>();
	private HashMap<Stock,svm_model> models = new HashMap<Stock,svm_model>();
	private HashMap<Stock,Double> priceDiff = new HashMap<Stock,Double>();
	
	public SVRPlayer(){
		name = "SVR Player";
		random = new Random();
	}
	
	@Override
	public void learn(ArrayList<EconomicIndicator> indicators,
			ArrayList<Stock> stocks) {
		
		transactionFee = Market.getTransactionFee();
				
		create_models(indicators,stocks);

	}
	
	private void create_models(ArrayList<EconomicIndicator> indicators,
			ArrayList<Stock> stocks){
		
		PrintStream originalStream = System.out;

		PrintStream dummyStream = new PrintStream(new OutputStream(){
		    public void write(int b) {
		        //NO-OP
		    }
		});
		
//		create the svm_problem and parameter to train an svm for each individual stock
		System.out.println("Training...");
		
		System.setOut(dummyStream);
		
		ArrayList<svm_problem> problems = new ArrayList<svm_problem>();
		ArrayList<svm_parameter> params = new ArrayList<svm_parameter>();
		for (Stock stock : stocks){
			svm_problem problem = new svm_problem();
			problem.y = getLabels(stock);
			problem.x = getTrainingX(stock,indicators);
			problem.l = problem.y.length;
			problems.add(problem);
			//add the svm params
			svm_parameter param = new svm_parameter();
            // default values
            param.svm_type = svm_parameter.EPSILON_SVR; //for regression instead of classification
            param.kernel_type = svm_parameter.RBF;
            param.degree = 3;
            param.gamma = 0;
            param.coef0 = 0;
            param.nu = 0.5;
            param.cache_size = 40;
            param.C = 1;
            param.eps = .001;
            param.p = 0.1;
            param.shrinking = 1;
            param.probability = 0;
            param.nr_weight = 0;
            param.weight_label = new int[0];
            param.weight = new double[0];
            params.add(param);
		}
		
		
		//train an svm model for each stock
		for (int i=0;i<problems.size();i++){
			Stock stock = stocks.get(i);
			svm_problem prob = problems.get(i);
			svm_parameter param = params.get(i);
			models.put(stock,svm.svm_train(prob, param));
		}
		
		System.setOut(originalStream);
		
	}
	
	private double[] getLabels(Stock stock){
		//price of the stock in the next round
		double[] result = new double[stock.getHistory().size()-1];
		for(int i=1;i<result.length;i++){
			result[i-1] = stock.getPriceAtRound(i);
		}
		return result;
	}
	
	private svm_node[][] getTrainingX(Stock stock, ArrayList<EconomicIndicator> indicators){
		//attributes are each economic indicator and then the current round price
		//scale the inputs to be [0,1)
		indicatorMax = new ArrayList<Double>();
		indicatorMin = new ArrayList<Double>();
		for(int i=0;i<indicators.size();i++){
			indicatorMin.add(Collections.min(indicators.get(i).getHistory()));
			indicatorMax.add(Collections.max(indicators.get(i).getHistory()));
		}
		stockMax.put(stock, Collections.max(stock.getHistory()));
		stockMin.put(stock, Collections.min(stock.getHistory()));
		
		//svm_node creation
		svm_node [][] nodes = (svm_node[][]) new svm_node[stock.getHistory().size()-1][indicators.size()+1];
		for(int i=0;i<stock.getHistory().size()-1;i++){
			int j=0;
			while(j<indicators.size()){
				svm_node node = new svm_node();
				node.index = j+1;
				node.value = (indicators.get(j).getValueAtRound(i) - indicatorMin.get(j))/(indicatorMax.get(j)-indicatorMin.get(j));
				nodes[i][j] = node;
				j++;
			}
			svm_node node = new svm_node();
			node.index = j+1;
			node.value = (stock.getPriceAtRound(i) - stockMin.get(stock))/(stockMax.get(stock)-stockMin.get(stock));
			nodes[i][j] = node;
		}
		return nodes;
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
		
		//first, re-train the models based on the new round
		create_models(indicators,stocks);
		//now, create a price prediction differential for each stock and keep track of the max
		double max = 0;
		Stock winner = null;
		for (Stock stock: stocks){
			svm_model model = models.get(stock);
			double new_price = svm.svm_predict(model, getTestX(stock,indicators,currentRound));
			double diff = new_price - stock.currentPrice();
			priceDiff.put(stock, diff);
			if(diff>max){
				winner = stock;
				max = diff;
			}
		}
		//System.out.println("winner is: "+ winner.getName()+"; price diff="+ max);
		
		//sell all stocks not the winner
		Set<Stock> myStocks = portfolioCopy.getAllStocks();
		double capital = portfolioCopy.getCapital();
		for(Stock stock : myStocks){
			if(winner == null || !stock.getName().equals(winner.getName())){
				int amount = portfolioCopy.getSharesOwned(stock);
				double price = stock.currentPrice();
				trades.add(new Trade(Trade.SELL,stock,amount));
				capital+=amount*price;
			}
		}
		
		//purchase as much as you can of the winner
		if(winner != null){
			int amount = (int)Math.floor(capital/winner.currentPrice());
			trades.add(new Trade(Trade.BUY,winner,amount));
			System.out.println("Buying "+amount+" of "+winner.getName()+" for "+winner.currentPrice());
		}
		
		
		return trades;
	}
	
	private svm_node[] getTestX(Stock stock, ArrayList<EconomicIndicator> indicators, int currentRound){
		//attributes are each economic indicator and then the current round price
		//scale the inputs to be [0,1)
		
		//svm_node creation
		svm_node [] nodes = (svm_node[]) new svm_node[indicators.size()+1];
		int j=0;
		while(j<indicators.size()){
			svm_node node = new svm_node();
			node.index = j+1;
			node.value = (indicators.get(j).getValueAtRound(currentRound) - indicatorMin.get(j))/(indicatorMax.get(j)-indicatorMin.get(j));
			nodes[j] = node;
			j++;
		}
		svm_node node = new svm_node();
		node.index = j+1;
		node.value = (stock.getPriceAtRound(currentRound) - stockMin.get(stock))/(stockMax.get(stock)-stockMin.get(stock));
		nodes[j] = node;
		return nodes;
	}

	
}
