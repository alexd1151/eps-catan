/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eps.catan.gui;

import eps.multij.Jugador;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Permite a un Jugador entrar en una PartidaRemota (un juego que está en 
 * construcción).
 * 
 * @author mfreire
 */
public interface PartidaRemota extends Remote {
    public boolean entrarEnPartida(Jugador j) throws RemoteException;
}
