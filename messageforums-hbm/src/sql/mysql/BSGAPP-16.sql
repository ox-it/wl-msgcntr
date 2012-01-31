
--////////////BSGAPP-16
--// Add Markup_Free flag to Forums
--////////////////////////////////////////////////////

-- Add functionality to flag forums to only accept Markup_Free Messages (BSGAPP-16)

-- add column to flag Forums as Markup Free
alter table MFR_OPEN_FORUM_T add column (MARKUP_FREE bit);
update MFR_OPEN_FORUM_T set MARKUP_FREE=0 where MARKUP_FREE is NULL;
alter table MFR_OPEN_FORUM_T modify column MARKUP_FREE bit not null;

