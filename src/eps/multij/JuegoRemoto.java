/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eps.multij;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author mfreire
 */
public interface JuegoRemoto extends Remote {
    
    /**
     * Devuelve el tablero
     */
    public Tablero getTablero() throws RemoteException;

    /**
     * Devuelve el jugador i-esimo; para saber el numero de jugadores,
     * consulata al tablero.
     */
    public Jugador getJugador(int i) throws RemoteException;

    /**
     * Devuelve el numero de un jugador dado, -1 si no esta registrado.
     * Usa busqueda lineal porque hay pocos, y mira el nombre para evitar
     * problemas en la P4.
     */
    public int getNumJugador(Jugador j) throws RemoteException;
    
    /**
     * Procesa una accion de un jugador. Como puede haber jugadores en distintos
     * hilos, se usa 'synchronized' para que a lo sumo pueda haber 1 hilo
     * ejectuando una accion en cada momento.
     */
    public void realizaAccion(Accion a) throws RemoteException;
    
    /**
     * Compara este juego con otro
     */
    public boolean equals(JuegoRemoto j) throws RemoteException;
}
