/* Variant 1: Straight Division + Print Immediately */

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Variant1 {

    // A shared lock object to synchronize printing to the console
    private static final Object gPrintLock = new Object();

    private static Map<String, Long> readConfig() {
        Properties props = new Properties();
        Map<String, Long> config = new HashMap<>();
        String filename = "config.txt"; 

        try (InputStream input = new FileInputStream(filename)) {
            props.load(input);
            config.put("threads", Long.parseLong(props.getProperty("threads", "4")));
            config.put("max_number", Long.parseLong(props.getProperty("max_number", "100000")));
        } catch (IOException ex) {
            System.err.println("Error: Could not open config file: " + filename + ". Creating default.");
            try (OutputStream output = new FileOutputStream(filename)) {
                props.setProperty("threads", "4");
                props.setProperty("max_number", "100000");
                props.store(output, null);
            } catch (IOException ex2) {
                System.err.println("Error: Could not create default config file.");
            }
            config.put("threads", 4L);
            config.put("max_number", 100000L);
        }
        return config;
    }

    /**
     * Gets a high-precision timestamp.
     * @return Formatted timestamp string
     */
    private static String getCurrentTimestamp() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").format(LocalDateTime.now());
    }

    private static boolean isPrime(long n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (long i = 5; i * i <= n; i = i + 6) {
            if (n % i == 0 || n % (i + 2) == 0)
                return false;
        }
        return true;
    }

    //Thread Worker
    static class PrimeFinderRunnable implements Runnable {
        private final long start;
        private final long end;
        private final int threadNum;

        public PrimeFinderRunnable(long start, long end, int threadNum) {
            this.start = start;
            this.end = end;
            this.threadNum = threadNum;
        }

        @Override
        public void run() {
            for (long n = start; n <= end; ++n) {
                if (isPrime(n)) {
                    // Lock the console before printing
                    synchronized (gPrintLock) {
                        System.out.println(
                            "(Thread: " + threadNum + 
                            ") (" + getCurrentTimestamp() + 
                            ") | Prime: " + n
                        );
                    }
                }
            }
        }
    }

    //Main Function
    public static void main(String[] args) {

        System.out.println("Variant 1: Straight Division, Print Immediately");
        System.out.println("Start time: " + getCurrentTimestamp());
        long appStartTime = System.nanoTime();

        Map<String, Long> config = readConfig();
        int threadCount = config.get("threads").intValue();
        long maxNumber = config.get("max_number");

        System.out.println("Config: " + threadCount + " threads and " + maxNumber + " max number");

        List<Thread> threads = new ArrayList<>();
        long rangePerThread = maxNumber / threadCount;

        for (int i = 0; i < threadCount; ++i) {
            long start = i * rangePerThread + 1;
            if (i == 0) start = 2; // Start from 2
            
            long end = (i == threadCount - 1) 
                            ? maxNumber
                            : (i + 1) * rangePerThread;
            
            Thread t = new Thread(new PrimeFinderRunnable(start, end, i + 1));
            threads.add(t);
            t.start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.err.println("Thread interrupted: " + e.getMessage());
            }
        }
        
        System.out.println("All threads finished.");
        
        long appEndTime = System.nanoTime();
        System.out.println("\nEnd Time: " + getCurrentTimestamp());
        
        double duration = (appEndTime - appStartTime) / 1_000_000_000.0;
        System.out.println("Total execution time: " + duration + " seconds");

    }
}

