package StockSim;

public class TrendInfo extends StockInfo{
	private boolean buyTrend             = false;
	private boolean sellTrend            = false;
	private boolean initialTradeComplete = false;
	private boolean finalTradeReady      = false;

	private Double macdStartingValue;
	
	public boolean isBuyTrend() {
		return buyTrend;
	}
	public void setBuyTrend(boolean buyTrend) {
		this.buyTrend = buyTrend;
	}
	
	public boolean isSellTrend() {
		return sellTrend;
	}
	public void setSellTrend(boolean sellTrend) {
		this.sellTrend = sellTrend;
	}
	
	public boolean isInitialTradeComplete() {
		return initialTradeComplete;
	}
	public void setInitialTradeComplete(boolean initialTradeComplete) {
		this.initialTradeComplete = initialTradeComplete;
	}
	
	public Double getMacdStartingValue() {
		return macdStartingValue;
	}
	public void setMacdStartingValue(Double macdStartingValue) {
		this.macdStartingValue = macdStartingValue;
	}
	
	public boolean isFinalTradeReady() {
		return finalTradeReady;
	}
	public void setFinalTradeReady(boolean finalTradeReady) {
		this.finalTradeReady = finalTradeReady;
	}
}