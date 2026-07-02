package generators.testdata;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.stream.Stream;

public class ValidDepositAmounts {
    public static Double validDepositAmount() {
        Random random = new Random();
        double value = 0.01 + (5000.00 - 0.01) * random.nextDouble();
        BigDecimal bd = new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}