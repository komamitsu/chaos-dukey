package org.komamitsu.example.jobqueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class Job {
  private static final Logger logger = LoggerFactory.getLogger(Job.class);
  public final String id;
  public final Runnable task;
  public String owner;
  public Instant expiredAt;

  public Job(String id, Runnable task) {
    this.id = id;
    this.task = task;
  }

  public synchronized void lease(String workerName, Instant expiredAt) {
    logger.debug("Leasing the job. job:{}, workerName:{}, expiredAt:{}", this, workerName, expiredAt);
    owner = workerName;
    this.expiredAt = expiredAt;
  }

  public synchronized boolean isLeased() {
    return expiredAt != null && Instant.now().isBefore(expiredAt);
  }

  @Override
  public String toString() {
    return "Job{" +
        "id='" + id + '\'' +
        ", owner='" + owner + '\'' +
        ", expiredAt=" + expiredAt +
        '}';
  }
}
