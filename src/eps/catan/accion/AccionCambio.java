/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eps.catan.accion;

import eps.cartas.Mazo;
import eps.catan.BarajaRecursos;
import eps.catan.BarajaRecursos.Recurso;
import eps.catan.Lado;
import eps.catan.TableroCatan;
import eps.catan.TableroCatan.Fase;
import eps.multij.Accion;
import eps.multij.Juego;
import java.util.ArrayList;

/**
 * Cambia una serie de recursos por otro, usando puertos o el cambio 4:1. 
 */
public class AccionCambio extends Accion {

    private Recurso rOrigen;
    private Recurso rDestino;
    private int tasa;

    public AccionCambio(int origen, Recurso rOrigen, Recurso rDestino, int tasa) {
        super(origen);
        this.rOrigen = rOrigen;
        this.rDestino = rDestino;
        this.tasa = tasa;
    }
    
    public boolean ejecuta(Juego j) {
        TableroCatan t = (TableroCatan)j.getTablero();
        if (t.getFase() != Fase.Normal || 
                t.getTurno() != origen ||
                ! t.isDadoTirado()) {
            resultado = "fase incorrecta o se te ha pasado la oportunidad";
            return false;
        }
        
        Lado l = t.getLado(origen);
        if (l.getTasaCambio(rOrigen) != tasa) {
            resultado = "quieres un "+this+", pero el cambio está a "+
                    l.getTasaCambio(rOrigen);
            return false;
        }
        
        if (t.getCartasRecurso(rDestino).isEmpty()) {
            resultado = "no quedan "+rDestino+" sobre el tablero";            
            return false;        
        }
        Mazo m = new Mazo(BarajaRecursos.getInstance());
        for (int i=0; i<tasa; i++) {
            m.add(BarajaRecursos.cartaParaTipo(rOrigen));
        }
        if ( ! l.getRecursos().saca(m)) {
            resultado = "no puedes cumplir tu parte: no tienes "+tasa+" "+rOrigen;
            return false;
        }

        t.getCartasRecurso(rOrigen).addAll(m);
        l.getRecursos().add(t.getCartasRecurso(rDestino).sacaPrimera());
        resultado = toString()+" efectuado";
        return true;
    }
    
    public static void genera(TableroCatan t, ArrayList<Accion> al) {
        Lado l = t.getLado(t.getTurno());
        Mazo m = new Mazo(BarajaRecursos.getInstance());
        for (Recurso ro : Recurso.values()) {
            m.clear();
            int tasa = l.getTasaCambio(ro);
            for (int i=0; i<tasa; i++) {
                m.add(BarajaRecursos.cartaParaTipo(ro));
            }
            if (l.getRecursos().puedeSacar(m)) {
                for (Recurso rd : Recurso.values()) {
                    if (rd == ro) continue; // no permite cambiar una cosa por ella misma
                    al.add(new AccionCambio(t.getTurno(), ro, rd, tasa));
                }
            } 
        }
    }

    public String toString() {
        return ""+tasa+" "+rOrigen+" x "+rDestino;
    }
}
