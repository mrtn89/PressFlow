package de.pressflow.service;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.io.Serializable;

/**
 * Gemeinsame Basis aller Services.
 * Stellt den EntityManager bereit und kapselt die Transaktionssteuerung,
 * damit begin/commit/rollback nicht in jeder Methode wiederholt werden müssen.
 */
public abstract class AbstractService implements Serializable {

    private static final long serialVersionUID = 1L;

    // Zugriff auf den Persistenzkontext (pro Request, siehe EntityManagerProducer)
    @Inject
    protected EntityManager entityManager;

    // Führt eine Schreiboperation in einer Transaktion aus; bei einem Fehler wird zurückgerollt
    protected void inTransaction(Runnable work) {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        try {
            work.run();
            transaction.commit();
        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }
}
