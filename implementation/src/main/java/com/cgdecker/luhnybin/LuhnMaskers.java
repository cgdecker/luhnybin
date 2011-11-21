package com.cgdecker.luhnybin;

import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.common.io.LineProcessor;
import com.google.common.util.concurrent.Futures;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.google.common.util.concurrent.Uninterruptibles.getUninterruptibly;
import static com.google.common.util.concurrent.Uninterruptibles.putUninterruptibly;
import static com.google.common.util.concurrent.Uninterruptibles.takeUninterruptibly;

/**
 * Factory for {@link LuhnMasker} implementations.
 *
 * @author cgdecker@gmail.com (Colin Decker)
 */
public class LuhnMaskers {
  
  private LuhnMaskers() {}

  /**
   * Returns a basic, single-threaded masker.
   */
  public static LuhnMasker newBasicMasker() {
    return new BasicLuhnMasker();
  }

  /**
   * Returns a multithreaded masker that reads and writes on separate threads and uses a thread
   * pool to mask lines.
   *
   * @param processingThreads the number of threads to use for processing input lines.
   */
  public static LuhnMasker newMultithreadedMasker(int processingThreads) {
    return new MultithreadedLuhnMasker(processingThreads);
  }

  private static class BasicLuhnMasker implements LuhnMasker {

    @Override public void run(InputSupplier<? extends Reader> inSupplier, final Writer out) {
      try {
        CharStreams.readLines(inSupplier, new LineProcessor<Void>() {
          @Override public boolean processLine(String line) throws IOException {
            char[] result = LuhnLineMasker.mask(line);
            out.write(result);
            out.write('\n');
            out.flush();
            return true;
          }

          @Override public Void getResult() {
            return null;
          }
        });
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static class MultithreadedLuhnMasker implements LuhnMasker {

    private static final Future<char[]> POISON = Futures.immediateFuture(null);

    private final ExecutorService processingExecutor;

    private final BlockingQueue<Future<char[]>> processFutures =
        new ArrayBlockingQueue<Future<char[]>>(200);

    MultithreadedLuhnMasker(int processingThreads) {
      this.processingExecutor = Executors.newFixedThreadPool(processingThreads);
    }

    @Override public void run(final InputSupplier<? extends Reader> inSupplier, final Writer out) {
      new Thread(new Runnable() {
        @Override public void run() {
          try {
            readLines(inSupplier);
            putUninterruptibly(processFutures, POISON);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }).start();

      new Thread(new Runnable() {
        @Override public void run() {
          Future<char[]> processedLineFuture;
          try {
            while ((processedLineFuture = takeUninterruptibly(processFutures)) != POISON) {
              writeLine(processedLineFuture, out);
            }
            processingExecutor.shutdown();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      }).start();
    }

    private void writeLine(Future<char[]> processedLineFuture, Writer out) throws ExecutionException, IOException {
      char[] line = getUninterruptibly(processedLineFuture);
      out.write(line);
      out.write('\n');
      out.flush();
    }

    private void readLines(InputSupplier<? extends Reader> inSupplier) throws IOException {
      CharStreams.readLines(inSupplier, new LineProcessor<Void>() {
        public boolean processLine(String line) throws IOException {
          putUninterruptibly(processFutures,
              processingExecutor.submit(new LuhnLineMasker(line)));
          return true;
        }

        public Void getResult() {
          return null;
        }
      });
    }
  }
}
