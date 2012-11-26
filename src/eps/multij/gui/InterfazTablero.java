/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eps.multij.gui;

import eps.multij.Evento;
import eps.multij.JuegoRemoto;
import eps.multij.Jugador;
import java.util.Collection;

/**
 * La interfaz de un tablero de un juego sin especificar.
 */
public interface InterfazTablero {
    /**
     * Cambia el juego actual por el suministrado. Se supone que esta interfaz
     * soporta el tipo de juego...
     * @param j
     */
    public void setJuego(JuegoRemoto j, Collection<Jugador> jugadoresMostradosPorCompleto);

    /**
     * Actualiza la representacion del juego actual
     */
    public void actualiza(Evento e);  
}
