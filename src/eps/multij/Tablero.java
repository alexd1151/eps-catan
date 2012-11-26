/*
 * Tablero.java
 *
 * Created on February 14, 2004, 9:13 PM
 *
 * (C) 2003-2004 Escuela Politécnica Superior, Universidad Autónoma de Madrid
 */

package eps.multij;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Representa un tablero de un juego multijugador sencillo
 */
public abstract class Tablero implements Serializable {           
    
    public enum Estado {
        /** juego en curso */
        EnCurso,
        /** juego finalizado; ganador == turno actual */
        Finalizado,
        /** juego finalizado; no hay ganadores */
        Tablas
    }
    
    /** por defecto, solo para 2 jugadores (las subclases pueden cambiarlo) */
    protected int numJugadores = 2;
    
    /** representa el jugador al que le toca mover (numeros del 0 a numJugadores) */
    protected int turno;
    
    /** devuelve el estado del juego */
    protected Estado estado;
    
    /**
     * Constructor abstracto - todas las subclases deben llamarlo, y
     * no se puede llamar desde fuera de las subclases
     */
    public Tablero(int numJugadores) {
        this.numJugadores = numJugadores;
        this.estado = Estado.EnCurso;
    }
         
    /**
     * Devuelve el numero del jugador al que le toca mover
     * @return el turno del jugador
     */
    public int getTurno() {
        return turno;
    }          

    /**
     * Cambia el estado de la partida
     * @param e el estado 
     */
    public void setEstado(Estado e) {
	estado = e;
    }    
    
    /**
     * Devuelve el estado de la partida
     */
    public Estado getEstado() {
        return estado;
    }                
    
    /**
     * Devuelve el numero de jugadores
     */
    public int getNumJugadores() {
        return numJugadores;
    }    

    /**
     * Cambia el turno
     */
    public void cambiaTurno() {
        if (estado != Estado.EnCurso) return;
        turno = (turno + 1) % numJugadores;
    }
    
    /**
     * Genera todas las acciones validos para el jugador actual en este tablero
     * @return un ArrayList que contiene las acciones validas
     */    
    public abstract ArrayList<Accion> accionesValidas(int nJugador);

    /** 
     * Inicializa el tablero al estado dado
     * @return la cadena pedida. 
     */
    public abstract void inicializa(String s);    
    
    /** 
     * Crea una cadena que contiene una representacion interna del tablero. 
     * Esta representacion se debe poder usar para inicializar un tablero a un
     * estado dado.
     * @return la cadena pedida. 
     */
    public abstract String toString();    
}
