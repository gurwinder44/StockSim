package StockSim;

public class StockInfo{
	private Double purchasePrice;
	private Double salePrice;
	private Double previousValue;
	private Double openValue;
	private Double closeValue;
	private Double volatility;
	private Double drift;
	private Double lossLimit;
	private Double gainLimit;
	private String stockSymbol;
	private int    holdTime = 0;
	
	 public Double getPurchasePrice() {
		return purchasePrice;
	 }

	 public void setPurchasePrice(Double purchasePrice) {
		this.purchasePrice = purchasePrice;
	 }
	 
	 public Double getSalePrice() {
		return salePrice;
	 }

	 public void setSalePrice(Double salePrice) {
	 	this.salePrice = salePrice;
	 }
	
	 public Double getPreviousValue() {
		return previousValue;
	 }

  	 public void setPreviousValue(Double previousValue) {
		this.previousValue = previousValue;
	 }
	 
	 public Double getOpenValue(){
		 return openValue;
	 }

	 public void setOpenValue(Double openValue){
		 this.openValue = openValue;
	 }
	 
	 public Double getCloseValue(){
		 return closeValue;
	 }
	 
	 public void setCloseValue(Double closeValue){
		 this.closeValue = closeValue;
	 }
	 
	public Double getVolatility() {
		return volatility;
	}

	public void setVolatility(Double volatility) {
		this.volatility = volatility;
	}

	public Double getDrift() {
		return drift;
	}

	public void setDrift(Double drift) {
		this.drift = drift;
	}
	 
	public Double getLossLimit() {
		return lossLimit;
	}

	public void setLossLimit(Double lossLimit) {
		this.lossLimit = lossLimit;
	}

	public Double getGainLimit() {
		return gainLimit;
	}

	public void setGainLimit(Double gainLimit) {
		this.gainLimit = gainLimit;
	}

	public String getStockSymbol() {
		return stockSymbol;
	}

	public void setStockSymbol(String stockSymbol) {
		this.stockSymbol = stockSymbol;
	}
	
	public int getHoldTime() {
		return holdTime;
	}

	public void setHoldTime(int holdTime) {
		this.holdTime = holdTime;
	}
	
	
}