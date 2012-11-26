/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eps.catan;

import eps.Util;
import eps.catan.BarajaRecursos.Recurso;
import eps.catan.gui.InterfazCatan;
import eps.multij.Accion;
import eps.multij.AccionCarga;
import eps.multij.AccionGuarda;
import eps.multij.Evento;
import eps.multij.Juego;
import eps.multij.Jugador;
import eps.multij.JugadorAleatorio;
import eps.multij.JugadorConsola;
import eps.multij.Tablero;
import eps.multij.gui.ConsolaGrafica;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Un jugador para Catan que funciona a traves de consola
 */
public class JugadorCatan extends JugadorConsola {
    
    /*
     * Etiquetas de los distintos comandos que puede realizar
     * el jugador de Catan desde la l�nea de comandos
     */
    private static final String INICIALIZAR = ":inicializa";
    private static final String GUARDAR = ":guarda";
    private static final String CARGAR = ":carga";
    private static final String PROBAR = ":prueba";
    private static final String LISTAR = ":lista";
    private static final String TASAS = ":tasas";
    private static final String SEMILLA = ":aleatoriza";
    private static final String SALIR = ":salir";
        
    private static final int NUM_JUGADORES = 3;
    private static final String TU_NOMBRE = "Alumno POO";
    
    private static final String PATH_SECUENCIAS = "test/";
    private static final String PATH_PARTIDAS = "test/";
    
    /**
     * Secuencia de sentencias que se quieren ejecutar para reproducir
     * una partida.
     */
    private StringTokenizer reproduccion = null;
    
    /**
     * Número de test que se está ejecutando.
     */
    private int numeroTest;

    public JugadorCatan(String nombre, Consola consola) {
        super(nombre, consola);
    }
    
    /**
     * Inicializa un nuevo juego, con los jugadores indicados y carga una
     * partido por defecto.
     */
    public void inicializa(int consolas, int aleatorios) {
        if (consolas+aleatorios > 4 || consolas < 0 || consolas+aleatorios < 2) {
            throw new IllegalArgumentException(
                    "al menos 1 consola, minimo 2 jugadores, maximo 4");
        }

        // genera una lista con tantos nombres como jugadores, partiendo del propio
        String[] listaNombres = {
            "Alberto","Alejandro","Alfonso","Almudena","Alvaro","Ana","Andres",
            "Angel","Antonio","Ascension","Carlos","Carmen","Conrado","Daniel",
            "David","Diana","Diego","Doroteo","Eduardo","Elena","Elias","Elisa",
            "Eloy","Enrique","Estanislao","Esther","Estrella","Eugenio",
            "Fabiano","Fabrizio","Fernando","Francisco","Gabriel","German",
            "Gonzalo","Guillermo","Gustavo","Ignacio","Ivan","Jaime","Javier",
            "Jesus","Joaquin","Jordi","Jorge","Jose","Juan","Juana","Julia",
            "Kostadin","Kurosh","Leila","Luis","Manuel","Maria","Mariano",
            "Marina","Mick","Miguel","Miren","Moises","Montserrat","Narciso",
            "Nicolas","Pablo","Pedro","Pilar","Rafael","Roberto","Rodolfo","Rosa",
            "Ruth","Saul","Sergio","Silvia","Susana","Vicente","Victor","Xavier"
        };
        List<String> permutacion = Arrays.asList(listaNombres);        
        Util.baraja(permutacion);
        ArrayList<String> nombres = new ArrayList<String>();               
        nombres.add(nombre);
        for (String s : permutacion) {
            if ( ! nombres.contains(s)) {
                nombres.add(s);              
                if (nombres.size() == consolas+aleatorios) break;
            }
        }

        // crea e inicializa los jugadores        
        ArrayList<Jugador> jugadores = new ArrayList<Jugador>();
        jugadores.add(this);        
        for (int i=1; i<consolas+aleatorios; i++) {            
            jugadores.add(i<consolas ? 
                new JugadorCatan(nombres.get(i), consola) :
                new JugadorAleatorio(nombres.get(i)));
        }
        
        // Arranca el juego
        juego = new Juego(new TableroCatan(consolas+aleatorios), jugadores);
        consola.setJuego(juego);
    }
    
    /**
     * Predicado que indica si la sentencia que recibe como par�metro es o no
     * un comando (todos los comandos empiezan por ":")
     */
    private static boolean esComando(String sentencia)
    {
        return(sentencia.startsWith(":"));
    }    
    
    /**
     * Predicado que indica si la sentencia que se le pasa como par�metro es o
     * no un comando (todos los comandos son n�meros enteros)
     */
    private static boolean esInstruccion(String sentencia)
    {
        return(Util.esEntero(sentencia));
    }
    
    /** Solo para que compile ... */
    public class Comando {        
    }
    
    /**
     * Genera una accion a partir de un comando de texto. La accion depende 
     * tanto del estado del juego actual (el juego es aquel del cual se recibio
     * un evento m�s recientemente) como del comando en s�.
     * 
     * @param comando a partir del cual generar una accion
     * @return true si accion generada correctamente
     * @return false en caso contrario
     */
    public boolean ejecuta(String comando)
    {    
        ArrayList<Comando> candidatos = null;
        Comando cmd;
        boolean retorno = false;
        
        // Retira espacios sobrantes por los lados
        comando = comando.trim();
        if(comando.length() == 0)
        {
            consola.muestraTexto("No has indicado ninguna sentencia que ejecutar\n");
        }
        else
        {
            // Parte el comando en 'palabras'; una o mas ',' o ' ' separa palabras
            String p[] = comando.split("[ ,]+");
        
            // Para depuracion: muestra las palabras del comando recibido
            // for(String s : p) System.err.print("'" + s + "'.. ");
            // System.err.println();
            
            // Instrucciones para el juego
            if(esInstruccion(comando))
            {
                if(juego == null)
                {
                    consola.muestraTexto("(partida no iniciada; usa '" + INICIALIZAR + "'\n");
                }
                else
                {
                    try {
                        Tablero t = juego.getTablero();
                        int turno = juego.getTablero().getTurno();
                        Accion a = t.accionesValidas(turno).get(Integer.parseInt(p[0]));
                        juego.realizaAccion(a);
                        retorno = true;
                    }
                    catch(Exception error) {
                        consola.muestraTexto("Instrucci�n no v�lida");
                    }
                }
            }
            // Ejecuci�n de comandos
            else
            {
        
                /* 
                 * Determinar si la entrada proporcionada por el usuario
                 * realmente es un comando. En caso de serlo, ejecutar el comando
                 * especificado. En caso de no serlo, indicar los candidatos que
                 * m�s se aproximen a lo que ha querido decir.
                 */

            }
        }
        
        return(retorno);
    }
    
    /**
     * Avanza un paso en la reproducci�n de una partida que
     */
    private void avanzarReproduccion()
    {
        
        /*
         * Mientras que en la variable reproduccion haya más tokens (sentencias
         * que ejecutar) se debe ejecutar la sentencia y si ésta es un comando
         * hacer una llamada recursiva a la función para que pase a la siguiente
         * sentencia. Si no es un comando la comunicación se hace por eventos,
         * pero de momento no tienes que preocuparte por ello.
         * 
         * Cuando se llegue al último token, la variable reproduccion se debe
         * hacer null y, al igual que en la práctica anterior, se debe comprobar
         * que el estado en el que se encuentra la partida es el esperado. Esto
         * lo puedes hacer con el código:
         *    String strIn = Util.leeFicheroACadena(new File(PATH_PARTIDAS + "partida" + numeroTest + ".txt"));
         *    boolean ok = Util.comienzanIgual(strIn, ((ConsolaGrafica) consola).getJuego().getTablero().toString());
         *    consola.muestraTexto("Prueba " + numeroTest + " --> " +
         *                         (ok ? " SUPERADA" : "FALLA") + ".\n");
         */
          
    }
    
    /**
     * Recibe una notificacion de un cambio en el juego y la muestra por
     * pantalla. Se sobreescribe aqu� para poder reproducir partidas.
     * 
     * @param e: Evento que ha provocado el cambio en el juego.
     */
    public void cambioEnJuego(Evento e) {
        super.cambioEnJuego(e);
        
        if((reproduccion != null) && (e.getTipo() == Evento.TipoEvento.Turno))
        {
            avanzarReproduccion();
        }
    }
    
    /**
     * Reproduce un secuencia de sentencias del juego
     * 
     * @param fn: Ruta del fichero en el que se encuentra almacenada la
     * secuencia de sentencias
     * @throws java.io.IOException
     */
    private void reproducePartida(String fn) throws IOException
    {
        
        /*
         * Abre y lee el fichero que la función recibe como parámetro (utiliza
         * las funciones del la clase Util), tokeniza el string que se obtiene
         * del fichero en las distintas sentencias que contiene y llama
         * al método avanzarReproduccion() para que comience la ejecución de
         * la partida.
         */
        
    }
    
    /**
     * Intenta pasar la prueba n-esima
     * 
     * @param n: N�mero de test que se quiere ejecutar
     * 
     * @throws IOExcepcion: Cuando se produce un error de lectura de la 
     * secuencia de comandos a ejecutar o del resultado del test
     */
    private void prueba(int n) throws IOException
    {        
        numeroTest = n;
        reproducePartida(PATH_SECUENCIAS + "secuencia" + n + ".txt");
    }
    
    /**
     * Lanza el programa de prueba de la P2
     */
    public static void main(String[] args) {
        ConsolaGrafica cg = new ConsolaGrafica(new InterfazCatan());
        JugadorCatan jugador = new JugadorCatan(TU_NOMBRE, cg);
        jugador.inicializa(0, NUM_JUGADORES);
    }
}
