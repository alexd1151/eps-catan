/*
 * Mazo.java
 *
 * Created on February 9, 2006, 11:10 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eps.cartas;

import eps.Util;
import java.util.Collections;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Un mazo es un conjunto de cartas. Se puede repartir entre distintos mazos,
 * mezclar, se pueden sacar cartas del comienzo, meter nuevas cartas,
 * etcetera.
 *<p>
 * Este contenedor está implementado encima de un ArrayList, lo cual permite
 * aprovechar todos sus métodos (que son muchos).
 */
public class Mazo extends ArrayList<Carta> {

    /** baraja a la que pertenece */
    private Baraja baraja;

    /**
     * Crea un mazo vacío de cartas de la baraja indicada
     */
    public Mazo(Baraja baraja) {
        this.baraja = baraja;
    }

    /**
     * Crea un mazo a partir de otro
     * @param m mazo de partida
     */
    public Mazo(Mazo m) {
        addAll(m);
        baraja = m.getBaraja();
    }

    /**
     * Crea un mazo a partir de una descripcion de la forma
     * <code> {idCarta0, idCarta1, ..., idCartaN} <code>, que es la misma
     * que genera el método <code>toString</code>
     * @param descripcion descripción del mazo a crear, generada con <code>toString</code>.
     * @param baraja {@link Baraja} a la que pertenecen esos IDs
     */
    public Mazo(String descripcion, Baraja baraja) {
        this.baraja = baraja;

        // saca todas las cartas de la cadena por ID y las inserta en el mazo
        StringTokenizer tok = new StringTokenizer(descripcion, "{, }");
        while (tok.hasMoreTokens()) {
            Carta nueva = baraja.cartaParaId(tok.nextToken());
            add(nueva);
        }
    }

    /**
     * Mezcla entre si las cartas de este mazo, es decir, lo baraja y deja
     * sus cartas en orden aleatorio
     */
    public void mezcla() {
        Util.baraja(this);
    }

    /**
     * Ordena el mazo de cartas; es decir, coloca todas las cartas en el
     * orden que marque la baraja a la que pertenecen
     */
    public void ordena() {
        Collections.sort(this);
    }

    /**
     * Devuelve la carta que esta en la posicion i
     * @param i posicion cuya carta se quiere extraer
     * @return la carta que esta en la posicion i
     */
    public Carta cartaEn(int i) {
        return get(i);
    }

    /**
     * Comprueba si se pueden sacar todas las cartas 'aSacar' de este mazo
     * @return true si es posible sacarlas todas
     */
    public boolean puedeSacar(Mazo aSacar) {
        Mazo copia = new Mazo(this);
        return copia.saca(aSacar);
    }

    /**
     * Intenta sacar todas las cartas aSacar del mazo actual
     * @return true si consigue sacarlas todas, false si falta alguna
     */
    public boolean saca(Mazo aSacar) {
        int total = 0;
        for (Carta c : aSacar) {
            for (int i=0; i<size(); i++) {
                if (cartaEn(i).equals(c)) { total ++ ; remove(i); break; }
            }
        }
        return total == aSacar.size();
    }

    /**
     * Saca la primera carta de este mazo, y la devuelve
     * @return la carta sacada
     */
    public Carta sacaPrimera() {
        return remove(0);
    }

    /**
     * Genera una descripcion de este mazo - en el mismo formato usado para
     * inicializar el mazo:
     * <code>{idCarta0, idCarta1, ..., idCartaN}</code>
     * @return una descripción de este mazo en forma de cadena.
     */
	@Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        int n = size();
        for (int i=0; i<n; i++) {
            sb.append(cartaEn(i).getId());
            if (i+1<n) sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Devuelve la baraja usada para crear este mazo
     */
    public Baraja getBaraja() {
        return baraja;
    }

    /**
     * Cuenta las cartas "iguales" a la dada en este mazo
     */
    public int cuenta(Carta c) {
        int total = 0;
        for (Carta k : this) {
            if (c.equals(k)) total ++;
        }
        return total;
    }
}
