package org.komamitsu.example.jobqueue;

import java.time.Instant;

public class Job {
  public final String id;
  public final Runnable task;
  public String owner;
  public Instant expiredAt;

  public Job(String id, Runnable task) {
    this.id = id;
    this.task = task;
  }

  public synchronized void lease(String workerName, Instant expiredAt) {
    owner = workerName;
    this.expiredAt = expiredAt;
  }

  public synchronized boolean isLeased() {
    return expiredAt != null && Instant.now().isBefore(expiredAt);
  }
}
