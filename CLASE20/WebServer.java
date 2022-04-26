import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;

public class WebServer {
    
    //declaración de las cadenas
    private static final String TASK_ENDPOINT = "/task";
    private static final String STATUS_ENDPOINT = "/status";
    private static final String SEARCHIPN_ENDPOINT="/searchipn";
    
    private final int port;
    private HttpServer server;

    public static void main(String[] args) {
        int serverPort = 8080;
        if (args.length == 1) {
            serverPort = Integer.parseInt(args[0]);
        }
        //se crea una instancia de la clase para despues inicializarlo
        WebServer webServer = new WebServer(serverPort);
        webServer.startServer();
        //mensaje del puerto usado
        System.out.println("Servidor escuchando en el puerto " + serverPort);
    }
    //inicializa la configuracion del servidor
    public WebServer(int port) {
        this.port = port;
    }
    public void startServer() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        HttpContext statusContext = server.createContext(STATUS_ENDPOINT);
        HttpContext taskContext = server.createContext(TASK_ENDPOINT);
        HttpContext searchipnContext = server.createContext(SEARCHIPN_ENDPOINT);

        statusContext.setHandler(this::handleStatusCheckRequest);
        taskContext.setHandler(this::handleTaskRequest);
        searchipnContext.setHandler(this::handleSearchIPNRequest);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
    }
    private void handleTaskRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
            exchange.close();
            return;
        }
        Headers headers = exchange.getRequestHeaders();
        if (headers.containsKey("X-Test") && headers.get("X-Test").get(0).equalsIgnoreCase("true")) {
            String dummyResponse = "123\n";
            sendResponse(dummyResponse.getBytes(), exchange);
            return;
        }
        boolean isDebugMode = false;
        if (headers.containsKey("X-Debug") && headers.get("X-Debug").get(0).equalsIgnoreCase("true")) {
            isDebugMode = true;
        }
        long startTime = System.nanoTime();
        byte[] requestBytes = exchange.getRequestBody().readAllBytes();
        byte[] responseBytes = calculateResponse(requestBytes);
        long finishTime = System.nanoTime();
        if (isDebugMode) {
            String debugMessage = String.format("La operación tomó %d nanosegundos", finishTime - startTime);
            exchange.getResponseHeaders().put("X-Debug-Info", Arrays.asList(debugMessage));
        }
        sendResponse(responseBytes, exchange);
    }
    private byte[] calculateResponse(byte[] requestBytes) {
        String bodyString = new String(requestBytes);
        String[] stringNumbers = bodyString.split(",");

        BigInteger result = BigInteger.ONE;

        for (String number : stringNumbers) {
            BigInteger bigInteger = new BigInteger(number);
            result = result.multiply(bigInteger);
        }

        return String.format("El resultado de la multiplicación es %s\n", result).getBytes();
    }


    private void handleStatusCheckRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }
        String responseMessage = "El servidor está vivo\n";
        sendResponse(responseMessage.getBytes(), exchange);
    }

    private void handleSearchIPNRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
            exchange.close();
            return;
        }
        Headers headers = exchange.getRequestHeaders();
        boolean isDebugMode = false;
        if (headers.containsKey("X-Debug") && headers.get("X-Debug").get(0).equalsIgnoreCase("true")) {
            isDebugMode = true;
        }
        long startTime = System.nanoTime();
        byte[] requestBytes = exchange.getRequestBody().readAllBytes();
        byte[] responseBytes = calcularCadenota(requestBytes);
        long finishTime = System.nanoTime();
        if (isDebugMode) {
            String debugMessage = String.format("La operación tomó %d nanosegundos", finishTime - startTime);
            exchange.getResponseHeaders().put("X-Debug-Info", Arrays.asList(debugMessage));
        }
        sendResponse(responseBytes, exchange);
    }
    private byte[] calcularCadenota(byte[] request){
      String aux= new String(request);
      String tokens[]= aux.split(",");
      int n = Integer.parseInt(tokens[0]);
        int min = 65;
    int max = 90;
    StringBuilder cadenota= new StringBuilder();
    int contador =0;

    for (int i =0; i <n; i++) {
//     Random random = new Random();
     cadenota.append(String.valueOf((char)((new Random()).nextInt(max-min+1)+min)));
     cadenota.append(String.valueOf((char)((new Random()).nextInt(max-min+1)+min)));
     cadenota.append(String.valueOf((char)((new Random()).nextInt(max-min+1)+min)));
     cadenota.append(" ");
     //char r2 = (char)((new Random()).nextInt(max-min+1)+min);
     //char r3 = (char)((new Random()).nextInt(max-min+1)+min);
     //char espacio =' ';

    }
    ArrayList<Integer> Posiciones = new  ArrayList<Integer>();
    int ocurrencias=0;
    int endIndex=0;
    boolean flag=true;
    //encontrar la palabra IPN en la cadenota
      while (flag){
        int i=cadenota.indexOf(tokens[1],endIndex);
        if (i!=-1){
          Posiciones.add(i);
          ocurrencias++;
          endIndex=i+1;
        }else flag=false;
      }
    System.out.println(Posiciones);
    System.out.println(ocurrencias);
//    String a = new String(cadenota, StandardCharsets.UTF_8);
//    System.out.println(cadenota);
            
    return String.format("El numero de veces que se encuentra %s es: %d\n",tokens[1],ocurrencias).getBytes();
//    String a = new String(cadenota, StandardCharsets.UTF_8);
  //  System.out.println(a);
        
  }

    
    //funcion de respuesta en al que se añade el status code, la longitud de la respuesta, los headers y el cuerpo de la respuesta
    private void sendResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.flush();
        outputStream.close();
        exchange.close();
    }
}