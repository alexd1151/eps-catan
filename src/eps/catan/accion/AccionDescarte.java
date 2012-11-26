/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eps.catan.accion;

import eps.cartas.Carta;
import eps.cartas.Mazo;
import eps.catan.BarajaRecursos;
import eps.catan.BarajaRecursos.Recurso;
import eps.catan.TableroCatan;
import eps.multij.Accion;
import eps.multij.Juego;
import java.util.ArrayList;

/**
 * Tira la mitad de las cartas de recurso (redondeando hacia abajo)
 */
public class AccionDescarte extends Accion {

    private Mazo aTirar;
       
    public AccionDescarte(int origen, Mazo aTirar) {
        super(origen);
        this.aTirar = aTirar;
    }    
    
    public boolean ejecuta(Juego j) {
        TableroCatan t = (TableroCatan)j.getTablero();
        if (t.getTurno() != origen) {
            resultado = "no te toca tirar a ti, listillo";
            return false;
        }        
        Mazo m = t.getLado(origen).getRecursos();
        if ( m.size() > 7 && (aTirar == null || aTirar.size() != m.size()/2)) {
            resultado = "tienes que descartarte de "+(m.size()/2)+" cartas, y no de "+
                    (aTirar == null ? 0 : aTirar.size());
            return false;
        }
        if (aTirar == null) {
            resultado = "descarte de 0 cartas OK (porque tenias menos de 7)";
            t.cambiaTurno();
            return true;
        }        
        if ( ! m.puedeSacar(aTirar)) {
            resultado = "imposible tirar "+aTirar+" de "+m;
            return false;
        }
        m.saca(aTirar);
        for (Carta c : aTirar) {
            t.getCartasRecurso(BarajaRecursos.tipoParaCarta(c)).add(c);
        }
        t.cambiaTurno();
        return true;
    }

    public static void genera(TableroCatan t, ArrayList<Accion> al) {
        Mazo m = t.getLado(t.getTurno()).getRecursos();
        
        if (m.size() < 8) {
            al.add(new AccionDescarte(t.getTurno(), null));
            return;
        }
        
        int k[] = new int[Recurso.values().length];                
        int i = 0;
        for (Recurso r : Recurso.values()) {
            k[i++] = m.cuenta(BarajaRecursos.cartaParaTipo(r));
        }
        
        elimina(m.size()/2, t.getTurno(), k, k, al);
    }
    
    /**
     * Genera posibles mazos de forma recursiva, sin repetir ninguno.
     * Si te gusta diseñar este tipo de cosas, apuntate a EAdlPR :-)
     * @param n
     * @param nLado
     * @param k - array con los numeros de cada recurso restantes
     * @param o - array con los numeros de cada recurso originales
     * @param al
     */
    private static void elimina(int n, int nLado, int[] k, int[] o, ArrayList<Accion> al) {
//        for (int i=0; i<k.length; i++) System.err.print(k[i]+" "); System.err.println();
//        for (int i=0; i<k.length; i++) System.err.print(o[i]+" "); System.err.println();
        if (n == 0) {
            Mazo m = new Mazo(BarajaRecursos.getInstance());
            int i=0; 
            for (Recurso r : Recurso.values()) {
                for (int j=0; j<o[i] - k[i]; j++) {
                    m.add(BarajaRecursos.cartaParaTipo(r));
                }
                i++;
            }
            al.add(new AccionDescarte(nLado, m));
        }
        else {
            for (int i=0; i<k.length; i++) {
                int k2[] = (int[])k.clone();
                if (k2[i] > 0) {
                    k2[i] --;
                    elimina(n-1, nLado, k2, o, al);
                }
            }
        }
    }    
    
    public String toString() {
        return "descarte de "+aTirar;
    } 
//    
//    public static void main(String[] ss) {
//        int[] ka = new int[] {3, 2, 1, 0, 0};
//        ArrayList<Accion> al = new ArrayList<Accion>();
//        elimina(3, 0, ka, ka, al);
//        for (Accion a : al) {
//            System.err.println(a);
//        }        
//    }
}
