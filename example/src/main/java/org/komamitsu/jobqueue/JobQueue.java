package org.komamitsu.jobqueue;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JobQueue {
  private final Duration leaseDuration;
  private final List<Job> jobs = new ArrayList<>();

  public JobQueue(Duration leaseDuration) {
    this.leaseDuration = leaseDuration;
  }

  public synchronized void enqueue(Job job) {
    jobs.add(job);
  }

  public synchronized Optional<Job> dequeue(String workerName) {
    for (Job job : jobs) {
      if (!job.isLeased()) {
        job.lease(workerName, Instant.now().plus(leaseDuration));
        return Optional.of(job);
      }
    }
    return Optional.empty();
  }

  public synchronized void finishJob(Job job) {
    // TODO: Add a warning log if the job doesn't exist.
    jobs.remove(job);
  }
}
