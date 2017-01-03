/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package StockSim;



import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import GenCol.entity;
import model.modeling.content;
import model.modeling.message;
import view.modeling.ViewableAtomic;


public class StockGenerator extends ViewableAtomic{

DecimalFormat df = new DecimalFormat("#.00"); 
int day;
private Random randomGenerator;
ArrayList<String> stockSymbols;
Map<String, Map> stocksMap;
int totalDaysForSimulation = 0;
double openMarketHours     = 6.5;
double currentMarketTime   = 0.0;
String stockPrices         = "";
String randomStockSymbol   = "";



public StockGenerator(String name, ArrayList<String> stockSymbols, Map<String, Map> stocksMap){
   super(name);
   this.stockSymbols = stockSymbols;
   this.stocksMap    = stocksMap;
   addOutport("OutPrices");
   addOutport("OutSymbol");
   addOutport("OutState");
   setBackgroundColor(Color.GREEN);
}

public void initialize(){
     holdIn("active", 0);
     day = 1;
     totalDaysForSimulation = stocksMap.size() + 1;
     super.initialize();
 }

public void deltext(double e, message x){}

public void deltint( ){
	randomGenerator = new Random();
	randomStockSymbol = (String)stockSymbols.get(randomGenerator.nextInt(stockSymbols.size()));
	stockPrices = "";
	
	if(day == totalDaysForSimulation + 1)
		sigma = INFINITY;
	else if(day == totalDaysForSimulation){
		phase = "end";
		sigma = 1;
		day++;
	}else if(currentMarketTime == 0.0){
		phase = "open";
		getOpenOrClosePrice(phase);
		sigma = .5;
		currentMarketTime = 0.5;
	}else if(currentMarketTime == 6.0){
		phase = "close";
		getOpenOrClosePrice(phase);
		sigma = 0.5;
		currentMarketTime = 0.0;
		day++;
	}else{
		phase = "mid";
		populateRandomHourlyValues();
		sigma = 0.5;
		currentMarketTime = currentMarketTime + 0.5;
	}
}

public message  out( )
{
   message  m = new message();

   content con1 = makeContent("OutPrices", new entity(stockPrices));
   m.add(con1);
   content con2 = makeContent("OutSymbol", new entity(randomStockSymbol));
   m.add(con2);
   content con3 = makeContent("OutState", new entity(phase));
   m.add(con3);
   
   return m;
}

private void getOpenOrClosePrice(String phase){
	for (int i = 0; i < stockSymbols.size(); i++) {
		StockInfo stockData = (StockInfo) stocksMap.get(Integer.toString(day)).get(stockSymbols.get(i));
		if(i != 0){
			stockPrices = stockPrices + ":";
		}
		if(phase.equalsIgnoreCase("open")){
			stockPrices = stockPrices + stockSymbols.get(i) + ":" + stockData.getOpenValue();
			stockData.setPreviousValue(stockData.getOpenValue());
		}else{
			stockPrices = stockPrices + stockSymbols.get(i) + ":" + stockData.getCloseValue();
		}
		
	}
}

private double standardRandomNormalVariable(){
	Random rand     = new Random();
    double VARIANCE = 3.0;

	return VARIANCE * rand.nextGaussian();
}

private double logReturn(double drift, double volatility){
	return drift + volatility * standardRandomNormalVariable();
}

private double generatedPriceValue(double initialValue, double drift, double volatility){
	return initialValue * Math.exp(logReturn(drift, volatility));
}

private void populateRandomHourlyValues(){
	for (int i = 0; i < stockSymbols.size(); i++) {
		StockInfo stockData = (StockInfo) stocksMap.get(Integer.toString(day)).get(stockSymbols.get(i));
		double previousPrice = stockData.getPreviousValue();
		double generatedPrice = generatedPriceValue(previousPrice, stockData.getDrift(), stockData.getVolatility());
		stockData.setPreviousValue(generatedPrice);
		if(i != 0){
			stockPrices = stockPrices + ":";
		}
		stockPrices = stockPrices + stockSymbols.get(i) + ":" + df.format(generatedPrice);
	}
}

public String getTooltipText(){
    return
    super.getTooltipText()
    	+"\n"+" Day: " + day
    	+"\n"+" Market Time: " + currentMarketTime;
}

}

