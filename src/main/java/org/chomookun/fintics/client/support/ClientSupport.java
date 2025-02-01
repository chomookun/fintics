package org.chomookun.fintics.client.support;

import java.math.BigDecimal;
import java.util.Currency;

public interface ClientSupport {

    /**
     * converts string to number
     * @param value string
     * @return number
     */
    default BigDecimal convertStringToNumber(String value, BigDecimal defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        value = value.replace(",", "");
        value = value.trim().isEmpty() ? "0" : value;
        try {
            return new BigDecimal(value);
        }catch(Throwable e){
            return defaultValue;
        }
    }

    /**
     * converts currency string to number
     * @param value currency string
     * @return currency number
     */
    default BigDecimal convertCurrencyToNumber(String value, Currency currency, BigDecimal defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            value = value.replace(currency.getSymbol(), "");
            return convertStringToNumber(value, defaultValue);
        } catch (Throwable e) {
            return defaultValue;
        }
    }

    /**
     * converts percentage string to number
     * @param value percentage string
     * @return percentage number
     */
    default BigDecimal convertPercentageToNumber(String value, BigDecimal defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            value = value.replace("%", "");
            return convertStringToNumber(value, defaultValue);
        }catch(Throwable e){
            return defaultValue;
        }
    }

}
