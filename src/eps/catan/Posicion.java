package eps.catan;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * Un punto, dado por coordenadas X e Y.
 */
public class Posicion implements Serializable {
       
    private int x;
    private int y;
        
    public Posicion() {
        this(0, 0);
    }
    
    public Posicion(Posicion p) {
        this(p.x, p.y);
    }

    public Posicion(String s) {
        StringTokenizer st = new StringTokenizer(s, "(), ");
        this.x = Integer.parseInt(st.nextToken()); 
        this.y = Integer.parseInt(st.nextToken());
    }
    
    public Posicion(int x, int y) {
        this.x = x;
        this.y = y;
    }
           
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }
        
    public boolean equals(Object o) {
        if (o instanceof Posicion) {
            Posicion p = (Posicion)o;
            return p.x == x && p.y == y;
        }
        return false;
    }

    public int hashCode() {
        return this.toString().hashCode();
    }
    
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
