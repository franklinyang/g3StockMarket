package stockmarket.g0;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Collections;

import stockmarket.sim.EconomicIndicator;
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
	
	public SVRPlayer(){
		name = "SVR Player";
		random = new Random();
	}
	
	@Override
	public void learn(ArrayList<EconomicIndicator> indicators,
			ArrayList<Stock> stocks) {
//		create the svm_problem and parameter to train an svm for each individual stock
		System.out.println("Setting up svm problems");
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
		ArrayList<svm_model> models = new ArrayList<svm_model>();
		for (int i=0;i<problems.size();i++){
			System.out.println("training "+stocks.get(i).getName());
			svm_problem prob = problems.get(i);
			svm_parameter param = params.get(i);
			models.add(svm.svm_train(prob, param));
		}

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
	
	private svm_node[][] getTestX(Stock stock, ArrayList<EconomicIndicator> indicators){
		//attributes are each economic indicator and then the current round price
		//scale the inputs to be [0,1)
		
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

	
}
