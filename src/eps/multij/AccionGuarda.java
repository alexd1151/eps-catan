/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eps.multij;

import eps.Util;
import java.io.File;

/**
 */
public class AccionGuarda extends Accion {
    
    private String nombreFichero;
    
    public AccionGuarda(int i, String nombreFichero) {
        super(i);
        this.nombreFichero = nombreFichero;
    }
    
    public boolean ejecuta(Juego j) {
        try {
            File f = new File(nombreFichero);
            Util.escribeCadenaAFichero(j.getTablero().toString(), f);
        } catch (Exception e) {
            e.printStackTrace();
            resultado = "Error guardando: "+e.getMessage();
            return false;
        }
        return true;
    }

    public String toString() {
        return "guardar la partida a '"+nombreFichero+"'";
    }
}
