package com.alcazaba.facturacion.ui;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;

/**
 * Arranca el toolkit JavaFX una unica vez por JVM, de modo que varias clases
 * de test pueden usarlo sin colisionar (Platform.startup solo puede llamarse
 * una vez). No se ejecuta Platform.exit; las ventanas se ocultan y el toolkit
 * se mantiene vivo hasta que el JVM de surefire termina.
 */
public final class JavaFxTestSupport {

    private static boolean arrancado;

    private JavaFxTestSupport() {
    }

    public static synchronized void arrancarFx() throws Exception {
        if (arrancado) {
            return;
        }
        try {
            CountDownLatch l = new CountDownLatch(1);
            Platform.startup(() -> l.countDown());
            if (!l.await(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("El toolkit JavaFX no arranco en 30 s");
            }
        } catch (IllegalStateException e) {
            // Ya arrancado por otra clase de test en el mismo JVM
        }
        Platform.setImplicitExit(false);
        arrancado = true;
    }
}
