package json.utils;

import json.ticks.TickData;

import com.opencsv.CSVReader;
import com.opencsv.ICSVParser;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExchangeUtils {

    private static final Logger log = LoggerFactory.getLogger("ExchangeUtils");

    private static final CharSequence EXCHANGEDATA = "exchangedata";

    private static CSVReader getCSVReader(File file) throws IOException{
        char quoteChar = ICSVParser.DEFAULT_QUOTE_CHARACTER;
        char delimiterChar = ICSVParser.DEFAULT_SEPARATOR;
        return new CSVReader(new FileReader(file.getAbsolutePath()), delimiterChar, quoteChar, 0);
    }

    private static List<File> getAllFilesThatContain(File searchDir, String containsString){
        List<File> allFilesThatContain = new ArrayList<>();

        File[] files = searchDir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isFile();
            }
        });

        for (File file : files) {
            if (file.getName().contains(containsString)) {
                allFilesThatContain.add(file);
            }
        }

        return allFilesThatContain;
    }

    public static List<TickData> getExchangeData() {

        List<TickData> allStocks = new ArrayList<>();

        // Process all the files from the csv directory
        File csvDir = new File(".", "src/main/resources/json/csv");

        List<File> files = getAllFilesThatContain(csvDir, EXCHANGEDATA.toString());

        for (File file : files) {
            try {
                allStocks.addAll(getExchangeData(file));
            } catch (FileNotFoundException e) {
                System.out.println("Could not process file : " + file.getAbsolutePath());
                e.printStackTrace();
                System.exit(1);
            } catch (IOException e) {
                System.out.println("Could not process file : " + file.getAbsolutePath());
                e.printStackTrace();
                System.exit(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        return allStocks;
    }

    private static List<TickData> getExchangeData(File file) throws IOException, InterruptedException{
        CSVReader reader = getCSVReader(file);

        String[] items;

        List<TickData> stocksList = new ArrayList<>();

        while ((items = reader.readNext()) != null) {
            stocksList.add(new TickData(
                    items[0].trim(),
                    items[1].trim(),
                    Double.valueOf(items[2].trim()),
                    items[3].trim(),
                    items[4].trim()));
        }

        reader.close();
        return stocksList;
    }
}