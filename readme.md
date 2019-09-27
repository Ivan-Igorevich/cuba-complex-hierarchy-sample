# Подготовка модели данных
Чтобы иметь возможность хранить не только иерархию, но и количество вхождений определённого элемента-наследника в элемент-родитель, недостаточно сформировать иерархию со связью многие-ко-многим. При создании связи будет неявно создана таблица, реализующая эту связь для БД, но появляется необходимость хранить в этой таблице дополнительную информацию, такую как, например, количество вхождений наследника в родителя. Поэтому эту таблицу следует ввести как сущность в приложение, и реализовать две связи один-ко-многим от основной сущности к вспомогательной. Таким образом сущности будут выглядеть следующим образом:
``` java
public class Prod extends StandardEntity {
    @Column(name = "NAME") protected String name;
    @OneToMany(mappedBy = "child") protected List<ProdEntry> parents;
    @OneToMany(mappedBy = "parent") protected List<ProdEntry> children;
}
public class ProdEntry extends StandardEntity {
    @Column(name = "AMNT") protected String amnt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID") protected Prod parent;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHILD_ID") protected Prod child;
}
```

# Подготовка неперсистентного контекста
Для иерархического отображения связи "многие-ко-многим", включающей в себя не только основные сущности, но и вспомогательные сущности с полезной нагрузкой, необходимо создать неперсистентную сущность, объединяющую необходимые для отображения поля обеих сущностей, например
``` java
@NamePattern("%s|prod")
@MetaClass(name = "parentchildren$TreeNode")
public class TreeNode extends BaseUuidEntity {
    @MetaProperty protected Prod prod;
    @MetaProperty protected TreeNode parent;
    @MetaProperty protected Integer amount;
}
```

# Подготовка источника данных
Поскольку основные сущности будут содержать довольно сложную связь через вспомогательную сущность - использовать стандартный источник данных не представляется возможным. Для формирования корректных связей внутри источника данных необходимо переопределить `HierarchicalDatasource`, переопределив в нём метод `loadData(Map<String, Object>)`.

Метод `loadData(Map<String, Object>)` выполняется, когда необходимо загрузить данные из персистентного контекста. Данные для выгрузки сохраняются в поле `LinkedMap data;` где ключ - это id объекта, а значение - сам объект.

``` java
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
```
Таким образом будет создан источник данных, формирующий связи в виртуальном дереве от родителя к наследникам, записывающий необходимые данные в виртуальный узел и подготавливающий этот узел к отображению в таблице. Также необходимо добавить корректный `view`, чтобы источник данных содержал весь перечень ссылок и полей. Для этого в файле `global/views.xml` необходимо явно прописать вью для узла

``` xml
    <view entity="parentchildren$TreeNode"
          name="node-view" extends="_local">
        <property name="prod" view="prod-view"/>
        <property name="parent"/>
        <property name="amount"/>
    </view>
```

# Подготовка экранов
Корректно подготовленный источник данных может быть использован в стандартных компонентах. В данном примере это `<treeTable>`. На форме, содержащей таблицу необходимо подключить источник данных, созданный на предыдущем шаге
``` xml
<hierarchicalDatasource id="prodsDs"
                        class="ru.iovchinnikov.parentchildren.entity.TreeNode"
                        datasourceClass="ru.iovchinnikov.parentchildren.web.prod.ProdDs"
                        view="node-view"
                        hierarchyProperty="parent">
</hierarchicalDatasource>
<treeTable id="prodsTable"
           width="100%">
           <columns>
               <column id="prod.name"/>
               <column id="amount"/>
           </columns>
           <rows datasource="prodsDs"/>
</treeTable>
```

При описании таблицы важно обратить внимание на то, что источник данных, как и сама таблица будут отображать созданную неперсистентную сущность, что вынуждает явно описывать процессы создания, редактирования и удаления.

# Манипуляции с данными
При использовании стандартных действий необходимо учесть то, что при нажатии кнопок таблица будет пытаться выполнить действия соответствующие той сущности, которая отображается в таблице, то есть, в данном случае `@MetaClass TreeNode`. Поэтому стандартные теги `<action>` следует удалить, не изменяя при этом привязку кнопок к этим действиям, и в контроллере, в методе `@Override public void init(Map<String, Object>)` создать для таблицы новые экшны с соответствующими названиями.

## Создание
``` java
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
```
В методе создания важно учесть, что прежде, чем вызвать экран редактирования для вновь созданного объекта, необходимо указать ему текущий выделенный элемент (или `null`) в качестве родителя. Родитель же создаётся не напрямую, а новым объектом `ProdEntry`, и добавлением этой записи во вновь созданный список, который также должен прикрепляться ко вновь создаваемому объекту. И это присваивание родителя важно учесть в экране редактирования объекта `Prod`. Таким образом экран редактирования будет иметь вид
``` java
public class ProdEdit extends AbstractEditor<Prod> {
    private Prod newItem;
    @Override public void init(Map<String, Object> params) {
        super.init(params);
        newItem = (Prod) params.get("prod");
    }

    @Override protected void initNewItem(Prod item) {
        super.initNewItem(item);
        item.setParents(newItem.getParents());
    }
}
```

## Редактирование
При редактировании объекта, важно учесть только тот факт, что редактироваться будет не тот объект, который выделен в строке таблицы, а те, которые содержатся внутри него. Таким образом возмникает несколько путей решения данной задачи - создание отдельного экрана для редактирования всех сущностей, входящих в состав неперсистентного объекта, расширение экрана редактирования неперсистентного объекта, или вызов экрана редактирования основной сущности, предоставив пользовалелю редактировать составные части иерархии самостоятельно, через списки внутри основной сущности. Пример кода ниже демонстрирует третий подход.
``` java
prodsTable.addAction(new EditAction(prodsTable, WindowManager.OpenType.DIALOG, "edit") {
    @Override
    public void actionPerform(Component component) {
        TreeNode thisNode = prodsTable.getSingleSelected();
        Prod p = thisNode != null ? thisNode.getProd() : null;
        AbstractEditor e = openEditor("parentchildren$Prod.edit", p, WindowManager.OpenType.DIALOG);
        e.addCloseWithCommitListener(() -> prodsDs.refresh());
    }
});
```

## Удаление
При удалении объекта из таблицы следует разработать индивидуальную модель поведения, которая будет соответствовать задаче. В примере ниже описано поведение, удаляющее как сам выделенный в таблице объект, так и связи наверх и вниз по иерархии от него. Собственно удаление производит метод `doRemove(HashSet<? extends Entity>);` который нужно вызвать из переопределённого метода `@Override public void actionPerform(Component)`, предварительно сформировав множество из удаляемых объектов.
``` java
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
```
