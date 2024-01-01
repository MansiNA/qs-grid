package de.dbuss.example.views.grid;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

@Async
@Service
public class BackendService {

    public ListenableFuture<String> longRunningTask() {
        try {
            // Simulate a long running task
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return AsyncResult.forValue("OK");
    }

}
