
package com.practice.bhanu.practice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class StockPicker {

    public static void main(String args[]) {
        Map<String, Double> stocks = readLocalPeaks();
        Map<String, StockData> tenStocks = new HashMap<>();
        Map<String, StockData> twentyStocks = new HashMap<>();
        for (Map.Entry<String, Double> entry : stocks.entrySet()) {
            StockData sd = pickStocks(entry.getKey(), entry.getValue());
            if (entry.getValue() - sd.current >= 20) {
                twentyStocks.put(entry.getKey(), sd);
            } else if (entry.getValue() - sd.current >= 10) {
                tenStocks.put(entry.getKey(), sd);
            }
        }

        if (!twentyStocks.isEmpty())
            System.out.println("************  Twenty dollar difference stocks ************");
        DecimalFormat df = new DecimalFormat("0.00");
        for (Map.Entry<String, StockData> entry : twentyStocks.entrySet()) {
            StockData sd = entry.getValue();
            System.out.println();
            System.out.println("Ticker= " + entry.getKey() + "  currentPrice= $" + sd.current + " recentPeak= $"
                    + sd.recentPeak + "  difference= $" + df.format(sd.recentPeak - sd.current));
        }

        if (!tenStocks.isEmpty())
            System.out.println("************   Ten dollar difference stocks ************");

        for (Map.Entry<String, StockData> entry : tenStocks.entrySet()) {
            StockData sd = entry.getValue();
            System.out.println();
            System.out.println("Ticker= " + entry.getKey() + "  currentPrice= $" + sd.current + " recentPeak= $"
                    + sd.recentPeak + "  difference= $" + df.format(sd.recentPeak - sd.current));
        }
    }

    private static Map<String, Double> readLocalPeaks() {
        Map<String, Double> stocks = new HashMap<>();
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
                stocks.put(stock[0], Double.parseDouble(stock[1]));
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

    public static StockData pickStocks(String ticker, double recentPeak) {
        try {
            // create the HttpURLConnection
            URL url = new URL("https://finnhub.io/api/v1/quote?symbol=" + ticker + "&token=bt4fhqv48v6u8ohnuid0");
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

            StockData stockData = new StockData();
            stockData.open = process(json.get("o"));
            stockData.high = process(json.get("h"));
            stockData.low = process(json.get("l"));
            stockData.current = process(json.get("c"));
            stockData.recentPeak = recentPeak;
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
