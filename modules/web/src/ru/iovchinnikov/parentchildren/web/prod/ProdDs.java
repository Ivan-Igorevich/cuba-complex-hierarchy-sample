package ru.iovchinnikov.parentchildren.web.prod;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.KeyValueEntity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.data.*;
import com.haulmont.cuba.gui.data.impl.HierarchicalDatasourceImpl;
import ru.iovchinnikov.parentchildren.entity.TreeNode;

import java.util.*;
import java.util.stream.Collectors;

public class ProdDs<T extends Entity<K>, K>
        extends HierarchicalDatasourceImpl<T, K>
        implements HierarchicalDatasource<T, K> {

    private DataManager dataManager = AppBeans.get(DataManager.class);
    private Metadata metadata = AppBeans.get(Metadata.class);

    private void createKids(TreeNode parent) {
        List<KeyValueEntity> list = dataManager.loadValues(
                "select pr, pe.amnt from parentchildren$Prod pr left join pr.parents pe where pe.parent.id = :prnt")
                .properties("prod", "amount")
                .parameter("prnt", (parent == null) ? null : parent.getProd())
                .list();

        List<TreeNode> tree = list.stream().map(keyValueEntity -> {
            TreeNode node = metadata.create(TreeNode.class);
            node.setProd(keyValueEntity.getValue("prod"));
            node.setParent(parent);
            String a = (keyValueEntity.getValue("amount") != null) ? keyValueEntity.getValue("amount") : "1";
            node.setAmount(Integer.parseInt(a));
            return node;
        }).collect(Collectors.toList());

        for (TreeNode node : tree) {
            data.put(node.getId(), node);
            createKids(node);
        }
    }

    @Override
    protected void loadData(Map<String, Object> params) {
        super.loadData(params);
        createKids(null);
    }
}
