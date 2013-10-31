/**
 * 
 */
package stockmarket.sim;

/**
 * This holds the info the Market uses to make a trade 
 *
 */
public class Trade {
	public static final int BUY    = 1;
	public static final int SELL   = -1;
	
	private Stock stock;
	private int quantity;
	private int action;
	
	public Trade (int action, Stock stock, int quantity){
		this.action = action;
		this.stock = stock;
		this.quantity = quantity;
	}
	
	
	public Stock getStock() {
		return stock;
	}
	
	public String getStockName() {
		return stock.getName();
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}	
	
	public double getCostBeforeTransactionFee() {
		return (stock.currentPrice() * quantity);
	}	
	
	
	
	@Override
	public String toString() {
		if (action == BUY){
			return "Trade: [ Buy " + quantity + " shares of "+ stock.getName() 
					+ " at " + stock.currentPrice() + "]";
		}
		return "Trade: [ Sell " + quantity + " shares of "+ stock.getName() 
				+ " at " + stock.currentPrice() + "]";
	}
	

}
