/*
    Proyecto 3
    Marco Antonio Lavarrios González
    4CM13
*/

package networking;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class WebClient {
    private HttpClient client;

    public WebClient() {
        //DEFINICION DEL CLIENTE HTTP Y SU COMUNICACION
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    public CompletableFuture<String> sendTask(String url, byte[] requestPayload) {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestPayload))
                .uri(URI.create(url))
                .build();
        //SE ENVIA LA SOLICITUD DE MANERA ASINCRONA
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse -> { return HttpResponse.body().toString()  +"\n"+ HttpResponse.uri() +"\n";});
    }
}
