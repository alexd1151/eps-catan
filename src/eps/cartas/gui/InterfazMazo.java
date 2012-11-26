/*
 * InterfazMazo.java
 *
 * Created on February 12, 2006, 3:04 PM
 */
package eps.cartas.gui;

import eps.cartas.*;
import eps.catan.BarajaRecursos;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

/**
 * Representa un mazo graficamente. Se pueden elegir varias opciones de
 * representacion. Acepta varias opciones que configuran como se va a
 * mostrar y como se va a poder interaccionar con lo mostrado:<ul>
 * <li>baraja -
 *  La baraja a usar; afecta los dibujos que se muestran. </li>
 * <li>orientacion -
 *  Direccion en la que se colocan las cartas: norte a sur,
 *  este a oeste, etcetera. </li>
 * <li>separacion -
 *  Espacio entre cartas: mucho, poco, o nada</li>
 * <li>mazo visto -
 *  Si se ven las cartas que componen este mazo o se muestra solo el reves. </li>
 * <li>modo de seleccion -
 *  La forma en que se seleccionan estas cartas. Puede que no sean seleccionables,
 *  que solo se pueda seleccionar una, o que se puedan seleccionar varias. </li>
 */
public class InterfazMazo extends JPanel {

	// atributos principales
	/** mazo a mostrar */
	private Mazo mazo;
	/** baraja a la que corresponden sus cartas (util para buscar imagenes) */
	private Baraja baraja;
	/** separacion entre cartas */
	private int separacion = SEPARADAS;
	/** sentido de colocacion de las cartas */
	private int orientacion = ESTE_A_OESTE;
	/** modo de seleccion del mazo */
	private int modoSel = SEL_NO;
	/** si el mazo debe ser visible o no */
	private boolean mazoVisto = true;
	// atributos mas internos
	/** color de las cartas seleccionadas */
	private Color colorSeleccion = Color.RED.darker();
	/** color de las cartas en proceso de seleccion */
	private Color colorCartaActual = Color.RED;
	/** indices de las cartas seleccionadas */
	private TreeSet<Integer> seleccionadas = new TreeSet<Integer>();
	/** ancho de una carta */
	private int ancho = 55;
	/** alto de una carta */
	private int alto = 95;
	/** carta en proceso de seleccion */
	private int cartaActual = -1;
	/** posicionamiento del mazo */
	private int x0, y0, dx, dy, padx, pady;
	/** tam. preferido */
	private Dimension preferredSize = new Dimension();
	/** tam. minimo */
	private Dimension minimumSize = new Dimension();
	;

    // constantes

    /** orientacion: primera carta arriba, resto va bajando */
    public static final int NORTE_A_SUR = 1;
	/** orientacion: primera carta abajo, resto va subiendo */
	public static final int SUR_A_NORTE = 2;
	/** orientacion: primera carta a la izquierda, resto va hacia la derecha */
	public static final int ESTE_A_OESTE = 3;
	/** orientacion: primera carta a la derecha, resto va hacia la izquierda */
	public static final int OESTE_A_ESTE = 4;
	/** separacion: apiladas */
	public static final int APILADAS = 1;
	/** separacion: separadas (pero poquito) */
	public static final int SEPARADAS = 2;
	/** separacion: a ser posible, se muestran del todo */
	public static final int MUY_SEPARADAS = 3;
	/** modo de seleccion: no se pueden seleccionar */
	public static final int SEL_NO = 0;
	/** modo de seleccion: a lo sumo, se puede seleccionar una */
	public static final int SEL_UNA = 1;
	/** modo de seleccion: se pueden seleccionar todas las que se quieran */
	public static final int SEL_MUCHAS = 2;
	/** margen minimo entre las cartas y los lados de la interfaz */
	private static int MARGEN = 6;
	/** distancia maxima entre extremos de cartas en el caso de MUY_SEPARADAS */
	private static int SEPARACION = 10;
	/** lista de MazoListeners interesados en saber cuando cambia la seleccion */
	private ArrayList<MazoListener> mazoListeners = new ArrayList<MazoListener>();

	/**
	 * constructor
	 */
	public InterfazMazo() {
		addMouseListener(new MazoMouseListener());
		estableceDimensiones(0);
	}

	public void addMazoListener(MazoListener ml) {
		mazoListeners.add(ml);
	}

	public void removeMazoListener(MazoListener ml) {
		mazoListeners.remove(ml);
	}

	public void clearMazoListeners() {
		mazoListeners.clear();
	}

	/**
	 * manejador de raton para este componente; permite seleccionar y
	 * deseleccionar cartas con un unico click en cualquier parte de la
	 * carta.
	 */
	private class MazoMouseListener extends MouseInputAdapter {

		private Rectangle r = new Rectangle();
		private Rectangle zonaSeleccion = new Rectangle();

		/** utilidad para en que carta cae una pulsacion */
		private void checkSelected(int x1, int y1) {
			if (cartaActual != -1) {
				r.setBounds(x0 + dx * cartaActual, y0 + dy * cartaActual, ancho, alto);
				if (r.contains(x1, y1)) {
					return;
				}
			}

			cartaActual = -1;
			r.setSize((dx == 0) ? ancho : Math.abs(dx), (dy == 0) ? alto : Math.abs(dy));
			for (int i = 0, x = x0, y = y0; i < mazo.size(); i++, x += dx, y += dy) {
				r.setLocation(x, y);
				if (i == mazo.size() - 1) {
					r.setSize(ancho, alto);
				} else {
					if (dx < 0) {
						r.setLocation(x + ancho + dx, y);
					}
					if (dy < 0) {
						r.setLocation(x, y + alto + dy);
					}
				}
				if (r.contains(x1, y1)) {
					cartaActual = i;
					repaint();
					break;
				}
			}
		}

		/**
		 * llamada cuando se detecta una pulsacion de raton
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			checkSelected(e.getX(), e.getY());
			if (cartaActual != -1) {
				zonaSeleccion.setBounds(
						x0 + dx * cartaActual, y0 + dy * cartaActual,
						ancho, alto);
			}
		}

		/**
		 * llamada cuando se suelta el boton (= click realizado)
		 */
		@Override
		public void mouseReleased(MouseEvent e) {

			if (cartaActual == -1 || modoSel == SEL_NO) {
				return;
			}

			// doble click en una carta: hacer algo al respecto
			if (e.getClickCount() == 2) {
				for (MazoListener ml : mazoListeners) {
					// OJO: 'this' es un MazoMouseListener;
					// 'InterfazMazo.this' es la InterfazMazo que lo contiene
					ml.dobleClickEnCarta(InterfazMazo.this, cartaActual);
				}
			}

			if (modoSel == SEL_MUCHAS) {
				if (seleccionadas.contains(cartaActual)) {
					seleccionadas.remove(cartaActual);
				} else {
					seleccionadas.add(cartaActual);
				}
			}

			if (modoSel == SEL_UNA) {
				if (zonaSeleccion.contains(e.getX(), e.getY())) {
					seleccionadas.clear();
					seleccionadas.add(cartaActual);
				} else {
					seleccionadas.clear();
				}
			}

			for (MazoListener ml : mazoListeners) {
				ml.clickEnCarta(InterfazMazo.this, cartaActual);
			}

			cartaActual = -1;
			repaint();
		}
	}

	/**
	 * Cambia el mazo que se esta mostrando por otro; tambien actualiza la
	 * baraja, y limpia la seleccion que se hubiera realizado. Luego repinta
	 * el componente.
	 * @param mazo el mazo a pintar
	 * @param baraja la baraja a la que pertenecen sus cartas
	 */
	public void setMazo(Mazo mazo) {
		cartaActual = -1;
		seleccionadas.clear();
		this.mazo = mazo;
		this.baraja = mazo.getBaraja();
		repaint();
	}

	/**
	 * Establece dimensiones a solicitar al LayoutManager dentro del cual se haya
	 * situado a este componente, suponiendo un maximo de N cartas con la
	 * separacion y orientacion actuales
	 * @param numero de cartas a pintar
	 */
	public void estableceDimensiones(int n) {
		Dimension nextPrefSize = new Dimension();
		Dimension nextMinSize = new Dimension();

		// separacion
		switch (separacion) {
			case APILADAS: {
				nextMinSize.setSize(ancho + n * 2 + 2 * SEPARACION, alto + n * 2 + 2 * SEPARACION);
				nextPrefSize.setSize(nextMinSize);
				break;
			}
			case SEPARADAS: {
				nextMinSize.setSize(Math.max(n - 1, 0) * ancho / 4 + ancho + 2 * SEPARACION,
						Math.max(n - 1, 0) * alto / 4 + alto + 2 * SEPARACION);
				nextPrefSize.setSize(nextMinSize);
				break;
			}
			case MUY_SEPARADAS: {
				nextMinSize.setSize(Math.max(n - 1, 0) * ancho / 4 + ancho + 2 * SEPARACION,
						Math.max(n - 1, 0) * alto / 4 + alto + 2 * SEPARACION);
				nextPrefSize.setSize(Math.max(n, 1) * (ancho + SEPARACION) + SEPARACION,
						Math.max(n, 1) * (alto + SEPARACION) + SEPARACION);
				break;
			}
		}

		boolean doValidate = !nextPrefSize.equals(preferredSize);
		preferredSize.setSize(nextPrefSize);
		minimumSize.setSize(nextMinSize);

		// signal size pref change
		if (doValidate && getParent() != null) {
			getParent().validate();
		}
	}

	/**
	 * Usada para recalcular donde se deben pintar las cartas, en funcion del
	 * espacio disponible, sus medidas, la orientacion del mazo, y si se
	 * deben separar mucho o no.
	 */
	private void refrescaDisposicion() {

		int n = (mazo == null) ? 0 : mazo.size();

		x0 = getWidth() - ancho;
		y0 = getHeight() - alto;
		if (n > 0) {
			dx = x0 / n;
			dy = y0 / n;
		}

		// separacion
		switch (separacion) {
			case APILADAS: {
				dx = 2;
				dy = 2;
				break;
			}
			case SEPARADAS: {
				if (dx > 0) {
					dx = Math.min(ancho / 4, dx);
				}
				if (dy > 0) {
					dy = Math.min(alto / 4, dy);
				}
				break;
			}
			case MUY_SEPARADAS: {
				if (dx > 0) {
					dx = Math.min(ancho + SEPARACION, dx);
				}
				if (dy > 0) {
					dy = Math.min(alto + SEPARACION, dy);
				}
				break;
			}
		}
		padx = MARGEN;
		pady = MARGEN;

		// orientacion
		switch (orientacion) {
			case NORTE_A_SUR:
				dx = 0;
				x0 /= 2;
				y0 = pady;
				break;
			case SUR_A_NORTE:
				dx = 0;
				x0 /= 2;
				dy *= -1;
				y0 -= pady;
				break;
			case OESTE_A_ESTE:
				dy = 0;
				y0 /= 2;
				x0 = padx;
				break;
			case ESTE_A_OESTE:
				dy = 0;
				y0 /= 2;
				dx *= -1;
				x0 -= padx;
				break;
		}
	}

	/**
	 * Dibuja una carta, incluyendo un cerco de color si esta seleccionada,
	 * y un reborde negro.
	 *
	 * @param g el entorno grafico en el que dibujarla
	 * @param x su posicion en horizontal
	 * @param y su posicion en vertical
	 * @param c la carta en cuestion
	 */
	private void dibujaCarta(Graphics g, int x, int y, Carta c, int ic) {

		Image img = mazoVisto
				? baraja.imagenParaCarta(c)
				: baraja.imagenReverso();

		g.drawImage(img, x, y, ancho, alto, this);
		if (seleccionadas.contains(ic)) {
			g.setColor(colorSeleccion);
			g.drawRoundRect(x, y, ancho, alto, 6, 6);
			g.drawRoundRect(x + 1, y + 1, ancho - 2, alto - 2, 5, 5);
			g.setColor(Color.BLACK);
		} else {
			g.drawRoundRect(x, y, ancho, alto, 6, 6);
		}
	}

	/**
	 * Pinta este mazo entero
	 * @param g el entorno grafico a usar
	 */
	@Override
	public void paint(Graphics g) {

		// esto limpia el fondo (no hace nada si el fondo esta a 'null')
		if (getBackground() != null) {
			super.paint(g);
		}

		// recalcula donde dibujar cada carta
		refrescaDisposicion();

		//g.setColor(Color.GRAY);
		//g.drawRect(0, 0, getWidth()-1, getHeight()-1);

		// mazo vacio: la forma de una carta, pero sin pintar nada dentro
		if (mazo.isEmpty()) {
			g.drawRoundRect(x0, y0, ancho, alto, 6, 6);
			return;
		}

		// pinta las cartas
		for (int i = 0, x = x0, y = y0; i < mazo.size(); i++, x += dx, y += dy) {
			dibujaCarta(g, x, y, mazo.cartaEn(i), i);
		}

		// si hay alguna en proceso de seleccion, la pinta encima del resto
		if (cartaActual != -1) {
			if (cartaActual >= mazo.size()) {
				cartaActual = -1;
			} else {
				int x = x0 + dx * cartaActual;
				int y = y0 + dy * cartaActual;
				dibujaCarta(g, x, y, mazo.cartaEn(cartaActual), cartaActual);
				g.setColor(colorCartaActual);
				g.drawRoundRect(x - 2, y - 2, ancho + 4, alto + 4, 9, 9);
				g.setColor(Color.BLACK);
			}
		}
	}

	/**
	 * devuelve el tipo de separacion entre cartas (APILADAS, SEPARADAS, ...)
	 * @return el tipo de separacion entre cartas
	 */
	public int getSeparacion() {
		return separacion;
	}

	/**
	 * modifica el tipo de separacion entre cartas (APILADAS, SEPARADAS, ...)
	 * @param separacion el tipo de separacion entre cartas
	 */
	public void setSeparacion(int separacion) {
		this.separacion = separacion;
	}

	/**
	 * devuelve la orientacion en la que se colocan las cartas (NORTE_A_SUR...)
	 * @return la orientacion en la que se colocan las cartas
	 */
	public int getOrientacion() {
		return orientacion;
	}

	/**
	 * cambia la orientacion en la que se colocan las cartas (NORTE_A_SUR...)
	 * @param orientacion la orientacion en la que se colocan las cartas
	 */
	public void setOrientacion(int orientacion) {
		this.orientacion = orientacion;
	}

	/**
	 * si se puede o no ver las cartas que componen el mazo
	 * @return si true, es que se ven las cartas
	 */
	public boolean isMazoVisto() {
		return mazoVisto;
	}

	/**
	 * cambia si se puede o no ver las cartas que componen el mazo
	 * @param mazoVisto: si true, es que se ven las cartas
	 */
	public void setMazoVisto(boolean mazoVisto) {
		this.mazoVisto = mazoVisto;
	}

	/**
	 * cambia el modo de seleccion de las cartas (SEL_NO, SEL_UNA, SEL_MUCHAS)
	 * @param modoSel el modo de seleccion de esta interfaz de mazo
	 */
	public void setModoSel(int modoSel) {
		this.modoSel = modoSel;
	}

	/**
	 * permite hacer pruebas con modos de seleccion...
	 */
	public static void main(String[] args) {

		Mazo mz = BarajaRecursos.getInstance().generaMazo();
		for (int i = 0; i < 32; i++) {
			mz.sacaPrimera();
		}
		JFrame jf = new JFrame();
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JTabbedPane jtp = new JTabbedPane();
		for (int i = 1; i <= 3; i++) {
			InterfazMazo im = new InterfazMazo();
			im.setOrientacion(OESTE_A_ESTE);
			im.setSeparacion(MUY_SEPARADAS);
			im.setModoSel(i);
			im.setMazo(mz);
			im.estableceDimensiones(mz.size());
			jtp.add("" + i + "", im);
		}
		jf.getRootPane().setLayout(new BorderLayout());
		jf.getRootPane().add(jtp, BorderLayout.CENTER);
		jf.setSize(400, 400);
		jf.setVisible(true);
	}

	/**
	 * Devuelve las dimensiones que se solicitaran al LayoutManager que
	 * contiene esta interfaz de mazo.
	 * @return las dimensiones a solicitar
	 */
	@Override
	public Dimension getPreferredSize() {
		refrescaDisposicion();
		return preferredSize;
	}

	/**
	 * Devuelve las dimensiones minimas que se solicitaran al LayoutManager que
	 * contiene esta interfaz de mazo. No garantizamos que se vea bien el mazo
	 * con menos
	 * @return las dimensiones minimas
	 */
	@Override
	public Dimension getMinimumSize() {
		refrescaDisposicion();
		return minimumSize;
	}

	/**
	 * Devuelve las dimensiones maximas que se solicitaran al LayoutManager que
	 * contiene esta interfaz de mazo. Con mas espacio, realmente no se haria
	 * nada util
	 * @return las dimensiones maximas
	 */
	@Override
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}

	/**
	 * Devuelve las cartas actualmente seleccionadas, en un mazo. El mazo es
	 * una copia, de forma que se puede manipular sin peligro alguno de afectar
	 * la seleccion real.
	 * @return las cartas seleccionadas, dentro de un nuevo mazo
	 */
	public Mazo getSeleccionadas() {
		Mazo m = new Mazo(mazo.getBaraja());
		for (int i : seleccionadas) {
			m.add(mazo.get(i));
		}
		return m;
	}

	/**
	 * Cambia las cartas actualmente seleccionadas. Ignora por completo si
	 * las cartas son seleccionables o no - de forma que se puede usar para
	 * resaltar cartas en cualquier mazo, independientemente de su modo de
	 * seleccion.
	 * @param sel mazo con cartas a seleccionar, null para 'ninguna'
	 */
	public void setSeleccionadas(Collection<Integer> sel) {
		seleccionadas.clear();
		if (sel != null) {
			seleccionadas.addAll(sel);
		}
		repaint();
	}
}
