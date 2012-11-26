/*
 * Lado.java
 *
 * Created on January 11, 2008, 3:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eps.catan;

import eps.Util;
import eps.cartas.Baraja;
import eps.cartas.Carta;
import eps.cartas.Mazo;
import eps.catan.BarajaDesarrollo.Desarrollo;
import eps.catan.BarajaRecursos.Recurso;
import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Contiene las cartas y fichas de un lado.
 */
public class Lado implements Serializable {

    public enum Medalla { CaminoMasLargo, EjercitoMasGrande };
    
    /** cartas de desarrollo sin usar */
    private Mazo desarrollos = new Mazo(BarajaDesarrollo.getInstance());
    /** cartas de desarrollo 'en suspenso' (no se pueden usar hasta fin de turno) */
    private Mazo nuevosDesarrollos = new Mazo(BarajaDesarrollo.getInstance());
    /** soldados usados */
    private int soldadosUsados = 0;
    /** cartas de recurso */
    private Mazo recursos = new Mazo(BarajaRecursos.getInstance());
    /** fichas no usadas, indexadas por clase de ficha */
    private HashMap<Class, Integer> noUsadas;
    /** medallas */
    private ArrayList<Medalla> medallas = new ArrayList<Medalla>();
    /** color del jugador */
    private Color color;
    /** lista de fichas en juego, por clase */
    private HashMap<Class, ArrayList<Ficha> > usadas;
    /** lista de tasas de cambio */
    private TreeMap<Recurso, Integer> tasaCambio = new TreeMap<Recurso, Integer>();

    /**
     * Inicializa un lado del juego
     */
    public Lado(int maxCaminos, int maxPoblados, int maxCiudades, Color color) {
        noUsadas = new HashMap<Class, Integer>();
        noUsadas.put(Camino.class, maxCaminos);
        noUsadas.put(Poblado.class, maxPoblados);
        noUsadas.put(Ciudad.class, maxCiudades);
        usadas = new HashMap<Class, ArrayList<Ficha> >();
        for (Class c : noUsadas.keySet()) {
            usadas.put(c, new ArrayList<Ficha>());
        }
        for (Recurso r : Recurso.values()) {
            tasaCambio.put(r, 4);
        }
        this.color = color;
    }
    
    /**
     * Devuelve una cadena que describe el contenido de este lado
     * <pre>
     * Lado -16776961                             // color, como entero con signo
     *   desarrollos: {}                          // cartas de desarrollo no jugadas
     *   soldados: 0                              // soldados jugados
     *   recursos: {}                             // cartas de recurso en manos del jugador
     *   max c/p/c: 13 3 4                        // max caminos, poblados, ciudades
     *   medallas:                                // toString de las medallas
     *   caminos: (3, 3):Este (4, 3):NorEste      // posiciones
     *   poblados: (4, 3):Norte (3, 1):Norte
     *   ciudades: 
     * </pre>
     */
    public String toString() {
        
        StringBuilder sb = new StringBuilder();
        
        
        // Insertar el código desarrollado en la Práctica 1
        
        
        return(sb.toString());
    }
    
    /**
     * Inicializa este lado a partir de una cadena que describe su contenido, y
     * completando los poblados a partir del tablero.
     */
    public void inicializa(String s, TableroCatan t, int numLado) {
        Baraja d = BarajaDesarrollo.getInstance();
        Baraja r = BarajaRecursos.getInstance();
        
        // permite leer linea a linea
        StringTokenizer st = new StringTokenizer(s, "\n");
        
        // nombre y color
        String l = st.nextToken();
        color = new Color(Integer.parseInt(l.substring(l.lastIndexOf(' ')+1)));

        // desarrollo
        l = st.nextToken();
        desarrollos = new Mazo(l.substring(l.lastIndexOf(":")+1).trim(), d);
        // soldados
        l = st.nextToken();
        soldadosUsados = Integer.parseInt(l.substring(l.lastIndexOf(":")+1).trim());
        // recursos
        l = st.nextToken();
        recursos = new Mazo(l.substring(l.lastIndexOf(":")+1).trim(), r);
        // max cam/pob/ciu
        String fichasNoUsadas = st.nextToken();
        StringTokenizer lt;
        l = st.nextToken();
        lt = new StringTokenizer(l.substring(l.lastIndexOf(":")+1), " ");
        while (lt.hasMoreTokens()) {
            medallas.add(Medalla.valueOf(lt.nextToken()));
        }
        l = st.nextToken();        
        lt = new StringTokenizer(l.substring(l.indexOf(":")+1), " ");
        while (lt.hasMoreTokens()) {
            Camino c = new Camino(numLado);
            t.setFichaEnPos(c, new PosicionArista(lt.nextToken() + lt.nextToken()));
        }
        l = st.nextToken();        
        lt = new StringTokenizer(l.substring(l.indexOf(":")+1), " ");
        while (lt.hasMoreTokens()) {
            Poblado p = new Poblado(numLado);
            t.setFichaEnPos(p, new PosicionVertice(lt.nextToken() + lt.nextToken()));
        }
        l = st.nextToken();        
        lt = new StringTokenizer(l.substring(l.indexOf(":")+1), " ");
        while (lt.hasMoreTokens()) {
            Ciudad c = new Ciudad(numLado);
            t.setFichaEnPos(c, new PosicionVertice(lt.nextToken() + lt.nextToken()));
        }
        l = fichasNoUsadas;
        lt = new StringTokenizer(l.substring(l.lastIndexOf(":")+1), " ");
        noUsadas.put(Camino.class, Integer.parseInt(lt.nextToken()));
        noUsadas.put(Poblado.class, Integer.parseInt(lt.nextToken()));
        noUsadas.put(Ciudad.class, Integer.parseInt(lt.nextToken()));
    }

    public Mazo getDesarrollos() {
        return desarrollos;
    }

    public int getSoldadosUsados() {
        return soldadosUsados;
    }
    
    public void setSoldadosUsados(int soldadosUsados) {
        this.soldadosUsados = soldadosUsados;
    }

    public Mazo getRecursos() {
        return recursos;
    }
    
    public Mazo getNuevosDesarrollos() {
        return nuevosDesarrollos;
    }
    
    public ArrayList<Medalla> getMedallas() {
        return medallas;
    }
    
    public Color getColor() {
        return color;
    }
    
    /**
     * Devuelve el numero de puntos que ha obtenido este jugador
     */
    public int getPuntos() {
        return usadas.get(Poblado.class).size() 
             + usadas.get(Ciudad.class).size()*2
             + medallas.size() * 2
             + cuentaPuntosDesarrollo();
    }
    
    private int cuentaPuntosDesarrollo() {
        return desarrollos.cuenta(
                BarajaDesarrollo.cartaParaTipo(Desarrollo.Punto));
    }
    
    /**
     * Devuelve los puntos de este jugador, ignorando las 
     * cartas de punto
     */
    public int getPuntosVisibles() {
        return getPuntos() - cuentaPuntosDesarrollo();
    }

    /**
     * Devuelve las fichas usadas de un determinado tipo; NO tocar directamente.
     */
    public ArrayList<Ficha> getUsadas(Class c) {
        return usadas.get(c);
    }

    /**
     * Devuelve el numero de fichas restantes de un determinado tipo
     */
    public int getNoUsadas(Class c) {
        return noUsadas.get(c);
    }

    /**
     * Devuelve el maximo numero de fichas de un determinado tipo
     */
    public int getMaxFichas(Class c) {
        return getNoUsadas(c) + getUsadas(c).size();
    }

    /**
     * Pasa una ficha de este jugador de usada a no usada.
     * Actualiza los puertos del jugador.
     */
    public void usaFicha(Ficha f) {
//                    System.err.println("Ficha usada!");
        noUsadas.put(f.getClass(), noUsadas.get(f.getClass())-1);
        usadas.get(f.getClass()).add(f);

        // actualiza puertos
        if (f instanceof Poblado) {
            Poblado p = (Poblado)f;
            for (Recurso r : Recurso.values()) {
//                    System.err.println("Considerando cambio de "+r+" para "+p.getRecursoCambio());
                if (p.getRecursoCambio()==null || p.getRecursoCambio()==r) {
                    tasaCambio.put(r, Math.min(tasaCambio.get(r), p.getTasaCambio()));
                }
            }
        }
        
        // si se trata de una ciudad, el poblado que habia debajo queda disponible
        if (f instanceof Ciudad) {
            Iterator<Ficha> it = usadas.get(Poblado.class).iterator();
            while (it.hasNext()) {
                Ficha anterior = it.next();
                if (anterior.getPosicion().equals(f.getPosicion())) {
                    it.remove();
                    noUsadas.put(Poblado.class, getNoUsadas(Poblado.class)+1);
                    break;
                }
            }
        }        
    }
    
    /**
     * Comprueba si el jugador puede permitirse jugar una nueva ficha del tipo
     * indicado
     */
    public boolean puedeComprar(Ficha f) {
        if (getNoUsadas(f.getClass()) == 0) return false;
        return (recursos.puedeSacar(f.getCoste()));
    }

    /**
     * Roba una carta al azar a este jugador
     */
    public Carta robaCarta() {
        if (recursos.isEmpty()) return null;
        return recursos.remove(Util.tiraDado(recursos.size()) - 1);
    }

    public int getTasaCambio(Recurso r) {
        return tasaCambio.get(r) == null ? 4 : tasaCambio.get(r);
    }
    
    /**
     * Cuenta el numero de tramos del camino mas largo del jugador que controla este lado.
     * @return
     */
    int getCaminoMasLargo() {
        // TODO - actually calculate something
        return 0;
    }    
}
