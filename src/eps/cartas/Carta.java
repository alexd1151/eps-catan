/*
 * Carta.java
 *
 * Created on February 9, 2006, 11:10 AM
 */

package eps.cartas;

import java.io.Serializable;

/**
 * Una carta de una {@link Baraja}. Las cartas tienen una ID interna, y un nombre
 * más largo que es el que se muestra a los jugadores. A veces
 * también incluyen una descripción que dice cómo se puede usar. Una vez creada,
 * una carta no se cambia (no tiene sentido cambiarla, las cartas no se "generan",
 * se "reparten").
 * <p>
 * Para generar las cartas, hay que sacarlas de una Baraja; solamente las
 * Barajas deberian llamar al constructor.
 */
public class Carta implements Comparable, Serializable {

    /** identificador de la carta */
    private String id;
    /** nombre de la carta */
    private String nombre;
    /** descripcion */
    private String descripcion;

    /**
     * Constructor de la clase. Sólo debe ser llamado desde una Baraja.
     * @param id el identificador de la carta (corto)
     * @param nombre su nombre (inteligible)
     * @param descripcion una descripción de su uso (opcional, puede ser "null" si esta Baraja no la requiere).
     */
    public Carta(String id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    /**
     * devuelve el identificador de una carta, por ejemplo "1E" para el
     * "As de Espadas" (suponiendo baraja espa&ntilde;ola). Los identificadores
     * tienen que ser cortos, y se usan para mostrar listados de cartas
     * @return el identificador de la carta.
     */
    public String getId() {
        return id;
    }

    /**
     * devuelve el nombre de la carta, por ejemplo "As de Espadas"
     * (suponiendo baraja espa&ntilde;ola)
     * @return el nombre de la carta
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * devuelve la descripcion de esta carta. Hay barajas en las que
     * las descripciones indican como se puede jugar la carta. Si no es aplicable,
     * basta con devolver 'null'.
     * @return la descripcion, o 'null'
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Compara una carta con otra
     * @param b la otra
     * @return true si son iguales
     */
	@Override
    public boolean equals(Object b) {
        return compareTo(b) == 0;
    }

	/**
	 * Genera el hash de una carta. Usa solamente su ID.
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 67 * hash + (this.id != null ? this.id.hashCode() : 0);
		return hash;
	}

    /**
     * compara dos cartas; dice que son iguales si tienen el mismo ID.
     * Implementa la interfaz "Comparable", y permite ordenar conjuntos de
     * cartas.
     * @param o un objeto con el que comparar a éste
     * @return un entero =0, <0, o >0 si la segunda carta es igual, menor
     * o mayor que la primera, respectivamente
     */
    public int compareTo(Object o) {
        return getId().compareTo(((Carta)o).getId());
    }
}
