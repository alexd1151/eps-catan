/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eps.catan.accion;

import eps.Util;
import eps.catan.TableroCatan;
import eps.catan.TableroCatan.Fase;
import eps.multij.Accion;
import eps.multij.Juego;

/**
 * La accion de tirar los dados
 */
public class AccionDados extends Accion {

    public AccionDados(int origen) {
        super(origen);
    }
    
    public boolean ejecuta(Juego j) {
        TableroCatan t = (TableroCatan)j.getTablero();
        if (t.getFase() != Fase.Normal || 
                t.getTurno() != origen || 
                t.isDadoTirado()) {
            resultado = "fase incorrecta, no te toca o ya has tirado";
            return false;
        }
        //int r = 7;//
        int r = Util.tiraDado(6) + Util.tiraDado(6);
        t.resuelveTirada(r);
        resultado = this + ": ha salido un "+r;
        return true;
    }

    public String toString() {
        return "el jugador "+origen+" tira los dados";
    }    
}
