package StockSim;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import GenCol.entity;
import model.modeling.content;
import model.modeling.message;
import view.modeling.ViewableAtomic;

public class StockDayTradeScalp extends ViewableAtomic {

	ArrayList<String> stockSymbols;
	Map<String, TrendInfo> trendMap;
	Map<String, Double> stockValues;
	Map<String, Double> macdValues;
	Map<String, Double> signalValues;
	DecimalFormat df = new DecimalFormat("#.00"); 
	private int day;
	private int shareCount;
	private int totalCompletedTrades;
	String INPUT_PRICES            = "InPrices";
	String INPUT_STATE             = "InState";
	String INPUT_MACD              = "InMACD";
	String INPUT_SIGNAL            = "InSignal";
	String BUY_SELL_BALANCE_OUTPUT = "BuySellBalance";
	
	public StockDayTradeScalp(String name, ArrayList<String> stockSymbols){
		super(name);
		this.stockSymbols = stockSymbols;
		addInport(INPUT_PRICES);
		addInport(INPUT_STATE);
		addInport(INPUT_MACD);
		addInport(INPUT_SIGNAL);

		addOutport(BUY_SELL_BALANCE_OUTPUT);
		setBackgroundColor(Color.CYAN);
	}

	public void initialize(){
		holdIn("passive", INFINITY);
		trendMap = new HashMap<String, TrendInfo>();
		day = 1;
		shareCount  = 100;
		totalCompletedTrades = 0;
		super.initialize();
	}

	public void  deltext(double e,message x){
		Map<String, String> messageMap = new HashMap<String,String>();
		for(int i=0; i< x.size();i++){
			//Iterate through all messages and put them in a map
			if(messageOnPort(x, INPUT_PRICES, i)){
		    	messageMap.put(INPUT_PRICES, x.getValOnPort(INPUT_PRICES, i).getName());
		    }
			if(messageOnPort(x, INPUT_STATE, i)){
		    	messageMap.put(INPUT_STATE, x.getValOnPort(INPUT_STATE, i).getName());
		    }
			if(messageOnPort(x, INPUT_MACD, i)){
		    	messageMap.put(INPUT_MACD, x.getValOnPort(INPUT_MACD, i).getName());
		    }
		    if(messageOnPort(x, INPUT_SIGNAL, i)){
		    	messageMap.put(INPUT_SIGNAL, x.getValOnPort(INPUT_SIGNAL, i).getName());
		    }
		}

		if(messageMap.containsKey(INPUT_STATE)){
			phase = messageMap.get(INPUT_STATE);
			if(phaseIs("end")){
				sigma = INFINITY;
			}else{
				Continue(e);
				if(messageMap.containsKey(INPUT_MACD) &&
				   messageMap.containsKey(INPUT_SIGNAL)){
					String[] prices       = messageMap.get(INPUT_PRICES).split(":");
					String[] macdInput    = messageMap.get(INPUT_MACD).split(":");
					String[] signalInput  = messageMap.get(INPUT_SIGNAL).split(":");
					stockValues  = new HashMap<String, Double>();
					macdValues   = new HashMap<String, Double>();
					signalValues = new HashMap<String, Double>();

				    for (int i = 0; i < prices.length; i = i+2){
				    	stockValues.put(prices[i], Double.parseDouble(prices[i+1]));
				    }
				    for (int i = 0; i < macdInput.length; i = i+2){
				    	macdValues.put(macdInput[i], Double.parseDouble(macdInput[i+1]));
				    }
				    for (int i = 0; i < signalInput.length; i = i+2){
				    	signalValues.put(signalInput[i], Double.parseDouble(signalInput[i+1]));
				    }
					
					for (String symbol : stockSymbols) {
						TrendInfo trendObject = new TrendInfo();
						//If there is no trend data for the current stock symbol
						//Check to see if a buy trend could be in the future
						if(!trendMap.containsKey(symbol)){
							//If the MACD value is below the current signal value,
							//then set the trend to watch for as a buy trend
							if(macdValues.get(symbol) <  signalValues.get(symbol)){
								trendObject.setBuyTrend(true);
								trendMap.put(symbol, trendObject);
							}
						//If the stock has already been purchased
						}else if(trendMap.get(symbol).isInitialTradeComplete()){
							//As soon as the current price is greater than the purchase price, sell the stock
							if(stockValues.get(symbol) > trendMap.get(symbol).getPurchasePrice()){
								if(trendMap.get(symbol).isBuyTrend()){
									trendMap.get(symbol).setSalePrice(stockValues.get(symbol));
									trendMap.get(symbol).setFinalTradeReady(true);
								}
							}
						//If looking for a time to buy and the MACD has surpassed the signal value	
						}else if(trendMap.get(symbol).isBuyTrend() &&
								macdValues.get(symbol) > signalValues.get(symbol)){
							//If looking for a buy trend and the MACD value passes the signal, buy the stock
							//and save the current MACD value
							trendMap.get(symbol).setPurchasePrice(stockValues.get(symbol));
							trendMap.get(symbol).setMacdStartingValue(Math.abs(macdValues.get(symbol)));
						}
					}
					
				}
				
				sigma = 0.5;
			}
			
		}

	}

	public void  deltint( ){
		sigma = 0.5;
	}

	public message out( ){
		message  m = new message();

		//If the phase is close, complete the trades on the remaining stocks
		if(phaseIs("close")) {
			day++;
			for (Map.Entry<String, TrendInfo> entry : trendMap.entrySet()){
				//Sell the stock to complete the final trade if the initial trade was completed
				if(entry.getValue().isInitialTradeComplete()){
					double transactionAmount = stockValues.get(entry.getKey()) * shareCount;
					if(entry.getValue().isBuyTrend()){
						content con = makeContent(BUY_SELL_BALANCE_OUTPUT, new entity("+" + transactionAmount));
						m.add(con);
						totalCompletedTrades++;
					}
				}
			}
			trendMap.clear();
		//Else try to complete a trade based on the current trend
		}else{
			for(Iterator<Map.Entry<String, TrendInfo>> it = trendMap.entrySet().iterator(); it.hasNext(); ) {
			      Map.Entry<String, TrendInfo> entry = it.next();
			      
			       if(!entry.getValue().isInitialTradeComplete()){
						if(entry.getValue().getPurchasePrice() != null){
							//Buy the stock to complete the initial trade
							entry.getValue().setInitialTradeComplete(true);
							content con = makeContent(BUY_SELL_BALANCE_OUTPUT, new entity("-" + entry.getValue().getPurchasePrice() * shareCount));
							m.add(con);
						}
					}else if(entry.getValue().isFinalTradeReady()){
						//Sell the stock to complete the final trade
						content con = makeContent(BUY_SELL_BALANCE_OUTPUT, new entity("+" + entry.getValue().getSalePrice() * shareCount));
						m.add(con);
						it.remove();
						totalCompletedTrades++;
					}
			}
			
		}
		
		return m;
	}
	
	public String getTooltipText(){
	    return
	    super.getTooltipText()
	    	+"\n"+" Day: " + day
	    	+"\n"+" Standard Trades: " + totalCompletedTrades;
	}

}
