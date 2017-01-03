package StockSim;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import GenCol.doubleEnt;
import GenCol.entity;
import model.modeling.content;
import model.modeling.message;
import view.modeling.ViewableAtomic;

public class StockBuyAndHold extends ViewableAtomic {

	private ArrayList<StockInfo> ownedStocks = new ArrayList<StockInfo>();
	private ArrayList<StockInfo> tradedStocks = new ArrayList<StockInfo>();
	Map<String, Double> stockValues;
	private int day;
	private int totalSimulationDays;
	private int maxStocks;
	private int shareCount;
	private int holdingTime;
	private double lossLimitPercent;
	private double gainLimitPercent;
	String INPUT_STATE             = "InState";
	String INPUT_SYMBOL            = "InSymbol";
	String INPUT_PRICES            = "InPrices";
	String BUY_SELL_BALANCE_OUTPUT = "BuySellBalance";
	
	public StockBuyAndHold(String name, int totalSimulationDays){
		super(name);
		this.totalSimulationDays = totalSimulationDays;

		addInport(INPUT_PRICES);
		addInport(INPUT_SYMBOL);
		addInport(INPUT_STATE);

		addOutport(BUY_SELL_BALANCE_OUTPUT);
		setBackgroundColor(Color.CYAN);
	}

	public void initialize(){
		holdIn("passive", INFINITY);
		day = 1;
		maxStocks = 10;
		shareCount = 100;
		holdingTime = 150;
		lossLimitPercent = 0.15;
		gainLimitPercent = 0.10;
		super.initialize();
	}

	public void  deltext(double e,message x){
		Map<String, String> messageMap = new HashMap<String,String>();
		for(int i=0; i< x.size();i++){
			//Iterate through all messages and put them in a map
			if(messageOnPort(x, INPUT_PRICES, i)){
		    	messageMap.put(INPUT_PRICES, x.getValOnPort(INPUT_PRICES, i).getName());
		    }
			if(messageOnPort(x, INPUT_SYMBOL, i)){
		    	messageMap.put(INPUT_SYMBOL, x.getValOnPort(INPUT_SYMBOL, i).getName());
		    }
		    if(messageOnPort(x, INPUT_STATE, i)){
		    	messageMap.put(INPUT_STATE, x.getValOnPort(INPUT_STATE, i).getName());
		    }
		}
		if(messageMap.containsKey(INPUT_STATE)){
			if(messageMap.get(INPUT_STATE).equalsIgnoreCase("open")){
				Continue(e);

				String[] prices = messageMap.get(INPUT_PRICES).split(":");
				stockValues = new HashMap<String, Double>();

			    for (int i = 0; i < prices.length; i = i+2){
			    	stockValues.put(prices[i], Double.parseDouble(prices[i+1]));
			    }

				//Iterate through owned stocks
				Iterator<StockInfo> iter = ownedStocks.iterator();
				while(iter.hasNext()){
					StockInfo stock = iter.next();
					//Check open price of owned stocks
					if(stockValues.get(stock.getStockSymbol()) < stock.getLossLimit() ||
					   (stock.getHoldTime() > holdingTime && stockValues.get(stock.getStockSymbol()) > stock.getGainLimit())){
					    stock.setSalePrice(stockValues.get(stock.getStockSymbol()));
						tradedStocks.add(stock);
						iter.remove();
						phase = "sell";
					}else{
						stock.setHoldTime(stock.getHoldTime() + 1);
					}
				}

				 if(phaseIs("passive")){
					 if(ownedStocks.size() < maxStocks &&
						day < (totalSimulationDays - holdingTime)){
						 StockInfo stock = new StockInfo();
						 stock.setStockSymbol(messageMap.get(INPUT_SYMBOL));
						 stock.setPurchasePrice(stockValues.get(messageMap.get(INPUT_SYMBOL)));
						 stock.setLossLimit(stockValues.get(messageMap.get(INPUT_SYMBOL)) - (stockValues.get(messageMap.get(INPUT_SYMBOL)) * lossLimitPercent));
						 stock.setGainLimit(stockValues.get(messageMap.get(INPUT_SYMBOL)) + (stockValues.get(messageMap.get(INPUT_SYMBOL)) * gainLimitPercent));
						 ownedStocks.add(stock);
						 tradedStocks.add(stock);
						 phase = "buy";
					 }
					 
				 }
				
				 sigma = 0.5;
			}else if(messageMap.get(INPUT_STATE).equalsIgnoreCase("mid")){
				sigma = 0.5;
			}else if(messageMap.get(INPUT_STATE).equalsIgnoreCase("close")){
				sigma = 0.5;
				day++;
			}else if(messageMap.get(INPUT_STATE).equalsIgnoreCase("end")){
				phase = "end";
				sigma = INFINITY;
			}
		}
	}

	public void  deltint( ){
		if(day == totalSimulationDays){
			phase = "sell all";
			sigma = 0.5;
		}else if(phaseIs("buy") ||
				 phaseIs("sell")){
			sigma = 0.5;
		}
	}

	public message out( ){
		message  m = new message();

		if(phaseIs("buy")) {
			for(StockInfo element : tradedStocks){
				content con = makeContent(BUY_SELL_BALANCE_OUTPUT, new entity("-" + element.getPurchasePrice()*shareCount));
				m.add(con);
			}
			tradedStocks.clear();
		}else if(phaseIs("sell")) {
			for(StockInfo element : tradedStocks){
				content con = makeContent(BUY_SELL_BALANCE_OUTPUT, new entity("+" + element.getSalePrice()*shareCount));
				m.add(con);
			}
			tradedStocks.clear();
		}else if(phaseIs("sell all")){
			for(StockInfo element : ownedStocks){
				content con = makeContent(BUY_SELL_BALANCE_OUTPUT, new entity("+" + stockValues.get(element.getStockSymbol())*shareCount));
				m.add(con);
			}
			ownedStocks.clear();
		}
		
		phase = "passive";
		
		return m;
	}
	
	public String getTooltipText(){
	    return
	    super.getTooltipText()
	    	+"\n"+" Day: " + day;
	}

}
