// src/main/java/com/kmbank/dto/AccountTypeStatProjection.java
package com.kmbank.dto;

import java.math.BigDecimal;

public interface AccountTypeStatProjection {
    String getAccountType();
    Long getCount();
    BigDecimal getTotalBalance();
}