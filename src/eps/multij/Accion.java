/*
 * Accion.java
 *
 * Created on March 3, 2007, 1:07 PM
 *
 */

package eps.multij;

import eps.catan.*;
import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * Una accion. Se puede ejecutar sobre un Juego.
 */
public abstract class Accion implements Serializable {
    
    /** confirmaciones requeridas; por defecto, ninguna */
    protected boolean[] confirmaciones = null;
    protected int origen;
    protected String resultado;
    
    /**
     * constructor abstracto; las superclases tienen que llamarlo, pero 
     * solo lo pueden llamar ellas.
     */
    public Accion(int origen) {
        this.origen = origen;
    }
    
    /**
     * ejecuta esta accion sobre este juego. Debe llamar al metodo de Juego
     * apropiado para notificar sus resultados. Por ejemplo, "notificaCambio", 
     * o "notificaError" (para avisar al jugador que la lanzó), o "notificaTurno"
     * para decirle al jugador actual que le toca mover.
     */
    public abstract boolean ejecuta(Juego j);
    
    /**
     * devuelve el resultado de la accion. Se notifica a todos.
     */
    public String getResultado() {
        return resultado;
    }
    
    /**
     * devuelve una descripcion de la accion
     */
    public abstract String toString();
    
    /**
     * dice si la accion requiere confirmacion de alguien. Si la requiere,
     * el juego esperara a que deje de requerirla antes de llevarla a cabo.
     */
    public boolean requiereConfirmacionDe(int i) {
        return confirmaciones != null && confirmaciones[i];
    }

    /**
     * el jugador i confirma la accion. Despues de llamar a esto, 
     * requiereConfirmacionDe devolvera "false". Si se trata de la ultima
     * confirmacion requerida, ejecuta la accion.
     */
    public void confirma(int i, JuegoRemoto j) {
        if (confirmaciones != null) {
            confirmaciones[i] = false;
            for (boolean b : confirmaciones) {
                if (b) return;
            }
            try {
                j.realizaAccion(this);
            }
            catch (RemoteException re) {
                re.printStackTrace();
            }
        }
    }
        
    /**
     * devuelve el jugador origen de la accion (util para notificar sus efectos)
     * Si no es nadie, se usa -1.
     */
    int getOrigen() {
        return origen;
    }
}
