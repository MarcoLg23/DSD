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

 //LIBRERIAS PARA CONSTRUIR SERVIDOR HTTP EN JAVA

import java.lang.Math;
import java.util.Random;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.Executors;

public class WebServer {
    //CADENAS PARA LAS RESPUESTAS DEL SERVIDOR
    private static final String TASK_ENDPOINT = "/task";
    private static final String STATUS_ENDPOINT = "/status";
    private static final String SEARCH_ENDPOINT = "/search";

    //VARIABLES PARA LA CREACION DEL SERVIDOR
    private final int port;
    private HttpServer server;

    public static void main(String[] args) {
        int serverPort = 8080;
        if (args.length == 1) {
            serverPort = Integer.parseInt(args[0]); //SI SE RECIBE UN ENTERO EN EL PRIMER ARGUMENTO, ESE SERA EL PUERTO
        }

        WebServer webServer = new WebServer(serverPort); //CONFIGURACION DEL SERVIDOR
        webServer.startServer(); //INICIALIZACION DEL SERVIDOR

        System.out.println("Servidor escuchando en el puerto " + serverPort);
    }

    public WebServer(int port) {
        this.port = port; //INICIALIZACION VARIABLE PORT
    }

    public void startServer() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0); //EL SEGUNDO ARGUMENTO ES EL NUMERO DE ELEMENTOS EN COLA, EN 0 ES DECISION DEL SISTEMA
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        HttpContext statusContext = server.createContext(STATUS_ENDPOINT); //CREACION DE OBJETO HTTPCONTEXT SIN HTTPHANDLER PERO CON LA RUTA ASIGNADA
        HttpContext taskContext = server.createContext(TASK_ENDPOINT);
        HttpContext searchContext = server.createContext(SEARCH_ENDPOINT);

        statusContext.setHandler(this::handleStatusCheckRequest); //VINCULACIÓN DEL OBJETO CON SU METODO
        taskContext.setHandler(this::handleTaskRequest);
        searchContext.setHandler(this::handleSearchRequest);

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start(); //EJECUCION DEL SERVIDOR EN SEGUNDO PLANO
    }

    private void handleTaskRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) { //ANALIZA EL TIPO DE REQUEST PARA SABER SI ES TIPO POST
            exchange.close(); //SI NO LO ES SE CIERRA
            return;
        }

        Headers headers = exchange.getRequestHeaders();
        if (headers.containsKey("X-Test") && headers.get("X-Test").get(0).equalsIgnoreCase("true")) { //VERIFICACION DE HEADER XTEST EN VALOR TRUE
            String dummyResponse = "123\n";
            sendResponse(dummyResponse.getBytes(), exchange);
            return;
        }

        boolean isDebugMode = false;
        if (headers.containsKey("X-Debug") && headers.get("X-Debug").get(0).equalsIgnoreCase("true")) { //VERIFICACION DE HEADER XDEBUG EN LAVOR TRUE
            isDebugMode = true;
        }

        //RECOLECCION DEL TIEMPO QUE TOMO EL PROCESAMIENTO DE DEPURACION
        long startTime = System.nanoTime();

        byte[] requestBytes = exchange.getRequestBody().readAllBytes(); //RECUPERA EL CUERPO DEL MENSAJE
        byte[] responseBytes = calculateResponse(requestBytes); //SE ALMACENA EN BYTES

        long finishTime = System.nanoTime();

        if (isDebugMode) { //VERIFICA SI FUE SOLICITADO EL DEBUG
            String debugMessage = String.format("La operación tomó %d nanosegundos", finishTime - startTime);
            exchange.getResponseHeaders().put("X-Debug-Info", Arrays.asList(debugMessage)); //SI HAY MODO DEBUG MANDA EL MENSAJE CON LA OPERACION DEL TIEMPO
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
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) { //VERIFICA SI LA PETICION ES TIPO CHECK
            exchange.close();
            return;
        }

        String responseMessage = "El servidor está vivo\n"; //RESPONDE CON EL MENSAJE
        sendResponse(responseMessage.getBytes(), exchange);
    }

    //CREACION DE LA RESPUESTA Y MANEJO DEL EXCHANGE
    private void sendResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.flush();
        outputStream.close();
        exchange.close();
    }

//-------------------METODO SEARCH IPN

    private void handleSearchRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) { //ANALIZA EL TIPO DE REQUEST PARA SABER SI ES TIPO POST
            exchange.close(); //SI NO LO ES SE CIERRA
            return;
        }

        Headers headers = exchange.getRequestHeaders();
        boolean isDebugMode = false;
        if (headers.containsKey("X-Debug") && headers.get("X-Debug").get(0).equalsIgnoreCase("true")) { //VERIFICACION DE HEADER XDEBUG EN LAVOR TRUE
            isDebugMode = true;
        }

        //RECOLECCION DEL TIEMPO QUE TOMO EL PROCESAMIENTO DE DEPURACION
        long startTime = System.nanoTime();

        byte[] requestBytes = exchange.getRequestBody().readAllBytes(); //RECUPERA EL CUERPO DEL MENSAJE
        byte[] responseBytes = calculateString(requestBytes); //SE ALMACENA EN BYTES

        long finishTime = System.nanoTime();

        if (isDebugMode) { //VERIFICA SI FUE SOLICITADO EL DEBUG
            String debugMessage = String.format("La operación tomó %d nanosegundos", finishTime - startTime);
            exchange.getResponseHeaders().put("X-Debug-Info", Arrays.asList(debugMessage)); //SI HAY MODO DEBUG MANDA EL MENSAJE CON LA OPERACION DEL TIEMPO
        }

        sendResponse(responseBytes, exchange);
    }

    private byte[] calculateString(byte[] requestBytes) {
        String bodyString = new String(requestBytes);
        String[] strings = bodyString.split(",");

        int firstUppercaseIndex = (int)'A';
        int n = new Integer(strings[0]);
        String[] cadena = new String[n];
        Random lr = new Random();

        String tmp = "";
        for(int i = 0; i < n; i++) {
            for (int j = 0; j < 3; j++) {
                int letterIndex = lr.nextInt(26);
                char letra = (char) (firstUppercaseIndex + letterIndex);
                tmp = tmp + letra;
            }
            cadena[i] = tmp;
            
        }
        
        String delimiter = "";
        String result = String.join(delimiter, cadena);
        
        
        int count = 0;
        for(int i = 0; i < result.length(); i++) {
            if(result.charAt(i) == (strings[1]) && result.charAt(i+1) == (strings[2]) && result.charAt(i+2) == (strings[3])) {
                count++;
            }
        }
        return String.format("La cadena se encontro %d veces\n", count).getBytes();
    }
}
