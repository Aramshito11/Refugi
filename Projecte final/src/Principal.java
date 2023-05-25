import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;

public class Principal {
    public static void main(String[] args) {
        try (Connection conn = connection.getConnection()) {
            System.out.println(String.format("Conexió a la bdd %s"+" correcte.", conn.getCatalog()));
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Refugi.establecerConexion();
                    Refugi.crearInterfaz();
                }
            });
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
