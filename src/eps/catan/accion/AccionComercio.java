/*
 * AccionMovimiento.java
 *
 * Created on March 3, 2007, 1:10 PM
 *
 */

package eps.catan.accion;

import eps.catan.*;
import eps.cartas.Mazo;
import eps.catan.TableroCatan.Fase;
import eps.multij.Accion;
import eps.multij.Juego;
import java.rmi.RemoteException;

/**
 * Intercambia unas cartas por otras; requiere confirmacion del destinatario.
 * Caduca cuando pasa el turno en el que se creo la jugada.
 */
public class AccionComercio extends Accion {
    
    private int ronda;
    // origen declarado en superclase (!)
    private int destino;
    private Mazo mOrigen;
    private Mazo mDestino;    
   
    /**
     * Creates a new instance of AccionComercio
     */
    public AccionComercio(Mazo mOrigen, int origen, Mazo mDestino, int destino, TableroCatan t) {
        super(origen);
        this.ronda = t.getRonda();
        this.origen = origen; 
        this.destino = destino;
        this.mDestino = mDestino;
        this.mOrigen = mOrigen;
        
        // exige confirmacion del destino
        this.confirmaciones = new boolean[t.getNumJugadores()];
        confirmaciones[destino] = true;
    }

    public boolean ejecuta(Juego j) {
        TableroCatan t = (TableroCatan)j.getTablero();
        if (t.getFase() != Fase.Normal || 
                t.getRonda() != ronda || 
                t.getTurno() != origen) {
            resultado = "fase incorrecta o se te ha pasado la oportunidad";
            return false;
        }
        Lado o = t.getLado(origen);
        Lado d = t.getLado(destino);
        if (o.getRecursos().puedeSacar(mOrigen) && 
                d.getRecursos().puedeSacar(mDestino)) {
            o.getRecursos().saca(mOrigen);
            d.getRecursos().saca(mDestino);
            o.getRecursos().addAll(mDestino);
            d.getRecursos().addAll(mOrigen);
            try {
                String no = j.getJugador(origen).getNombre();
                String nd = j.getJugador(destino).getNombre();
                resultado = no+" comercia "+mOrigen+" por "+mDestino+" de "+nd;
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
            return true;
        }
        else {
            resultado = "imposible hacer intercambio: alguien exagera";
            return false;
        }        
    }
    
    public String toString() {
        return "Jugador " + origen + " quiere cambiar "+mOrigen+" por "+mDestino+
                " con Jugador "+destino;
    }
}
