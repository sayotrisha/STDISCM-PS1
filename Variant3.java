/* Variant 3: Straight Division + Print at End */

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Variant3 {

    //Data Structure for Results
    static class PrimeResult implements Comparable<PrimeResult> {
        long prime;
        int threadNum;
        String timestamp;

        public PrimeResult(long prime, int threadNum, String timestamp) {
            this.prime = prime;
            this.threadNum = threadNum;
            this.timestamp = timestamp;
        }

        @Override
        public int compareTo(PrimeResult other) {
            return Long.compare(this.prime, other.prime);
        }
    }

    //Globals
    private static final Object gResultsLock = new Object();
    private static final List<PrimeResult> gAllPrimes = new ArrayList<>();

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
            List<PrimeResult> localPrimes = new ArrayList<>();
            for (long n = start; n <= end; ++n) {
                if (isPrime(n)) {
                    localPrimes.add(new PrimeResult(n, threadNum, getCurrentTimestamp()));
                }
            }
            
            // Lock once to dump local results into the global list
            if (!localPrimes.isEmpty()) {
                synchronized (gResultsLock) {
                    gAllPrimes.addAll(localPrimes);
                }
            }
        }
    }

    //Main Function
    public static void main(String[] args) {
    
        System.out.println("Variant 3: Straight Division, Print at End");
        System.out.println("Start time: " + getCurrentTimestamp());
        long appStartTime = System.nanoTime();

        Map<String, Long> config = readConfig();
        int threadCount = config.get("threads").intValue();
        long maxNumber = config.get("max_number");

        System.out.println("Config: " + threadCount + " threads and " + maxNumber + " max number");
        
        System.out.println("Processing");

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

        // Sort and print all results at the end
        Collections.sort(gAllPrimes);

        System.out.println("Prime Numbers Sorted by Number");
        for (PrimeResult result : gAllPrimes) {
            System.out.println(
                "(Thread: " + result.threadNum + 
                ") (" + result.timestamp + 
                ") | Prime: " + result.prime
            );
        }
        System.out.println("\nTotal Prime Numbers: " + gAllPrimes.size());
        System.out.println("--- End of List ---");

        long appEndTime = System.nanoTime();
        System.out.println("\nEnd Time: " + getCurrentTimestamp());
        
        double duration = (appEndTime - appStartTime) / 1_000_000_000.0;
        System.out.println("Total execution time: " + duration + " seconds");
        
    }
}


