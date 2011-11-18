package com.cgdecker.luhnybin;

import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.common.util.concurrent.Futures;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class LuhnMaskService {

  private static final Future<char[]> POISON = Futures.immediateFuture(null);

  private final ExecutorService processingExecutor = Executors.newFixedThreadPool(
      Runtime.getRuntime().availableProcessors());

  private final BlockingQueue<Future<char[]>> processFutures =
      new ArrayBlockingQueue<Future<char[]>>(20);

  public void run(final InputSupplier<? extends Reader> inSupplier, final Writer out) {
    new Thread(new Runnable() {
      @Override public void run() {
        try {
          CharStreams.readLines(inSupplier,
              LuhnyLineMasker.newLineProcessor(processingExecutor, processFutures));
          processFutures.put(POISON);
        } catch (IOException e) {
          throw new RuntimeException(e);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException(e);
        }
      }
    }).start();

    new Thread(new Runnable() {
      @Override public void run() {
        Future<char[]> processedLineFuture;
        try {
          while ((processedLineFuture = processFutures.take()) != POISON) {
            char[] line = processedLineFuture.get();
            out.write(line);
            out.write('\n');
            out.flush();
          }
          processingExecutor.shutdown();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException(e);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }).start();
  }
}
