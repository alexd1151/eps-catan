/*
 * EventoPartida.java
 *
 * Created on March 2, 2005, 11:46 AM
 */

package eps.multij;

import java.io.Serializable;

/**
 * Un evento usado para notificar a los suscriptores a una partida de todo
 * lo que sucede en la misma.
 */
public class Evento implements Serializable {
    
    /** Tipos de evento */
    public enum TipoEvento {
        /** para cambios genericos al estado (ej.: alguien mueve, pero no te toca) */
        Cambio, 
        /** para notificar que te toca mover */
        Turno, 
        /** para pedir una confirmacion */
        Confirma, 
        /** para notificar errores en peticiones (ej.: alguien no confirma tu accion) */
        Error
    }
    
    /** tipo de evento */
    private TipoEvento tipo;
    /** descripcion del evento */
    private String descripcion;
    /** partida actual (de la cual se pueden consultar tablero, jugadores, ...) */
    private JuegoRemoto juego;
    /** la accion que ocasiono este evento (puede ser null) */
    private Accion accion;
    
    /** Creates a new instance of EventoPartida */
    public Evento(TipoEvento tipo, String descripcion, 
            Juego juego, Accion accionCausa) {
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.juego = juego;
        this.accion = accionCausa;        
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public JuegoRemoto getJuego() {
        return juego;
    }
    
    public Accion getCausa() {
        return accion;
    }
}
