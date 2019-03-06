package ru.iovchinnikov.parentchildren.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.chile.core.annotations.NamePattern;
import java.util.List;
import javax.persistence.OneToMany;

@NamePattern("%s|name")
@Table(name = "PARENTCHILDREN_PROD")
@Entity(name = "parentchildren$Prod")
public class Prod extends StandardEntity {
    private static final long serialVersionUID = 6477279029232852075L;

    @Column(name = "NAME")
    protected String name;

    @OneToMany(mappedBy = "child")
    protected List<ProdEntry> parents;

    @OneToMany(mappedBy = "parent")
    protected List<ProdEntry> children;

    public void setParents(List<ProdEntry> parents) {
        this.parents = parents;
    }

    public List<ProdEntry> getParents() {
        return parents;
    }


    public void setChildren(List<ProdEntry> children) {
        this.children = children;
    }

    public List<ProdEntry> getChildren() {
        return children;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


}