package StockSim;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import GenCol.entity;
import model.modeling.content;
import model.modeling.message;
import view.modeling.ViewableAtomic;

public class MACD extends ViewableAtomic {

	Map<String, Queue<Double>> twelvePriceEmaMap;
	Map<String, Double> twelvePriceCalulatedEmaMap;
	Map<String, Queue<Double>> twentySixPriceEmaMap;
	Map<String, Double> twentySixPriceCalulatedEmaMap;
	Map<String, Queue<Double>> ninePriceMacMap;
	Map<String, Double> currentMacdMap;
	Map<String, Double> currentSignalMap;
	String INPUT_PRICES            = "InPrices";
	String INPUT_STATE             = "InState";
	String MACD_OUTPUT             = "MACD";
	String SIGNAL_OUTPUT           = "Signal";
	String macdOutputString        = "";
	String signalOutputString      = "";
	
	public MACD(String name){
		super(name);

		addInport(INPUT_PRICES);
		addInport(INPUT_STATE);

		addOutport(MACD_OUTPUT);
		addOutport(SIGNAL_OUTPUT);
		setBackgroundColor(Color.CYAN);
	}

	public void initialize(){
		twelvePriceEmaMap    = new HashMap<String, Queue<Double>>();
		twentySixPriceEmaMap = new HashMap<String, Queue<Double>>();
		ninePriceMacMap      = new HashMap<String, Queue<Double>>();
		twelvePriceCalulatedEmaMap = new HashMap<String, Double>();
		twentySixPriceCalulatedEmaMap = new HashMap<String, Double>();
		currentMacdMap   = new HashMap<String, Double>();
		currentSignalMap = new HashMap<String, Double>();
		holdIn("active", 0.5);
		super.initialize();
	}

	public void  deltext(double e,message x){
		Continue(e);
		Map<String, String> messageMap = new HashMap<String,String>();
		for(int i=0; i< x.size();i++){
			//Iterate through all messages and put them in a map
			if(messageOnPort(x, INPUT_PRICES, i)){
		    	messageMap.put(INPUT_PRICES, x.getValOnPort(INPUT_PRICES, i).getName());
		    }
		    if(messageOnPort(x, INPUT_STATE, i)){
		    	messageMap.put(INPUT_STATE, x.getValOnPort(INPUT_STATE, i).getName());
		    }
		}
		if(messageMap.containsKey(INPUT_STATE)){
			if(messageMap.get(INPUT_STATE).equalsIgnoreCase("open") ||
			   messageMap.get(INPUT_STATE).equalsIgnoreCase("mid") ||
			   messageMap.get(INPUT_STATE).equalsIgnoreCase("close")){

				String[] prices = messageMap.get(INPUT_PRICES).split(":");
				calculateTwelveDayEma(prices);
				calculateTwentySixDayEma(prices);
				if(!twelvePriceCalulatedEmaMap.isEmpty() &&
				   !twentySixPriceCalulatedEmaMap.isEmpty()){
					calculateMACD(prices);
				}
				if(!ninePriceMacMap.isEmpty()){
					if(ninePriceMacMap.get(prices[0]).size() == 9){
						calculateSignal(prices);
					}
				}
				if(!currentSignalMap.isEmpty()){
					macdOutputString   = "";
					signalOutputString = "";
					createOutputStrings(prices);
				}else{
					macdOutputString   = "";
					signalOutputString = "";
				}
				
				sigma = 0.5;
			}else if(messageMap.get(INPUT_STATE).equalsIgnoreCase("end")){
				phase = "end";
				sigma = INFINITY;
			}
		}
	}

	public void  deltint( ){
		sigma = 0.5;
	}

	public message out( ){
		message  m = new message();
		if(!macdOutputString.isEmpty()){
			content con1 = makeContent(MACD_OUTPUT, new entity(macdOutputString));
			m.add(con1);
		}
		if(!signalOutputString.isEmpty()){
			content con2 = makeContent(SIGNAL_OUTPUT, new entity(signalOutputString));
			m.add(con2);
		}
		
		return m;
	}
	
	public String getTooltipText(){
	    return
	    super.getTooltipText();
	}
	
	private void calculateTwelveDayEma(String[] priceArray){
		for (int i = 0; i < priceArray.length; i = i+2){
			if(twelvePriceCalulatedEmaMap.containsKey(priceArray[i])){
				double currentPrice = Double.parseDouble(priceArray[i+1]);
				double previousEma  = twelvePriceCalulatedEmaMap.get(priceArray[i]);
				//EMA = (Current Price * (2/12+1)) + (previous EMA * (1-(2/12+1)))
				double ema =  currentPrice * (2D/13D) + previousEma * (1D-(2D/13D));
				twelvePriceCalulatedEmaMap.put(priceArray[i], ema);
			}else if(!twelvePriceEmaMap.containsKey(priceArray[i])){
	    		Queue<Double> tempQueue = new LinkedList<Double>();
	    		tempQueue.add(Double.parseDouble(priceArray[i+1]));
	    		twelvePriceEmaMap.put(priceArray[i], tempQueue);
			}else if(twelvePriceEmaMap.get(priceArray[i]).size() == 11){
				double ema = 0.0;
				//Add the current value to make twelve total values
				twelvePriceEmaMap.get(priceArray[i]).add(Double.parseDouble(priceArray[i+1]));
				//Add the twelve values together
		    	for(Double value : twelvePriceEmaMap.get(priceArray[i])){
		    		ema = ema + value;
		    	}
		    	
		    	ema = ema / twelvePriceEmaMap.get(priceArray[0]).size();
		    	twelvePriceCalulatedEmaMap.put(priceArray[i], ema);
		    }else{
			    twelvePriceEmaMap.get(priceArray[i]).add(Double.parseDouble(priceArray[i+1]));
		    }
		}
	}
	
	private void calculateTwentySixDayEma(String[] priceArray){
		for (int i = 0; i < priceArray.length; i = i+2){
			if(twentySixPriceCalulatedEmaMap.containsKey(priceArray[i])){
				double currentPrice = Double.parseDouble(priceArray[i+1]);
				double previousEma  = twentySixPriceCalulatedEmaMap.get(priceArray[i]);
				//EMA = (Current Price * (2/26+1)) + (previous EMA * (1-(2/26+1)))
				double ema = currentPrice * (2D/27D) + previousEma * (1D-(2D/27D));
				twentySixPriceCalulatedEmaMap.put(priceArray[i], ema);
			}else if(!twentySixPriceEmaMap.containsKey(priceArray[i])){
	    		Queue<Double> tempQueue = new LinkedList<Double>();
	    		tempQueue.add(Double.parseDouble(priceArray[i+1]));
	    		twentySixPriceEmaMap.put(priceArray[i], tempQueue);
			}else if(twentySixPriceEmaMap.get(priceArray[i]).size() == 25){
				double ema = 0.0;
				//Add the current value to make twenty six total values
				twentySixPriceEmaMap.get(priceArray[i]).add(Double.parseDouble(priceArray[i+1]));
				//Add the twelve values together
		    	for(Double value : twentySixPriceEmaMap.get(priceArray[i])){
		    		ema = ema + value;
		    	}

		    	ema = ema / twentySixPriceEmaMap.get(priceArray[0]).size();
		    	twentySixPriceCalulatedEmaMap.put(priceArray[i], ema);
		    }else{
			    twentySixPriceEmaMap.get(priceArray[i]).add(Double.parseDouble(priceArray[i+1]));
		    }
		}
	}
	
	private void calculateMACD(String [] priceArray){
		for (int i = 0; i < priceArray.length; i = i+2){
			if(!ninePriceMacMap.containsKey(priceArray[i])){
				Queue<Double> tempQueue = new LinkedList<Double>();
	    		tempQueue.add(twelvePriceCalulatedEmaMap.get(priceArray[i]) - twentySixPriceCalulatedEmaMap.get(priceArray[i]));
	    		ninePriceMacMap.put(priceArray[i], tempQueue);
			}else if(ninePriceMacMap.get(priceArray[i]).size() == 9){
				currentMacdMap.put(priceArray[i], twelvePriceCalulatedEmaMap.get(priceArray[i]) - twentySixPriceCalulatedEmaMap.get(priceArray[i]));
			}else if(ninePriceMacMap.get(priceArray[i]).size() < 9){
				ninePriceMacMap.get(priceArray[i]).add(twelvePriceCalulatedEmaMap.get(priceArray[i]) - twentySixPriceCalulatedEmaMap.get(priceArray[i]));
				currentMacdMap.put(priceArray[i], twelvePriceCalulatedEmaMap.get(priceArray[i]) - twentySixPriceCalulatedEmaMap.get(priceArray[i]));
			}else{
				currentMacdMap.put(priceArray[i], twelvePriceCalulatedEmaMap.get(priceArray[i]) - twentySixPriceCalulatedEmaMap.get(priceArray[i]));
			}
		}
	}
	
	private void calculateSignal(String [] priceArray){
		for (int i = 0; i < priceArray.length; i = i+2){
			double signal = 0.0;
			if(!currentSignalMap.containsKey(priceArray[i])){
				//Calculate simple moving average
				for(double macdValue : ninePriceMacMap.get(priceArray[i])){
					signal = signal + macdValue;
				}
				signal = signal / ninePriceMacMap.get(priceArray[i]).size();
			}else{
				//Calculate exponential moving average
				//EMA = (Current MACD * (2/9+1)) + (previous signal * (1-(2/9+1)))
				signal = currentMacdMap.get(priceArray[i]) * (2D/10D) + currentSignalMap.get(priceArray[i]) * (1D-(2D/10D));
			}
			currentSignalMap.put(priceArray[i], signal);
		}
		
	}
	
	private void createOutputStrings(String [] priceArray){
		for (int i = 0; i < priceArray.length; i = i+2){
			if(i != 0){
				macdOutputString   = macdOutputString + ":";
			}
			if(i != 0){
				signalOutputString = signalOutputString + ":";
			}
			macdOutputString   = macdOutputString + priceArray[i] + ":" + currentMacdMap.get(priceArray[i]);
			signalOutputString = signalOutputString + priceArray[i] + ":" + currentSignalMap.get(priceArray[i]);
			
		}
	}

}
