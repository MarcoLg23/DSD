/*
    Proyecto 3
    Marco Antonio Lavarrios González
    4CM13
*/

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class WebServer {
    private static final String TASK_ENDPOINT = "/task";
    private static final String STATUS_ENDPOINT = "/status";

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

        statusContext.setHandler(this::handleStatusCheckRequest); //VINCULACIÓN DEL OBJETO CON SU METODO
        taskContext.setHandler(this::handleTaskRequest);

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start(); //EJECUCION DEL SERVIDOR EN SEGUNDO PLANO
    }

    private void handleTaskRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) { //ANALIZA EL TIPO DE REQUEST PARA SABER SI ES TIPO POST
            exchange.close(); //SI NO LO ES SE CIERRA
            return;
        }
        byte[] requestBytes = exchange.getRequestBody().readAllBytes(); //RECUPERA EL CUERPO DEL MENSAJE
        byte[] responseBytes = calculateRep(requestBytes); //SE ALMACENA EN BYTES

        sendResponse(responseBytes, exchange);
    }

    private byte[] calculateRep(byte[] requestBytes) throws IOException {
        File file = new File("Biblia.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        String bodyString = new String(requestBytes);
        int count = 1;
        String palabras[] = null;

        while ((line = br.readLine()) != null){
            palabras = line.split(" ");
            for (String cadena : palabras) {
                if(cadena.toUpperCase().contains(bodyString.toUpperCase())){
                    System.out.println(cadena);
                    count++;
                }
            }
        }
        br.close();
        return String.format("La palabra %s se repite %d veces", bodyString, count).getBytes();
    }

    private void handleStatusCheckRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) { //VERIFICA SI LA PETICION ES TIPO GET
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
}
