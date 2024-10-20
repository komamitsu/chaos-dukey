package org.komamitsu.example.db.account;

import org.komamitsu.example.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AccountDb {
  private static final Logger logger = LoggerFactory.getLogger(AccountDb.class);
  private final Map<String, Long> accounts = new ConcurrentHashMap<>();

  @SafeVarargs
  public AccountDb(Tuple<String, Long>... accountNameAndBalance) {
    for (Tuple<String, Long> account : accountNameAndBalance) {
      accounts.put(account.first(), account.second());
    }
  }

  public long getBalance(String accountName) {
    return accounts.get(accountName);
  }

  public void incrementBalance(String accountName, long amount) {
    accounts.compute(accountName, (k, v) -> v + amount);
    logger.debug("Incremented the balance. accountName:{}, amount:{}", accountName, amount);
  }
}
