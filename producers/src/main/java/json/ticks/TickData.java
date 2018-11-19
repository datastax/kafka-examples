package json.ticks;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicLong;

public class TickData {
    private static final LocalDateTime BASE_TIME = LocalDateTime.now();
    private static final AtomicLong TIME_OFFSET = new AtomicLong();

    private String name;
    private String symbol;
    private double value;
    private String exchange;
    private String industry;
    private String datetime;

    public TickData(String name, String symbol, double value, String exchange, String industry) {
        this.name = name;
        this.symbol = symbol;
        this.value = value;
        this.exchange = exchange;
        this.industry = industry;
        this.datetime = BASE_TIME.plus(TIME_OFFSET.incrementAndGet(), ChronoUnit.SECONDS).toString();
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getValue() {
        return value;
    }

    public String getExchange() {
        return exchange;
    }

    public String getIndustry() {
        return industry;
    }

    public String getDateTime() {
        return datetime;
    }

    public void setDateTime(){
        this.datetime = BASE_TIME.plus(TIME_OFFSET.incrementAndGet(), ChronoUnit.SECONDS).toString();
    }

    public void setValue(double v){
        this.value = v;
    }

    @Override
    public String toString() {
        return "TickData [" +
                "name=" + name + ", " +
                "symbol=" + symbol + ", " +
                "value=" + value + ", " +
                "exchange=" + exchange + ", " +
                "industry=" + industry +"]";
    }

}