package StockSim;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class ReadExcel {
 private String inputFile;
 private Map <String, Map> dateMap;
 private ArrayList<String> stockSymbols;
 
 public void setInputFile(String inputFile) {
  this.inputFile = inputFile;
 }
 
 @SuppressWarnings("unchecked")
public Map<String, Map> read() throws IOException{
  String filePath = System.getProperty("user.dir") + "\\src\\StockSim\\Data.xls";
  setInputFile(filePath);
  File inputWorkbook = new File(inputFile);
  Workbook w;
  dateMap      = new HashMap<String, Map>();
  stockSymbols = new ArrayList<String>();
 try {
  w = Workbook.getWorkbook(inputWorkbook);
  
  for (int i=0;i<w.getNumberOfSheets(); i++){
	  Sheet sheet = w.getSheet(i);
	  storeData(sheet);
  }
  
  } catch(BiffException e) {
   e.printStackTrace();
 }

 return dateMap;
 
 }
 
 public Map<String, Map> getDateMap(){
	 return dateMap;
 }
 
 public ArrayList<String> getStockSymbolList(){
	 return stockSymbols;
 }
 
 @SuppressWarnings("unchecked")
 private void storeData(Sheet sheet){
	  Map <String, StockInfo> valueMap;
	  Cell stockSymbol      = sheet.getCell(3,0);
	  Cell hourlyVolatility = sheet.getCell(6,1);
	  Cell hourlyDrift      = sheet.getCell(7,1);
	  stockSymbols.add(stockSymbol.getContents());
	  for (int i=1;i<sheet.getRows(); i++) {
		StockInfo stockData = new StockInfo();
		Cell date = sheet.getCell(0,i);
		Cell open = sheet.getCell(1,i);
		Cell close = sheet.getCell(2,i);
		valueMap = dateMap.get(date.getContents());
		if(valueMap == null){
			valueMap =  new HashMap();
		}
		stockData.setOpenValue(Double.parseDouble(open.getContents()));
		stockData.setCloseValue(Double.parseDouble(close.getContents()));
		stockData.setVolatility(Double.parseDouble(hourlyVolatility.getContents()));
		stockData.setDrift(Double.parseDouble(hourlyDrift.getContents()));
		valueMap.put(stockSymbol.getContents(), stockData);
		dateMap.put(date.getContents(), valueMap);
	  }
}
 
 public static void main(String[] args) throws IOException {
  ReadExcel test= new ReadExcel();
  test.setInputFile("C:\\Users\\Ryan\\Desktop\\Project\\Data.xls");
  Map <String, Map> stocksMap = test.read();
  System.out.println("VALUE: " + stocksMap.get("14").get("TSLA"));
  System.out.println("VALUE: " + stocksMap.get("7").get("AAPL"));
  System.out.println("VALUE: " + stocksMap.get("4").get("TEST"));
 }
 
 
}