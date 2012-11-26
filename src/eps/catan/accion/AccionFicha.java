/*
 * AccionMovimiento.java
 *
 * Created on March 3, 2007, 1:10 PM
 *
 */

package eps.catan.accion;

import eps.cartas.Carta;
import eps.catan.BarajaRecursos;
import eps.catan.Camino;
import eps.catan.Ciudad;
import eps.catan.Ficha;
import eps.catan.Lado;
import eps.catan.Poblado;
import eps.catan.Posicion;
import eps.catan.PosicionArista;
import eps.catan.PosicionVertice;
import eps.catan.TableroCatan;
import eps.catan.TableroCatan.Fase;
import eps.catan.TableroCatan.OrientacionArista;
import eps.catan.TableroCatan.OrientacionVertice;
import eps.multij.Accion;
import eps.multij.Juego;
import java.util.ArrayList;

/**
 * Coloca una ficha en una posicion; puede implicar o no adquisicion.
 */
public class AccionFicha extends Accion {
        
    private boolean compra;
    private Ficha f;
    private Posicion p;
    
    /**
     * Creates a new instance of AccionColoca
     */
    public AccionFicha(int origen, boolean compra, Ficha f, Posicion p) {
        super(origen);
        this.compra = compra;
        this.f = f;
        this.p = p;
    }
    
    /**
     * coloca una ficha de un jugador en una posicion; actualiza el jugador,
     * la ficha, etcetera. Asume que el jugador puede adquirir la ficha especificada.
     */
    public boolean ejecuta(Juego j) {
        TableroCatan t = (TableroCatan)j.getTablero();
        Lado l = t.getLado(origen);
        String nf = f.getClass().getSimpleName();
        
        // verifica momento correcto
        if ( origen != f.getNumLado() || t.getTurno() != origen ||
                (t.getFase() == Fase.Normal && ! t.isDadoTirado()) ||
                t.getFase() == Fase.Descarte) {
            resultado = "en este momento no puedes construir";
            return false;
        }
        
        // verifica posicion legal
        if ( ! f.puedeColocar(t, p)) {
            resultado = "posicion para ficha "+nf+" ilegal";
            return false;
        }       
        
        // compra - si debe y puede
        if (compra && ! l.getRecursos().puedeSacar(f.getCoste())) {
            resultado = "Te faltan recursos para comprar ";
            return false;
        }
        
        // verifica disponibilidad 
        if (l.getNoUsadas(f.getClass()) == 0) {
            resultado = "No te quedan "+nf+" que colocar";
            return false;
        }
        if (compra) {
            l.getRecursos().saca(f.getCoste());
            for (Carta c : f.getCoste()) {
                t.getCartasRecurso(BarajaRecursos.tipoParaCarta(c)).add(c);
            }
        }
        
        // coloca y actualiza lado y puertos
        t.setFichaEnPos(f, p);
        
        // si estamos en fase de colocacion y es un camino, avanza turno
        if ((t.getFase() == Fase.ColocacionAscendente || 
             t.getFase() == Fase.ColocacionDescendente) && 
            f instanceof Camino) {

            t.cambiaTurno();
        }        

        resultado = "colocado "+nf+" en "+p;
        return true;
    }
    
    /**
     * Genera acciones de ficha validas para el jugador actual en el
     * estado de juego actual.
     * @return nada, y 'al' actualizado.
     */
    public static void genera(TableroCatan t, ArrayList<Accion> al) {
//        System.err.println("Generando ("+t+")");
        Fase f = t.getFase();
        if (f == Fase.Normal) {
            generaAsentamientos(t, al, t.getTurno(), true);
            generaCaminos(t, al, t.getTurno(), true);
        }
        else if (f == Fase.ColocacionAscendente || 
                 f == Fase.ColocacionDescendente) {
            Lado l = t.getLado(t.getTurno());
            if (l.getUsadas(Poblado.class).size() == 
                    (f == Fase.ColocacionAscendente ? 0 : 1)) {
                generaAsentamientos(t, al, t.getTurno(), false);                    
            }
            else {
                generaCaminos(t, al, t.getTurno(), false);
            }
        }
    }    

    public static void generaCaminos(TableroCatan t, ArrayList<Accion> al, 
            int numLado, boolean compra) {
        Camino ca = new Camino(numLado);
        Lado l = t.getLado(numLado);
        
        PosicionArista p = new PosicionArista();
        if ( ! compra || l.puedeComprar(ca)) {
            for (int i=0; i<t.getDim(); i++) {
                for (int j=0; j<t.getDim(); j++) {
                    p.setPos(j,i);
                    for (OrientacionArista oa : PosicionArista.OCanonicas) {
                        p.setOrientacion(oa);
                        if (ca.puedeColocar(t, p)) {
                            al.add(new AccionFicha(numLado, compra, ca, 
                                    new PosicionArista(p)));
                        }
                    }
                }
            }
        }        
    }
    
    public static void generaAsentamientos(TableroCatan t, ArrayList<Accion> al, 
            int numLado, boolean compra) {
        Lado l = t.getLado(numLado);
        Poblado po = new Poblado(numLado);
        Ciudad ci = new Ciudad(numLado);
        if ( compra && ! l.puedeComprar(po)) po = null;
        if ( ! compra || (compra && ! l.puedeComprar(ci))) ci = null;
        if (ci != null || po != null) {
            PosicionVertice p = new PosicionVertice();
            for (int i=0; i<t.getDim(); i++) {
                for (int j=0; j<t.getDim(); j++) {
                    p.setPos(j,i);
                    for (OrientacionVertice ov : PosicionVertice.OCanonicas) {
                        p.setOrientacion(ov);
                        if (ci != null && ci.puedeColocar(t, p)) {
                            al.add(new AccionFicha(numLado, compra, ci, 
                                    new PosicionVertice(p)));
                        }
                        if (po != null && po.puedeColocar(t, p)) {
                            al.add(new AccionFicha(numLado, compra, po, 
                                    new PosicionVertice(p)));
                        }
                    }
                }
            }
        }
    }
    
    public Ficha getFicha() {
        return f;
    }
    
    public String toString() {
        String nf = f.getClass().getSimpleName();
        return (compra?"compra y ":"") + "coloca "+nf+" en  "+p;
    }        
}
