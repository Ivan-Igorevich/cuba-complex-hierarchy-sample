-- begin PARENTCHILDREN_PROD_ENTRY
alter table PARENTCHILDREN_PROD_ENTRY add constraint FK_PARENTCHILDREN_PROD_ENTRY_ON_PARENT foreign key (PARENT_ID) references PARENTCHILDREN_PROD(ID)^
alter table PARENTCHILDREN_PROD_ENTRY add constraint FK_PARENTCHILDREN_PROD_ENTRY_ON_CHILD foreign key (CHILD_ID) references PARENTCHILDREN_PROD(ID)^
create index IDX_PARENTCHILDREN_PROD_ENTRY_ON_PARENT on PARENTCHILDREN_PROD_ENTRY (PARENT_ID)^
create index IDX_PARENTCHILDREN_PROD_ENTRY_ON_CHILD on PARENTCHILDREN_PROD_ENTRY (CHILD_ID)^
-- end PARENTCHILDREN_PROD_ENTRY
