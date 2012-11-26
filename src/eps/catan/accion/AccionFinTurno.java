/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eps.catan.accion;

import eps.catan.Lado;
import eps.catan.TableroCatan;
import eps.multij.Accion;
import eps.multij.Juego;

/**
 * La accion de pasar de turno. Solo es necesaria en la fase de juego normal - 
 * durante la fase de colocacion, los turnos se pasan en cuanto se coloca el camino.
 */
public class AccionFinTurno extends Accion {

    public AccionFinTurno(int origen) {
        super(origen);
    }
    
    public boolean ejecuta(Juego j) {
        TableroCatan t = (TableroCatan)j.getTablero();
        if (t.getTurno() != origen) {
            resultado = "no te toca a ti, listillo";
            return false;
        }
        
        // si hubo compra de desarrollos durante el turno, los consolida
        Lado l = t.getLado(origen);
        l.getDesarrollos().addAll(l.getNuevosDesarrollos());
        l.getNuevosDesarrollos().clear();
        
        t.cambiaTurno();
        resultado = "fin del turno del jugador "+origen;
        return true;
    }

    public String toString() {
        return "fin del turno del jugador "+origen;
    }
}
