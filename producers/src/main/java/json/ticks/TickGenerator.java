package json.ticks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

public class TickGenerator {

    static final Logger log = LoggerFactory.getLogger("TickGenerator");

    private long TOTAL_TICKS = 0;

    private final List<TickData> stocksList;

    public TickGenerator(List<TickData> stocks) {
        this.stocksList = stocks;
    }

    public long getTicksGenerated(){
        return TOTAL_TICKS;
    }


    public TickValue getTickValueRandom(int i) {
        TickData thisStock = stocksList.get(i);
        TickValue tickValue = new TickValue(thisStock.getName(), thisStock.getValue());
        tickValue.value = this.createRandomValue(tickValue.value);
        return tickValue;
    }

    public TickData getStockWithRandomValue(int i) {
        TickData thisStock = stocksList.get(i);
        thisStock.setValue(this.createRandomValue(thisStock.getValue()));
        return thisStock;
    }

    class TickValue implements Serializable {
        String tickSymbol;
        double value;

        public TickValue(String tickSymbol, double value) {
            super();
            this.tickSymbol = tickSymbol;
            this.value = value;
        }
    }

    private double createRandomValue(double lastValue) {

        double up = Math.random() * 2;
        double percentMove = (Math.random() * 1.0) / 100;

        if (up < 1) {
            lastValue -= percentMove*lastValue;
        } else {
            lastValue += percentMove*lastValue;
        }

        return lastValue;
    }

}