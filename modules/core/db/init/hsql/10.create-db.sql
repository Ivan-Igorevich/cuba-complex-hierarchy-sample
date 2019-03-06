-- begin PARENTCHILDREN_PROD
create table PARENTCHILDREN_PROD (
    ID varchar(36) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    NAME varchar(255),
    --
    primary key (ID)
)^
-- end PARENTCHILDREN_PROD
-- begin PARENTCHILDREN_PROD_ENTRY
create table PARENTCHILDREN_PROD_ENTRY (
    ID varchar(36) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    AMNT varchar(255),
    PARENT_ID varchar(36),
    CHILD_ID varchar(36),
    --
    primary key (ID)
)^
-- end PARENTCHILDREN_PROD_ENTRY
