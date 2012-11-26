/*
 * VisorMapa.java
 *
 * Created on January 11, 2008, 3:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package eps.catan.gui;

import eps.catan.TableroCatan;
import eps.multij.Evento;
import eps.multij.JuegoRemoto;
import eps.multij.Jugador;
import eps.multij.Tablero;
import eps.multij.gui.InterfazTablero;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.rmi.RemoteException;
import java.util.Collection;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

/**
 * Componente grafica que muestra un tablero de Catan, incluyendo tambien
 * contadores y mazos de cartas.
 */
public class InterfazCatan extends JSplitPane implements InterfazTablero {

    private JuegoRemoto j;
    private VisorMapa vm;
    private VisorJugador vj[];
    private JSplitPane split;

    /**
     * Creates a new instance of InterfazCatan
     */
    public InterfazCatan() {
        super(JSplitPane.HORIZONTAL_SPLIT);
        setOneTouchExpandable(true);
        vm = new VisorMapa();
        vj = new VisorJugador[TableroCatan.MAX_LADOS];
        for (int i = 0; i < vj.length; i++) {
            vj[i] = new VisorJugador();
        }

        JPanel left = new JPanel();
        left.setLayout(new GridBagLayout());
        for (int i = 0; i < vj.length; i++) {
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.weighty = 1.0;
            c.weightx = 1.0;
            c.gridx = 0;
            c.gridy = i;
            left.add(vj[i], c);
        }
        setLeftComponent(left);
        setRightComponent(vm);
        setDividerLocation(500);
        revalidate();
    }
    // Jugadores de los que se muestra toda la informacion disponible
    private boolean puedeMostrar[];
    String ju = null;

    public void setJuego(JuegoRemoto j, Collection<Jugador> jugadores) {
        try {
            this.j = j;
            puedeMostrar = new boolean[j.getTablero().getNumJugadores()];
            for (Jugador usado : jugadores) {
                puedeMostrar[j.getNumJugador(usado)] = true;
                ju = usado.getNombre();
                System.err.println("Tablero inicializado para " + usado.getNombre());
            }
            vm.setTablero((TableroCatan) j.getTablero());
            for (int i = 0; i < vj.length; i++) {
                if (i < j.getTablero().getNumJugadores()) {
                    vj[i].setVisible(true);
                    vj[i].setLado(j, i, vm);
                } else {
                    vj[i].setVisible(false);
                }
            }
            actualiza(null);
        } catch (RemoteException re) {
            re.printStackTrace();
        }
    }

    public void actualiza(Evento e) {
        try {
            System.err.println("Actualizando interfaz del jugador " + ju);
            if (j != null) {
                Tablero t = j.getTablero();
                vm.setTablero((TableroCatan)t);

                for (int i = 0; i < t.getNumJugadores(); i++) {
                    vj[i].refresca(j, puedeMostrar[i], e);
                }
            }
        } catch (RemoteException re) {
            re.printStackTrace();
        }
    }
}
