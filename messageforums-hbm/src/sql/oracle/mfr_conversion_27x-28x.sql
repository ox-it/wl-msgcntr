--MSGCNTR-309
--Start and End dates on Forums and Topics
alter table MFR_AREA_T add (AVAILABILITY_RESTRICTED NUMBER(1,0));
update MFR_AREA_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is NULL;
alter table MFR_AREA_T modify (AVAILABILITY_RESTRICTED NUMBER(1,0) default 0 not null );

alter table MFR_AREA_T add (AVAILABILITY NUMBER(1,0));
update MFR_AREA_T set AVAILABILITY=0 where AVAILABILITY is NULL;
alter table MFR_AREA_T modify (AVAILABILITY NUMBER(1,0) default 0 not null);

alter table MFR_AREA_T add (OPEN_DATE timestamp);

alter table MFR_AREA_T add (CLOSE_DATE timestamp);


alter table MFR_OPEN_FORUM_T add (AVAILABILITY_RESTRICTED NUMBER(1,0));
update MFR_OPEN_FORUM_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is NULL;
alter table MFR_OPEN_FORUM_T modify (AVAILABILITY_RESTRICTED NUMBER(1,0) default 0 not null );

alter table MFR_OPEN_FORUM_T add (AVAILABILITY NUMBER(1,0));
update MFR_OPEN_FORUM_T set AVAILABILITY=0 where AVAILABILITY is NULL;
alter table MFR_OPEN_FORUM_T modify (AVAILABILITY NUMBER(1,0) default 0 not null );

alter table MFR_OPEN_FORUM_T add (OPEN_DATE timestamp);

alter table MFR_OPEN_FORUM_T add (CLOSE_DATE timestamp);

alter table MFR_TOPIC_T add (AVAILABILITY_RESTRICTED NUMBER(1,0));
update MFR_TOPIC_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is NULL;
alter table MFR_TOPIC_T modify (AVAILABILITY_RESTRICTED NUMBER(1,0) default 0 not null );

alter table MFR_TOPIC_T add (AVAILABILITY NUMBER(1,0));
update MFR_TOPIC_T set AVAILABILITY=0 where AVAILABILITY is NULL;
alter table MFR_TOPIC_T modify (AVAILABILITY NUMBER(1,0) default 0 not null );

alter table MFR_TOPIC_T add (OPEN_DATE timestamp);

alter table MFR_TOPIC_T add (CLOSE_DATE timestamp);


--MSGCNTR-355
insert into MFR_TOPIC_T (UUID, MODERATED, AUTO_MARK_THREADS_READ, SORT_INDEX, MUTABLE, TOPIC_DTYPE, VERSION, CREATED, CREATED_BY, MODIFIED, MODIFIED_BY, TITLE, SHORT_DESCRIPTION, EXTENDED_DESCRIPTION, TYPE_UUID, pf_surrogateKey, USER_ID)

    select UUID, MODERATED, AUTO_MARK_THREADS_READ, 3 as SORT_INDEX, 0 as MUTABLE, TOPIC_DTYPE, VERSION, CREATED, CREATED_BY, MODIFIED, MODIFIED_BY, TITLE, SHORT_DESCRIPTION, EXTENDED_DESCRIPTION, TYPE_UUID, pf_surrogateKey, USER_ID from (
                    select count(*) as c1, SYS_GUID() as UUID, mtt.MODERATED, mtt.AUTO_MARK_THREADS_READ, mtt.TOPIC_DTYPE, 0 as VERSION, mtt.CREATED, mtt.CREATED_BY, mtt.MODIFIED, mtt.MODIFIED_BY, 'pvt_drafts' as TITLE, 'short-desc' as SHORT_DESCRIPTION, 'ext-desc' as EXTENDED_DESCRIPTION, mtt.TYPE_UUID, mtt.pf_surrogateKey, mtt.USER_ID
                    from MFR_PRIVATE_FORUM_T mpft, MFR_TOPIC_T mtt
                    where mpft.ID = mtt.pf_surrogateKey and mpft.TYPE_UUID = mtt.TYPE_UUID
                    Group By mtt.USER_ID, mtt.pf_surrogateKey) s1
    where s1.c1 = 3;
    
    

--MSGCNTR-360
--Hibernate could have missed this index, if this fails, then the index may already be in the table
CREATE INDEX user_type_context_idx ON MFR_PVT_MSG_USR_T ( USER_ID, TYPE_UUID, CONTEXT_ID, READ_STATUS);
