package com.tiendaautos;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Punto de arranque de la aplicación. */
public class Main {

    public static void main(String[] args) {
        // Swing debe arrancar en su propio hilo.
        SwingUtilities.invokeLater(() -> {
            try {
                // Estilo visual del sistema (GTK en Linux, Windows en Windows).
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Si falla, se queda con el estilo por defecto.
            }
            new VentanaPrincipal().setVisible(true);
        });
    }
}
