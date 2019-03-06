package ru.iovchinnikov.parentchildren.web.prod;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import ru.iovchinnikov.parentchildren.entity.Prod;
import ru.iovchinnikov.parentchildren.entity.ProdEntry;
import ru.iovchinnikov.parentchildren.entity.TreeNode;

import javax.inject.Inject;
import java.util.*;

public class ProdHierarchy extends AbstractLookup {

    @Inject private TreeTable<TreeNode> prodsTable;
    @Inject private ProdDs prodsDs;
    @Inject private DataManager dataManager;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        prodsTable.addAction(new CreateAction(prodsTable, WindowManager.OpenType.DIALOG, "create") {
            @Override
            public void actionPerform(Component component) {
                TreeNode thisNode = prodsTable.getSingleSelected();
                Prod thisProd = (thisNode == null) ? null : thisNode.getProd();
                Prod p = metadata.create(Prod.class);
                ArrayList<ProdEntry> pts = new ArrayList<>();
                ProdEntry pe = metadata.create(ProdEntry.class);
                pe.setParent(thisProd);
                pe.setChild(p);
                pe.setAmnt("1");
                pts.add(pe);
                p.setParents(pts);
                AbstractEditor e = openEditor("parentchildren$Prod.edit", p,
                        WindowManager.OpenType.DIALOG, ParamsMap.of("prod", p, "parent", thisProd));
                e.addCloseWithCommitListener(() -> prodsDs.refresh());
            }
        });
        prodsTable.addAction(new EditAction(prodsTable, WindowManager.OpenType.DIALOG, "edit") {
            @Override
            public void actionPerform(Component component) {
                TreeNode thisNode = prodsTable.getSingleSelected();
                Prod p = thisNode != null ? thisNode.getProd() : null;
                AbstractEditor e = openEditor("parentchildren$Prod.edit", p, WindowManager.OpenType.DIALOG);
                e.addCloseWithCommitListener(() -> prodsDs.refresh());

            }
        });
        prodsTable.addAction(new RemoveAction(prodsTable, true, "remove") {
            @Override
            public void actionPerform(Component component) {
                TreeNode thisNode = prodsTable.getSingleSelected();
                Prod p = thisNode != null ? thisNode.getProd() : null;
                List<ProdEntry> lst = dataManager.loadList(
                        LoadContext.create(ProdEntry.class)
                                .setQuery(LoadContext.createQuery("select e from parentchildren$ProdEntry e where e.child.id = :pid or e.parent.id = :pid")
                                        .setParameter("pid", p)));
                HashSet<Entity> set = new HashSet<>(lst);
                set.add(p);
                doRemove(set, true);
                prodsDs.refresh();
            }
        });
    }

    @Override
    public void ready() {
        super.ready();

        //prodsTableEdit.setWindowId("parentchildren$TreeNode.edit");
    }
}