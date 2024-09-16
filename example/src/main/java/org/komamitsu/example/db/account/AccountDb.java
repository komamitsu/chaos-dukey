package org.komamitsu.example.db.account;

import org.komamitsu.example.Tuple;

import java.util.HashMap;
import java.util.Map;

public class AccountDb {
  private final Map<String, Long> accounts = new HashMap<>();

  @SafeVarargs
  public AccountDb(Tuple<String, Long>... accountNameAndBalance) {
    for (Tuple<String, Long> account : accountNameAndBalance) {
      this.accounts.put(account.first(), account.second());
    }
  }

  public long getBalance(String accountName) {
    return accounts.get(accountName);
  }

  public void incrementBalance(String accountName, long amount) {
    accounts.compute(accountName, (k, v) -> v + amount);
  }
}
