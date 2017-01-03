package StockSim;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import GenCol.entity;
import model.modeling.content;
import model.modeling.message;
import view.modeling.ViewableAtomic;

/*
 * This stock transducer uses primitive data types to measure metrics of interest.
 */
public class StockTransducer extends ViewableAtomic {

	DecimalFormat df = new DecimalFormat("#.00");
	String BUY_SELL_BALANCE_INPUT = "TransactionInput";
	String BUY_SELL_SYMBOL_INPUT  = "BuySellSymbol";
	String BUY_SELL_BALANCE_POSITIVE_OUTPUT = "PositiveBalance";
	String BUY_SELL_BALANCE_NEGATIVE_OUTPUT = "NegativeBalance";
	HashMap<String, Integer> stockTradeCounts = new HashMap<String, Integer>();

	double overallCashFlow;

	public StockTransducer(){
		super("STOCKTRANSDUCER");
		addInport(BUY_SELL_BALANCE_INPUT);
		addOutport(BUY_SELL_BALANCE_POSITIVE_OUTPUT);
		addOutport(BUY_SELL_BALANCE_NEGATIVE_OUTPUT);
		setBackgroundColor(Color.MAGENTA);
	}

	public StockTransducer(String name){
		super(name);
		addInport(BUY_SELL_BALANCE_INPUT);
		addOutport(BUY_SELL_BALANCE_POSITIVE_OUTPUT);
		addOutport(BUY_SELL_BALANCE_NEGATIVE_OUTPUT);
		setBackgroundColor(Color.MAGENTA);
	}

	public void initialize(){
		holdIn("passive", INFINITY);
		overallCashFlow = 0;
		super.initialize();
	}

	public void  deltext(double e, message msg){
		Continue(e);
		//events received from Generator
		for(int i = 0 ; i < msg.getLength() ; i++){
			if(messageOnPort(msg, BUY_SELL_BALANCE_INPUT, i)){
				entity genSignal = msg.getValOnPort(BUY_SELL_BALANCE_INPUT, i);
				overallCashFlow = overallCashFlow + Double.parseDouble(genSignal.toString());
				sigma = 0;
			}
		}

	}

	public message out( ){
		message  m = new message();

		if(overallCashFlow < 0){
			content con = makeContent(BUY_SELL_BALANCE_NEGATIVE_OUTPUT, new entity(df.format(Math.abs(overallCashFlow))));
			m.add(con);
		}else{
			content con = makeContent(BUY_SELL_BALANCE_POSITIVE_OUTPUT, new entity(df.format(overallCashFlow)));
			m.add(con);
		}
		
		return m;
	}

	public void  deltint( ){
		holdIn("passive", INFINITY);
	}
	
	public String getTooltipText(){
	    return
	    super.getTooltipText()
	    	+"\n"+" Overall Cash Flow: " + df.format(overallCashFlow);
	}

}
