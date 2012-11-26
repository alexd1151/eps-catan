/*
 * AccionMovimiento.java
 *
 * Created on March 3, 2007, 1:10 PM
 *
 */

package eps.catan.accion;

import eps.cartas.Carta;
import eps.cartas.Mazo;
import eps.catan.*;
import eps.catan.BarajaDesarrollo;
import eps.catan.BarajaDesarrollo.Desarrollo;
import eps.catan.BarajaRecursos.Recurso;
import eps.catan.TableroCatan.OrientacionArista;
import eps.multij.Accion;
import eps.multij.Juego;
import java.util.ArrayList;

/**
 * La accion de comprar o jugarse una carta de desarrollo. 
 * Los efectos dependen de si se esta comprando o usando carta, y de la carta.
 * - Compra: pierde 1 trigo, 1 oveja, 1 roca, gana 1 carta desarrollo
 * - Abundancia: elige 2 recursos y llevate sus cartas
 * - Camino: coloca 2 caminos
 * - Monopolio: elige 1 recurso, llevate todas las cartas de ese recurso
 * - Punto: inaplicable; se suma al total secreto de puntos
 * - Soldado: roba
 */
public abstract class AccionDesarrollo extends Accion {
    
    /**
     * Creates a new instance of AccionDesarrollo
     */
    public AccionDesarrollo(int origen) {
        super(origen);
    }        
    
    /**
     * intenta jugar un desarrollo, verificando condiciones de
     * juego (turno, dado tirado, desarrollos anteriores) y consmiendo
     * la carta necesaria.
     * @return true si se consigue sacar con exito
     */
    protected boolean sacaDesarrollo(Juego j, Desarrollo d) {
        TableroCatan t = (TableroCatan)j.getTablero();
        Lado l = t.getLado(origen);
        if ( t.getTurno() != origen || t.isDadoTirado() || t.isDesarrolloUsado()) {
            j.notificaError("no es tu turno, " +
                    "ya has tirado el dado o " +
                    "ya te has jugado un desarrollo", this);
            return false;
        }
        BarajaDesarrollo b = BarajaDesarrollo.getInstance();
        Mazo m = new Mazo(b);
        m.add(BarajaDesarrollo.cartaParaTipo(d));
        if ( ! l.getDesarrollos().saca(m)) {
            j.notificaError("habrias necesitado un desarrollo de tipo "+d, this);
            return false;
        }
        t.getCartasDesarrollo().add(0, BarajaDesarrollo.cartaParaTipo(d));
        t.setDesarrolloUsado(true);
        return true;
    }
    
    /**
     * Genera todas las posibles acciones de desarrollo para el jugador actual
     * del tablero suministrado.
     * @param t
     * @param al
     */
    public static void genera(TableroCatan t, ArrayList<Accion> al) {
        // acciones de compra
        Compra.genera(t, al);
        // resto de acciones
        if ( ! t.isDesarrolloUsado() && ! t.isDadoTirado()) {
            Lado l = t.getLado(t.getTurno());
            for (Carta c : l.getDesarrollos()) {
                switch (BarajaDesarrollo.tipoParaCarta(c)) {
                    case Abundancia: Abundancia.genera(t, al); break;
                    case Caminos:    Caminos.genera(t, al); break;
                    case Monopolio:  Monopolio.genera(t, al); break;
                    case Soldado:    Soldado.genera(t, al); break;
                }
            }
        }
    }    
    
    /** Compra de carta */
    public static class Compra extends AccionDesarrollo {
        public static Mazo costeDesarrollo = 
            new Mazo("{o,r,t}", BarajaRecursos.getInstance());                
        public Compra(int origen) {
            super(origen);
        }
        public boolean ejecuta(Juego j) {
            TableroCatan t = (TableroCatan)j.getTablero();
            Lado l = t.getLado(origen);
            if ( ! l.getRecursos().saca(costeDesarrollo)) {
                resultado = "No te llega para comprar un desarrollo";
                return false;
            }
            
            for (Carta c : costeDesarrollo) {
                t.getCartasRecurso(BarajaRecursos.tipoParaCarta(c)).add(c);
            }            
            l.getNuevosDesarrollos().add(t.getCartasDesarrollo().sacaPrimera());
            resultado = "Carta de desarrollo comprada";
            return true;
        }
        public String toString() {
            return "compra de un desarrollo";
        }
        public static void genera(TableroCatan t, ArrayList<Accion> al) {
            Lado l = t.getLado(t.getTurno());
            if (l.getRecursos().puedeSacar(costeDesarrollo)) {
                al.add(new Compra(t.getTurno()));
            }
        }
    }
    
    /** Año de abundancia */
    public static class Abundancia extends AccionDesarrollo {
        private Recurso r1, r2;
        public Abundancia(int origen, Recurso r1, Recurso r2) {
            super(origen);
            this.r1 = r1; 
            this.r2 = r2;
        }        
        public boolean ejecuta(Juego j) {
            TableroCatan t = (TableroCatan)j.getTablero();
            Lado l = t.getLado(origen);
            if ( ! sacaDesarrollo(j, Desarrollo.Abundancia)) {
                resultado = "no tienes la carta necesaria, listillo";
                return false;
            }
            String s = "";
            if ( ! t.getCartasRecurso(r1).isEmpty()) {
                l.getRecursos().add(t.getCartasRecurso(r1).sacaPrimera());                
                s += r1.name();
            }
            if ( ! t.getCartasRecurso(r2).isEmpty()) {
                l.getRecursos().add(t.getCartasRecurso(r2).sacaPrimera());
                s += " " + r2.name();
            }
            resultado = "año de abundancia: "+s.trim();
            return true;
        }        
        public String toString() {
            return "Año de abundancia, solicitando "+r1+" y "+r2;
        }        
        public static void genera(TableroCatan t, ArrayList<Accion> al) {
            for (Recurso r1 : Recurso.values()) {
                if (t.getCartasRecurso(r1).isEmpty()) continue;
                for (Recurso r2 : Recurso.values()) {
                   if (t.getCartasRecurso(r1).isEmpty()) continue;
                   al.add(new Abundancia(t.getTurno(), r1, r2));
                }
            }
        }
    }    
    
    /** Caminos */
    public static class Caminos extends AccionDesarrollo {
        private PosicionArista a1, a2;
        public Caminos(int origen, PosicionArista a1, PosicionArista a2) {
            super(origen);
            this.a1 = a1;
            this.a2 = a2;
        }        
        public boolean ejecuta(Juego j) {            
            if ( ! sacaDesarrollo(j, Desarrollo.Caminos)) return false;
            int o = origen;
            boolean c1 = a1 == null ? false :
                (new AccionFicha(o, false, new Camino(o), a1)).ejecuta(j);
            boolean c2 = a2 == null ? false :
                (new AccionFicha(o, false, new Camino(o), a2)).ejecuta(j);
            resultado = "caminos construidos: "+a1+(c1?"bien":"mal")+", "+a2+
                    (c2?"bien":"mal");
            return true;
        }
        
        public String toString() {
            return "Caminos en " + ((a1!=null) ? 
                a1 + ((a2!=null) ? " y " + a2 : "") : 
                "ninguna parte");
        }        
        public static void genera(TableroCatan t, ArrayList<Accion> al) {

            Camino ca = new Camino(t.getTurno());
            int n = t.getLado(t.getTurno()).getMaxFichas(ca.getClass());
            if (n == 0) {
                al.add(new Caminos(t.getTurno(), null, null));
            }            
            PosicionArista p1 = new PosicionArista();
            PosicionArista p2 = new PosicionArista();
            for (int i=0; i<t.getDim(); i++) {
                for (int j=0; j<t.getDim(); j++) {
                    p1.setPos(j,i);
                    for (OrientacionArista oa : PosicionArista.OCanonicas) {
                        p1.setOrientacion(oa);
                        if (ca.puedeColocar(t, p1)) {
                            if (n == 1) { // puede colocar 1 camino
                                al.add(new Caminos(t.getTurno(), p1, null));
                            }
                            else {        // puede colocar 2 caminos
                                for (int i2=0; i2<t.getDim(); i2++) {
                                    for (int j2=0; j2<t.getDim(); j2++) {
                                        p2.setPos(j2,i2);
                                        for (OrientacionArista oa2 : PosicionArista.OCanonicas) {
                                            p2.setOrientacion(oa2);
                                            if (ca.puedeColocar(t, p1, p2)) {
                                                al.add(new Caminos(
                                                        t.getTurno(), 
                                                        new PosicionArista(p1), 
                                                        new PosicionArista(p2)));
                                            }
                                        }
                                    }
                                }                                
                            }
                        }
                    }
                }
            }
        }
    }    

    /** Monopolio */
    public static class Monopolio extends AccionDesarrollo {
        private Recurso r;
        public Monopolio(int origen, Recurso r) {
            super(origen);
            this.r = r;
        }        
        public boolean ejecuta(Juego j) {
            TableroCatan t = (TableroCatan)j.getTablero();
            if ( ! sacaDesarrollo(j, Desarrollo.Monopolio)) return false;            
            Mazo robadas = new Mazo(BarajaRecursos.getInstance());
            int total = 0;
            for (int i=0; i<t.getNumJugadores(); i++) {
                if (i != origen) {
                    Lado l = t.getLado(i);
                    for (Carta c : l.getRecursos()) {
                        if (BarajaRecursos.tipoParaCarta(c) == r) {
                            robadas.add(c);
                        }
                    }
                    if ( ! robadas.isEmpty()) {
                        l.getRecursos().saca(robadas);
                        t.getLado(i).getRecursos().addAll(robadas);
                        total += robadas.size();
                        robadas.clear();
                    }
                }
            }
            resultado = "monopolio de "+r+": "+total+" cartas robadas";
            return true;
        }        
        public String toString() {
            return "Monopolio de "+r;
        }   
        public static void genera(TableroCatan t, ArrayList<Accion> al) {
            for (Recurso r : Recurso.values()) {
                al.add(new Monopolio(t.getTurno(), r));
            }
        }        
    }    
    
    /** Soldado */
    public static class Soldado extends AccionDesarrollo {
        public Soldado(int origen) {
            super(origen);
        }        
        public boolean ejecuta(Juego j) {            
            TableroCatan t = (TableroCatan)j.getTablero();
            if ( ! sacaDesarrollo(j, Desarrollo.Soldado)) return false;            
            resultado = "soldado jugado";
            Lado l = t.getLado(t.getTurno());
            l.setSoldadosUsados(l.getSoldadosUsados()+1);
            t.setFaseRobo();            
            return true;
        }
        
        public String toString() {
            return "soldado mueve ladron";
        }        
        public static void genera(TableroCatan t, ArrayList<Accion> al) {
            al.add(new Soldado(t.getTurno()));
        }        
    }    
}
