/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ACME_BANK.jsf.session;

import ACME_BANK.persistence.entities.Transactions;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author WEIQIANG LIANG
 */
@Stateless
public class TransactionsFacade extends AbstractFacade<Transactions> {
    @PersistenceContext(unitName = "ACME_BANK_ACME_MA-web_war_1.0-SNAPSHOTPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public TransactionsFacade() {
        super(Transactions.class);
    }
    
}
