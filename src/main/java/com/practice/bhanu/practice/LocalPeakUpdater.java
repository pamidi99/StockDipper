
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
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

public class LocalPeakUpdater {
    static String parentDir;
    
    LocalPeakUpdater(){
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
        Map<String, Double> stocks = readLocalPeaks();
        Map<String, Double> stocksUpdated = new HashMap<>();
        for (Map.Entry<String, Double> entry : stocks.entrySet()) {
            stocksUpdated.put(entry.getKey(), getThirtyDayPeak(entry.getKey()));
            System.out.println("30 day peak of" + entry.getKey() + " is " + stocksUpdated.get(entry.getKey()));
        }
        writeToCsv(stocksUpdated);
    }

    private static void writeToCsv(Map<String, Double> localPeaks) {
        try (FileWriter writer = new FileWriter(parentDir+"\\localPeaks.csv"); BufferedWriter bw = new BufferedWriter(writer)) {

            for (Map.Entry<String, Double> localPeakEntry : localPeaks.entrySet()) {
                bw.write(localPeakEntry.getKey() + "," + localPeakEntry.getValue());
                bw.newLine();
            }

        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
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

    public static double getThirtyDayPeak(String ticker) {
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
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject json = (JSONObject) jsonArray.get(i);
                high = Math.max(high, process(json.get("uHigh")));
            }
            return high;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static Double process(Object o) {
        if (o instanceof Long) {
            return Double.parseDouble(String.valueOf((Long) o));
        }
        return (Double) o;
    }
}
