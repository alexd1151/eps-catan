/*
 * Poblado.java
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
public class Poblado extends Ficha {

    private static Mazo coste;

    private int tasaCambio;
    private Recurso recursoCambio;
    
    /** Creates a new instance of Poblado */
    public Poblado(int numLado) {
        super(numLado);     
        tasaCambio = 4;
        recursoCambio = null;
    }
    
    public Mazo getCoste() {
        if (coste == null) {
            BarajaRecursos b = (BarajaRecursos) BarajaRecursos.getInstance();
            coste = new Mazo(b);
            coste.add(b.cartaParaTipo(Recurso.Madera));
            coste.add(b.cartaParaTipo(Recurso.Ladrillo));
            coste.add(b.cartaParaTipo(Recurso.Oveja));
            coste.add(b.cartaParaTipo(Recurso.Trigo));
        }
        return coste;
    }
       
    /**
     * Debe haber un camino adyacente a la posicion, y no debe haber ningun
     * poblado o ciudad a 1 arista de distancia
     */
    public boolean puedeColocar(TableroCatan t, Posicion posicion) {

        // no puede haber nada debajo
        if (t.getFichaEnPos(posicion) != null) return false;

        // la posicion debe limitar con al menos una tierra (= no oceano)
        boolean tocaTierra = false;
        for (Posicion p : ((PosicionVertice)posicion).sinonimos(null)) {
            if (t.dentro(p) && t.getCelda(p).getTerreno() != Terreno.Oceano) {
                tocaTierra = true;
                break;
            }
        }

        // si no toca tierra, fallo
        if ( ! tocaTierra) {
            return false;
        }

        // mira a ver si hay algun camino propio que toque esta posicion
        PosicionArista aristas[] = ((PosicionVertice)posicion).getAristas(null);
        boolean lleganCaminos = false;
        for (PosicionArista pa : aristas) {
            Ficha f = t.getFichaEnPos(pa);
            if (f != null && f.getNumLado() == getNumLado()) {
                lleganCaminos = true;
                break;
            }
        }

        // si no lo hay, y no estamos en fase de colcacion, fallo
        if ( ! lleganCaminos && t.getFase() == TableroCatan.Fase.Normal) {
            return false;
        }

        // si hay un poblado en cualquier extremo de esas aristas, imposible
        PosicionVertice vertices[] = new PosicionVertice[2];
        for (PosicionArista pa : aristas) {
            for (PosicionVertice pv : pa.getVertices(vertices)) {
                if (t.getFichaEnPos(pv) != null) {
                    return false;
                }
            }
        }
        return true;
    }

    public void setPosicion(Posicion pos, TableroCatan t) {
        super.setPosicion(pos, t);

        PosicionVertice[] rpv = null;
        // actualiza la tasa de cambio, si toca puertos
        for (Posicion p : ((PosicionVertice)pos).sinonimos(null)) {
            
            // para cada celda vecina de oceano (que puede tener puerto)
            if (t.dentro(p) && t.getCelda(p).getTerreno() == Terreno.Oceano) {
                Celda c = t.getCelda(p);
                
                // si tiene puerto
                if (c.getOrientacionPuerto() != null) {
                    
                    // mira a ver si la arista del puerto 'toca' al vertice en cuestion
                    PosicionArista pa = new PosicionArista(p, c.getOrientacionPuerto());
                    for (PosicionVertice pv : pa.getVertices(null)) {
                        for (PosicionVertice extremo : pv.sinonimos(rpv)) {
//                            System.err.println("Toca puerto? "+pv+"="+extremo+"=>"+pa+"?");
                            if (extremo.equals(p)) {
                                tasaCambio = c.getTasaPuerto();
                                recursoCambio = c.getRecursoPuerto();
//                                System.err.println("Tocado!!! - tasa = " + tasaCambio);
                            }
                        }
                    }
                }
            }
        }        
    }

    public int getTasaCambio() {
        return tasaCambio;
    }

    public Recurso getRecursoCambio() {
        return recursoCambio;
    }
}
