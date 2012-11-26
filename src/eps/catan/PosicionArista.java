/*
 * Posicion.java
 *
 * Created on January 14, 2008, 2:20 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eps.catan;
import eps.catan.TableroCatan.OrientacionArista;
import eps.catan.TableroCatan.OrientacionVertice;

/**
 * Una posicion de arista en el juego de Catan
 */
public class PosicionArista extends Posicion {

    private OrientacionArista o;

    public static final OrientacionArista OCanonicas[] = new OrientacionArista[] {
        OrientacionArista.NorOeste, OrientacionArista.NorEste, OrientacionArista.Este
    };
    
    /** Creates a new instance of Posicion */
    public PosicionArista() {
        this(new Posicion(), OrientacionArista.Este);
    }

    public PosicionArista(PosicionArista pa) {
        this(pa, pa.getOrientacion());
    }
    
    public PosicionArista(Posicion p, OrientacionArista o) {
        super(p);
        this.o = o;
    }
    
    public PosicionArista(String s) {
        super(s);
        this.o = OrientacionArista.valueOf(s.substring(s.indexOf(':')+1));
    }
    
    /**
     * Hay 2 posiciones equivalentes para cada arista:
     * Opcion 1: (x, y):NO, (x-1, y):SE
     * Opcion 3: (x, y):NE  (x, y-1):SO
     * Opcion 2: (x, y):E,  (x+1, y-1):O
     * Esta funcion se asegura de que se normaliza para usar la primera variante
     */
    public void normaliza() {
        switch (o) {
            case SurEste:
                setPos(getX()+1, getY());
                o = OrientacionArista.NorOeste;
                break;
            case SurOeste: 
                setPos(getX(), getY()+1);
                o = OrientacionArista.NorEste;
                break;
            case Oeste: 
                setPos(getX()-1, getY()+1);
                o = OrientacionArista.Este;
                break;
        }
    }
    
    /**
     * devuelve los puntos que se encuentran a ambos
     * lados de la arista suministrada (incluye la propia arista)
     */
    public PosicionArista[] sinonimos(PosicionArista[] rpa) {

        // inicializacion
        rpa = (rpa == null) ? new PosicionArista[2] : rpa;
        for (int i=0; i<rpa.length; i++) {
            rpa[i] = (rpa[i] == null) ? new PosicionArista() : rpa[i];
        }
        normaliza();
        
        rpa[0].setPos(getX(), getY());
        rpa[0].setOrientacion(o);        
        switch (rpa[0].getOrientacion()) {
            case Este:
                rpa[1].setPos(getX()+1, getY()-1);
                rpa[1].setOrientacion(OrientacionArista.Oeste);
                break;
            case NorEste:
                rpa[1].setPos(getX(), getY()-1);
                rpa[1].setOrientacion(OrientacionArista.SurOeste);
                break;                
            case NorOeste:
                rpa[1].setPos(getX()-1, getY());
                rpa[1].setOrientacion(OrientacionArista.SurEste);
                break;                
        }
        return rpa;
    }        
    
    /**
     * Devuelve los 2 vertices que limitan con esta arista
     */
    public PosicionVertice[] getVertices(PosicionVertice rpv[]) {
        
        // inicializacion
        rpv = (rpv == null) ? new PosicionVertice[2] : rpv;
        for (int i=0; i<rpv.length; i++) {
            rpv[i] = (rpv[i] == null) ? new PosicionVertice() : rpv[i];
        }
        normaliza();
                
        switch(getOrientacion()) {
            case Este:
                rpv[0].setPos(getX(), getY()-1);
                rpv[0].setOrientacion(OrientacionVertice.Sur);
                rpv[1].setPos(getX()+1, getY());
                rpv[1].setOrientacion(OrientacionVertice.Norte);
                break;
            case NorEste:
                rpv[0].setPos(getX(), getY());
                rpv[0].setOrientacion(OrientacionVertice.Norte);
                rpv[1].setPos(getX(), getY()-1);
                rpv[1].setOrientacion(OrientacionVertice.Sur);
                break;                
            case NorOeste:
                rpv[0].setPos(getX(), getY());
                rpv[0].setOrientacion(OrientacionVertice.Norte);
                rpv[1].setPos(getX()-1, getY());
                rpv[1].setOrientacion(OrientacionVertice.Sur);
                break;                
        }
        return rpv;
    }
    
    public OrientacionArista getOrientacion() {
        return o;
    }

    public void setOrientacion(OrientacionArista o) {
        this.o = o;
    }        
    
    public boolean equals(Object otra) {
        return (otra instanceof PosicionArista) && 
                ((PosicionArista)otra).getX() == getX() &&
                ((PosicionArista)otra).getY() == getY() &&
                ((PosicionArista)otra).getOrientacion() == o;
    }
    
    public String toString() {
        return super.toString() + ":" + o;
    }        
}
