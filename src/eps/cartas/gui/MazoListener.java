/*
 * MazoListener.java
 *
 * Created on January 16, 2008, 6:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eps.cartas.gui;

/**
 * Usado para suscribirse a eventos de seleccion y deseleccion de cartas, y 
 * hacer cosas cuando corresponda.
 */
public interface MazoListener {
    public void dobleClickEnCarta(InterfazMazo im, int posCarta);
    public void clickEnCarta(InterfazMazo im, int posCarta);
}
