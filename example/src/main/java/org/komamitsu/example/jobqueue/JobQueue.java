package org.komamitsu.example.jobqueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JobQueue {
  private static final Logger logger = LoggerFactory.getLogger(JobQueue.class);
  private final Duration leaseDuration;
  private final List<Job> jobs = new ArrayList<>();

  public JobQueue(Duration leaseDuration) {
    this.leaseDuration = leaseDuration;
  }

  public synchronized void enqueue(Job job) {
    jobs.add(job);
    logger.debug("Enqueued a job. job:{}", job);
  }

  public synchronized Optional<Job> dequeue(String workerName) {
    for (Job job : jobs) {
      if (!job.isLeased()) {
        logger.debug("This job isn't leased. Leasing it. job:{}", job);
        job.lease(workerName, Instant.now().plus(leaseDuration));
        logger.debug("Dequeued a job. job:{}", job);
        return Optional.of(job);
      }
    }
    logger.debug("No job to dequeue was found.");
    return Optional.empty();
  }

  public synchronized void finishJob(Job job) {
    if (!jobs.remove(job)) {
      logger.warn("The target job wasn't found.");
    }
    logger.debug("Finished the job. job:{}", job);
  }
}
