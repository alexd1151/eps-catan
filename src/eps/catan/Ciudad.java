/*
 * Ciudad.java
 *
 * Created on January 11, 2008, 10:42 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eps.catan;

import eps.cartas.Mazo;
import eps.catan.BarajaRecursos.Recurso;
import eps.catan.TableroCatan.Fase;

/**
 */
public class Ciudad extends Poblado {
    
    private static Mazo coste;
    
    /** Creates a new instance of Ciudad */
    public Ciudad(int numLado) {
        super(numLado);
    }
    
    public Mazo getCoste() {
        if (coste == null) {
            BarajaRecursos b = (BarajaRecursos) BarajaRecursos.getInstance();
            coste = new Mazo(b);
            for (int i=0; i<2; i++) coste.add(b.cartaParaTipo(Recurso.Trigo));
            for (int i=0; i<3; i++) coste.add(b.cartaParaTipo(Recurso.Roca));
        }
        return coste;
    }
    
    /**
     * Debe haber un poblado del mismo lado en esta posicion
     */
    public boolean puedeColocar(TableroCatan t, Posicion p) {
        Ficha f = t.getFichaEnPos(p);
        return (t.getFase() == Fase.Normal) && 
               (f != null && f.getNumLado() == getNumLado());
    }    
}
