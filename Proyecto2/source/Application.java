/*
    Proyecto 3
    Marco Antonio Lavarrios González
    4CM13
*/

import java.util.Arrays;
import java.util.List;

public class Application {
    private static final String IP1 = "http://20.25.86.205:8080/task";
    private static final String IP2 = "http://20.127.48.47:8080/task";
    private static final String IP3 = "http://20.121.34.112:8080/task";
    
    public static void main(String[] args) {

        String[] words = new String[10];
        for (int i = 0; i < args.length; i++) {
                words[i] = args[i];
            System.out.println(args[i]);
        }
        Aggregator aggregator = new Aggregator();

        //SE CREA LA TAREA DE PETICIONES A MANDAR
        List<String> results = aggregator.sendTasksToWorkers(Arrays.asList(IP1,IP2,IP3),
                Arrays.asList(words), args.length);
        //SE IMPRIMEN LAS PETICIONES
        for (String result : results) {
            System.out.println(result);
        }
    }
}
