/*
 * Ficha.java
 *
 * Created on January 11, 2008, 3:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eps.catan;

import eps.cartas.Mazo;
import java.io.Serializable;

/**
 * Una ficha del juego de Catan. Puede ser un poblado, una ciudad, una carretera,
 * o el ladron. Las fichas las crea el tablero al inicializarse.
 */
public abstract class Ficha implements Serializable {   
        
    private int numLado;
    protected Posicion posicion;
    
    protected Ficha(int numLado) {
        this.numLado = numLado;
    }
    
    public int getNumLado() {
        return numLado;
    }
    
    public Posicion getPosicion() {
        return posicion;
    }

    public void setPosicion(Posicion p, TableroCatan t) {
        this.posicion = p;
    }
    
    /** 
     * Devuelve el mazo de cartas de recurso necesario para comprar una ficha
     * del tipo dado.
     */
    public abstract Mazo getCoste();        

    /**
     * Devuelve 'true' si se puede colocar esta ficha en esa posicion
     */
    public abstract boolean puedeColocar(TableroCatan t, Posicion posicion);
}
