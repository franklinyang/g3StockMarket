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

public class SVMPlayer extends stockmarket.sim.Player {
	private Random random;
	private double transactionFee = 5;
	
	public SVMPlayer(){
		name = "Random Player";
		random = new Random();
	}
	
	@Override
	public void learn(ArrayList<EconomicIndicator> indicators,
			ArrayList<Stock> stocks) {
//		create the svm_problem and parameter to train an svm for each individual stock
		ArrayList<svm_problem> problems = new ArrayList<svm_problem>();
		ArrayList<svm_parameter> params = new ArrayList<svm_parameter>();
		for (Stock stock : stocks){
			svm_problem problem = new svm_problem();
			problem.y = getLabels(stock);
			problem.x = getTrainingX(stock,indicators);
			problem.l = problem.y.length;
			//add the svm params
			svm_parameter param = new svm_parameter();
            // default values
            param.svm_type = svm_parameter.C_SVC;
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
			svm_problem prob = problems.get(i);
			
		}
		
		System.out.println("Indicators");
		for (EconomicIndicator indicator : indicators){
			System.out.println(indicator);
		}

	}
	
	private double[] getLabels(Stock stock){
		//buy = 1
		//hold = 0
		//sell = -1
		//buy when above transaction fee, hold when within transactionFee of 0, sell when below
		double[] result = new double[stock.getHistory().size()-1];
		for(int i=1;i<result.length;i++){
			double diff = stock.getPriceAtRound(i) - stock.getPriceAtRound(i-1);
			if(diff - transactionFee > 0)
				result[i-1] = (double) 1;
			else if(-transactionFee < diff && diff < transactionFee)
				result[i-1] = (double) 0;
			else
				result[i-1] = (double) -1;
		}
		return result;
	}
	
	private svm_node[][] getTrainingX(Stock stock, ArrayList<EconomicIndicator> indicators){
		//attributes are each economic indicator and then the current round price
		svm_node [][] nodes = (svm_node[][]) new svm_node[stock.getHistory().size()-1][indicators.size()+1];
		for(int i=0;i<stock.getHistory().size()-1;i++){
			int j=0;
			while(j<indicators.size()){
				svm_node node = new svm_node();
				nodes[i][j+1] = node;
				j++;
			}
			svm_node node = new svm_node();
			nodes[i][j+1] = node;
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

	
}
