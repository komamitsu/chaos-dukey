package org.komamitsu.example.jobqueue;

import org.junit.jupiter.api.Test;
import org.komamitsu.example.Tuple;
import org.komamitsu.example.db.account.AccountDb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class JobQueueTest {
  private static final Logger logger = LoggerFactory.getLogger(JobQueueTest.class);

  @Test
  void test() throws ExecutionException, InterruptedException {
    // Create an Account DB with Account A, who has an initial balance of 200.
    AccountDb accountDb = new AccountDb(Tuple.of("A", 200L));

    // Create a JobQueue with a 4-second lease duration.
    JobQueue jobQueue = new JobQueue(Duration.ofSeconds(4));

    // Enqueue a job that increments the balance of Account A by 50.
    jobQueue.enqueue(new Job(UUID.randomUUID().toString(), () -> accountDb.incrementBalance("A", 50L)));

    try (ExecutorService executorService = Executors.newCachedThreadPool()) {
      // Worker 1 dequeues the job and processes it.
      Future<?> worker1Future = executorService.submit(
          () -> {
            Optional<Job> jobOpt = jobQueue.dequeue("worker-1");
            jobOpt.ifPresent(job -> {
              job.task.run();
              jobQueue.finishJob(job);
            });
          });

      // Wait for 5 seconds. Worker 1 should have finished the job by now, right?
      TimeUnit.SECONDS.sleep(5);

      // Worker 2 dequeues any available job and processes it if it exists.
      Future<?> worker2Future = executorService.submit(
          () -> {
            Optional<Job> jobOpt = jobQueue.dequeue("worker-2");
            jobOpt.ifPresent(job -> {
              job.task.run();
              jobQueue.finishJob(job);
            });
          });

      worker1Future.get();
      worker2Future.get();

      // The balance should be 250.
      logger.info("The balance of Account A: {}", accountDb.getBalance("A"));

      assertEquals(250, accountDb.getBalance("A"));
    }
  }
}
