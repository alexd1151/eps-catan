/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eps.catan.accion;

import eps.catan.Celda.Terreno;
import eps.catan.Ficha;
import eps.catan.Posicion;
import eps.catan.PosicionVertice;
import eps.catan.TableroCatan;
import eps.catan.TableroCatan.OrientacionVertice;
import eps.multij.Accion;
import eps.multij.Juego;
import java.util.ArrayList;

/**
 */
public class AccionRobo extends Accion {

    private PosicionVertice pv;
        
    public AccionRobo(int origen, PosicionVertice pv) {
        super(origen);
        this.pv = pv;
    }
    
    public boolean ejecuta(Juego j) {
        TableroCatan t = (TableroCatan)j.getTablero();
        t.setPosLadron(new Posicion(pv), pv);
        t.cambiaTurno();
        resultado = "ladron movido a "+pv;
        return true;
    }

    public String toString() {        
        return "ladron colocado en " + pv;
    }
    
    public static void genera(TableroCatan t, ArrayList<Accion> al) {
        Posicion p = new Posicion();
        for (int i=0; i<t.getDim(); i++) {
            for (int j=0; j<t.getDim(); j++) {
                p.setPos(j, i);
                if (t.dentro(p) && ! p.equals(t.getPosLadron())) {
                    if (t.getCelda(p).getTerreno() != Terreno.Oceano) {
                        for (OrientacionVertice ov : OrientacionVertice.values()) {
                            PosicionVertice pv = new PosicionVertice(p, ov);
                            al.add(new AccionRobo(t.getTurno(), pv));
                        }
                    }
                }
            }
        }
    }    
}
