/*
 * VisorMapa.java
 *
 * Created on January 11, 2008, 3:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package eps.catan.gui;

import eps.Util;
import eps.cartas.gui.InterfazMazo;
import eps.catan.BarajaRecursos.Recurso;
import eps.catan.Celda;
import eps.catan.Celda.Terreno;
import eps.catan.Ciudad;
import eps.catan.Ficha;
import eps.catan.Posicion;
import eps.catan.PosicionArista;
import eps.catan.PosicionVertice;
import eps.catan.TableroCatan;
import eps.catan.TableroCatan.OrientacionArista;
import eps.catan.TableroCatan.OrientacionVertice;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JSplitPane;
import javax.swing.Timer;

/**
 * Componente grafica que muestra un tablero de Catan.
 */
public class VisorMapa extends JLayeredPane {

    private TableroCatan t;
    private LayerTablero layerTablero = new LayerTablero();
    private LayerRealce layerRealce = new LayerRealce();
    private JLabel labelTexto = new JLabel();
    private static HashMap<String, Image> imagenes = new HashMap<String, Image>();
    private static Font fuenteTirada = null;
    private static int cellW = 214 - 1;
    private static int cellH = 288 - 1;
    private static int cellHO = 73;
    private static int resW = 100;
    private static int resH = 100;
    private Dimension oldDims = new Dimension();
    private AffineTransform transform;
    private ArrayList<MapaListener> listeners = new ArrayList<MapaListener>();
    private Posicion posActual;
    private Color colorRealcePosActual = Color.green.brighter();
    private HashMap<Posicion, Color> posicionesRealzadas = new HashMap<Posicion, Color>();
    private HashMap<Posicion, Point> posicionesPantalla = new HashMap<Posicion, Point>();

    /**
     * Crea e inicializa un VisorMapa
     */
    public VisorMapa() {
        // dos capas: tablero y realce
        add(layerTablero, new Integer(1));
        add(layerRealce, new Integer(2));

        // prepara imagenes
        if (imagenes.isEmpty()) {

            for (Terreno t : Terreno.values()) {
                preparaImagen("celdas/" + t.toString().toLowerCase());
            }
            preparaImagen("celdas/bandido");
            preparaImagen("celdas/ciudad");
            preparaImagen("celdas/poblado");
            for (Recurso r : Recurso.values()) {
                preparaImagen("recursos/" + r.toString().toLowerCase());
            }
            preparaImagen("recursos/cualquiera");
            for (OrientacionArista o : OrientacionArista.values()) {
                preparaImagen("celdas/puerto-" + o.toString().toLowerCase());
            }
            try {
                mt.waitForAll();
            } catch (InterruptedException ex) {
                System.err.println("Error cargando imagenes: interrupcion");
            }
            if (mt.isErrorAny()) {
                System.err.println("Error cargando imagenes");
            }
        }

        // asigna manejadores para el raton
        layerRealce.addMouseMotionListener(new MouseMotionListener() {

            public void mouseDragged(MouseEvent e) {
            }

            public void mouseMoved(MouseEvent e) {
                movimientoRaton(e);
            }
        });
        layerRealce.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                clickRaton(e);
            }
        });

        setPreferredSize(new Dimension(800, 800));
        revalidate();        
    }

    /**
     * Registra un manejador de eventos
     */
    public void addMapaListener(MapaListener ml) {
        listeners.add(ml);
    }

    /**
     * Desregistra un manejador de eventos
     */
    public void removeMapaListener(MapaListener ml) {
        listeners.remove(ml);
    }

    /**
     * Cambia el tablero actual por otro
     */
    public void setTablero(TableroCatan t) {
        posicionesRealzadas.clear();
        this.t = t;
        if (posicionesPantalla.isEmpty()) generaPosiciones();
        repaint();
    }

    /**
     * Cambia el texto que aparece en la parte superior
     */
    public void setText(Color color, String text) {
        labelTexto.setForeground(color);
        labelTexto.setText(text);
        repaint();
    }

    /**
     * Refresca realces
     */
    public void setRealce(Posicion p, Color c) {
        if (c == null) {
            posicionesRealzadas.remove(p);
        } else {
            posicionesRealzadas.put(p, c);
        }
    }

    /**
     * Limpia todos los realces
     */
    public void limpiaRealces() {
        posicionesRealzadas.clear();
    }

    /**
     * Refresca el tablero
     */
    public void repaint() {
        layerTablero.refresca();
        super.repaint();
    }

    // --------- metodos que NO hace falta llamar directamente --------- //
    /**
     * Sin esto no se pinta nada; reparte areas entre hijos, y si tercia
     * calcula una transformada que aprovecha mejor la pantalla y evita
     * problemas de escalado
     */
    public void setBounds(int x, int y, int width, int height) {
        if (getSize().equals(oldDims) || t == null) {
            super.setBounds(x, y, width, height);
            return;
        }
        getSize(oldDims);

        // calcula la transformada que hace que todo quepa bien en pantalla
        int rw = cellW * t.getDim();
        int rh = (cellH - cellHO) * t.getDim() + cellHO;
        int w = width;
        int h = height;
        int m = Math.min(w, h);
        double sx = m / (double) rw;
        double sy = m / (double) rh;
        transform = AffineTransform.getScaleInstance(sx, sy);

        // actualiza tamaños de dibujo del tablero y los realces
        layerTablero.setBounds(0, 0, width, height);
        layerRealce.setBounds(0, 0, width, height);

        // refresca posiciones
        Posicion ph = new Posicion();
        for (Posicion p : posicionesPantalla.keySet()) {
            Point pp = posicionesPantalla.get(p);
            posCentro(p, ph, pp);
        }

        super.setBounds(x, y, width, height);
    }

    private void generaPosiciones() {
        posicionesPantalla.clear();
        PosicionVertice[] rpv = new PosicionVertice[3];
        PosicionArista[] rpa = new PosicionArista[2];
        for (int i = 0; i < t.getDim(); i++) {
            for (int j = 0; j < t.getDim(); j++) {
                Posicion p = new Posicion(i, j);
                if (!t.dentro(p)) {
                    continue;
                }
                if (t.getCelda(p).getTerreno() != Terreno.Oceano) {
                    posicionesPantalla.put(p, new Point());
                }
                for (OrientacionVertice ov : PosicionVertice.OCanonicas) {
                    PosicionVertice pv = new PosicionVertice(p, ov);
                    for (Posicion pf : pv.sinonimos(rpv)) {
                        if (t.getCelda(pf) != null &&
                                t.getCelda(pf).getTerreno() != Terreno.Oceano) {
                            posicionesPantalla.put(pv, new Point());
                            break;
                        }
                    }
                }
                for (OrientacionArista oa : PosicionArista.OCanonicas) {
                    PosicionArista pa = new PosicionArista(p, oa);
                    for (Posicion pf : pa.sinonimos(rpa)) {
                        if (t.getCelda(pf) != null &&
                                t.getCelda(pf).getTerreno() != Terreno.Oceano) {
                            posicionesPantalla.put(pa, new Point());
                            break;
                        }
                    }
                }
            }
        }
    }

    private Posicion coordenadasPantallaAPosicion(Point p) {
        Point p2 = new Point();
        try {
            transform.inverseTransform(p, p2);
        } catch (Exception e2) {
        }
        double dist = Double.MAX_VALUE;
        Posicion best = null;
        for (Posicion pc : posicionesPantalla.keySet()) {
            Point pp = posicionesPantalla.get(pc);
            if (pp.distance(p2) < dist) {
                best = pc;
                dist = pp.distance(p2);
            }
        }
        // si muy lejos de cualquier punto o fuera del tablero, devuelve null
        return (dist < 100 && best != null && t.dentro(best)) ? best : null;
    }

    private void movimientoRaton(MouseEvent e) {
        Posicion best = coordenadasPantallaAPosicion(e.getPoint());
        if (posActual != best) {
            posActual = best;
            layerRealce.repaint();
        }
    }

    private void clickRaton(MouseEvent e) {
        Posicion p = coordenadasPantallaAPosicion(e.getPoint());
        System.err.println("Click en " + p);
        for (MapaListener ml : listeners) {
            ml.clickEnPosicion(p, e);
        }
    }

    /**
     * Pinta realces
     */
    private void paintRealces(Graphics2D g2d) {
        // pinta realces
        Posicion ph = new Posicion();
        Point pt = new Point();
        g2d.setStroke(new BasicStroke(5f));
        for (Posicion pos : posicionesRealzadas.keySet()) {
            pt = posCentro(pos, ph, pt);
            g2d.setColor(posicionesRealzadas.get(pos));
            g2d.drawOval((int) pt.getX() - resW / 2, (int) pt.getY() - resH / 2, resW, resH);
        }

        if (posActual != null) {
            pt = posCentro(posActual, ph, pt);
            g2d.setColor(colorRealcePosActual);
            g2d.drawOval((int) pt.getX() - resW / 2, (int) pt.getY() - resH / 2, resW, resH);
        }
    }

    /**
     * Pinta el tablero y las piezas; el ultimo parametro especifica que pintar.
     * El orden correcto es casillas y numeros y puertos, caminos, y terreno
     */
    private void paintTablero(Graphics2D g2d) {
        Posicion p = new Posicion();
        Posicion ph = new Posicion();
        Point pt = new Point();
        Ficha f = null;

        // se pintan solo ciertas aristas o vertices por celda, para evitar repeticiones
        OrientacionArista aristasPintadas[] = new OrientacionArista[]{OrientacionArista.NorEste, OrientacionArista.NorOeste, OrientacionArista.Este};
        OrientacionVertice verticesPintados[] = new OrientacionVertice[]{OrientacionVertice.Norte, OrientacionVertice.Sur};

        for (int pasada = 0; pasada < 3; pasada++) {
            for (int i = 0; i < t.getDim(); i++) {
                for (int j = 0; j < t.getDim(); j++) {
                    p.setPos(j, i);
                    if (t.dentro(p)) {
                        switch (pasada) {
                            case 0: // terreno
                                pt = posCentro(p, ph, pt);
                                paintCelda(t.getCelda(p), (int) pt.getX(), (int) pt.getY(), g2d);
                                break;
                            case 1: // caminos
                                for (OrientacionArista oa : aristasPintadas) {
                                    if ((f = t.getFichaEnPos(new PosicionArista(p, oa))) != null) {
                                        Color c = t.getLado(f.getNumLado()).getColor();
                                        pt = posCentro(p, ph, pt);
                                        paintArista(g2d, (int) pt.getX(), (int) pt.getY(), f, c);
                                    }
                                }
                                break;
                            case 2: // poblados y ciudades
                                for (OrientacionVertice ov : verticesPintados) {
                                    if ((f = t.getFichaEnPos(new PosicionVertice(p, ov))) != null) {
                                        Color c = t.getLado(f.getNumLado()).getColor();
                                        pt = posCentro((PosicionVertice) f.getPosicion(), ph, pt);
                                        paintVertice(g2d, (int) pt.getX(), (int) pt.getY(), f, c);
                                    }
                                }
                                break;
                        }
                    }
                }
            }
        }

        // en las aristas de la fila inferior, pinta los mazos de cartas del juego
        p.setPos(2, 7);
        PosicionArista pa = new PosicionArista(p, OrientacionArista.Este);
        pt = posCentro(pa, ph, pt);
        AffineTransform st = AffineTransform.getTranslateInstance(pt.getX(), pt.getY());
        AffineTransform ct = g2d.getTransform();
        ct.concatenate(st);
        g2d.setTransform(ct);
        st = AffineTransform.getTranslateInstance(cellW, 0);
        InterfazMazo im = new InterfazMazo();
        im.setBackground(null);
        im.setSize(im.getPreferredSize());
        im.setMazoVisto(true);
        im.setSeparacion(InterfazMazo.APILADAS);
        for (Recurso r : Recurso.values()) {
            im.setMazo(t.getCartasRecurso(r));
            im.paint(g2d);
            ct = g2d.getTransform();
            ct.concatenate(st);
            g2d.setTransform(ct);
        }
        im.setMazoVisto(false);
        im.setMazo(t.getCartasDesarrollo());
        im.paint(g2d);
    }

    /**
     * Pinta una celda del juego - sin fichas
     */
    public void paintCelda(Celda c, int x, int y, Graphics2D g) {
        FontRenderContext frc = g.getFontRenderContext();
        g.setColor(Color.black);

        // pinta terreno
        Image imgT = imagenes.get("celdas/" + c.getTerreno().toString().toLowerCase());
        g.drawImage(imgT, x - cellW / 2, y - cellH / 2, this);

        // si es puerto, pinta recurso, puerto, tasa de intercambio
        if (c.getOrientacionPuerto() != null) {
            String recurso = c.getRecursoPuerto() == null ? "cualquiera" : c.getRecursoPuerto().toString().toLowerCase();
            Image imgR = imagenes.get("recursos/" + recurso);
            g.drawImage(imgR, x - resW / 2, y - resH / 2, this);
            Image imgP = imagenes.get("celdas/puerto-" +
                    ("" + c.getOrientacionPuerto()).toLowerCase());
            g.drawImage(imgP, x - cellW / 2, y - cellH / 2, this);
            String tasa = "" + c.getTasaPuerto() + ":1";
            g.setFont(fuenteTirada.deriveFont(Font.BOLD, 55));
            Rectangle2D r = g.getFont().getStringBounds(tasa, frc);
            g.drawString(tasa,
                    x - (int) (r.getWidth() / 2), y + (int) (r.getHeight() / 2) - 5);
        }

        // si da recursos, pinta el recurso y la tirada necesaria
        if (c.getTirada() != 0) {
            Image imgR = imagenes.get("recursos/" + c.getRecurso().toString().toLowerCase());
            g.drawImage(imgR, x - resW / 2, y - resH / 2, this);
            int tirada = c.getTirada();
            g.setFont(fuenteParaTirada(g, tirada));
            Rectangle2D r = g.getFont().getStringBounds("" + tirada, frc);
            g.drawString("" + tirada,
                    x - (int) (r.getWidth() / 2), y + (int) (r.getHeight() / 2) - 5);
        }

        // pinta coordenadas en blanco
        g.setColor(Color.white);
//        Composite oldComposite = g.getComposite();
// FIXME        g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER, 0.9f));
        g.setFont(fuenteTirada.deriveFont(6));
        g.drawString(c.getPosicion().toString(),
                x + (int) (cellW * -.30), y + (int) (cellH * .35));
//        g.setComposite(oldComposite);

        // pinta bandido
        if (t.getPosLadron().equals(c.getPosicion())) {
            Image ladron = imagenes.get("celdas/bandido");
            g.drawImage(ladron, x - resW / 2, y - resH / 2, this);
        }
    }

    private void paintVertice(Graphics2D g, int x, int y, Ficha f, Color c) {
        Image img = imagenes.get("celdas/" + (f instanceof Ciudad ? "ciudad" : "poblado"));
        g.setColor(c);
        g.fillOval(x - resW / 2, y - resH / 2, resW, resH);
        g.setColor(Color.black);
        g.drawOval(x - resW / 2, y - resH / 2, resW, resH);
        g.drawImage(img, x - resW / 2, y - resH / 2, this);
    }

    private void paintArista(Graphics2D g, int x, int y, Ficha f, Color c) {
        Shape r = null;
        OrientacionArista oa = ((PosicionArista) f.getPosicion()).getOrientacion();
        if ((r = caminos.get("" + oa)) == null) {
            int xx[] = null, yy[] = null, n = 4;
            switch (oa) {
                case Este:
                    xx = new int[]{203, 221, 221, 203};
                    yy = new int[]{70, 70, 216, 216};
                    break;
                case NorOeste:
                    xx = new int[]{-6, 101, 110, 3};
                    yy = new int[]{64, -6, 5, 77};
                    break;
                case NorEste:
                    xx = new int[]{114, 224, 214, 103};
                    yy = new int[]{-4, 68, 81, 6};
                    break;
            }
            r = new Polygon(xx, yy, 4);
            caminos.put("" + oa, r);
        }
        AffineTransform a = AffineTransform.getTranslateInstance(x - cellW / 2, y - cellH / 2);
        r = a.createTransformedShape(r);
        g.setColor(c);
        g.fill(r);
        g.setColor(Color.black);
        g.draw(r);
    }

    private Point posCentro(Posicion p, Posicion ph, Point rp) {
        rp = (rp == null) ? new Point() : rp;
        double dx = 0, dy = 0;
        if (p instanceof PosicionVertice) {
            switch (((PosicionVertice) p).getOrientacion()) {
                case Sur:
                    dy = cellH - 20;
                    break;
            }
            dy -= 0.68 * (cellH - cellHO);
        } else if (p instanceof PosicionArista) {
            switch (((PosicionArista) p).getOrientacion()) {
                case Este:
                    dx = .5 * cellW;
                    dy = 0;
                    break; // red
                case NorOeste:
                    dx = -.2 * cellW;
                    dy = -.4 * cellH;
                    break; // blue
                case NorEste:
                    dx = .2 * cellW;
                    dy = -.4 * cellH;
                    break; // green
            }
        }
        t.compactoAHexagonal(p, ph);
        rp.setLocation(
                (ph.getX() + 1) * cellW / 2 + dx,
                ph.getY() * (cellH - cellHO) + dy + 2 * cellHO);
        return rp;
    }
    /** una cache de aristas correctamente rotadas */
    private HashMap<String, Shape> caminos = new HashMap<String, Shape>();

    private Font fuenteParaTirada(Graphics2D g, int tirada) {
        g.setColor(Color.black);
        switch (tirada) {
            case 2:
            case 12:
                return fuenteTirada;
            case 3:
            case 11:
                return fuenteTirada.deriveFont(60f);
            case 4:
            case 10:
                return fuenteTirada.deriveFont(65f);
            case 5:
            case 9:
                return fuenteTirada.deriveFont(Font.BOLD, 65f);
            case 6:
            case 8:
                g.setColor(Color.red);
                return fuenteTirada.deriveFont(Font.BOLD, 65f);
        }
        return null;
    }

    public boolean imageUpdate(Image img, int infoflags,
            int x, int y, int w, int h) {
        repaint();
        return super.imageUpdate(img, infoflags, x, y, w, h);
    }
    private MediaTracker mt;

    private void preparaImagen(String s) {
        Image i = Util.getImage("eps/catan/gui/" + s + ".png");
        if (mt == null) {
            mt = new MediaTracker(this);
        }
        mt.addImage(i, imagenes.size());
        imagenes.put(s, i);
    }

    private class LayerTablero extends JComponent {

        BufferedImage bi = null;

        /**
         * Cambia el tamaño del lienzo.
         * Cuando cambia la transformacion, hay que actualizar la imagen.
         * reducir mucho una imagen hace que el texto quede feo.
         */
        public void setBounds(int x, int y, int width, int height) {
            super.setBounds(x, y, width, height);
            if (bi == null || bi.getWidth() != width || bi.getHeight() != height) {
                if (width == 0 || height == 0) {
                    return;
                }
                bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            }
            refresca();
        }

        /**
         * Regenera la imagen de pantalla (algo habra cambiado)
         */
        private Timer repaintTimer;
        public void refresca() {
            if (bi == null) {
                // FEO: en el primer pintado no hay un 'setBounds', y no se pinta el componente...
                if (repaintTimer == null)  {
                    repaintTimer = new Timer(500, new ActionListener() {
                        public void actionPerformed(ActionEvent ae) {
                            repaintTimer.stop();
                            repaintTimer = null;
                            Component c = getParent();
                            while ( ! (c instanceof JSplitPane)) c = c.getParent();
                            ((JSplitPane)c).setDividerLocation(((JSplitPane)c).getDividerLocation());
                        }
                    });
                    repaintTimer.start();
                    System.err.println("Started!");
                }
                return;
            }
            Graphics2D g2d = bi.createGraphics();
            if (fuenteTirada == null) {
                fuenteTirada = getFont().deriveFont(50f);
            }
            g2d.setFont(fuenteTirada);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setTransform(transform);
            paintTablero(g2d);
        // System.err.println("Tablero refrescado");
        }

        /**
         * Repintado rapido: planta una imagen en pantalla, siempre
         * del tamaño preciso.
         */
        public void paintComponent(Graphics g) {
            if (bi == null) {
                refresca();
            }
            g.drawImage(bi, 0, 0, this);
        }
    }

    private class LayerRealce extends JComponent {

        public void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            AffineTransform t0 = g2d.getTransform();
            AffineTransform t1 = new AffineTransform(t0);
            t1.concatenate(transform);
            g2d.setTransform(t1);
            // pinta realces
            paintRealces(g2d);
            g2d.setTransform(t0);
            labelTexto.setBounds(0, 12, getWidth(), 12);
            labelTexto.paint(g);
        }
    }

    public interface MapaListener {

        public void clickEnPosicion(Posicion p, MouseEvent e);
    }
}
