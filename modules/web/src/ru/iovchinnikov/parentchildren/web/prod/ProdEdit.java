package ru.iovchinnikov.parentchildren.web.prod;

import com.haulmont.cuba.gui.components.AbstractEditor;
import ru.iovchinnikov.parentchildren.entity.Prod;

import java.util.Map;

public class ProdEdit extends AbstractEditor<Prod> {
    private Prod newItem;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        newItem = (Prod) params.get("prod");
    }

    @Override
    protected void initNewItem(Prod item) {
        super.initNewItem(item);
        item.setParents(newItem.getParents());
    }
}