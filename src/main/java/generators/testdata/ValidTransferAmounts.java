package generators.testdata;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class ValidTransferAmounts {
    public static Double validTransferAmount() {
        Random random = new Random();
        double value = 0.01 + (10000.00 - 0.01) * random.nextDouble();
        BigDecimal bd = new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
