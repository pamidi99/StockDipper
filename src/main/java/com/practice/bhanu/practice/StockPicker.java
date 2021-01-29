
package com.practice.bhanu.practice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class StockPicker {

    public static void main(String args[]) {
        List<StockData> stocks = readLocalPeaks();
        List<StockData> tenStocks = new ArrayList<>();
        List<StockData> twentyStocks = new ArrayList<>();
        for (StockData entry : stocks) {
            StockData sd = pickStocks(entry);
            if (sd.recentPeak - sd.current >= 20) {
                twentyStocks.add(sd);
            } else if (sd.recentPeak - sd.current >= 10) {
                tenStocks.add(sd);
            }
        }
        sortStocksList(tenStocks);
        sortStocksList(twentyStocks);

        if (!twentyStocks.isEmpty())
            System.out.println("************  Twenty dollar difference stocks ************");

        printStocks(twentyStocks);

        if (!tenStocks.isEmpty())
            System.out.println("************   Ten dollar difference stocks ************");
        printStocks(tenStocks);

    }

    private static void printStocks(List<StockData> stocks) {
        System.out.println();
        DecimalFormat df = new DecimalFormat("0.00");
        System.out.format("%8s%15s%15s%15s%20s%15s%20s", "TICKER", "CURRENT-PRICE", "DIFFERENCE", "RECENT-PEAK",
                "RECENT-PEAK-DAY", "RECENT DIP", "RECENT-DIP-DAY");
        System.out.println();
        for (StockData sd : stocks) {

            System.out.println();
            System.out.format("%8s%15s%15s%15s%20s%15s%20s", sd.ticker, sd.current,
                    df.format(sd.recentPeak - sd.current), df.format(sd.recentPeak), sd.recentPeakDay,
                    df.format(sd.recentDip), sd.recentDipDay);
            System.out.println();
            /*
             * System.out.println("Ticker= " + sd.ticker + "  currentPrice= $" +
             * sd.current + " recentPeak= $" + sd.recentPeak + "  difference= $"
             * + df.format(sd.recentPeak -
             * sd.current)+" recentPeakDay= "+sd.recentPeakDay+" recentDip= $"
             * +sd.recentDip+" recentDipDay="+sd.recentDipDay);
             */
        }
        System.out.println();
        System.out.println();

    }

    private static void sortStocksList(List<StockData> stocks) {
        Collections.sort(stocks, (stock1,
                stock2) -> (int) ((stock2.recentPeak - stock2.current) - (stock1.recentPeak - stock1.current)));
    }

    private static List<StockData> readLocalPeaks() {
        List<StockData> stocks = new ArrayList<>();
        String line = "";
        String splitBy = ",";
        BufferedReader br = null;
        try {
            // parsing a CSV file into BufferedReader class constructor
            String parentDir = new File("..").getCanonicalPath();
            br = new BufferedReader(new FileReader(parentDir + "\\localPeaks.csv"));
            while ((line = br.readLine()) != null) // returns a Boolean value
            {
                String[] stock = line.split(splitBy); // use comma as separator
                StockData stockData = new StockData();
                stockData.ticker = stock[0];
                stockData.recentPeak = Double.parseDouble(stock[1]);
                stockData.recentPeakDay = stock[2];
                stockData.recentDip = Double.parseDouble(stock[3]);
                stockData.recentDipDay = stock[4];
                stocks.add(stockData);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
        return stocks;
    }

    public static StockData pickStocks(StockData stockData) {
        try {
            // create the HttpURLConnection
            URL url = new URL(
                    "https://finnhub.io/api/v1/quote?symbol=" + stockData.ticker + "&token=bt4fhqv48v6u8ohnuid0");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // just want to do an HTTP GET here
            connection.setRequestMethod("GET");

            // uncomment this if you want to write output to this url
            // connection.setDoOutput(true);

            // give it 15 seconds to respond
            connection.setReadTimeout(15 * 1000);
            connection.connect();

            // read the output from the server
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
            stringBuilder.toString();

            JSONParser jsonParser = new JSONParser();
            JSONObject json = (JSONObject) jsonParser.parse(stringBuilder.toString());

            stockData.open = process(json.get("o"));
            stockData.high = process(json.get("h"));
            stockData.low = process(json.get("l"));
            stockData.current = process(json.get("c"));
            return stockData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Double process(Object o) {
        if (o instanceof Long) {
            return Double.parseDouble(String.valueOf((Long) o));
        }
        return (Double) o;
    }
}
