/*
 * Baraja.java
 *
 * Created on February 9, 2006, 3:00 PM
 *
 */

package eps.cartas;

import eps.Util;
import java.awt.Image;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Una baraja es un conjunto completo de cartas. Por ejemplo, está la 
 * baraja española, la baraja francesa o de poquer, etcétera. Sólo se
 * considera baraja al conjunto completo de cartas, y no a los subconjuntos
 * (para eso ya están los {@link Mazo}s).
 * <p>
 * Esta clase está pensada para aplicar el patrón de programación "singleton". 
 * Las subclases no deben usar constructor, sino el método estático <code>getInstance()</code> 
 * para obtener un objeto de tipo Baraja. Este objeto deberá ser siempre el mismo, 
 * garantizando que <code>cartaParaId()</code> siempre va a devolver el 
 * resultado esperado.
 */
public abstract class Baraja implements Serializable {
    
    /** el nombre de la imagen usada como reverso de la baraja en 'iconos'*/
    protected static final String REVERSO = "reverso";    
    
    /** mazo maestro; solo se genera una vez */
    protected Mazo mazo = null;

    /** correspondencias de id a carta; util para parsear */
    protected HashMap<String,Carta> idACarta = new HashMap<String,Carta>();
    protected transient HashMap<Object,Image> iconos = new HashMap<Object,Image>();
    protected HashMap<String,String> idAFicheroIcono = new HashMap<String,String>();
    
    /**
     * Constructor protegido - no se debe llamar excepto desde subclases
     */
    protected Baraja(String ficheroImagenReverso) {
        mazo = new Mazo(this);
        idAFicheroIcono.put(REVERSO, ficheroImagenReverso);
        // a continuacion, la subclase debe inicializar el mazo maestro
    }
    
    /**
     * Devuelve un nuevo mazo con las cartas de esta baraja. Todos los mazos
     * generados por una misma baraja serán idénticos, para garantizar que
     * <code>cartaParaId</code> va a funcionar.
     * <p>
     * El mazo puede salir "sin barajar", es cuestion de llamar a 
     * <code>mazo.mezcla()</code> una vez generado para asegurarse
     * de que el orden es realmente aleatorio.
     * @return el mazo recien generado
     */        
    public Mazo generaMazo() {
        return new Mazo(mazo);
    }
    
    /**
     * Devuelve la {@link Carta} que corresponde a un id dado. Útil para
     * parsear descripciones.
     * @param id el identificador
     * @return la carta pedida, o null si no es de esta baraja
     */
    public Carta cartaParaId(String id) {
        return idACarta.get(id);
    }
    
    /**
     * Devuelve una <code>Image</code>n para una carta dada
     * @param c la carta cuya imagen se quiere obtener
     * @return la imagen pedida, null si error
     */
    public Image imagenParaCarta(Carta c) {
        return getIcono(c.getId());
    }
    
    private Image getIcono(String id) {
        if (iconos == null) {
            iconos = new HashMap<Object, Image>();
        }
        if (iconos.get(id) == null) {
            iconos.put(id, Util.getImage(idAFicheroIcono.get(id)));
        }
        return iconos.get(id);
    }

    /**
     * Devuelve la <code>Image</code>n a usar para el lado oculto de las cartas
     * @return la imagen pedida, null si error
     */
    public Image imagenReverso() {
        return getIcono(REVERSO);
    }
}
