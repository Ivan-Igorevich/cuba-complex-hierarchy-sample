package ru.iovchinnikov.parentchildren.web.treenode;

import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.data.Datasource;
import ru.iovchinnikov.parentchildren.entity.Prod;
import ru.iovchinnikov.parentchildren.entity.ProdEntry;
import ru.iovchinnikov.parentchildren.entity.TreeNode;

import javax.inject.Inject;
import java.util.Map;

public class TreeNodeEdit extends AbstractEditor<TreeNode> {

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
    }
}