/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ACME_BANK.jpa.controller;

import ACME_BANK.jpa.controller.exceptions.NonexistentEntityException;
import ACME_BANK.jpa.controller.exceptions.PreexistingEntityException;
import ACME_BANK.jpa.controller.exceptions.RollbackFailureException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import ACME_BANK.persistence.entities.Customer;
import ACME_BANK.persistence.entities.Savings;
import ACME_BANK.persistence.entities.Transactions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author WEIQIANG LIANG
 */
public class SavingsJpaController implements Serializable {

    public SavingsJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Savings savings) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (savings.getTransactionsCollection() == null) {
            savings.setTransactionsCollection(new ArrayList<Transactions>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Customer CId = savings.getCId();
            if (CId != null) {
                CId = em.getReference(CId.getClass(), CId.getCId());
                savings.setCId(CId);
            }
            Collection<Transactions> attachedTransactionsCollection = new ArrayList<Transactions>();
            for (Transactions transactionsCollectionTransactionsToAttach : savings.getTransactionsCollection()) {
                transactionsCollectionTransactionsToAttach = em.getReference(transactionsCollectionTransactionsToAttach.getClass(), transactionsCollectionTransactionsToAttach.getTId());
                attachedTransactionsCollection.add(transactionsCollectionTransactionsToAttach);
            }
            savings.setTransactionsCollection(attachedTransactionsCollection);
            em.persist(savings);
            if (CId != null) {
                CId.getSavingsCollection().add(savings);
                CId = em.merge(CId);
            }
            for (Transactions transactionsCollectionTransactions : savings.getTransactionsCollection()) {
                Savings oldAccnumOfTransactionsCollectionTransactions = transactionsCollectionTransactions.getAccnum();
                transactionsCollectionTransactions.setAccnum(savings);
                transactionsCollectionTransactions = em.merge(transactionsCollectionTransactions);
                if (oldAccnumOfTransactionsCollectionTransactions != null) {
                    oldAccnumOfTransactionsCollectionTransactions.getTransactionsCollection().remove(transactionsCollectionTransactions);
                    oldAccnumOfTransactionsCollectionTransactions = em.merge(oldAccnumOfTransactionsCollectionTransactions);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findSavings(savings.getAccnum()) != null) {
                throw new PreexistingEntityException("Savings " + savings + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Savings savings) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Savings persistentSavings = em.find(Savings.class, savings.getAccnum());
            Customer CIdOld = persistentSavings.getCId();
            Customer CIdNew = savings.getCId();
            Collection<Transactions> transactionsCollectionOld = persistentSavings.getTransactionsCollection();
            Collection<Transactions> transactionsCollectionNew = savings.getTransactionsCollection();
            if (CIdNew != null) {
                CIdNew = em.getReference(CIdNew.getClass(), CIdNew.getCId());
                savings.setCId(CIdNew);
            }
            Collection<Transactions> attachedTransactionsCollectionNew = new ArrayList<Transactions>();
            for (Transactions transactionsCollectionNewTransactionsToAttach : transactionsCollectionNew) {
                transactionsCollectionNewTransactionsToAttach = em.getReference(transactionsCollectionNewTransactionsToAttach.getClass(), transactionsCollectionNewTransactionsToAttach.getTId());
                attachedTransactionsCollectionNew.add(transactionsCollectionNewTransactionsToAttach);
            }
            transactionsCollectionNew = attachedTransactionsCollectionNew;
            savings.setTransactionsCollection(transactionsCollectionNew);
            savings = em.merge(savings);
            if (CIdOld != null && !CIdOld.equals(CIdNew)) {
                CIdOld.getSavingsCollection().remove(savings);
                CIdOld = em.merge(CIdOld);
            }
            if (CIdNew != null && !CIdNew.equals(CIdOld)) {
                CIdNew.getSavingsCollection().add(savings);
                CIdNew = em.merge(CIdNew);
            }
            for (Transactions transactionsCollectionOldTransactions : transactionsCollectionOld) {
                if (!transactionsCollectionNew.contains(transactionsCollectionOldTransactions)) {
                    transactionsCollectionOldTransactions.setAccnum(null);
                    transactionsCollectionOldTransactions = em.merge(transactionsCollectionOldTransactions);
                }
            }
            for (Transactions transactionsCollectionNewTransactions : transactionsCollectionNew) {
                if (!transactionsCollectionOld.contains(transactionsCollectionNewTransactions)) {
                    Savings oldAccnumOfTransactionsCollectionNewTransactions = transactionsCollectionNewTransactions.getAccnum();
                    transactionsCollectionNewTransactions.setAccnum(savings);
                    transactionsCollectionNewTransactions = em.merge(transactionsCollectionNewTransactions);
                    if (oldAccnumOfTransactionsCollectionNewTransactions != null && !oldAccnumOfTransactionsCollectionNewTransactions.equals(savings)) {
                        oldAccnumOfTransactionsCollectionNewTransactions.getTransactionsCollection().remove(transactionsCollectionNewTransactions);
                        oldAccnumOfTransactionsCollectionNewTransactions = em.merge(oldAccnumOfTransactionsCollectionNewTransactions);
                    }
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = savings.getAccnum();
                if (findSavings(id) == null) {
                    throw new NonexistentEntityException("The savings with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(String id) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Savings savings;
            try {
                savings = em.getReference(Savings.class, id);
                savings.getAccnum();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The savings with id " + id + " no longer exists.", enfe);
            }
            Customer CId = savings.getCId();
            if (CId != null) {
                CId.getSavingsCollection().remove(savings);
                CId = em.merge(CId);
            }
            Collection<Transactions> transactionsCollection = savings.getTransactionsCollection();
            for (Transactions transactionsCollectionTransactions : transactionsCollection) {
                transactionsCollectionTransactions.setAccnum(null);
                transactionsCollectionTransactions = em.merge(transactionsCollectionTransactions);
            }
            em.remove(savings);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Savings> findSavingsEntities() {
        return findSavingsEntities(true, -1, -1);
    }

    public List<Savings> findSavingsEntities(int maxResults, int firstResult) {
        return findSavingsEntities(false, maxResults, firstResult);
    }

    private List<Savings> findSavingsEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Savings.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Savings findSavings(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Savings.class, id);
        } finally {
            em.close();
        }
    }

    public int getSavingsCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Savings> rt = cq.from(Savings.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
