package ru.iovchinnikov.parentchildren.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.chile.core.annotations.NamePattern;

@NamePattern("%s|parent")
@Table(name = "PARENTCHILDREN_PROD_ENTRY")
@Entity(name = "parentchildren$ProdEntry")
public class ProdEntry extends StandardEntity {
    private static final long serialVersionUID = 1393350038672952922L;

    @Column(name = "AMNT")
    protected String amnt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    protected Prod parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHILD_ID")
    protected Prod child;

    public void setAmnt(String amnt) {
        this.amnt = amnt;
    }

    public String getAmnt() {
        return amnt;
    }

    public void setParent(Prod parent) {
        this.parent = parent;
    }

    public Prod getParent() {
        return parent;
    }

    public void setChild(Prod child) {
        this.child = child;
    }

    public Prod getChild() {
        return child;
    }


}