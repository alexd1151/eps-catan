/*
 * BarajaEsp.java
 *
 * Created on February 9, 2006, 3:14 PM
 */

package eps.catan;

import eps.Util;
import eps.cartas.Baraja;
import eps.cartas.Carta;

/**
 * Una baraja de 'cartas de desarrollo' para el juego de Catan.
 * <p>
 * Forma de uso para sacar mazo de esta baraja:
 * <code>Mazo m = BarajaDesarrollo.getInstance().generaMazo();</code>
 */
public class BarajaDesarrollo extends Baraja {
  
    /*
     * 25 cartas, de las cuales
     * 12 soldados
     * 3 monopolios
     * 3 abundancias
     * 3 caminos
     * 4 puntos
     */
    public enum Desarrollo { Soldado, Monopolio, Abundancia, Caminos, Punto };
               
    /** baraja maestra; solo se inicializa una vez */
    private static BarajaDesarrollo baraja = new BarajaDesarrollo();
        
    /**
     * Devuelve una instancia de la BarajaEsp maestra, implementando el 
     * patrón "singleton".
     * @return la baraja compartida
     */
    public static BarajaDesarrollo getInstance() {
        return (BarajaDesarrollo)baraja;
    }
    
    private void creaCarta(Desarrollo tipo, int n, String descripcion) {
        String nombre = tipo.toString().toLowerCase();
        Carta nueva = new CartaDesarrollo(nombre, tipo, descripcion);
        for (int i=0; i<n; i++) {
            mazo.add(nueva);
        }
        idACarta.put(nueva.getId(), nueva);     
        idAFicheroIcono.put(nueva.getId(), "eps/catan/gui/cartas/d-"+nombre+".png");
    }

    /**
     * Constructor privado, implementando el patron singleton.
     * Inicializa el mazo maestro y las correspondencias de id a carta
     */
    private BarajaDesarrollo() {
        super("eps/catan/gui/cartas/d-reverso.png");
        creaCarta(Desarrollo.Soldado, 12,
            "El jugador que coloca un soldado tiene que mover el ladrón a" +
            "la celda que considere oportuna. Puede robar una carta de recurso" +
            "a cualquier jugador que tenga un poblado o ciudad cerca de esa celda.");
        creaCarta(Desarrollo.Monopolio, 3,
            "Al jugar esta carta, elije un recurso; los demás jugadores deberán" +
            "todas las cartas de ese recurso que posean.");
        creaCarta(Desarrollo.Abundancia, 3, 
            "Llévate 2 cartas de cualesquiera 2 recursos. Pueden ser los dos iguales.");
        creaCarta(Desarrollo.Caminos, 3,
            "Construye 2 caminos, igual que si los hubieses adquirido usando recursos.");
        creaCarta(Desarrollo.Punto, 4,
            "Esta carta vale 1 punto de victoria; se usa automáticamente cuando estás" +
            "a punto de ganar.");
    }

    /**
     * Devuelve la {@link Carta} que corresponde a un recurso dado
     * @param d desarrollo cuya carta se desea encontrar
     * @return la carta encontrada, o null si no esta en la baraja
     */
    public static Carta cartaParaTipo(Desarrollo d) {
        return getInstance().cartaParaId(d.toString().toLowerCase().substring(0,1));
    }    
    
    /**
     * Devuelve el {@link Desarrollo} que corresponde a una carta de esta baraja
     * @param c una carta de esta baraja
     * @return el desarrollo
     */
    public static Desarrollo tipoParaCarta(Carta c) {        
        return ((CartaDesarrollo)c).getDesarrollo();
    }
    
    private class CartaDesarrollo extends Carta {
        private Desarrollo d;
        private CartaDesarrollo(String nombre, Desarrollo d, String descripcion) {            
            super(nombre.substring(0,1), d.toString(), descripcion);
            this.d = d;
        }
        public Desarrollo getDesarrollo()  {
            return d;
        }
    }
}
