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
import ACME_BANK.persistence.entities.Savings;
import ACME_BANK.persistence.entities.Transactions;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author WEIQIANG LIANG
 */
public class TransactionsJpaController implements Serializable {

    public TransactionsJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Transactions transactions) throws PreexistingEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Savings accnum = transactions.getAccnum();
            if (accnum != null) {
                accnum = em.getReference(accnum.getClass(), accnum.getAccnum());
                transactions.setAccnum(accnum);
            }
            em.persist(transactions);
            if (accnum != null) {
                accnum.getTransactionsCollection().add(transactions);
                accnum = em.merge(accnum);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findTransactions(transactions.getTId()) != null) {
                throw new PreexistingEntityException("Transactions " + transactions + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Transactions transactions) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Transactions persistentTransactions = em.find(Transactions.class, transactions.getTId());
            Savings accnumOld = persistentTransactions.getAccnum();
            Savings accnumNew = transactions.getAccnum();
            if (accnumNew != null) {
                accnumNew = em.getReference(accnumNew.getClass(), accnumNew.getAccnum());
                transactions.setAccnum(accnumNew);
            }
            transactions = em.merge(transactions);
            if (accnumOld != null && !accnumOld.equals(accnumNew)) {
                accnumOld.getTransactionsCollection().remove(transactions);
                accnumOld = em.merge(accnumOld);
            }
            if (accnumNew != null && !accnumNew.equals(accnumOld)) {
                accnumNew.getTransactionsCollection().add(transactions);
                accnumNew = em.merge(accnumNew);
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
                String id = transactions.getTId();
                if (findTransactions(id) == null) {
                    throw new NonexistentEntityException("The transactions with id " + id + " no longer exists.");
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
            Transactions transactions;
            try {
                transactions = em.getReference(Transactions.class, id);
                transactions.getTId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The transactions with id " + id + " no longer exists.", enfe);
            }
            Savings accnum = transactions.getAccnum();
            if (accnum != null) {
                accnum.getTransactionsCollection().remove(transactions);
                accnum = em.merge(accnum);
            }
            em.remove(transactions);
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

    public List<Transactions> findTransactionsEntities() {
        return findTransactionsEntities(true, -1, -1);
    }

    public List<Transactions> findTransactionsEntities(int maxResults, int firstResult) {
        return findTransactionsEntities(false, maxResults, firstResult);
    }

    private List<Transactions> findTransactionsEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Transactions.class));
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

    public Transactions findTransactions(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Transactions.class, id);
        } finally {
            em.close();
        }
    }

    public int getTransactionsCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Transactions> rt = cq.from(Transactions.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
