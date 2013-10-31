/**
 * 
 */
package stockmarket.sim;

import java.util.ArrayList;

/**
 * @author Anne
 *
 */
public class EconomicIndicator {
	private String name;
	private double value;
	private ArrayList<Double> history;
	
	public EconomicIndicator(String name, double initialValue){
		this.name = name;
		value = initialValue;
		history = new ArrayList<Double>();
		history.add(initialValue);
	}
	
	public EconomicIndicator(String _name, double currentValue, ArrayList<Double> history){
		name = _name;
		this.value = currentValue;
		this.history = history;
	}
	
	public void updateValue (Integer round, double newValue){
		value = newValue;
		history.add(round, newValue);
	}
	
	public double currentValue(){
		return value;
	}
	
	public double getValueAtRound(int round){
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
		return "Economic Indicator " + name + ", Current Value: " + value; 
	}

	public EconomicIndicator copy(){
		ArrayList<Double> copyHistory = new ArrayList<Double>();
		for (int i = 0; i < history.size(); i++){
			copyHistory.add(history.get(i));
		}
		return new EconomicIndicator(name, value, copyHistory);
	}
	
	
}

