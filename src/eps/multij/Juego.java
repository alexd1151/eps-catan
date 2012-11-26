/*
 * Juego.java
 *
 * Created on March 2, 2005, 11:46 AM
 */
package eps.multij;

import eps.multij.Evento.TipoEvento;
import eps.multij.Tablero.Estado;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Una Juego permite a varios Jugadores jugar sobre un Tablero a lo que sea.
 * Los jugadores actuan sobre la partida mediante acciones (patron 'comando'), y
 * son notificados de los cambios que sufre la partida suscribiendose a eventos
 * de tipo 'Evento' (patron 'publicacion-suscripcion').
 * Las acciones pueden ser 'confirmadas', en cuyo caso solamente se llevara a 
 * cabo si todos los jugadores llaman al metodo confirmaAccion y contestan 
 * afirmativamente.
 */
public class Juego implements JuegoRemoto {

    /** todos los jugadores que estan jugando en este momento */
    private ArrayList<Jugador> jugadores;
    /** tablero sobre el que estan jugando */
    private Tablero tablero;

    /** 
     * Crea una nueva partida, notificando a los jugadores de su estado, y 
     * avisando al jugador actual de que le toca mover 
     */
    public Juego(Tablero tablero, ArrayList jugadores) {
        try {
            java.rmi.server.UnicastRemoteObject.exportObject(this);
        } catch (RemoteException e) {
            System.out.println("RE registrando Juego");
            e.printStackTrace();
        }
        comienzaPartida(tablero, jugadores);
    }

    /**
     * Comienza una partida
     */
    public void comienzaPartida(Tablero tablero, ArrayList<Jugador> jugadores) {
        this.tablero = tablero;
        this.jugadores = jugadores;

        try {

            // comprueba argumentos
            if (jugadores.size() != tablero.getNumJugadores()) {
                throw new IllegalArgumentException(
                        "Faltan o sobran jugadores en este tablero");
            }
            if (tablero.estado != Estado.EnCurso) {
                throw new IllegalArgumentException(
                        "Este tablero ya tiene una posicion final: no se puede empezar");
            }
            for (int i = 0; i < jugadores.size(); i++) {
                if (getNumJugador(jugadores.get(i)) != i) {
                    throw new IllegalArgumentException(
                            "Hay mas de un jugador que se llama '" + jugadores.get(i).getNombre() + "'");
                }
            }

            // envia a todos los jugadores el estado actual
            notificaCambio("La partida va a comenzar", null);

            notificaTurno("Te toca", null);
        } catch (RemoteException re) {
            System.err.println("RE inicializando partida");
            re.printStackTrace();
        }
    }

    // metodos de notificacion
    /**
     * Notifica el turno al jugador al que le toca mover
     */
    public void notificaTurno(String descripcion, Accion accion) throws RemoteException {
        jugadores.get(tablero.getTurno()).cambioEnJuego(new Evento(
                TipoEvento.Turno, descripcion, this, accion));
    }

    /**
     * Notifica el turno al jugador al que le toca mover
     */
    public void notificaError(String descripcion, Accion accion) {
        try {
            int o = accion.getOrigen();
            if (o >= 0 && o < tablero.getNumJugadores()) {
                jugadores.get(o).cambioEnJuego(new Evento(
                        TipoEvento.Error, descripcion, this, accion));
            }
        } catch (RemoteException re) {
            re.printStackTrace();
        }        
    }

    /**
     * Notifica un cambio de estado a todos los jugadores
     */
    public void notificaCambio(String descripcion, Accion accion) throws RemoteException {
        Evento e = new Evento(TipoEvento.Cambio,
                descripcion, this, accion);
        for (Jugador j : jugadores) {
            j.cambioEnJuego(e);
        }
    }

    // Metodos de acceso a variables de instancia
    /**
     * Devuelve el tablero actual (solo para consultas - los jugadores no
     * deben usarlo para mover directamente).
     */
    public Tablero getTablero() {
        return tablero;
    }

    /**
     * Devuelve el jugador i-esimo; para saber el numero de jugadores,
     * consulata al tablero.
     */
    public Jugador getJugador(int i) {
        return jugadores.get(i);
    }

    /**
     * Devuelve el numero de un jugador dado, -1 si no esta registrado.
     * Usa busqueda lineal porque hay pocos, y mira el nombre para evitar
     * problemas en la P4.
     */
    public int getNumJugador(Jugador j) {
        try {
            int i = 0;
            for (Jugador otro : jugadores) {
                if (otro.getNombre().equals(j.getNombre())) {
                    return i;
                }
                i++;
            }
        } catch (RemoteException re) {
            re.printStackTrace();
        }
        return -1;
    }
    
    /**
     * Devuelve 'true' si el otro juego es 'igual' que este, es decir, 
     * si tiene los mismos jugadores e identico tablero
     */
    public boolean equals(JuegoRemoto o) throws RemoteException {    
        if (o == null) return false;
        Tablero t = o.getTablero();        
        for (int i=0; i<t.getNumJugadores(); i++) {
            if (! o.getJugador(i).getNombre().equals(getJugador(i).getNombre())) {
                return false;
            }
        }
        return t.equals(getTablero());        
    }

    /**
     * Procesa una accion de un jugador. Como puede haber jugadores en distintos
     * hilos, se usa 'synchronized' para que a lo sumo pueda haber 1 hilo
     * ejectuando una accion en cada momento.
     */
    public synchronized void realizaAccion(Accion a) {

        try {

            // si requiere confirmacion, se envian peticiones de confirmacion a todos
            boolean seHaPedidoConfirmacion = false;
            for (int i = 0; i < jugadores.size(); i++) {
                if (a.requiereConfirmacionDe(i)) {
                    Evento e = new Evento(TipoEvento.Confirma, "Propuesta", this, a);
                    jugadores.get(i).cambioEnJuego(e);
                    // el último en confirmarla la reenviará aqui
                    seHaPedidoConfirmacion = true;
                }
            }
            if (seHaPedidoConfirmacion) {
                return;
            }

            // si no hace falta confirmar, o ya ha sido confirmada, realiza la accion
            boolean ok = a.ejecuta(this);
            System.err.println("[[Ejecutado: " + a + "=>>" + a.getResultado());
            if (ok) {
                notificaCambio(a.getResultado(), a);
            } else {
                notificaError(a.getResultado(), a);
            }

            // avisa al siguiente jugador, o avisa de fin de partida
            switch (tablero.getEstado()) {
                case EnCurso:
                    System.err.println("[[Notificado el fin de turno");
                    notificaTurno("te toca", a);
                    break;
                case Finalizado:
                    System.err.println("[[Partida finalizada");
                    notificaCambio("Partida finalizada: gana el jugador " + jugadores.get(tablero.getTurno()).getNombre(), a);
                    break;
                case Tablas:
                    System.err.println("[[Partida finalizada");
                    notificaCambio("Partida finalizada: tablas.", a);
                    break;
            }
        } catch (RemoteException re) {
            System.err.println("RE - Realizando accion");
            re.printStackTrace();
        }
    }
}
