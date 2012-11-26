/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eps.multij;

import eps.Util;
import java.io.File;

/**
 */
public class AccionCarga extends Accion {
    
    private String nombreFichero;
    
    public AccionCarga(int i, int nJugadores, String nombreFichero) {
        super(i);
        this.nombreFichero = nombreFichero;
        this.confirmaciones = new boolean[nJugadores];
    }
    
    public boolean ejecuta(Juego j) {
        try {
            File f = new File(nombreFichero);            
            j.getTablero().inicializa(Util.leeFicheroACadena(f));
        } catch (Exception e) {
            e.printStackTrace();
            resultado = "Imposible cargar: "+e.getMessage();
            return false;
        }
        resultado = "Se acaba de cargar otra partida";
        return true;
    }

    public String toString() {
        return "cargar la partida partida de '"+nombreFichero+"'";
    }
}
