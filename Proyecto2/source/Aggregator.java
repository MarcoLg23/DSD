/*
    Proyecto 3
    Marco Antonio Lavarrios González
    4CM13
*/

import networking.WebClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Aggregator {
    private WebClient webClient;

    public Aggregator() {
        this.webClient = new WebClient();
    }

    public List<String> sendTasksToWorkers(List<String> workersAddresses, List<String> tasks, int size) {
        CompletableFuture<String>[] futures = new CompletableFuture[size];
        int j=0;
        for (int i = 0; i < size; i++) {
            String workerAddress = workersAddresses.get(j);
            String task = tasks.get(i);

            byte[] requestPayload = task.getBytes();
            futures[i] = webClient.sendTask(workerAddress, requestPayload);
            j++;
            if(j==3)
            j=0;
        }

        List<String> results = Stream.of(futures).map(CompletableFuture::join).collect(Collectors.toList());

        return results;
    }
}
