/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eps.multij;

import eps.Util;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import javax.swing.Timer;

/**
 * Un jugador aleatorio. Juega a *cualquier* juego:  *eso* es flexibilidad.
 */
public class JugadorAleatorio implements Jugador, Serializable {

    private String nombre;
    private static int numAleatorios = 0;
    private transient Cabeza cabeza = null;

    /** Constructor por defecto */
    public JugadorAleatorio() {
        this("Aleatorio " + (++numAleatorios));
    }

    /** Constructor con nombre propio */
    public JugadorAleatorio(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Recibe una notificacion de un cambio en el juego
     */
    public void cambioEnJuego(Evento e) {
        try {
            JuegoRemoto j = e.getJuego();
            switch (e.getTipo()) {
                case Cambio:
                    System.out.println(nombre + ": Cambio: " + e.getDescripcion());
//                System.out.println(nombre + ": Tablero es:\n" + j.getTablero());
                    break;

                case Confirma:
                    System.out.println(nombre + ": Confirmacion: " + e.getDescripcion());

                    // este jugador confirma al azar 2/3 de las veces
                    if (Math.random() > 0.33) {
                        e.getCausa().confirma(j.getNumJugador(this), j);
                    }
                    break;

                case Turno:
                    System.out.println(nombre + ": Turno: " + e.getDescripcion());
                    break;
            }

            // mientras pueda, intenta jugar cada 500 ms
            if (cabeza == null) {
                try {
                    cabeza = new Cabeza(j, j.getNumJugador(this), 500);
                } catch (RemoteException re) {
                    re.printStackTrace();
                }
            }
            cabeza.piensa();
        } catch (RemoteException re) {
            re.printStackTrace();
        }
    }

    /**
     * clase interna que puede realizar una acción legal cuando suene
     * la campana
     */
    private class Cabeza implements ActionListener {

        private JuegoRemoto j;
        private int l;
        private Timer timer;

        private Cabeza(JuegoRemoto j, int l, int delay) {
            this.j = j;
            this.l = l;
            this.timer = new Timer(delay, this);
        }

        public void piensa() {
            timer.start();
        }

        public void actionPerformed(ActionEvent ev) {
            try {
                Tablero t = j.getTablero();
                ArrayList<Accion> validas = t.accionesValidas(l);
                if (!validas.isEmpty()) {
                    int r = Util.tiraDado(validas.size()) - 1;
                    j.realizaAccion(validas.get(r));
                } else {
                    timer.stop();
                    cabeza = null;
                }
            } catch (RemoteException re) {
                re.printStackTrace();
            }
        }
    }

    /**
     * @return el nombre de este jugador
     */
    public String getNombre() {
        return nombre;
    }
}
