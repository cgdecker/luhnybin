package com.cgdecker.luhnybin;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.Uninterruptibles;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Colin Decker
 */
public class SegmentProcessors {

  /**
   * Returns a new {@link SegmentProcessor} that handles matching segments on multiple threads and
   * writes futures to a given queue for processing. The given poison future will be used to signal
   * end of data.
   */
  public static SegmentProcessor newMultithreadedProcessor(
      BlockingQueue<Future<char[]>> processFutures, Future<char[]> poison) {
    return new MultithreadedSegmentProcessor(processFutures, poison);
  }

  private static class MultithreadedSegmentProcessor implements SegmentProcessor {

    private final ExecutorService processingExecutor = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors());

    private final BlockingQueue<Future<char[]>> processFutures;
    private final Future<char[]> poison;

    private MultithreadedSegmentProcessor(BlockingQueue<Future<char[]>> processFutures,
        Future<char[]> poison) {
      this.processFutures = processFutures;
      this.poison = poison;
    }

    @Override public void processMatching(char[] buffer, int totalDigits) {
      Uninterruptibles.putUninterruptibly(processFutures,
          processingExecutor.submit(new ProcessTask(buffer, totalDigits)));
    }

    @Override public void processUnmatching(char[] buffer) {
      Uninterruptibles.putUninterruptibly(processFutures, Futures.immediateFuture(buffer));
    }

    @Override public void done() {
      Uninterruptibles.putUninterruptibly(processFutures, poison);
      processingExecutor.shutdown();
    }
  }

  private static class ProcessTask implements Callable<char[]> {

    private final char[] buffer;
    private final int totalDigits;

    private ProcessTask(char[] buffer, int totalDigits) {
      this.buffer = buffer;
      this.totalDigits = totalDigits;
    }

    @Override public char[] call() throws Exception {
      process(buffer, totalDigits);
      return buffer;
    }
  }

  private static void process(char[] buffer, int totalDigits) {
    // use an array-based list that tracks the Luhn digit sums of the values in it
    LuhnyList digits = new LuhnyList(totalDigits);

    for (int i = 0; i < buffer.length; i++) {
      char c = buffer[i];

      if (isDigit(c)) {
        digits.add(c, i);

        if (digits.length() >= 14) {
          digits.mask(buffer, 0);
        }
      }
    }
  }

  private static boolean isDigit(char c) {
    return '0' <= c && c <= '9';
  }
}
