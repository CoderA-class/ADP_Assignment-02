package za.ac.cput.adp_assignment02.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*
 * IdSequence.java
 * Keeps the next number for each auto-generated ID prefix (CUS, EMP, PRD, SUP, ORD, ...)
 * so users never have to type IDs themselves.
 */
@Entity
@Table(name = "id_sequence")
public class IdSequence {
    @Id
    @Column(name = "prefix", length = 10)
    private String prefix;

    @Column(nullable = false)
    private long nextValue;

    public IdSequence() {
    }

    public IdSequence(String prefix, long nextValue) {
        this.prefix = prefix;
        this.nextValue = nextValue;
    }

    public String getPrefix() {
        return prefix;
    }

    public long getNextValue() {
        return nextValue;
    }

    public void setNextValue(long nextValue) {
        this.nextValue = nextValue;
    }
}
