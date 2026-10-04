package com.tiendaautos;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.tiendaautos.ui.FramePrincipal;

/**
 * Punto de entrada de la aplicación.
 * Sistema de gestión para una tienda/concesionaria de autos hecho con Java Swing.
 * Solo usa Java SE (JDK estándar): sin Spring Boot y sin librerías externas.
 */
public final class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            instalarLookAndFeel();
            FramePrincipal frame = new FramePrincipal();
            frame.setVisible(true);
        });
    }

    /** Usa el Look & Feel del sistema (GTK en Linux, Windows en Windows 11). */
    private static void instalarLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si falla, se usa el Look & Feel por defecto (Metal).
        }
    }

    private Main() {
    }
}
