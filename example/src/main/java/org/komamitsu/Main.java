package org.komamitsu;

import org.komamitsu.db.account.AccountDb;
import org.komamitsu.jobqueue.Job;
import org.komamitsu.jobqueue.JobQueue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

public class Main {
  public static void main(String[] args) throws ExecutionException, InterruptedException {
    AccountDb accountDb = new AccountDb(Tuple.of("A", 200L));

    JobQueue jobQueue = new JobQueue(Duration.ofSeconds(4));
    jobQueue.enqueue(new Job(UUID.randomUUID().toString(), () -> accountDb.incrementBalance("A", 50L)));

    try (ExecutorService executorService = Executors.newCachedThreadPool()) {
      List<Future<?>> futures = new ArrayList<>();
      for (int i = 0; i < 10; i++) {
        int workerIndex = i;
        futures.add(
            executorService.submit(
                () -> {
                  Optional<Job> jobOpt = jobQueue.dequeue("worker-" + workerIndex);
                  jobOpt.ifPresent(job -> {
                    job.task.run();
                    jobQueue.finishJob(job);
                  });
                }));
        TimeUnit.SECONDS.sleep(1);
      }
      for (Future<?> future : futures) {
        future.get();
      }

      System.out.printf("The balance of Account A: %d\n", accountDb.getBalance("A"));
    }
  }
}
