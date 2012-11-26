/*
 * Jugador.java
 *
 * Created on January 11, 2008, 3:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eps.multij;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Un jugador de un juego dado. Sabe cómo jugar con un lado, y cómo mostrar
 * eventos que recibe del juego.
 */
public interface Jugador extends Remote {
   
    /**
     * Un evento de un juego. Incluye el tipo de evento, su descripcion, y 
     * el juego que lo origina; a partir de el, el jugador puede averiguar todo
     * lo necesario para jugar.
     * @param e un Evento
     */
    public void cambioEnJuego(Evento e) throws RemoteException;
    
    /**
     * Los jugadores tienen un nombre
     * @return el nombre del jugador
     */
    public String getNombre() throws RemoteException;
}
