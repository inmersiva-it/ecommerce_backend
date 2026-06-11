package com.ecommerce.ecommerce_backend.service;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class KernelFileLogger {

    /*
     * =========================================================================
     *           FUNDAMENTOS DE SISTEMAS OPERATIVOS - EXAMEN PARCIAL 3
     * =========================================================================
     * CONCEPTO 1: HILOS Y CONCURRENCIA (PROCESSES & THREADS)
     * Utiliza un Thread Pool (ExecutorService) con hilos de trabajo en segundo
     * plano. Esto permite delegar tareas costosas de Entrada/Salida (I/O) sin
     * bloquear el hilo principal (Event Loop / Hilo del Servidor Web HTTP).
     *
     * CONCEPTO 2: INTERACCIÓN CON EL KERNEL Y DISPOSITIVOS (SYSTEM CALLS)
     * La creación y escritura de archivos físicos realiza llamadas al sistema
     * (System Calls: sys_open, sys_write, sys_mkdir) para interactuar con los
     * drivers de almacenamiento administrados por el Kernel del SO.
     *
     * CONCEPTO 3: SINCRONIZACIÓN Y EXCLUSIÓN MUTUA (MUTEX)
     * El método 'log' está decorado con 'synchronized' para garantizar exclusión
     * mutua sobre el descriptor del archivo de log, evitando que escrituras
     * concurrentes corrompan o sobrepongan líneas en el archivo físico.
     * =========================================================================
     */
    private final ExecutorService threadPool = Executors.newFixedThreadPool(2);
    private final String LOG_PATH = "uploads/audit.log";

    @PostConstruct
    public void init() {
        try {
            // System Call para crear directorios
            Files.createDirectories(Paths.get("uploads"));
            log("SISTEMA", "Logger de Kernel (System Calls & Hilos) iniciado correctamente.");
        } catch (IOException e) {
            System.err.println("Error al crear directorio de logs: " + e.getMessage());
        }
    }

    /**
     * Encola la escritura en disco en un hilo del pool de forma asíncrona.
     */
    public void logAsync(String origen, String mensaje) {
        threadPool.submit(() -> {
            log(origen, mensaje);
        });
    }

    /**
     * Escribe la entrada de log en el archivo físico.
     * La palabra clave synchronized actúa como un semáforo/mutex binario de Java.
     */
    private synchronized void log(String origen, String mensaje) {
        try (FileWriter fw = new FileWriter(LOG_PATH, true);
             PrintWriter pw = new PrintWriter(fw)) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
            // Llama internamente a las System Calls de escritura sobre el File System
            pw.printf("[%s] [%s] %s%n", timestamp, origen.toUpperCase(), mensaje);
        } catch (IOException e) {
            System.err.println("Error escribiendo en audit.log: " + e.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() {
        threadPool.shutdown();
    }
}
