package de.pressflow.config;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.io.Serializable;

/**
 * Stellt per CDI einen EntityManager pro Request bereit.
 * Übernommen aus Require4Testing und auf den jakarta-Namespace umgestellt.
 */
@ApplicationScoped
public class EntityManagerProducer implements Serializable {

    private static final long serialVersionUID = 1L;

    // Name der Persistence Unit aus persistence.xml
    private static final String PERSISTENCE_UNIT = "pressflowPU";

    // Zentrale Factory für EntityManager-Instanzen
    private volatile EntityManagerFactory entityManagerFactory;

    // Lazy Initialisierung der Factory beim ersten Zugriff
    private EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null) {
            synchronized (this) {
                if (entityManagerFactory == null) {
                    entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
                }
            }
        }
        return entityManagerFactory;
    }

    // Pro Request einen eigenen EntityManager bereitstellen
    @Produces
    @RequestScoped
    public EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    // EntityManager nach Ende des Requests schließen
    public void closeEntityManager(@Disposes EntityManager entityManager) {
        if (entityManager != null && entityManager.isOpen()) {
            entityManager.close();
        }
    }

    // Factory beim Herunterfahren der Anwendung freigeben
    @PreDestroy
    public void destroy() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }
}
