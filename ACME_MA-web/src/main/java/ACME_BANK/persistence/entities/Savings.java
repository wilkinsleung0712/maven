/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ACME_BANK.persistence.entities;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author WEIQIANG LIANG
 */
@Entity
@Table(name = "ACME_BANK_COPY.SAVINGS")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Savings.findAll", query = "SELECT s FROM Savings s"),
    @NamedQuery(name = "Savings.findByAccnum", query = "SELECT s FROM Savings s WHERE s.accnum = :accnum"),
    @NamedQuery(name = "Savings.findByBalance", query = "SELECT s FROM Savings s WHERE s.balance = :balance")})
public class Savings implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10)
    @Column(name = "ACCNUM")
    private String accnum;
    @Column(name = "BALANCE")
    private Integer balance;
    @OneToMany(mappedBy = "accnum")
    private Collection<Transactions> transactionsCollection;
    @JoinColumn(name = "C_ID", referencedColumnName = "C_ID")
    @ManyToOne
    private Customer cId;

    public Savings() {
    }

    public Savings(String accnum) {
        this.accnum = accnum;
    }

    public String getAccnum() {
        return accnum;
    }

    public void setAccnum(String accnum) {
        this.accnum = accnum;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    @XmlTransient
    public Collection<Transactions> getTransactionsCollection() {
        return transactionsCollection;
    }

    public void setTransactionsCollection(Collection<Transactions> transactionsCollection) {
        this.transactionsCollection = transactionsCollection;
    }

    public Customer getCId() {
        return cId;
    }

    public void setCId(Customer cId) {
        this.cId = cId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (accnum != null ? accnum.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Savings)) {
            return false;
        }
        Savings other = (Savings) object;
        if ((this.accnum == null && other.accnum != null) || (this.accnum != null && !this.accnum.equals(other.accnum))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ACME_BANK.persistence.entities.Savings[ accnum=" + accnum + " ]";
    }
    
}
