package com.digitalforensics.service;

import com.digitalforensics.exception.ForensicsException;
import com.digitalforensics.util.BackgroundExecutor;
import com.digitalforensics.util.HashUtil;

import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Calculates SHA-256 on a background thread (multithreading).
 * The hashing runs as a Callable in the shared thread pool; the request
 * thread waits on the Future with a timeout so a huge file cannot hang the page.
 */
public class HashService {

    private static final long TIMEOUT_SECONDS = 30;

    public String calculate(Path file) throws ForensicsException {
        Future<String> future = BackgroundExecutor.submit(() -> HashUtil.sha256(file));
        try {
            return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new ForensicsException("Hash calculation took longer than " + TIMEOUT_SECONDS + " seconds.");
        } catch (ExecutionException e) {
            throw new ForensicsException("Could not read the evidence file: " + e.getCause().getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ForensicsException("Hash calculation was interrupted.", e);
        }
    }

    /** Integrity rule from the project spec: original hash == current hash means verified. */
    public boolean isUnchanged(String originalHash, String currentHash) {
        return HashUtil.matches(originalHash, currentHash);
    }
}
