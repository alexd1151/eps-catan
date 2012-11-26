/*
 * VisorJugador.java
 *
 * Created on January 12, 2008, 3:18 AM
 */
package eps.catan.gui;

import eps.Util;
import eps.cartas.Carta;
import eps.cartas.Mazo;
import eps.cartas.gui.InterfazMazo;
import eps.cartas.gui.MazoListener;
import eps.catan.BarajaDesarrollo;
import eps.catan.BarajaRecursos.Recurso;
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
import eps.catan.accion.AccionCambio;
import eps.catan.accion.AccionDados;
import eps.catan.accion.AccionDesarrollo;
import eps.catan.accion.AccionDescarte;
import eps.catan.accion.AccionFicha;
import eps.catan.accion.AccionFinTurno;
import eps.catan.accion.AccionRobo;
import eps.catan.gui.VisorMapa.MapaListener;
import eps.multij.Accion;
import eps.multij.Evento;
import eps.multij.Evento.TipoEvento;
import eps.multij.JuegoRemoto;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 */
public class VisorJugador extends javax.swing.JPanel {

    private JuegoRemoto j;
    private TableroCatan t;
    private int numLado;
    private InterfazMazo[] desarrollos;
    private InterfazMazo recursos;
    private Vector<Accion> accionesCambio;
    private ArrayList<Accion> acciones;
    private VisorMapa mapa;

    /** modo de interacción con el mapa (para la P3) */
    public enum Modo {

        /** no se espera nada */
        nada,
        /** click = celda que se roba */
        posRobo1,
        /** click = poblado/ciudad que se roba (o vertice vacio para no robar) */
        posRobo2,
        /** click = arista para poner camino */
        posCamino,
        /** click = vertice para poner poblado */
        posPoblado,
        /** click = vertice para poner ciudad */
        posCiudad,
        /** click = recurso que monopolizar o pedir */
        posRecurso1,
        /** click = segundo recurso que pedir */
        posRecurso2,
        /** primer camino que colocar */
        posCaminos1,
        /** segundo camino que colocar */
        posCaminos2
    };
    
    private Modo tipoSeleccion;
    private Posicion celdaRobo;

    private static HashMap<String, Image> imagenes = new HashMap<String, Image>();
    
    public void setLado(JuegoRemoto j, int numLado, VisorMapa mapa) throws RemoteException {     
        this.j = j;
        this.t = (TableroCatan)j.getTablero();
        this.numLado = numLado;  
        this.mapa = mapa;
        Lado l = t.getLado(numLado);

        // borde coloreado
        setBorder(BorderFactory.createLineBorder(l.getColor(), 4));

        // desarrollos
        jpDesarrollo.setLayout(new FlowLayout());
        jpDesarrollo.removeAll();
        desarrollos = new InterfazMazo[2];
        int i = 0;
        for (Mazo m : new Mazo[]{l.getDesarrollos(), l.getNuevosDesarrollos()}) {
            desarrollos[i] = new InterfazMazo();
            desarrollos[i].setModoSel(i == 0 ? InterfazMazo.SEL_UNA : InterfazMazo.SEL_NO);
            desarrollos[i].setSeparacion(InterfazMazo.SEPARADAS);
            desarrollos[i].setMazo(m);
            jpDesarrollo.add(desarrollos[i]);
            i++;
        }

        // manejador de eventos para desarrollos
        desarrollos[0].addMazoListener(new MazoListener() {

            public void dobleClickEnCarta(InterfazMazo im, int posCarta) {
                try {
                    clickEnDesarrollo(posCarta);
                } catch (RemoteException re) {
                    re.printStackTrace();
                }
            }

            public void clickEnCarta(InterfazMazo im, int posCarta) {
            // se ignora
            }
        });

        // manejador de eventos para el mapa
        mapa.addMapaListener(new MapaListener() {

            public void clickEnPosicion(Posicion p, MouseEvent e) {
                try {
                    clickEnMapa(p);
                } catch (RemoteException re) {
                    re.printStackTrace();
                }
            }
        });
        tipoSeleccion = Modo.nada;

        // recursos
        jpRecursos.removeAll();
        recursos = new InterfazMazo();
        recursos.setModoSel(InterfazMazo.SEL_NO);
        recursos.setSeparacion(InterfazMazo.MUY_SEPARADAS);
        recursos.setMazo(l.getRecursos());
        jpRecursos.add(recursos);

        jpCabecera.setVisible(true);
        jpAcciones.setVisible(true);
        jbDados.setVisible(true);
        refresca(j, false, null);
    }

    private void clickEnDesarrollo(int posCarta) throws RemoteException {
        if (t.getTurno() == numLado && !t.isDesarrolloUsado()) {
            Carta c = t.getLado(numLado).getDesarrollos().get(posCarta);
            switch (BarajaDesarrollo.tipoParaCarta(c)) {
                case Abundancia:
                    mapa.setText(Color.white, "Abundancia: selecciona una celda que tenga el recurso deseado");
                    tipoSeleccion = Modo.posRecurso2;
                    break;
                case Caminos:
                    mapa.setText(Color.white, "Caminos: selecciona las posiciones para los 2 caminos que vas a construir");
                    tipoSeleccion = Modo.posCaminos1;
                    break;
                case Monopolio:
                    mapa.setText(Color.white, "Monopolio: selecciona una celda que tenga el recurso deseado");
                    tipoSeleccion = Modo.posRecurso1;
                    break;
                case Soldado:
                    j.realizaAccion(new AccionDesarrollo.Soldado(numLado));
                    break;
            }
        }
    }

    private void clickEnMapa(Posicion p) throws RemoteException {
        Accion a = null;
        switch (tipoSeleccion) {
            case nada:
                return;
            case posCamino:
                if (p instanceof PosicionArista) {
                    Ficha f = new Camino(numLado);
                    boolean compra = (t.getFase() == Fase.Normal);
                    j.realizaAccion(new AccionFicha(numLado, compra, f, p));
                    tipoSeleccion = Modo.nada;
                }
                break;
            case posPoblado:
            case posCiudad:
                if (p instanceof PosicionVertice) {
                    Ficha f = (tipoSeleccion == Modo.posPoblado) ? new Poblado(numLado) : new Ciudad(numLado);
                    boolean compra = (t.getFase() == Fase.Normal);
                    a = new AccionFicha(numLado, compra, f, p);
                    j.realizaAccion(a);
                    tipoSeleccion = Modo.nada;
                }
                break;
            case posRobo2:
                if (p instanceof PosicionVertice) {
                    for (PosicionVertice pv : ((PosicionVertice) p).sinonimos(null)) {
                        if (celdaRobo.equals(pv)) {
                            j.realizaAccion(new AccionRobo(numLado, pv));
                            tipoSeleccion = Modo.nada;
                            break;
                        }
                    }
                }
                break;
            case posRobo1:
                if (p.getClass().equals(Posicion.class)) {
                    celdaRobo = p;
                    tipoSeleccion = Modo.posRobo2;
                }
                break;
        }
    }

    public void refresca(JuegoRemoto j, boolean completo, Evento e) throws RemoteException {

        try {
            if ( ! j.equals(this.j)) {
                setLado(j, numLado, mapa);
            }
            
            if (e != null && completo && e.getTipo() == TipoEvento.Confirma) {
// TEMPLATE_START
                Accion a = e.getCausa();
                String nombre = j.getJugador(t.getTurno()).getNombre();
                int rc = JOptionPane.showConfirmDialog(this,
                        "<html><h3>" + nombre + " propone:</h3>" +
                        "<p>" + a.toString() + "<p></html>", "Oferta de comercio",
                        JOptionPane.YES_NO_OPTION);
                if (rc == JOptionPane.YES_OPTION) {
                    a.confirma(numLado, j);
                }
/* TEMPLATE_ELSE 
            // Practica 3 : Mostrar dialogo de confirmacion aqui
            Accion a = e.getCausa();            
            // y si en el dialogo se acepta la accion, se debe confirmar
            // mediante "a.confirma(...)"
            return;
TEMPLATE_END */
            }

            t = (TableroCatan)j.getTablero();
            Lado l = t.getLado(numLado);
            recursos.setMazo(l.getRecursos());
            desarrollos[0].setMazo(l.getDesarrollos());
            desarrollos[1].setMazo(l.getNuevosDesarrollos());            

            if (completo) {
                String nombre = j.getJugador(numLado).getNombre();
                if (t.getTurno() == numLado) {
                    mapa.setText(l.getColor(), nombre + ": te toca");
                } else {
                    Color c = t.getLado(t.getTurno()).getColor();
                    mapa.setText(c, nombre + ": es el turno de " +
                            j.getJugador(t.getTurno()).getNombre());

                }
            }

            jlNombreJugador.setText(j.getJugador(numLado).getNombre());
            jpCabecera.setBorder(j.getTablero().getTurno() == numLado ? BorderFactory.createRaisedBevelBorder() : null);
            jbSoldado.setText("" + l.getSoldadosUsados());
            jbCamino.setText("" + (l.getUsadas(Camino.class).size()) + "/" + l.getMaxFichas(Camino.class));
            jbPoblado.setText("" + (l.getUsadas(Poblado.class).size()) + "/" + l.getMaxFichas(Poblado.class));
            jbCiudad.setText("" + (l.getUsadas(Ciudad.class).size()) + "/" + l.getMaxFichas(Ciudad.class));
            jlPuntos.setText(completo ? "" + l.getPuntos() : "?");
            recursos.setMazoVisto(completo);
            recursos.repaint();
            for (int i = 0; i < 2; i++) {
                desarrollos[i].setMazoVisto(completo);
                desarrollos[i].repaint();
            }
            desarrollos[0].setModoSel(InterfazMazo.SEL_NO);
            recursos.setModoSel(InterfazMazo.SEL_NO);
            recursos.setSeleccionadas(null);
            jbFin.setText("Finalizar turno");

            acciones = t.accionesValidas(numLado);

            jbFin.setEnabled(false);
            jbDados.setEnabled(false);
            jcbCambio.setEnabled(false);
            jcbComercio.setEnabled(false);
            jbSoldado.setEnabled(false);
            jbCambio.setEnabled(false);
            jbComercio.setEnabled(false);
            jbPoblado.setEnabled(false);
            jbCiudad.setEnabled(false);
            jbCamino.setEnabled(false);

            // no permite hacer acciones en nombre de un rival
            if (!completo) {
                return;
            }

            accionesCambio = new Vector<Accion>();
            for (Accion a : acciones) {
                if (a instanceof AccionCambio) {
                    accionesCambio.add(a);
                }
                if (a instanceof AccionFicha) {
                    Ficha f = ((AccionFicha) a).getFicha();
                    if (f instanceof Camino) {
                        jbCamino.setEnabled(true);
                    }
                    if (f instanceof Ciudad) {
                        jbCiudad.setEnabled(true);
                    } else if (f instanceof Poblado) {
                        jbPoblado.setEnabled(true);
                    }
                }
                if (a instanceof AccionDados) {
                    jbDados.setEnabled(true);
                }
                if (a instanceof AccionFinTurno) {
                    jbFin.setEnabled(true);
                }
                if (a instanceof AccionDesarrollo) {
                    if (a instanceof AccionDesarrollo.Compra) {
                        jbSoldado.setEnabled(true);
                    } else {
                        desarrollos[0].setModoSel(InterfazMazo.SEL_UNA);
                    }
                }
                if (a instanceof AccionDescarte && t.getFase() == Fase.Descarte) {
                    if (l.getRecursos().size() >= 8) {
                        recursos.setModoSel(InterfazMazo.SEL_MUCHAS);
                    }
                    jbFin.setText("Descartar");
                    jbFin.setEnabled(true);
                }
                if (a instanceof AccionRobo) {
                    mapa.setText(Color.white, "Robo: selecciona la celda que ocupará el ladrón");
                    tipoSeleccion = Modo.posRobo1;
                }
            }

            jcbCambio.setModel(new DefaultComboBoxModel(accionesCambio));
            if (!accionesCambio.isEmpty()) {
                jcbCambio.setEnabled(true);
                jbCambio.setEnabled(true);
            }

//      PRACTICA 3 - Implementar una interfaz de comercio        
            if (!l.getRecursos().isEmpty() && t.getTurno() == numLado) {
                jcbComercio.setEnabled(true);
// TEMPLATE_START
                ArrayList<String> conCartas = new ArrayList<String>();
                for (int i = 0; i < t.getNumJugadores(); i++) {
                    if (i != numLado && !t.getLado(i).getRecursos().isEmpty()) {
                        conCartas.add(j.getJugador(i).getNombre());
                    }
                }
                jcbComercio.setModel(new DefaultComboBoxModel(conCartas.toArray()));
// TEMPLATE_ELSE
                // inicializar labelsComercio con los nombres de los jugadores con >0 recursos
                // jcbComercio.setModel(new DefaultComboBoxModel(labelsComercio));
                // tambien falta por poner un manejador de evento a jbComercio ...
// TEMPLATE_END            
                jbComercio.setEnabled(true);
            }
        } catch (RemoteException re) {
            re.printStackTrace();
        }
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

    /** Creates new form VisorJugador */
    public VisorJugador() {
        initComponents();
        jpCabecera.setVisible(false);
        jpAcciones.setVisible(false);
        jbDados.setVisible(false);
        if (imagenes.isEmpty()) {
            for (String s : new String[]{
                "soldado", "camino", "poblado", "ciudad",
                "puntos", "dados", "aceptar"
            }) {
                preparaImagen("iconos/" + s);
            }
            for (Recurso r : Recurso.values()) {
                preparaImagen("recursos/" + r.name().toLowerCase());
            }
            preparaImagen("recursos/cualquiera");
            try {
                mt.waitForAll();
            } catch (InterruptedException ex) {
                System.err.println("Error cargando imagenes: interrupcion");
            }
            if (mt.isErrorAny()) {
                System.err.println("Error cargando imagenes");
            }
        }
        jbSoldado.setIcon(new ImageIcon(imagenes.get("iconos/soldado")));
        jbCamino.setIcon(new ImageIcon(imagenes.get("iconos/camino")));
        jbPoblado.setIcon(new ImageIcon(imagenes.get("iconos/poblado")));
        jbCiudad.setIcon(new ImageIcon(imagenes.get("iconos/ciudad")));
        jlPuntos.setIcon(new ImageIcon(imagenes.get("iconos/puntos")));
        jbDados.setIcon(new ImageIcon(imagenes.get("iconos/dados")));
        jbDados.setPreferredSize(new Dimension(24, 60));
        jbCambio.setIcon(new ImageIcon(imagenes.get("iconos/aceptar")));
        jbCambio.setPreferredSize(new Dimension(24, 24));
        jbComercio.setIcon(new ImageIcon(imagenes.get("iconos/aceptar")));
        jbComercio.setPreferredSize(new Dimension(24, 24));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jpCabecera = new javax.swing.JPanel();
        jlNombreJugador = new javax.swing.JLabel();
        jpRelleno = new javax.swing.JPanel();
        jbCamino = new javax.swing.JButton();
        jbPoblado = new javax.swing.JButton();
        jbCiudad = new javax.swing.JButton();
        jlPuntos = new javax.swing.JLabel();
        jbSoldado = new javax.swing.JButton();
        jpPrincipal = new javax.swing.JPanel();
        jpDesarrollo = new javax.swing.JPanel();
        jpRecursos = new javax.swing.JPanel();
        jpAcciones = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jbFin = new javax.swing.JButton();
        jcbCambio = new javax.swing.JComboBox();
        jcbComercio = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        jbCambio = new javax.swing.JButton();
        jbComercio = new javax.swing.JButton();
        jbDados = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jpCabecera.setBackground(new java.awt.Color(204, 204, 255));
        jpCabecera.setLayout(new java.awt.GridBagLayout());

        jlNombreJugador.setFont(new java.awt.Font("Dialog", 3, 14));
        jlNombreJugador.setText("<No asignado>");
        jpCabecera.add(jlNombreJugador, new java.awt.GridBagConstraints());

        jpRelleno.setBackground(new java.awt.Color(204, 204, 255));
        jpRelleno.setLayout(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jpCabecera.add(jpRelleno, gridBagConstraints);

        jbCamino.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCaminoActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        jpCabecera.add(jbCamino, gridBagConstraints);

        jbPoblado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbPobladoActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        jpCabecera.add(jbPoblado, gridBagConstraints);

        jbCiudad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCiudadActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        jpCabecera.add(jbCiudad, gridBagConstraints);

        jlPuntos.setText("?");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jpCabecera.add(jlPuntos, gridBagConstraints);

        jbSoldado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSoldadoActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jpCabecera.add(jbSoldado, gridBagConstraints);

        add(jpCabecera, java.awt.BorderLayout.NORTH);

        jpPrincipal.setLayout(new java.awt.GridBagLayout());

        jpDesarrollo.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.3;
        jpPrincipal.add(jpDesarrollo, gridBagConstraints);

        jpRecursos.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jpPrincipal.add(jpRecursos, gridBagConstraints);

        jpAcciones.setLayout(new java.awt.GridBagLayout());

        jLabel7.setText("Cambiar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 3, 0);
        jpAcciones.add(jLabel7, gridBagConstraints);

        jbFin.setText("Finalizar turno");
        jbFin.setEnabled(false);
        jbFin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbFinActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 2, 3);
        jpAcciones.add(jbFin, gridBagConstraints);

        jcbCambio.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-- no hay acciones --" }));
        jcbCambio.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 3, 3);
        jpAcciones.add(jcbCambio, gridBagConstraints);

        jcbComercio.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "--" }));
        jcbComercio.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 3);
        jpAcciones.add(jcbComercio, gridBagConstraints);

        jLabel9.setText("Comercio");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jpAcciones.add(jLabel9, gridBagConstraints);

        jbCambio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCambioActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jpAcciones.add(jbCambio, gridBagConstraints);

        jbComercio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbComercioActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        jpAcciones.add(jbComercio, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        jpPrincipal.add(jpAcciones, gridBagConstraints);

        jbDados.setEnabled(false);
        jbDados.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbDadosActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        jpPrincipal.add(jbDados, gridBagConstraints);

        add(jpPrincipal, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    private void jbDadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbDadosActionPerformed
        try {
            j.realizaAccion(new AccionDados(numLado));
        } catch (RemoteException re) {
            re.printStackTrace();
        }
    }//GEN-LAST:event_jbDadosActionPerformed

    private void jbFinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbFinActionPerformed
        try {
            if (jbFin.getText().startsWith("Descartar")) {
                j.realizaAccion(
                        new AccionDescarte(numLado, recursos.getSeleccionadas()));
            } else {
                j.realizaAccion(new AccionFinTurno(numLado));
            }
        } catch (RemoteException re) {
            re.printStackTrace();
        }
    }//GEN-LAST:event_jbFinActionPerformed

    private void jbCambioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbCambioActionPerformed
        int i = jcbCambio.getSelectedIndex();
        try {
            j.realizaAccion(accionesCambio.get(i));
        } catch (RemoteException re) {
            re.printStackTrace();
        }
    }//GEN-LAST:event_jbCambioActionPerformed

    private void jbCaminoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbCaminoActionPerformed
        // un camino
        mapa.setText(Color.white, "selecciona una posicion para el nuevo camino");
        tipoSeleccion = Modo.posCamino;
        
    }//GEN-LAST:event_jbCaminoActionPerformed

    private void jbPobladoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbPobladoActionPerformed
        // un poblado
        mapa.setText(Color.white, "selecciona una posicion para el nuevo poblado");
        tipoSeleccion = Modo.posPoblado;       
    }//GEN-LAST:event_jbPobladoActionPerformed

    private void jbCiudadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbCiudadActionPerformed
        // una ciudad
        mapa.setText(Color.white, "selecciona una posicion para la nueva ciudad");
        tipoSeleccion = Modo.posCiudad;        
    }//GEN-LAST:event_jbCiudadActionPerformed

    private void jbSoldadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbSoldadoActionPerformed
        // TODO add your handling code here:
        try {
            j.realizaAccion(new AccionDesarrollo.Compra(numLado));
        } catch (RemoteException re) {
            re.printStackTrace();
        }
}//GEN-LAST:event_jbSoldadoActionPerformed

    private void jbComercioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbComercioActionPerformed
        // TODO add your handling code here:
        Component c = getParent();
        while (!(c instanceof Dialog)) {
            c = c.getParent();
        }
        DialogoComercio dc = new DialogoComercio(
                (Dialog) c, t.getLado(numLado).getRecursos());
        dc.setVisible(true);
        String nombreOtro = jcbComercio.getSelectedItem().toString();
        int otro = -1;
        try {
            for (int i = 0; i < j.getTablero().getNumJugadores(); i++) {
                try {
                    if (j.getJugador(i).getNombre().equals(nombreOtro)) {
                        otro = i;
                        break;
                    }
                } catch (RemoteException re) {
                    re.printStackTrace();
                }
            }
            Accion a = dc.getAccion(numLado, otro, t);
            if (a != null) {
                j.realizaAccion(a);
            }
        } catch (RemoteException re) {
            re.printStackTrace();
        }
    }//GEN-LAST:event_jbComercioActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JButton jbCambio;
    private javax.swing.JButton jbCamino;
    private javax.swing.JButton jbCiudad;
    private javax.swing.JButton jbComercio;
    private javax.swing.JButton jbDados;
    private javax.swing.JButton jbFin;
    private javax.swing.JButton jbPoblado;
    private javax.swing.JButton jbSoldado;
    private javax.swing.JComboBox jcbCambio;
    private javax.swing.JComboBox jcbComercio;
    private javax.swing.JLabel jlNombreJugador;
    private javax.swing.JLabel jlPuntos;
    private javax.swing.JPanel jpAcciones;
    private javax.swing.JPanel jpCabecera;
    private javax.swing.JPanel jpDesarrollo;
    private javax.swing.JPanel jpPrincipal;
    private javax.swing.JPanel jpRecursos;
    private javax.swing.JPanel jpRelleno;
    // End of variables declaration//GEN-END:variables
}
