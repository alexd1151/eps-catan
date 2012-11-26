/*
 * Posicion.java
 *
 * Created on January 14, 2008, 2:20 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eps.catan;
import eps.catan.TableroCatan.OrientacionVertice;
import eps.catan.TableroCatan.OrientacionArista;

/**
 * Una posicion de vertice en el juego de Catan
 */
public class PosicionVertice extends Posicion {

    private OrientacionVertice o;
    
    public static final OrientacionVertice OCanonicas[] = new OrientacionVertice[] {
        OrientacionVertice.Norte, OrientacionVertice.Sur
    };
    
    
    /** Creates a new instance of Posicion */
    public PosicionVertice() {
        this(new Posicion(), OrientacionVertice.Norte);
    }
    
    public PosicionVertice(PosicionVertice pv) {
        this(pv, pv.getOrientacion());
    }
        
    public PosicionVertice(Posicion p, OrientacionVertice o) {
        super(p);
        this.o = o;
    }

    public PosicionVertice(String s) {
        super(s);
        this.o = OrientacionVertice.valueOf(s.substring(s.indexOf(':')+1));
    }    
    
    /**
     * Hay 3 posiciones equivalentes para un vertice:
     * Opcion 1: (x, y):N, (x, y-1):SO, (x-1, y):SE
     * Opcion 2: (x, y):S, (x+1, y):NO, (x, y+1):NE
     * Esta funcion se asegura de que se normaliza para usar la primera variante
     */
    public void normaliza() {
        switch (o) {
            case SurOeste: 
                setPos(getX(), getY()+1);
                o = OrientacionVertice.Norte;
                break;
            case SurEste: 
                setPos(getX()+1, getY());
                o = OrientacionVertice.Norte;
                break;
            case NorOeste: 
                setPos(getX()-1, getY());
                o = OrientacionVertice.Sur;
                break;
            case NorEste: 
                setPos(getX(), getY()-1);
                o = OrientacionVertice.Sur;
                break;                
        }
    }
    
    /**
     * devuelve los sinonimos del vertice actual, incluyendo el propio vertice
     */
    public PosicionVertice[] sinonimos(PosicionVertice[] rpv) {
        
        // inicializacion
        rpv = (rpv == null) ? new PosicionVertice[3] : rpv;
        for (int i=0; i<rpv.length; i++) {
            rpv[i] = (rpv[i] == null) ? new PosicionVertice(this) : rpv[i];
        }
        normaliza();
        
        rpv[0].setPos(getX(), getY());
        rpv[0].setOrientacion(o);        
        switch (rpv[0].getOrientacion()) {
            case Norte:
                rpv[1].setPos(getX(), getY()-1);
                rpv[1].setOrientacion(OrientacionVertice.SurOeste);
                rpv[2].setPos(getX()-1, getY());
                rpv[2].setOrientacion(OrientacionVertice.SurEste);
                break;
            case Sur:
                rpv[1].setPos(getX()+1, getY());
                rpv[1].setOrientacion(OrientacionVertice.NorOeste);
                rpv[2].setPos(getX(), getY()+1);
                rpv[2].setOrientacion(OrientacionVertice.NorEste);
                break;                
        }
        return rpv;
    }

    /**
     * Devuelve las 3 aristas que limitan con este vertice
     */
    public PosicionArista[] getAristas(PosicionArista rpa[]) {
        
        // inicializacion
        rpa = (rpa == null) ? new PosicionArista[3] : rpa;
        for (int i=0; i<rpa.length; i++) {
            rpa[i] = (rpa[i] == null) ? new PosicionArista() : rpa[i];
        }
        normaliza();
                
        switch(getOrientacion()) {
            case Norte:
                rpa[0].setPos(getX(), getY());
                rpa[0].setOrientacion(OrientacionArista.NorOeste);
                rpa[1].setPos(getX(), getY());
                rpa[1].setOrientacion(OrientacionArista.NorEste);
                rpa[2].setPos(getX()-1, getY());
                rpa[2].setOrientacion(OrientacionArista.Este);
                break;
            case Sur:
                rpa[0].setPos(getX(), getY()+1);
                rpa[0].setOrientacion(OrientacionArista.Este);
                rpa[1].setPos(getX(), getY()+1);
                rpa[1].setOrientacion(OrientacionArista.NorEste);
                rpa[2].setPos(getX()+1, getY());
                rpa[2].setOrientacion(OrientacionArista.NorOeste);
                break;
        }
        return rpa;
    }
    
    /**
     * Devuelve el vertice que se obtiene avanzando por una arista
     * con la orientacion dada; 
     * Modifica el vertice actual y lo devuelve una vez modificado.
     */
    public PosicionVertice avanza(OrientacionArista oa) {
        normaliza();
        
        switch(o) {
            case Norte: switch (oa) {
                case Este: 
                    setPos(getX()-1, getY()-1); 
                    o = OrientacionVertice.Sur;
                    return this;
                case NorEste:
                    setPos(getX(), getY() -1);
                    o = OrientacionVertice.Sur;
                    return this;
                case NorOeste:
                    setPos(getX()-1, getY());
                    o = OrientacionVertice.Sur;
                    return this;
            }            
            
            case Sur: switch (oa) {
                case Este: 
                    setPos(getX()+1, getY()+1); 
                    o = OrientacionVertice.Norte;
                    return this;
                case SurEste:
                    setPos(getX(), getY() +1);
                    o = OrientacionVertice.Norte;
                    return this;
                case SurOeste:
                    setPos(getX()+1, getY());
                    o = OrientacionVertice.Norte;
                    return this;                
            }
        }
        
        throw new IllegalArgumentException("Error gordo");
    }
    
    public OrientacionVertice getOrientacion() {
        return o;
    }

    public void setOrientacion(OrientacionVertice o) {
        this.o = o;
    }        
    
    public boolean equals(Object otra) {
        return (otra instanceof PosicionVertice) && 
                ((PosicionVertice)otra).getX() == getX() &&
                ((PosicionVertice)otra).getY() == getY() &&
                ((PosicionVertice)otra).getOrientacion() == o;
    }
    
    public String toString() {
        return super.toString() + ":" + o;
    }    
}
