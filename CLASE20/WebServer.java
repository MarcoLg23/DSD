/*
 *  MIT License
 *
 *  Copyright (c) 2019 Michael Pogrebinsky - Distributed Systems & Cloud Computing with Java
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
//importacion de las librerias necesarias para crear el servidor
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;

public class WebServer {
    
    //declaración de las cadenas que se usan como END POINTS
    private static final String TASK_ENDPOINT = "/task";
    private static final String STATUS_ENDPOINT = "/status";
    private static final String SEARCHIPN_ENDPOINT="/searchipn";
    //variables privadas para crear el webServer
    private final int port;
    private HttpServer server;

    public static void main(String[] args) {
      // por default el webServer trabaja en el puerto 8080
        int serverPort = 8080;
        // cuando se pasa el como argumento en la consola se usa el puerto definido por el usario
        if (args.length == 1) {
            serverPort = Integer.parseInt(args[0]);
        }
        //se crea una instancia de la clase para despues inicializarlo
        WebServer webServer = new WebServer(serverPort);
        webServer.startServer();
        //cuando el servidor arranca se imprime en consola el siguiente mensaje
        System.out.println("Servidor escuchando en el puerto " + serverPort);
    }
    //inicializa la configuracion del servidor
    public WebServer(int port) {
        this.port = port;
    }
    public void startServer() {
        try {
            // crea una intancia de tcp con el puerto y la ip que se designan
            // tambien se pone en 0 para la lista de clientes pendientes
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        //se crean los contextos de HTTP haciendo uso de los ENS POINTS y de sus rutas relativas
        HttpContext statusContext = server.createContext(STATUS_ENDPOINT);
        HttpContext taskContext = server.createContext(TASK_ENDPOINT);
        HttpContext searchipnContext = server.createContext(SEARCHIPN_ENDPOINT);

        //metodos que se vehinculan s los END POINTS de tal manera que cuando se tenga una 
        //peticion se ejecunte el HANDLE del CONTEXT 
        statusContext.setHandler(this::handleStatusCheckRequest);
        taskContext.setHandler(this::handleTaskRequest);
        searchipnContext.setHandler(this::handleSearchIPNRequest);
        //Se crea el THREAD de hilos usando POOL de tamaño 8 y se inicializa
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
    }
    //es el metodo que ejecuta las acciones del END POINT TASK
    private void handleTaskRequest(HttpExchange exchange) throws IOException {
        //se recupera el metodo para asegurarse de que es metodo post en otro caso terminara 
        //la ejecucion del metodo
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
            exchange.close();
            return;
        }
        // se recuperan todos los headers para recuprar el X-TEST y si su valor es true se genera la respuesta "123"
        Headers headers = exchange.getRequestHeaders();
        if (headers.containsKey("X-Test") && headers.get("X-Test").get(0).equalsIgnoreCase("true")) {
            String dummyResponse = "123\n";
            sendResponse(dummyResponse.getBytes(), exchange);
            return;
        }
        // si entre los headers se encutnra el header X-DEBUG y ademas su valor es true entoncers la variable isDebugMode es establece en true
        boolean isDebugMode = false;
        if (headers.containsKey("X-Debug") && headers.get("X-Debug").get(0).equalsIgnoreCase("true")) {
            isDebugMode = true;
        }
        //las siguietes lineas son informacion de depuracion
        long startTime = System.nanoTime();
        //recupera la informacion del request especificamente del body 
        byte[] requestBytes = exchange.getRequestBody().readAllBytes();
        // se calcula la multiplicacion con los datos tomandolos como int
        byte[] responseBytes = calculateResponse(requestBytes);
        //se calgula el tiempo final 
        long finishTime = System.nanoTime();
        //se agrega la info de depuracion en un header de respuesta
        if (isDebugMode) {
            String debugMessage = String.format("La operación tomó %d nanosegundos", finishTime - startTime);
            exchange.getResponseHeaders().put("X-Debug-Info", Arrays.asList(debugMessage));
        }
        //se envia la respuesta 
        sendResponse(responseBytes, exchange);
    }
    //calculate response
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
      //se asegura de que el metodo es get 
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }
        //se añade el mensaje de respuesta y se envia la respuesta
        String responseMessage = "El servidor está vivo\n";
        sendResponse(responseMessage.getBytes(), exchange);
    }

    private void handleSearchIPNRequest(HttpExchange exchange) throws IOException {
        //se recupera el metodo para asegurarse de que es metodo post en otro caso terminara 
        //la ejecucion del metodo
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
            exchange.close();
            return;
        }
        // se recuperan todos los headers para recuprar el X-TEST y si su valor es true se genera la respuesta "123"
        Headers headers = exchange.getRequestHeaders();
        // si entre los headers se encutnra el header X-DEBUG y ademas su valor es true entoncers la variable isDebugMode es establece en true
        boolean isDebugMode = false;
        if (headers.containsKey("X-Debug") && headers.get("X-Debug").get(0).equalsIgnoreCase("true")) {
            isDebugMode = true;
        }
        //las siguietes lineas son informacion de depuracion
        long startTime = System.nanoTime();
        //recupera la informacion del request especificamente del body 
        byte[] requestBytes = exchange.getRequestBody().readAllBytes();
        // se calcula la multiplicacion con los datos tomandolos como int
        byte[] responseBytes = calcularCadenota(requestBytes);
        //se calgula el tiempo final 
        long finishTime = System.nanoTime();
        //se agrega la info de depuracion en un header de respuesta
        if (isDebugMode) {
            String debugMessage = String.format("La operación tomó %d nanosegundos", finishTime - startTime);
            exchange.getResponseHeaders().put("X-Debug-Info", Arrays.asList(debugMessage));
        }
        //se envia la respuesta 
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