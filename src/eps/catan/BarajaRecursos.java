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
 * Una baraja de recursos para el juego de Catan.
 * <p>
 * Forma de uso para sacar mazo de esta baraja:
 * <code>Mazo m = BarajaRecursos.getInstance().generaMazo();</code>
 */
public class BarajaRecursos extends Baraja {

    /** 19 de cada: ladrillo, madera, oveja, roca, trigo */
    public enum Recurso { Oveja, Madera, Ladrillo, Roca, Trigo };

    /** baraja maestra; solo se inicializa una vez */
    private static BarajaRecursos baraja = new BarajaRecursos();

    /**
     * Devuelve una instancia de la BarajaEsp maestra, implementando el 
     * patrón "singleton".
     * @return la baraja compartida
     */
    public static BarajaRecursos getInstance() {
        return (BarajaRecursos)baraja;
    }
    
    private void creaCarta(Recurso tipo, int n, String descripcion) {
        String nombre = tipo.toString().toLowerCase();
        Carta nueva = new CartaRecurso(nombre, tipo, descripcion);
        for (int i=0; i<n; i++) {
            mazo.add(nueva);
        }
        idACarta.put(nueva.getId(), nueva);
        idAFicheroIcono.put(nueva.getId(), "eps/catan/gui/cartas/r-"+nombre+".png");
    }

    /**
     * Constructor privado, implementando el patron singleton.
     * Inicializa el mazo maestro y las correspondencias de id a carta
     */
    private BarajaRecursos() {
        super("eps/catan/gui/cartas/r-reverso.png");
        creaCarta(Recurso.Ladrillo, 19, "Usado para carreteras y poblados");
        creaCarta(Recurso.Madera, 19, "Usado para carreteras y poblados");
        creaCarta(Recurso.Oveja, 19, "Necesaria para cartas de desarrollo y poblados");
        creaCarta(Recurso.Roca, 19, "Necesaria para cartas de desarrollo y ciudades");
        creaCarta(Recurso.Trigo, 19, "Usado para cartas de desarrollo, poblados y ciudades");
    }
    /**
     * Devuelve la {@link Carta} que corresponde a un recurso dado
     * @param d desarrollo cuya carta se desea encontrar
     * @return la carta encontrada, o null si no esta en la baraja
     */
    public static Carta cartaParaTipo(Recurso r) {
        return getInstance().cartaParaId(r.toString().toLowerCase().substring(0,1));
    }    
    
    /**
     * Devuelve el {@link Recurso} que corresponde a una carta de esta baraja
     * @param c una carta de esta baraja
     * @return el recurso
     */
    public static Recurso tipoParaCarta(Carta c) {        
        return ((CartaRecurso)c).getRecurso();
    }
    
    private class CartaRecurso extends Carta {
        private Recurso r;
        private CartaRecurso(String nombre, Recurso r, String descripcion) {            
            super(nombre.substring(0,1), r.toString(), descripcion);
            this.r = r;
        }
        public Recurso getRecurso()  {
            return r;
        }
    }
}
