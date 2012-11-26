/*
 * Camino.java
 *
 * Created on January 11, 2008, 10:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eps.catan;

import eps.cartas.Mazo;
import eps.catan.BarajaRecursos.Recurso;
import eps.catan.Celda.Terreno;

/**
 */
public class Camino extends Ficha {
    
    private static Mazo coste;
    
    /** Creates a new instance of Camino */
    public Camino(int numLado) {
        super(numLado);
    }
    
    public Mazo getCoste() {
        if (coste == null) {
            BarajaRecursos b = (BarajaRecursos) BarajaRecursos.getInstance();
            coste = new Mazo(b);
            coste.add(b.cartaParaTipo(Recurso.Madera));
            coste.add(b.cartaParaTipo(Recurso.Ladrillo));
        }
        return coste;
    }
    
    /**
     * Verifica si dos caminos pueden ser colocados, suponiendo que primero
     * se ponga uno y luego el otro
     */
    public boolean puedeColocar(TableroCatan t, PosicionArista p1, PosicionArista p2) {
        return puedeColocar(t, p1) && verifica(t, p1, p2);
    }

    /**
     * Un camino debe tocar (sin ciudad enemiga interpuesta) otro camino, o 
     * tener una ciudad al lado.
     */
    public boolean puedeColocar(TableroCatan t, Posicion posicion) {
        return verifica(t, posicion, null);
    }
        
    /**
     * Un camino debe tocar (sin ciudad enemiga interpuesta) otro camino, o 
     * tener una ciudad al lado. El campo "virtual" es opcional, y permite especificar
     * una arista que se asume como camino propio, pero que no esta en el tablero.
     */
    private boolean verifica(TableroCatan t, Posicion posicion, PosicionArista virtual) {

        if (virtual != null) virtual.normaliza();
        ((PosicionArista)posicion).normaliza();        
        
        // no puede haber nada debajo
        if (t.getFichaEnPos(posicion) != null || posicion.equals(virtual)) { 
            return false;
        }

        // la posicion debe limitar con al menos una tierra (= no oceano)
        boolean tocaTierra = false;
        for (Posicion p : ((PosicionArista)posicion).sinonimos(null)) {
            if (t.dentro(p) && t.getCelda(p).getTerreno() != Terreno.Oceano) {
                tocaTierra = true;
                break;
            }
        }

        // si no toca tierra, fallo
        if ( ! tocaTierra) return false;

        // si algun extremo es ciudad propia, estupendo
        PosicionVertice extremos[] = ((PosicionArista)posicion).getVertices(null);
        for (int i=0; i<extremos.length; i++) {
            Poblado poblado = (Poblado)t.getFichaEnPos(extremos[i]);
            if (poblado != null) {
                // hay poblado; o es de los mios, o no sigo mirando por ese lado
                if (poblado.getNumLado() == getNumLado()) return true;
            }
            else {
                // no hay poblado de nadie; si hay carretera mia, estupendo
                PosicionArista aristas[] = ((PosicionVertice)extremos[i]).getAristas(null);
                for (int j=0; j<aristas.length; j++) {
                    Camino camino = (Camino)t.getFichaEnPos(aristas[j]);
                    if ((camino != null && camino.getNumLado() == getNumLado()) 
                        || (virtual != null && aristas[j].equals(virtual))) {
                        
                        return true;                        
                    }
                }
            }
        }
        return false;        
    }
}
