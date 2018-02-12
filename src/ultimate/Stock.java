package ultimate;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Stock {
    private final String ticker;
    private String  e="", name="";
    //Current
    private String cp="", l="", hi52="", lo52="";

    //Current Mutual Fund
    private String nav_prior="", nav_c="", nav_cp="", nav_time="";
    
    //Resonse StringBuilders
    private StringBuilder responseCurrent, responseHistorical;

    //Historical
    private String EXCHANGE ="", MARKET_OPEN_MINUTE="", MARKET_CLOSE_MINUTE="", INTERVAL="", TIMEZONE_OFFSET="";
    private boolean processHeader=true;
    private Integer timezoneInteger=0;
    private String timezoneString, unixTime;
    String[][] csv2DArray = new String[20][9];
    
    Date historicalDateTime; 
    SimpleDateFormat historicalDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    
    public Stock(String t){
        ticker=t;
    }

    public void getCurrent(){        
        try{
            URL url = new URL("https://finance.google.com/finance?q="+ticker+"&output=json");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            ParseJSON stock = new ParseJSON();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
                if (inputLine.toLowerCase().contains("\"e\"".toLowerCase()) && e.equals("")){
                    e = stock.getAttributeValue(inputLine);
                }
                else if (inputLine.toLowerCase().contains("\"name\"".toLowerCase()) && name.equals("")){
                    name = stock.getAttributeValue(inputLine);
                }
                if (e.equals("MUTF")) {
                    if (inputLine.toLowerCase().contains("\"nav_prior\"".toLowerCase()) && nav_prior.equals("")){
                        nav_prior = stock.getAttributeValue(inputLine);
                    }
                    else if (inputLine.toLowerCase().contains("\"nav_c\"".toLowerCase()) && nav_c.equals("")){
                        nav_c = stock.getAttributeValue(inputLine);
                    }
                    else if (inputLine.toLowerCase().contains("\"nav_cp\"".toLowerCase()) && nav_cp.equals("")){
                        nav_cp = stock.getAttributeValue(inputLine);
                    }
                    else if (inputLine.toLowerCase().contains("\"nav_time\"".toLowerCase()) && nav_time.equals("")){
                        nav_time = stock.getAttributeValue(inputLine);
                    }
                } else {
                     if (inputLine.toLowerCase().contains("\"cp\"".toLowerCase()) && cp.equals("")){
                        cp = stock.getAttributeValue(inputLine);
                    }
                    else if (inputLine.toLowerCase().contains("\"l\"".toLowerCase()) && l.equals("")){
                        l = stock.getAttributeValue(inputLine);
                    }
                    else if (inputLine.toLowerCase().contains("\"hi52\"".toLowerCase()) && hi52.equals("")){
                        hi52 = stock.getAttributeValue(inputLine);
                    }
                    else if (inputLine.toLowerCase().contains("\"lo52\"".toLowerCase()) && lo52.equals("")){
                        lo52 = stock.getAttributeValue(inputLine);
                    }
                }
            }
            responseCurrent=content;
            in.close();
        } catch (IOException ex) {
            System.out.println("ERROR IOException");
            System.out.println(ex);
        }
    }
        
    public void getHistorical(){
        try{
            URL url = new URL("https://finance.google.com/finance/getprices?q="+ticker+"&output=csv");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                StringBuilder content = new StringBuilder();
                String inputLine;
                int i=0; //loop index for building csv2DArray from each csvLine
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine).append("\n");
                        //Assign header variables
                    if (processHeader){
                        if (EXCHANGE.equals("") && inputLine.substring(0,8).equals("EXCHANGE")){
                            EXCHANGE=inputLine.substring(11,inputLine.length());
                        } else if (MARKET_OPEN_MINUTE.equals("") && inputLine.substring(0,18).equals("MARKET_OPEN_MINUTE")){
                            MARKET_OPEN_MINUTE=inputLine.substring(19,inputLine.length());
                        } else if (MARKET_CLOSE_MINUTE.equals("") && inputLine.substring(0,19).equals("MARKET_CLOSE_MINUTE")){
                            MARKET_CLOSE_MINUTE=inputLine.substring(20,inputLine.length());
                        } else if (INTERVAL.equals("") && inputLine.substring(0,8).equals("INTERVAL")){
                            INTERVAL=inputLine.substring(9,inputLine.length());
                        } else if (inputLine.equals("DATA=")){ //Skip
                        } else if (TIMEZONE_OFFSET.equals("") && inputLine.substring(0,15).equals("TIMEZONE_OFFSET")){
                            TIMEZONE_OFFSET=inputLine.substring(16,inputLine.length());
                            processHeader=false; //Done with header
                        }
                    } else { //CSV Line
                        String[] csvLine = inputLine.split(",");
                        //check for unix time record (should be first)
                        if (inputLine.substring(0,1).equals("a")){
                            unixTime=csvLine[0].substring(1,csvLine[0].length());                            
                            csvLine[0]="0";
                        }
                        
                        System.arraycopy(csvLine, 0, csv2DArray[i], 0, 5); //add CSVLine to the csv2DArray (fixed 20x5)
                        
                        //Create timezone string ex: GMT-5
                        if (timezoneInteger==0){
                            timezoneInteger = (Integer.parseInt(TIMEZONE_OFFSET)/60);
                            timezoneString = "GMT"+(timezoneInteger.toString());                            
                            historicalDateTimeFormat.setTimeZone(TimeZone.getTimeZone(timezoneString));
                        }

                        //Determine date //convert seconds to milliseconds
                        Integer unixTimeInteger= Integer.parseInt(unixTime);
                        Integer dayInterval = Integer.parseInt(csv2DArray[i][0]);
                        dayInterval = dayInterval*86400;
                        unixTimeInteger = unixTimeInteger+dayInterval;
                        historicalDateTime = new Date(unixTimeInteger*1000L);                        
                        csv2DArray[i][5] = historicalDateTimeFormat.format(historicalDateTime);
                        
                        //Assign year/month/day to array record
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(historicalDateTime);
                        int year = cal.get(Calendar.YEAR);
                        int month = cal.get(Calendar.MONTH);
                        int day = cal.get(Calendar.DAY_OF_MONTH);
                        csv2DArray[i][6] = Integer.toString(year);
                        csv2DArray[i][7] = Integer.toString(month);
                        csv2DArray[i][8] = Integer.toString(day);
                        
                        //loop index for csvLine
                        i++; 
                    }
                }
                responseHistorical=content;
            in.close();
            }


        }catch (IOException ex) {
            System.out.println("ERROR IOException");
            System.out.println(ex);
        }
    }

    
    public void printResponseHistorical(){
        System.out.println(responseHistorical);
    }
    
    public void printResponseCurrent(){
        System.out.println(responseCurrent);
    }    
    
    public void printHistoricalStockDateTime(){
        System.out.println(historicalDateTimeFormat.format(historicalDateTime));
    }
    
    
    public void printStock(){
        if (e.equals("MUTF")){
            System.out.print(name+" ("+ticker+") $"+nav_prior+" "+nav_cp+"%");
            System.out.print(" Time: ");
            System.out.println(nav_time);
        } else {
            System.out.println(name+" ("+ticker+") $"+l+" "+cp+"%");
            System.out.println("52 week between $"+lo52+" and $"+hi52);
        }
    }
    
    public void printHistoricalPrice(){
        for(int i=0; i<20; i++){
            System.out.println(csv2DArray[i][5]+"\t"+"$"+csv2DArray[i][1]);
        }
    }
    
    public void printHistoricalStockJSON() {
        System.out.println("{\n" +
        "  \"cols\": [\n" +
        "        {\"id\":\"\",\"label\":\"Date\",\"pattern\":\"\",\"type\":\"date\"},\n" +
        "        {\"id\":\"\",\"label\":\""+ticker+"\",\"pattern\":\"\",\"type\":\"number\"}\n" +
        "      ],\n" +
        "  \"rows\": [");
        
        
        for(int i=0; i<20; i++){
            if (csv2DArray[i][0] != null && csv2DArray[i][0].length() > 0){
                System.out.println("        {\"c\":[{\"v\":\"Date("+csv2DArray[i][6]+","+csv2DArray[i][7]+","+csv2DArray[i][8]+")\",\"f\":null},{\"v\":"+csv2DArray[i][1]+",\"f\":null}]},");
            }
        }
        
        System.out.println("      ]\n" +
        "}");
    }
        
    
    public void createHistoricalStockJSON() {
        //Get the file reference
        Path path = Paths.get("/home/matt/stack/apache2/htdocs/mattbauman.com/stock/"+ticker+"_HistoricalStock.json");

        //Use try-with-resource to get auto-closeable writer instance
        try (BufferedWriter writer = Files.newBufferedWriter(path))
        {
            writer.write("{\n" +
            "  \"cols\": [\n" +
            "        {\"id\":\"\",\"label\":\"Date\",\"pattern\":\"\",\"type\":\"date\"},\n" +
            "        {\"id\":\"\",\"label\":\""+ticker+"\",\"pattern\":\"\",\"type\":\"number\"}\n" +
            "      ],\n" +
            "  \"rows\": [\n");


            for(int i=0; i<20; i++){
                if (csv2DArray[i][0] != null && csv2DArray[i][0].length() > 0){
                writer.write("        {\"c\":[{\"v\":\"Date("+csv2DArray[i][6]+","+csv2DArray[i][7]+","+csv2DArray[i][8]+")\",\"f\":null},{\"v\":"+csv2DArray[i][1]+",\"f\":null}]},\n");                    
                }
            }

            writer.write("      ]\n" +
            "}");
        } catch (IOException ex) {
            
        }

    

    }

}
