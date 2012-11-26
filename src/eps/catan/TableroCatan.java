package eps.catan;

import eps.Util;
import eps.cartas.Carta;
import eps.cartas.Mazo;

import eps.catan.BarajaRecursos.Recurso;
import eps.catan.Celda.Terreno;

import eps.catan.accion.AccionCambio;
import eps.catan.accion.AccionDados;
import eps.catan.accion.AccionDesarrollo;
import eps.catan.accion.AccionDescarte;
import eps.catan.accion.AccionFicha;
import eps.catan.accion.AccionFinTurno;
import eps.catan.accion.AccionRobo;
import eps.multij.Accion;
import eps.multij.Tablero;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.awt.Color;

/**
 * Un tablero del juego de Catan contiene un mapa y una serie de fichas.
 * Funciones del tablero son:
 * <ul>
 * <li> El tablero gestiona la colocacion de fichas, obedeciendo siempre
 * las reglas del juego. </li>
 * <li> El tablero puede tirar un dado y asignar recursos a los jugadores 
 * correspondientes tras una tirada. </li>
 * <li> Es posible leer y escribir los contenidos de un tablero de/a una cadena. </li>
 * <li> Un tablero puede calcular el camino mas largo de un jugador. </li>
 * </ul>
 */
public class TableroCatan extends Tablero {

    public static final int MIN_LADOS = 2;
    public static final int MAX_LADOS = 4;

    /** orientaciones de los lados en una rejilla hexagonal con 'norte' horizontal */
    public enum OrientacionArista {
        NorEste, Este, SurEste, SurOeste, Oeste, NorOeste
    };
    /** orientaciones de los vertices en una rejilla hexagonal con 'norte' horizontal */
    public  enum OrientacionVertice {
        Norte, NorEste, SurEste, Sur, SurOeste, NorOeste  
    };
    /** Fases de un juego de Catan (todas dentro de Tablero.Estado.EnCurso) */
    public enum Fase { 
        /** Jugadores colocan 1 casa y 1 camino gratuitos por turno, orden de juego */
        ColocacionAscendente, 
        /** Jugadores colocan 1 casa y 1 camino gratuitos por turno, orden inverso */
        ColocacionDescendente,
        /** Rondas normales */
        Normal, 
        /** Ronda de descarte, tras sacar un 7 */
        Descarte,
        /** Robo; la unica accion valida ahora es robar a alguien */
        Robo 
    };
    
    /** celdas del tablero; la celda (x, y) se encuentra en celda[y][x] */
    private Celda[][] celdas;
    
    /** dimensiones del tablero (dim x dim) */
    private int dim;
    
    /** para cortar esquinas del tablero (minSumaCoords <= x+y <= 2*dim-2*minSumaCoords) */
    private int minSumaCoords;
    
    /** lado, bando o faccion; cada cual dispone de fichas y mazos de cartas */
    private ArrayList<Lado> lados = new ArrayList<Lado>();

    /** mazos con cartas de cada recurso */
    private TreeMap<Recurso, Mazo> cartasRecurso;
    
    /** mazo con cartas de desarrollo */
    private Mazo cartasDesarrollo;

    /** posicion -> ficha en juego; incluye todos los sinonimos de cada posicion */
    private HashMap<Posicion,Ficha> fichaEnPos  = new HashMap<Posicion,Ficha>();
    
    /** posicion del ladron */
    private Posicion posLadron;

    /** puntos de victoria */
    private int puntosVictoria = 10;
    /** fase de juego en la que se esta */
    private Fase fase;
    /** numero de vueltas que ha dado la partida */
    private int ronda;
    /** si el jugador que tiene el turno ha tirado, true */
    private boolean dadoTirado;
    /** si el jugador que tiene el turno ha jugado un desarrollo, true */
    private boolean desarrolloUsado;
    /** privado; usado para guardar el turno antes de una fase de descartes */
    private int viejoTurno;
    /** colores por defecto */
    private Color colores[] = {
        Color.red.brighter(), Color.white,
        Color.orange, Color.blue
    };

    public boolean equals(Object o) {
        return toString().equals(o.toString());
    }
    
    /**
     * Inicializa un tablero de forma aleatoria, siguiendo las reglas del juego
     * Catan en su version basica. Cambiando esto se podrian implementar otras
     * versiones del juego.
     */
    public TableroCatan(int nLados) {
        this(nLados, 7, 3);
        BarajaRecursos br = (BarajaRecursos) BarajaRecursos.getInstance();

        // inicializa los jugadores con los caminos, poblados y ciudades por defecto
        for (int i = 0; i < nLados; i++) {
            lados.add(new Lado(15, 5, 4, colores[i]));
        }

        // prepara los mazos de cartas del tablero con su contenido inicial
        Mazo m = br.generaMazo();
        for (Carta c : m) {
            cartasRecurso.get(BarajaRecursos.tipoParaCarta(c)).add(c);
        }
        cartasDesarrollo = BarajaDesarrollo.getInstance().generaMazo();
        cartasDesarrollo.mezcla();

        // lista barajada de tiradas para celdas terrestres (el desierto se queda sin)
        ArrayList<Integer> tiradas = new ArrayList<Integer>();
        for (int i : new int[]{8, 8, 9, 9, 10, 10, 11, 11, 12, 6, 6, 5, 5, 4, 4, 3, 3, 2}) {
            tiradas.add(i);
        }
        Collections.shuffle(tiradas, Util.getRandom());

        // lista barajada de recursos para celdas con recursos
        Mazo recursos = new Mazo(br);
        for (int i = 0; i < 3; i++) {
            recursos.add(BarajaRecursos.cartaParaTipo(Recurso.Ladrillo));
            recursos.add(BarajaRecursos.cartaParaTipo(Recurso.Roca));
        }
        for (int i = 0; i < 4; i++) {
            recursos.add(BarajaRecursos.cartaParaTipo(Recurso.Madera));
            recursos.add(BarajaRecursos.cartaParaTipo(Recurso.Oveja));
            recursos.add(BarajaRecursos.cartaParaTipo(Recurso.Trigo));
        }
        recursos.mezcla();

        // crea celdas terrestres con recurso, añade desierto, y las baraja
        ArrayList<Celda> terrestres = new ArrayList<Celda>();
        for (int i = 0; i < 18; i++) {
            Recurso r = Recurso.valueOf(recursos.sacaPrimera().getNombre());
            terrestres.add(new Celda(r, tiradas.remove(0)));
        }
        terrestres.add(new Celda(null, 0));
        Collections.shuffle(terrestres);

        // coloca celdas terrestres
        int posTerrestres[][] =
                {{1, 3}, {2, 2}, {3, 1}, {1, 4}, {2, 3}, {3, 2}, {4, 1}, {1, 5}, {2, 4}, {3, 3},
            {4, 2}, {5, 1}, {2, 5}, {3, 4}, {4, 3}, {5, 2}, {3, 5}, {4, 4}, {5, 3}};
        for (int i = 0; i < posTerrestres.length; i++) {
            Celda c = terrestres.remove(0);
            Posicion p = new Posicion(posTerrestres[i][0], posTerrestres[i][1]);
            if (c.getTerreno() == Terreno.Desierto) {
                posLadron = p;
            }
            celdas[p.getY()][p.getX()] = c;
            c.setPosicion(this, p);
        }

        // crea celdas marinas con puertos, y las baraja bien
        ArrayList<Celda> marinasConPuerto = new ArrayList<Celda>();
        for (Recurso r : Recurso.values()) {
            marinasConPuerto.add(new Celda(null, 0, 2, r));
        }
        for (int i = 0; i < 4; i++) {
            marinasConPuerto.add(new Celda(null, 0, 3, null));
        }
        Collections.shuffle(marinasConPuerto);

        // añade celdas marinas sin puerto
        ArrayList<Celda> marinasSinPuerto = new ArrayList<Celda>();
        for (int i = 0; i < 9; i++) {
            marinasSinPuerto.add(new Celda(null, 0, 4, null));
        }

        // coloca las celdas marinas
        int posMarinas[][] =
                {{3, 0}, {4, 0}, {5, 0}, {6, 0}, {6, 1}, {6, 2}, {6, 3}, {5, 4}, {4, 5},
            {3, 6}, {2, 6}, {1, 6}, {0, 6}, {0, 5}, {0, 4}, {0, 3}, {1, 2}, {2, 1}};
        for (int i = 0; i < posMarinas.length; i++) {
            Celda c = (i % 2 == 0) ? marinasConPuerto.remove(0) : marinasSinPuerto.remove(0);
            celdas[posMarinas[i][1]][posMarinas[i][0]] = c;
            c.setPosicion(this, new Posicion(posMarinas[i][0], posMarinas[i][1]));
        }
    }

    /**
     * Primera parte de la inicializacion del tablero; deja mucho por hacer
     */
    private TableroCatan(int nLados, int dim, int minSumaCoords) {
        super(nLados);
        if ((dim % 2) == 0) {
            throw new IllegalArgumentException(
                    "Los tableros deben ser de NxN, con N impar; " + dim + " es par.");
        }
        this.minSumaCoords = minSumaCoords;

        // inicializa los mazos de recursos (uno por tipo)
        cartasRecurso = new TreeMap<Recurso, Mazo>();
        for (Recurso r : Recurso.values()) {
            cartasRecurso.put(r, new Mazo(BarajaRecursos.getInstance()));
        }

        // dimensiones y tablero
        this.dim = dim;
        this.celdas = new Celda[dim][dim];

        // fichas (en un comienzo, ninguna)
        this.fichaEnPos.clear();

        // pone el estado y la fase iniciales
        this.estado = Estado.EnCurso;
        this.fase = Fase.ColocacionAscendente;
    }

    /**
     * Pasa de coordenadas en representacion compacta (celdas) a coordenadas en 
     * pantalla. En pantalla el '8' queda en (0,3), el '3' en (1,2), y 
     * el '4' en (2,3)
     * <pre>
     * compacta:         pantalla: 
     * _ _ _ a b c d          p   j   e   a
     * _ _ e f g h i        w   q   k   f   b
     * _ j k l m n o      3   x   r   l   g   c
     * p q r s t u v    8   4   y   s   m   h   d
     * w x y z 1 2 _      9   5   z   t   n   i 
     * 3 4 5 6 7 _ _        0   6   1   u   o 
     * 8 9 0 * _ _ _          *   7   2   v
     * </pre>
     *
     * @return rv, que ha sido inicializado al valor del punto-hexagonal 
     *  correspondiente; p puede ser igual que rp sin efectos adversos.
     */
    public Posicion compactoAHexagonal(Posicion p, Posicion rp) {
        rp = (rp == null) ? new Posicion() : rp;
        rp.setPos((dim - 1) - (p.getY() - p.getX()), p.getX() + p.getY() - minSumaCoords);
        return rp;
    }

    /**
     * @return true si p esta dentro del mapa (y tiene una celda asociada), 
     *  false si no
     */
    public boolean dentro(Posicion p) {
        return p.getX() >= 0 && p.getY() >= 0 &&
                p.getX() < dim && p.getY() < dim &&
                celdas[p.getY()][p.getX()] != null;
    }

    /**
     * @return la celda que hay en ese punto, o null si no hay ninguna
     * (es posible asegurarse de que la hay usando 'dentro' antes de llamar
     * a esta funcion)
     */
    public Celda getCelda(Posicion p) {
        return dentro(p) ? celdas[p.getY()][p.getX()] : null;
    }

    /**
     * Devuelve los vecinos de un punto, usando una rejilla hexagonal
     * @return los puntos pedidos (pueden caer fuera del tablero)
     */
    public Posicion[] getPuntosVecinos(Posicion p, Posicion vecinos[]) {

        // inicializacion
        vecinos = (vecinos == null) ? new Posicion[6] : vecinos;
        for (int i = 0; i < vecinos.length; i++) {
            vecinos[i] = (vecinos[i] == null) ? new Posicion() : vecinos[i];
        }

        int delta[][] = {{-1, -1}, {-1, 0}, {+1, 0}, {+1, +1}, {0, +1}, {0, -1}};
        for (int i = 0; i < vecinos.length; i++) {
            vecinos[i].setPos(p.getX() + delta[i][0], p.getY() + delta[i][1]);
        }
        return vecinos;
    }

    /**
     * Devuelve el punto vecino desde un punto de partida 
     * cuando se cruza una arista en un sentido dado 
     * (puede caer fuera del tablero)
     */
    public Posicion getVecino(Posicion p, OrientacionArista o, Posicion rp) {
        rp = (rp == null) ? new Posicion(p) : rp;
        switch (o) {
            case NorEste:
                rp.setPos(p.getX(), p.getY() - 1);
                break;
            case Este:
                rp.setPos(p.getX() + 1, p.getY() - 1);
                break;
            case SurEste:
                rp.setPos(p.getX() + 1, p.getY());
                break;
            case SurOeste:
                rp.setPos(p.getX(), p.getY() + 1);
                break;
            case Oeste:
                rp.setPos(p.getX() - 1, p.getY() + 1);
                break;
            case NorOeste:
                rp.setPos(p.getX() - 1, p.getY());
                break;
        }
        return rp;
    }

    /**
     * Devuelve una cadena con el contenido de un tablero, usando ASCII
     * El formato es el mismo que espera "inicializa"
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
// TEMPLATE_START  
        // almacena el tam. y el num. de jugadores
        sb.append(
                "Ronda: " + ronda + " Fase: " + fase +
                " Turno: " + turno + " Lados: " + lados.size() + "\n");

        // muestra mazos de recurso
        for (Mazo m : cartasRecurso.values()) {
            sb.append(m.toString() + "\n");
        }

        // y de otars cosas
        sb.append(cartasDesarrollo + "\n");

        // genera una representacion en memoria
        int h = 3;
        int w = 6;
        char c[][] = new char[h * dim][w * dim];
        for (int i = 0; i < h * dim; i++) {
            for (int j = 0; j < w * dim; j++) {
                c[i][j] = ' ';
            }
        }
        Posicion p = new Posicion();
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                p.setPos(j, i);
                if (dentro(p)) {
                    getCelda(p).dibujaEnAscii(this, c);
                }
            }
        }
        // pasa la repre. en memoria a cadena, linea a linea
        for (int i = 0; i < h * dim; i++) {
            for (int j = 0; j < w * dim; j++) {
                sb.append(c[i][j]);
            }
            sb.append("\n");
        }

        // el ladron
        sb.append("Ladron: " + posLadron + "\n");

        // los lados
        for (Lado l : lados) {
            sb.append("" + l);
        }
// TEMPLATE_ELSE
        // Insertar el código desarrollado en la Práctica 1
// TEMPLATE_END        
        return sb.toString();
    }

    /**
     * Carga el estado de un tablero contenido en una cadena
     */
    public void inicializa(String s) {
        // lee ronda, fase, turno y numero de lados
        StringTokenizer st = new StringTokenizer(s, "\n");
        String l = st.nextToken();
        StringTokenizer st2 = new StringTokenizer(l, " ");
        String sub = st2.nextToken();
        sub = st2.nextToken();
        ronda = Integer.parseInt(sub);
        sub = st2.nextToken();
        sub = st2.nextToken();
        fase = Fase.valueOf(sub);
        sub = st2.nextToken();
        sub = st2.nextToken();
        turno = Integer.parseInt(sub);
        sub = st2.nextToken();
        sub = st2.nextToken();
        int numLados = Integer.parseInt(sub);
        lados.clear();
        for (int i = 0; i < numLados; i++) {
            lados.add(new Lado(15, 5, 4, colores[i]));
        }

        // carga los recursos
        for (Recurso r : Recurso.values()) {
            l = st.nextToken();
            cartasRecurso.put(r, new Mazo(l, BarajaRecursos.getInstance()));
        }
        
        // y las cartas de desarrollo
        l = st.nextToken();
        cartasDesarrollo = new Mazo(l, BarajaDesarrollo.getInstance());
        
        // carga el tablero en si
        int h = 3;
        int w = 6;
        char c[][] = new char[h * dim][w * dim];
        for (int i = 0; i < h * dim; i++) {
            l = st.nextToken();
            for (int j = 0; j < w * dim; j++) {
                c[i][j] = l.charAt(j);
            }
        }
        Posicion p = new Posicion();
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                p.setPos(j, i);
                if (dentro(p)) {
                    celdas[i][j] = new Celda(this, new Posicion(p), c);
                }
            }
        }

        // el ladron
        l = st.nextToken();        
        posLadron = new Posicion(l.substring(l.indexOf(":") + 1));

        // los lados: lados.get(i).inicializa(s);
        int numLado = 0;
        for (Lado lado : lados) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 9; i++) {
                sb.append(st.nextToken() + "\n");
            }
            lado.inicializa(sb.toString(), this, numLado++);
        }
    }

    public int getDim() {
        return dim;
    }

    public Lado getLado(int numLado) {
        return lados.get(numLado);
    }

    /**
     * devuelve la ficha encontrada en la posicion dada, si la hay
     */
    public Ficha getFichaEnPos(Posicion p) {
        return fichaEnPos.get(p);
    }

    /**
     * coloca una ficha en la posicion dada (y en todos sus sinonimos)
     * No verifica legalidad ni nada similar.
     */
    public void setFichaEnPos(Ficha f, Posicion p) {
        if (p instanceof PosicionArista) {
            for (PosicionArista pa : ((PosicionArista) p).sinonimos(null)) {
                fichaEnPos.put(pa, f);
            }
        }
        if (p instanceof PosicionVertice) {
            for (PosicionVertice pv : ((PosicionVertice) p).sinonimos(null)) {
                fichaEnPos.put(pv, f);
            }
        }
        f.setPosicion(p, this);
        getLado(f.getNumLado()).usaFicha(f);
    }

    /**
     * Hace frente a un resultado de los dados
     */
    public void resuelveTirada(int tirada) {
        dadoTirado = true;
        if (tirada == 7) {
            viejoTurno = turno;
            turno = 0;
            fase = Fase.Descarte;
            if (getLado(0).getRecursos().size() <= 7) {
                cambiaTurno();
            }
        } else {
            reparteRecursos(tirada);
        }
    }

    /**
     * Actualiza los recursos de los jugadores en base a una tirada de dado
     */
    private void reparteRecursos(int tirada) {
        TreeMap<Recurso, Integer> necesarios = new TreeMap<Recurso, Integer>();
        for (Recurso r : Recurso.values()) {
            necesarios.put(r, 0);
        }

        Posicion p = new Posicion();

        // encuentra celdas que producen recursos, y cuenta los necesarios
        ArrayList<Celda> producen = new ArrayList<Celda>();
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                p.setPos(j, i);
                Celda c = getCelda(p);
                if (c != null && c.getTirada() == tirada && !p.equals(posLadron)) {
                    producen.add(c);
                    for (OrientacionVertice ov : OrientacionVertice.values()) {
                        Ficha f = getFichaEnPos(new PosicionVertice(p, ov));
                        if (f != null) {
                            necesarios.put(c.getRecurso(),
                                    necesarios.get(c.getRecurso()) +
                                    (f instanceof Ciudad ? 2 : 1));
                        }
                    }
                }
            }
        }

        // si no hay suficiente de algun recurso, lo pone a cero
        for (Recurso r : Recurso.values()) {
            if (necesarios.get(r) > cartasRecurso.get(r).size()) {
                necesarios.remove(r);
            }
        }

        // reparte recursos
        for (Celda c : producen) {
            p = c.getPosicion();
            if (necesarios.containsKey(c.getRecurso())) {
                for (OrientacionVertice ov : OrientacionVertice.values()) {
                    Ficha f = getFichaEnPos(new PosicionVertice(p, ov));
                    if (f != null) {
                        for (int k = 0; k < (f instanceof Ciudad ? 2 : 1); k++) {
                            getLado(f.getNumLado()).getRecursos().add(
                                    cartasRecurso.get(c.getRecurso()).sacaPrimera());
                        }
                    }
                }
            }
        }
    }

    public Posicion getPosLadron() {
        return posLadron;
    }

    /**
     * Coloca el ladron y roba a un vecino; si no hay vecino en la dir. 
     * indicada, no roba a nadie
     * @param posLadron
     */
    public void setPosLadron(Posicion posLadron, PosicionVertice pv) {
        if (!dentro(posLadron) || getCelda(posLadron).getTerreno() == Terreno.Oceano) {
            throw new IllegalArgumentException("No puedes poner el ladron en el oceano");
        }
        Ficha f = getFichaEnPos(pv);
        if (f != null && f.getNumLado() != turno) {
            Lado robador = getLado(turno);
            Lado robado = getLado(f.getNumLado());
            if (!robado.getRecursos().isEmpty()) {
                robado.getRecursos().mezcla();
                robador.getRecursos().add(robado.getRecursos().sacaPrimera());
            }
        }
        this.posLadron = posLadron;
        return;
    }

    public Mazo getCartasRecurso(Recurso r) {
        return cartasRecurso.get(r);
    }

    public Mazo getCartasDesarrollo() {
        return cartasDesarrollo;
    }

    public int getPuntosVictoria() {
        return puntosVictoria;
    }

    public Fase getFase() {
        return fase;
    }

    public int getRonda() {
        return ronda;
    }

    public boolean isDesarrolloUsado() {
        return desarrolloUsado;
    }

    public boolean isDadoTirado() {
        return dadoTirado;
    }

    public void setDesarrolloUsado(boolean b) {
        desarrolloUsado = b;
    }

    /**
     * Actualiza las medallas de los jugadores; debe llamarse
     * cada vez que se cambia algo que pueda afectarlas, es decir
     * a) se construye una nueva carretera o 
     * b) alguien juega un soldado
     */
    public void actualizaMedallas() {
        for (Lado l : lados) {
            l.getMedallas().clear();
        }
        // caminos
        int max = 0;
        Lado ganador = null;
        for (Lado l : lados) {
            int pasos = l.getCaminoMasLargo();
            if (pasos > max) {
                ganador = l;
                max = pasos;
            }
        }
        if (max >= 5) {
            ganador.getMedallas().add(Lado.Medalla.CaminoMasLargo);
        }
        // soldados
        max = 0;
        for (Lado l : lados) {
            if (l.getSoldadosUsados() > max) {
                ganador = l;
                max = l.getSoldadosUsados();
            }
        }
        if (max >= 3) {
            ganador.getMedallas().add(Lado.Medalla.EjercitoMasGrande);
        }
    }

    /**
     * Cambia la fase, sin modificar el turno. Util para activar el ladron
     * (usando fase = Robo)
     * @param fase
     */
    public void setFaseRobo() {
        fase = Fase.Robo;
    }

    /**
     * hace que, ademas de avanzar el turno, se avance la ronda
     */
    public void cambiaTurno() {
        switch (fase) {
            case ColocacionAscendente:
                if (turno == lados.size() - 1) {
                    fase = Fase.ColocacionDescendente;
                // no cambia el turno ni avanza el indice de ronda
                } else {
                    turno++;
                }
                break;
            case ColocacionDescendente:
                if (turno == 0) {
                    fase = Fase.Normal;
                // no cambia el turno ni avanza el indice de ronda
                } else {
                    turno = turno - 1;
                }
                break;

            case Normal:
                if (getLado(turno).getPuntos() >= puntosVictoria) {
                    estado = Estado.Finalizado;
                    return;
                }
                if (turno == lados.size() - 1) {
                    ronda++;
                    turno = 0;
                } else {
                    turno++;
                }
                dadoTirado = false;
                desarrolloUsado = false;
                break;

            case Descarte:
                while (turno < lados.size() - 1 && 
                        getLado(turno).getRecursos().size() <= 7) {
                    turno ++;
                }
                if (turno == lados.size() - 1) {
                    fase = Fase.Robo;
                    turno = viejoTurno;
                }
                break;

            case Robo:
                // fin de robo: no cambia el turno ni nada
                fase = fase.Normal;
                break;
        }
    }

    /**
     * Genera una lista de acciones validas para el jugador actual.
     * Las acciones generadas solo incluyen cosas del tipo AccionCambia, 
     * AccionFicha y AccionDesarrollo - las AccionComercio *no* se generan
     * automaticamente (porque todas son inicialmente validas, y habria muchas).
     * 
     * @param nJugador
     * @return
     */
    public ArrayList<Accion> accionesValidas(int nJugador) {
        ArrayList<Accion> al = new ArrayList<Accion>();
        if (turno != nJugador || estado != Estado.EnCurso) {
            return al;
        }
        switch (fase) {
            case ColocacionAscendente:
            case ColocacionDescendente:
                AccionFicha.genera(this, al);
                break;
            case Descarte:
                System.err.println("Antes de generar descartes: " + al.size());
                AccionDescarte.genera(this, al);
                System.err.println("Tras la gen. de acciones descarte: " + al.size());
                break;
            case Robo:
                System.err.println("Antes del robo: " + al.size());
                AccionRobo.genera(this, al);
                System.err.println("Tras la gen. de acciones robo: " + al.size());
                break;
            case Normal:
                if (!dadoTirado && !desarrolloUsado) {
                    AccionDesarrollo.genera(this, al);
                }
                if (!dadoTirado) {
                    al.add(new AccionDados(nJugador));
                } else {
                    AccionFicha.genera(this, al);
                    AccionCambio.genera(this, al);
                    AccionDesarrollo.genera(this, al);
                    al.add(new AccionFinTurno(turno));
                }
                break;
        }
        return al;
    }
}
