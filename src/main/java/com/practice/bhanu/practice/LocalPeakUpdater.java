
package com.practice.bhanu.practice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

public class LocalPeakUpdater {
    static String parentDir;

    LocalPeakUpdater() {
        try {
            // parsing a CSV file into BufferedReader class constructor
            parentDir = new File("..").getCanonicalPath();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        try {
            // parsing a CSV file into BufferedReader class constructor
            parentDir = new File("..").getCanonicalPath();

        } catch (IOException e) {
            System.out.print(e.getLocalizedMessage());
            e.printStackTrace();
        }
        List<String> stocks = readStockTickers();
        Map<String, StockData> stocksUpdated = new HashMap<>();
        for (String ticker : stocks) {
            stocksUpdated.put(ticker, getThirtyDayPeak(ticker));
            System.out.println("30 day peak of" + ticker + " is " + stocksUpdated.get(ticker).recentPeak);
        }
        writeToCsv(stocksUpdated);
    }

    private static void writeToCsv(Map<String, StockData> localPeaks) {
        try (FileWriter writer = new FileWriter(parentDir + "\\localPeaks.csv");
                BufferedWriter bw = new BufferedWriter(writer)) {

            for (Map.Entry<String, StockData> localPeakEntry : localPeaks.entrySet()) {
                StockData stockData = localPeakEntry.getValue();
                bw.write(localPeakEntry.getKey() + "," + stockData.recentPeak + "," + stockData.recentPeakDay + ","
                        + stockData.recentDip + "," + stockData.recentDipDay);
                bw.newLine();
            }

        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }
    }

    private static List<String> readStockTickers() {
        List<String> stocks = new ArrayList<>();

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
                stocks.add(stock[0]);
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

    public static StockData getThirtyDayPeak(String ticker) {
        try {
            // create the HttpURLConnection
            // https://cloud.iexapis.com/stable/stock/twtr/chart/5d?token=pk_5dfd15070aa04462a3df347e7cd7abc3
            URL url = new URL("https://cloud.iexapis.com/stable/stock/" + ticker
                    + "/chart/30d?token=pk_5dfd15070aa04462a3df347e7cd7abc3");
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
            JSONArray jsonArray = (JSONArray) jsonParser.parse(stringBuilder.toString());
            double high = 0;
            String highDay = "";
            double low = Double.MAX_VALUE;
            String lowDay = "";
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject json = (JSONObject) jsonArray.get(i);
                double currentDayHighValue = process(json.get("uHigh"));
                double currentDayLowValue = process(json.get("uLow"));
                if (currentDayHighValue > high) {
                    high = currentDayHighValue;
                    highDay = (String) json.get("date");
                }
                if (currentDayLowValue < low) {
                    low = currentDayLowValue;
                    lowDay = (String) json.get("date");
                }

            }
            StockData stockData = new StockData();
            stockData.ticker = ticker;
            stockData.recentPeak = high;
            stockData.recentPeakDay = highDay;
            stockData.recentDip = low;
            stockData.recentDipDay = lowDay;
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
