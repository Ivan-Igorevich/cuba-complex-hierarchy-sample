package ru.iovchinnikov.parentchildren.entity;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.chile.core.annotations.NamePattern;

@NamePattern("%s|prod")
@MetaClass(name = "parentchildren$TreeNode")
public class TreeNode extends BaseUuidEntity {
    private static final long serialVersionUID = -7904423580062618455L;

    @MetaProperty
    protected Prod prod;

    @MetaProperty
    protected TreeNode parent;

    @MetaProperty
    protected Integer amount;

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getAmount() {
        return amount;
    }


    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }


    public void setProd(Prod prod) {
        this.prod = prod;
    }

    public Prod getProd() {
        return prod;
    }


}