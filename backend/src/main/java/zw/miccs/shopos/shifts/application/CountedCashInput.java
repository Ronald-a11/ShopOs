package zw.miccs.shopos.shifts.application;

import java.math.BigDecimal;

public record CountedCashInput(String currency, BigDecimal amount) {
}
