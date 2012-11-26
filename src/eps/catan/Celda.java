/*
 * Celda.java
 *
 * Created on January 11, 2008, 6:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eps.catan;

import eps.Util;
import eps.catan.BarajaRecursos.Recurso;
import eps.catan.TableroCatan.OrientacionArista;
import java.io.Serializable;

/**
 * Una celda hexagonal en el juego de Catan. Solo contiene informacion de la
 * celda, y no de las fichas que puede haber encima de ella.
 */
public class Celda implements Serializable {

    public enum Terreno { Desierto, Oceano, Arcilla, Bosque, Pasto, Roca, Trigo };
    
    private Posicion posicion;
    private Terreno terreno;
    
    // los desiertos y oceanos no tienen tiradas ni recursos
    private Recurso recurso;
    private int tirada;
    
    // los oceanos pueden tener puertos (si lo tiene, 0<tasaPuerto<4)
    private int tasaPuerto;
    private Recurso recursoPuerto;
    private OrientacionArista orientacionPuerto;   
    
    // probabilidad de cada tirada de dos dados
    private static float[] probabilidad;   

    /**
     * crea una celda 'de interior'
     */
    public Celda(Recurso recurso, int tirada) {
        this(recurso, tirada, 4, null);        
        terreno = terrenoParaRecurso(recurso);
    }    
    
    /**
     * crea una celda especificando, si lo hay, un puerto
     */
    public Celda(Recurso recurso, int tirada, int tasaPuerto, Recurso recursoPuerto) {
        if (probabilidad == null) {
            probabilidad = new float[13];
            for (int i=1; i<7; i++) {
                for (int j=1; j<7; j++) {
                    probabilidad[i+j] += 1.0f/36f;
                }
            }
        }

        this.recurso = recurso;
        this.tirada = tirada;
        this.tasaPuerto = tasaPuerto;
        this.recursoPuerto = recursoPuerto;
        this.orientacionPuerto = null;
        
        // por defecto, el terreno es oceano
        this.terreno = Terreno.Oceano;
    }
    
    /**
     * Asocia un recurso con cada terreno
     */
    public static Terreno terrenoParaRecurso(Recurso r) {
        if (r == null) {
            return Terreno.Desierto;
        }
        switch (r) {
            case Ladrillo : return Terreno.Arcilla; 
            case Madera : return Terreno.Bosque; 
            case Roca : return Terreno.Roca; 
            case Oveja : return Terreno.Pasto; 
            case Trigo : return Terreno.Trigo; 
        }       
        return null;
    }
    
    /**
     * Asocia la celda a una posicion dentro de un mapa, de forma que sea posible
     * recuperar las celdas vecinas. Ademas, asegura que los puertos siempre estan 
     * conectados con tierra firme. Si toca varias tierras, elige una al azar.
     */
    public void setPosicion(TableroCatan t, Posicion p) {
        this.posicion = p;
        
        if (terreno == Terreno.Oceano && tasaPuerto != 4) {
            
            // comienza con el puerto en una direccion al azar
            boolean giroPositivo = Util.tiraDado(2) == 1;
            int numDirs = OrientacionArista.values().length;
            int dir = Util.tiraDado(numDirs) - 1;
            orientacionPuerto = OrientacionArista.values()[dir];

            int nGiros = 0;
            for (/**/; nGiros < OrientacionArista.values().length; nGiros ++) {
                // gira                 
                dir = giroPositivo ? (dir+1)%numDirs : (dir+numDirs-1)%numDirs;
                orientacionPuerto = OrientacionArista.values()[dir];
                
                // mira si cae dentro y no es mar
                Posicion otro = t.getVecino(p, orientacionPuerto, null);
                if (t.getCelda(otro) != null) { 
                    if ( ! t.getCelda(otro).getTerreno().equals(Terreno.Oceano)) {
                        break;
                    }
                }                
            }
            
            if (nGiros == OrientacionArista.values().length) {
                System.err.println("puerto imposible en "+p);
                tasaPuerto = 8;
            }
        }
    }            

    /**
     * Devuelve el punto (en rejilla compacta) en el que esta esta celda
     */
    public Posicion getPosicion() {
        return posicion;
    }
    
    public OrientacionArista getOrientacionPuerto() {
        return orientacionPuerto;
    }    
    
    public Recurso getRecurso() {
        return recurso;
    }

    public int getTirada() {
        return tirada;
    }

    public float getProbabilidad() {
        return probabilidad[tirada];
    }

    /**
     * La posesion de un poblado o ciudad conectado a la arista de 
     * orientacionPuerto permite al poseedor cambiar tasaPuerto recursos
     * de tipo recursoPuerto por un recurso de su eleccion. Si recursoPuerto
     * es 'null', cualesquiera tasaPuerto recursos (del mismo tipo) son 
     * intercambiables.
     */
    public int getTasaPuerto() {
        return tasaPuerto;
    }

    public Recurso getRecursoPuerto() {
        return recursoPuerto;
    }      
    
    public Terreno getTerreno() {
        return terreno;
    }    
    
    /**
     * Muestra esta celda en representacion ASCII sencilla con celdas 6x3;
     * necesita acceso al tablero para buscar las fichas.
     * 
     * Usa compactoAHexagonal() para calcular donde poner la celda!
     * 
     * <pre>
     * Interior:       Oceano:
     *   001122334455    001122334455
     * 0               0   \~\ /~/
     * 1   R r T T     1 = R r : K = 
     * 2               2   /~/ \~\
     * </pre>
     * (R r = 2 primeras letras del nombre del recurso, T T = la tirada, 
     * los puertos se indican mediante // \\ ó =, y K es su tasa de intercambio)
     */
    public void dibujaEnAscii(TableroCatan t, char[][] c) {  
        // Insertar el código desarrollado en la Práctica 1
    }
    
    /**
     * Carga esta celda a partir de una representacion ASCII sencilla;
     * necesita acceso al tablero (que debe estar vacio) para colocar las fichas.
     */
    public Celda(TableroCatan t, Posicion p, char[][] c) {
        this.tasaPuerto = 4;
        this.tirada = 0;        
        this.posicion = p;
        
        Posicion ph = t.compactoAHexagonal(p, null);

        int x = ph.getX()*3;
        int y = ph.getY()*3;
        if (c[y  ][x+1] == '~' || c[y  ][x+4] == '~') {
            terreno = Terreno.Oceano;
            if (c[y+1][x+3] == ':') {
                tasaPuerto = Integer.parseInt("" + c[y+1][x+4]);
                for (Recurso r : Recurso.values()) {
                    if (c[y+1][x+1] == r.toString().charAt(0)) {
                        recursoPuerto = r; 
                        break;
                    }
                }                
                
                if (c[y+1][x+5] == '=') orientacionPuerto = OrientacionArista.Este;
                if (c[y  ][x+3] == '/') orientacionPuerto = OrientacionArista.NorEste;
                if (c[y  ][x+1] == '\\') orientacionPuerto = OrientacionArista.NorOeste;
                if (c[y+1][x  ] == '=') orientacionPuerto = OrientacionArista.Oeste;
                if (c[y+2][x+3] == '\\') orientacionPuerto = OrientacionArista.SurEste;
                if (c[y+2][x+1] == '/') orientacionPuerto = OrientacionArista.SurOeste;
            }            
        }
        else {
            for (Recurso r : Recurso.values()) {
                if (c[y+1][x+1] == r.toString().charAt(0)) {
                    recurso = r; 
                    break;
                }
            }
            if (recurso != null) {
                tirada = Integer.parseInt(("" + c[y+1][x+3] +c[y+1][x+4]).trim());
            }
            terreno = terrenoParaRecurso(recurso);
        }
    }
}
