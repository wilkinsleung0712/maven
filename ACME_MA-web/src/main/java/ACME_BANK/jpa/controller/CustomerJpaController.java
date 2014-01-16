/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ACME_BANK.jpa.controller;

import ACME_BANK.jpa.controller.exceptions.NonexistentEntityException;
import ACME_BANK.jpa.controller.exceptions.RollbackFailureException;
import ACME_BANK.persistence.entities.Customer;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import ACME_BANK.persistence.entities.Savings;
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
public class CustomerJpaController implements Serializable {

    public CustomerJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Customer customer) throws RollbackFailureException, Exception {
        if (customer.getSavingsCollection() == null) {
            customer.setSavingsCollection(new ArrayList<Savings>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Collection<Savings> attachedSavingsCollection = new ArrayList<Savings>();
            for (Savings savingsCollectionSavingsToAttach : customer.getSavingsCollection()) {
                savingsCollectionSavingsToAttach = em.getReference(savingsCollectionSavingsToAttach.getClass(), savingsCollectionSavingsToAttach.getAccnum());
                attachedSavingsCollection.add(savingsCollectionSavingsToAttach);
            }
            customer.setSavingsCollection(attachedSavingsCollection);
            em.persist(customer);
            for (Savings savingsCollectionSavings : customer.getSavingsCollection()) {
                Customer oldCIdOfSavingsCollectionSavings = savingsCollectionSavings.getCId();
                savingsCollectionSavings.setCId(customer);
                savingsCollectionSavings = em.merge(savingsCollectionSavings);
                if (oldCIdOfSavingsCollectionSavings != null) {
                    oldCIdOfSavingsCollectionSavings.getSavingsCollection().remove(savingsCollectionSavings);
                    oldCIdOfSavingsCollectionSavings = em.merge(oldCIdOfSavingsCollectionSavings);
                }
            }
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

    public void edit(Customer customer) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Customer persistentCustomer = em.find(Customer.class, customer.getCId());
            Collection<Savings> savingsCollectionOld = persistentCustomer.getSavingsCollection();
            Collection<Savings> savingsCollectionNew = customer.getSavingsCollection();
            Collection<Savings> attachedSavingsCollectionNew = new ArrayList<Savings>();
            for (Savings savingsCollectionNewSavingsToAttach : savingsCollectionNew) {
                savingsCollectionNewSavingsToAttach = em.getReference(savingsCollectionNewSavingsToAttach.getClass(), savingsCollectionNewSavingsToAttach.getAccnum());
                attachedSavingsCollectionNew.add(savingsCollectionNewSavingsToAttach);
            }
            savingsCollectionNew = attachedSavingsCollectionNew;
            customer.setSavingsCollection(savingsCollectionNew);
            customer = em.merge(customer);
            for (Savings savingsCollectionOldSavings : savingsCollectionOld) {
                if (!savingsCollectionNew.contains(savingsCollectionOldSavings)) {
                    savingsCollectionOldSavings.setCId(null);
                    savingsCollectionOldSavings = em.merge(savingsCollectionOldSavings);
                }
            }
            for (Savings savingsCollectionNewSavings : savingsCollectionNew) {
                if (!savingsCollectionOld.contains(savingsCollectionNewSavings)) {
                    Customer oldCIdOfSavingsCollectionNewSavings = savingsCollectionNewSavings.getCId();
                    savingsCollectionNewSavings.setCId(customer);
                    savingsCollectionNewSavings = em.merge(savingsCollectionNewSavings);
                    if (oldCIdOfSavingsCollectionNewSavings != null && !oldCIdOfSavingsCollectionNewSavings.equals(customer)) {
                        oldCIdOfSavingsCollectionNewSavings.getSavingsCollection().remove(savingsCollectionNewSavings);
                        oldCIdOfSavingsCollectionNewSavings = em.merge(oldCIdOfSavingsCollectionNewSavings);
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
                Integer id = customer.getCId();
                if (findCustomer(id) == null) {
                    throw new NonexistentEntityException("The customer with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Customer customer;
            try {
                customer = em.getReference(Customer.class, id);
                customer.getCId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The customer with id " + id + " no longer exists.", enfe);
            }
            Collection<Savings> savingsCollection = customer.getSavingsCollection();
            for (Savings savingsCollectionSavings : savingsCollection) {
                savingsCollectionSavings.setCId(null);
                savingsCollectionSavings = em.merge(savingsCollectionSavings);
            }
            em.remove(customer);
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

    public List<Customer> findCustomerEntities() {
        return findCustomerEntities(true, -1, -1);
    }

    public List<Customer> findCustomerEntities(int maxResults, int firstResult) {
        return findCustomerEntities(false, maxResults, firstResult);
    }

    private List<Customer> findCustomerEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Customer.class));
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

    public Customer findCustomer(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Customer.class, id);
        } finally {
            em.close();
        }
    }

    public int getCustomerCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Customer> rt = cq.from(Customer.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
