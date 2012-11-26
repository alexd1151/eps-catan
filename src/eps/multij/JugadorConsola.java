/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eps.multij;

import eps.catan.*;
import eps.multij.gui.ConsolaGrafica;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Un jugador que muestra el estado de forma textual, y que recibe comandos
 * de Stdin.
 */
public abstract class JugadorConsola implements Jugador {
    
    protected JuegoRemoto juego;
    protected String nombre;
    protected Consola consola;
    
    /** 
     * Constructor con nombre propio 
     */
    public JugadorConsola(String nombre, Consola consola) {
        this.nombre = nombre;
        this.consola = consola;
        try {
            UnicastRemoteObject.exportObject(this);
        } catch (RemoteException e){
            System.out.println("RE registrando JugadorConsola");
            e.printStackTrace();
        }        
    }    

    /**
     * @return el nombre de este jugador
     */
    public String getNombre() {
        return nombre;
    }
       
    /**
     * Recibe una notificacion de un cambio en el juego y la muestra por
     * pantalla
     */
    public void cambioEnJuego(Evento e) {
        try {
            if ( ! e.getJuego().equals(juego)) {
                ((ConsolaGrafica)consola).setJuego(e.getJuego(), this);
            }
        } catch (RemoteException re) {
            re.printStackTrace();
        }      
        this.juego = e.getJuego();
        switch (e.getTipo()) {
            case Cambio:
                consola.muestraTexto(
                    nombre + ": Cambio: " + e.getDescripcion() + "\n"); 
                break;

            case Confirma:
                consola.muestraTexto(
                    nombre + ": Confirmacion: " + e.getDescripcion() + "\n");
                break;

            case Turno:
                consola.muestraTexto(
                    nombre + ": Turno: "+e.getDescripcion() + "\n");
                break;
        }
        consola.actualiza(e);
    }

    /**
     * Ejecuta una orden.
     * @param comando texto introducido en la interfaz (o leido de un fichero,
     *     a traves de una prueba).
     * @return false si la orden no se ha interpretado bien (y por tanto no se
     * debe borrar de la linea de entrada, para que se puedan hacer cambios), o
     * true (si se ha interpretado correctamente y se debe limpiar la linea
     * de entrada de cara a la siguiente orden).
     */
    public abstract boolean ejecuta(String comando);
    
    /**
     * La definicion de una 'consola' para jugar a juegos.
     * Puede mostrar texto y actualizarse: muy sencilla.
     */
    public static interface Consola {
        public void setJuego(JuegoRemoto j);
        public void muestraTexto(String t);
        public void actualiza(Evento e);
        public void cierraVentana();
    }
}
