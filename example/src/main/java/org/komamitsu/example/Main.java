package org.komamitsu.example;

import org.checkerframework.checker.units.qual.C;
import org.komamitsu.example.db.account.AccountDb;
import org.komamitsu.example.jobqueue.Job;
import org.komamitsu.example.jobqueue.JobQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws InterruptedException, ExecutionException {
    int n = 1000;

    // Create an Account DB with Account A, who has an initial balance of 0.
    AccountDb accountDb = new AccountDb(Tuple.of("A", 0L));

    // Create a JobQueue with a 4-second lease duration.
    JobQueue jobQueue = new JobQueue(Duration.ofSeconds(4));
    // Enqueue all jobs.
    for (int i = 0; i < n; i++) {
      // Enqueue a job that increments the balance of Account A by 50.
      jobQueue.enqueue(new Job(UUID.randomUUID().toString(),
          () -> accountDb.incrementBalance("A", 50L)));
    }

    List<Future<Void>> futures = new ArrayList<>();

    try (ExecutorService executorService = Executors.newCachedThreadPool()) {
      // Invoke 4 consumers.
      for (int i = 0; i < 4; i++) {
        int workerId = i;
        futures.add(
            executorService.submit(
                () -> {
                  while (jobQueue.remainingSize() > 0) {
                    Optional<Job> jobOpt = jobQueue.dequeue("worker-" + workerId);
                    if (jobOpt.isPresent()) {
                      jobOpt.get().task.run();
                      jobQueue.finishJob(jobOpt.get());
                    }
                    else {
                      TimeUnit.MILLISECONDS.sleep(50);
                    }
                  }
                  return null;
                }));
      }
    }
    while (jobQueue.remainingSize() > 0) {
      TimeUnit.SECONDS.sleep(1);
    }
    for (Future<Void> future : futures) {
      future.get();
    }

    logger.info("The balance of Account A: {}", accountDb.getBalance("A"));
  }
}
