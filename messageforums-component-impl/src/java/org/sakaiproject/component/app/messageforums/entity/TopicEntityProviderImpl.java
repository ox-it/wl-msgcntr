package org.sakaiproject.component.app.messageforums.entity;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.DBMembershipItem;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.OpenForum;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.PrivateTopic;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.entity.DecoratedForumInfo;
import org.sakaiproject.api.app.messageforums.entity.DecoratedTopicInfo;
import org.sakaiproject.api.app.messageforums.entity.ForumMessageEntityProvider;
import org.sakaiproject.api.app.messageforums.entity.TopicEntityProvider;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateTopicImpl;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.PropertyProvideable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestAware;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestStorable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;

public class TopicEntityProviderImpl implements TopicEntityProvider,
AutoRegisterEntityProvider, PropertyProvideable, RESTful, RequestStorable, RequestAware, ActionsExecutable {

	private DiscussionForumManager forumManager;
	private UIPermissionsManager uiPermissionsManager;
	private MessageForumsMessageManager messageManager;
	private MessageForumsTypeManager typeManager;
	private PrivateMessageManager privateMessageManager;
	private static final ResourceLoader rb = new ResourceLoader("org.sakaiproject.api.app.messagecenter.bundle.Messages");
	private static final Log LOG = LogFactory.getLog(TopicEntityProviderImpl.class);
	public static final String PVTMSG_MODE_DRAFT = "Drafts";

	private RequestStorage requestStorage;
	public void setRequestStorage(RequestStorage requestStorage) {
		this.requestStorage = requestStorage;
	}

	private RequestGetter requestGetter;
    public void setRequestGetter(RequestGetter requestGetter){
    	this.requestGetter = requestGetter;
    }
	
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	public boolean entityExists(String id) {
		Topic topic = null;
		try {
			topic = forumManager.getTopicById(new Long(id));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return (topic != null);
	}

	public List<String> findEntityRefs(String[] prefixes, String[] name, String[] searchValue,
			boolean exactMatch) {
		List<String> rv = new ArrayList<String>();

		String forumId = null;
		String userId = null;
		String siteId = null;

		if (ENTITY_PREFIX.equals(prefixes[0])) {

			for (int i = 0; i < name.length; i++) {
				if ("context".equalsIgnoreCase(name[i]) || "site".equalsIgnoreCase(name[i]))
					siteId = searchValue[i];
				else if ("user".equalsIgnoreCase(name[i]) || "userId".equalsIgnoreCase(name[i]))
					userId = searchValue[i];
				else if ("parentReference".equalsIgnoreCase(name[i])) {
					String[] parts = searchValue[i].split("/");
					forumId = parts[parts.length - 1];
				}
			}
			String siteRef = siteId;
			if(siteRef != null && !siteRef.startsWith("/site/")){
				siteRef = "/site/" + siteRef;
			}
			// TODO: need a way to generate the url with out having siteId in search
			if (forumId != null && userId != null) {
				DiscussionForum forum = forumManager.getForumByIdWithTopics(new Long(forumId));
				List<Topic> topics = forum.getTopics();
				for (int i = 0; i < topics.size(); i++) {
					// TODO: authz is way too basic, someone more hip to message center please improve...
					//This should also allow people with read access to an item to link to it
					if (forumManager.isInstructor(userId, siteRef)
							|| userId.equals(topics.get(i).getCreatedBy()))
						rv.add("/" + ENTITY_PREFIX + "/" + topics.get(i).getId().toString());
				}
			}
			else if (siteId != null && userId != null) {
				List<DiscussionForum> forums = forumManager.getDiscussionForumsByContextId(siteId);
				for (int i = 0; i < forums.size(); i++) {
					List<Topic> topics = forums.get(i).getTopics();
					for (int j = 0; j < topics.size(); j++) {
						// TODO: authz is way too basic, someone more hip to message center please improve...
						//This should also allow people with read access to an item to link to it
						if (forumManager.isInstructor(userId, siteRef)
								|| userId.equals(topics.get(j).getCreatedBy()))
							rv.add("/" + ENTITY_PREFIX + "/" + topics.get(j).getId().toString());
					}
				}
			}
		}

		return rv;
	}

	public Map<String, String> getProperties(String reference) {
		Map<String, String> props = new HashMap<String, String>();
		Topic topic =
			forumManager.getTopicById(new Long(reference.substring(reference.lastIndexOf("/") + 1)));

		props.put("title", topic.getTitle());
		props.put("author", topic.getCreatedBy());
		if (topic.getCreated() != null)
			props.put("date", DateFormat.getInstance().format(topic.getCreated()));
		if (topic.getModified() != null) {
			props.put("modified_by", topic.getModifiedBy());
			props.put("modified_date", DateFormat.getInstance().format(topic.getModified()));
		}
		props.put("short_description", topic.getShortDescription());
		props.put("description", topic.getExtendedDescription());
		if (topic.getModerated() != null)
			props.put("moderated", topic.getModerated().toString());
		props.put("child_provider", ForumMessageEntityProvider.ENTITY_PREFIX);

		return props;
	}

	public String getPropertyValue(String reference, String name) {
		// TODO: don't be so lazy, just get what we need...
		Map<String, String> props = getProperties(reference);
		return props.get(name);
	}

	public void setPropertyValue(String reference, String name, String value) {
		// This does nothing for now... we could all the setting of many published assessment properties
		// here though... if you're feeling jumpy feel free.
	}

	public void setForumManager(DiscussionForumManager forumManager) {
		this.forumManager = forumManager;
	}

	public String createEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {
		
		System.out.println("ForumTopicEntityPrivider.createEntity");
		
  		String userId = UserDirectoryService.getCurrentUser().getId();
  		if (userId == null || "".equals(userId)){
  			throw new SecurityException("Could not create entity, permission denied: " + ref);
  		}
	 
  		Long topicId = null;
        
      	if (entity.getClass().isAssignableFrom(DecoratedTopicInfo.class)) {
          
      	    try {
      	    	
      	    	DecoratedTopicInfo dTopic = (DecoratedTopicInfo) entity;
      	    	if (topicId == null && dTopic.getTopicId() != null) {
          			topicId = dTopic.getTopicId();
          		}
      	    	
      	    	if (dTopic.getForumId() == null) {
      	    		throw new IllegalArgumentException("Invalid entity for creation, no forum entered");
      	    	}
      	    	DiscussionForum forum = forumManager.getForumById(dTopic.getForumId());
      	    	if (forum == null) {
      	    		throw new IllegalArgumentException("Invalid entity for creation, no forum found");
      	    	}
      	      
      	    	DiscussionTopic topic = forumManager.createTopic(forum);
      	    	if (topic == null) {
      	    		throw new IllegalArgumentException("Invalid entity for creation, failed to create topic");
      	    	}

      	    	String siteId = forumManager.getContextForForumById(forum.getId());
      	    	if (!uiPermissionsManager.isNewTopic(forum, siteId)) {
      	    		throw new SecurityException("Could not create entity, permission denied: " + ref);
      	    	}
      	    	
      	    	StringBuilder alertMsg = new StringBuilder();
      	    	topic.setTitle(FormattedText.processFormattedText(dTopic.getTopicTitle(), alertMsg));
      	    	String shortDescFormatted = FormattedText.processFormattedText(dTopic.getTopicShortDescription(), alertMsg);
      	    	if(shortDescFormatted.length() > 255){
      	    		shortDescFormatted = shortDescFormatted.substring(0, 255);
      	    	}
      	    	topic.setShortDescription(shortDescFormatted);
      	    	topic.setExtendedDescription(FormattedText.processFormattedText(dTopic.getTopicExtendedDescription(), alertMsg));
      	    	
      	    	List<DBMembershipItem> myList = forumManager.getTopicMembershipItems(siteId);
      	    	Set<DBMembershipItem> mySet = new HashSet(myList);
      	    	
      	    	topic.setMembershipItemSet(mySet);
      	    	
      	    	forumManager.saveTopic(topic);
      	    	
          	} catch (Exception e) {
          		e.printStackTrace();
              	throw new SecurityException("Could not create Forum Message, permission denied: " + ref, e);
          	} 
      	    
      	} else {
      		throw new IllegalArgumentException("Invalid entity for creation, must be Topic or DecoratedTopicInfo object");
      	}
      	
      	if (null == topicId) {
      		return null;
      	}
      	return topicId.toString();
	}

	public Object getSampleEntity() {
		return new DecoratedTopicInfo();
	}

	public void updateEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {
		
		String userId = UserDirectoryService.getCurrentUser().getId();
    	if (userId == null || "".equals(userId)){
    		throw new SecurityException("Could not get entity, permission denied: " + ref);
    	}
	  
    	String id = ref.getId();
    	if (id == null || "".equals(id)) {
    		throw new IllegalArgumentException("Cannot update, No id in provided reference: " + ref);
    	}
    	
    	DiscussionTopic topic = forumManager.getTopicById(new Long(ref.getId()));
    	if (null == topic) {
    		throw new IllegalArgumentException("Cannot update, No topic in provided reference: " + ref);
    	}
		DiscussionForum forum = (DiscussionForum)topic.getBaseForum();
		if (forum == null) {
			throw new IllegalArgumentException("Invalid entity for creation, no forum found");
		}
		if (!uiPermissionsManager.isChangeSettings(topic, forum)) {
			throw new SecurityException("Could not get entity, permission denied: " + ref);
		}
    	if (entity.getClass().isAssignableFrom(DecoratedTopicInfo.class)) {
    		// if they instead pass in the DecoratedMessage object
    		DecoratedTopicInfo dTopic = (DecoratedTopicInfo) entity;

  	    	
    		String siteId = forumManager.getContextForForumById(forum.getId());
    		
			StringBuilder alertMsg = new StringBuilder();
			topic.setTitle(FormattedText.processFormattedText(dTopic.getTopicTitle(), alertMsg));
			topic.setShortDescription(FormattedText.processFormattedText(dTopic.getTopicShortDescription(), alertMsg));
			topic.setModified(new Date());
		
    		//if (message.getInReplyTo() != null) {
    			//grab a fresh copy of the message incase it has changes (staleobjectexception)
    		//	message.setInReplyTo(forumManager.getMessageById(message.getInReplyTo().getId()));
    		//}
    		
    		forumManager.saveTopic(topic);
    	}
	}

	public Object getEntity(EntityReference ref) {
		
		DecoratedTopicInfo dTopic = null;
		String userId = UserDirectoryService.getCurrentUser().getId();
		//if (userId == null || "".equals(userId)){
		//	throw new SecurityException("Could not get entity, permission denied: " + ref);
		//}

		DiscussionTopic topic = forumManager.getTopicById(new Long(ref.getId()));
		if (null == topic) {
			throw new IllegalArgumentException("IdUnusedException in Resource Entity Provider");
		}
		
		DiscussionForum forum = forumManager.getForumById(topic.getBaseForum().getId());
	    if (forum == null) {
	    	  throw new IllegalArgumentException("Invalid entity for creation, no forum found");
	    }
	    String siteId = forumManager.getContextForForumById(forum.getId());

		int unreadMessages = 0;
		int totalMessages = 0;
		
		if (topic.getDraft().equals(Boolean.FALSE)) {
			
			if (getUiPermissionsManager().isRead(topic.getId(), topic.getDraft(), forum.getDraft(), userId, siteId)) {
				
				if (!topic.getModerated().booleanValue()
						|| (topic.getModerated().booleanValue() && 
							getUiPermissionsManager().isModeratePostings(topic.getId(), forum.getLocked(), forum.getDraft(), topic.getLocked(), topic.getDraft(), userId, siteId))){

					unreadMessages = getMessageManager().findUnreadMessageCountByTopicIdByUserId(topic.getId(), userId);										
				
				} else {	
					// b/c topic is moderated and user does not have mod perm, user may only
					// see approved msgs or pending/denied msgs authored by user
					unreadMessages = getMessageManager().findUnreadViewableMessageCountByTopicIdByUserId(topic.getId(), userId);
				}
			
				totalMessages = getMessageManager().findViewableMessageCountByTopicIdByUserId(topic.getId(), userId);

				dTopic = new DecoratedTopicInfo(
					topic.getId(), topic.getTitle(), unreadMessages, totalMessages, "", 
					topic.getBaseForum().getId(), topic.getShortDescription(), "");
				
			} else {
				throw new SecurityException("Could not get entity, permission denied: " + ref);
			}
		}
		
		return dTopic;
	}

	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
		
		String userId = UserDirectoryService.getCurrentUser().getId();
		if (userId == null || "".equals(userId)){
		    throw new SecurityException("Could not get entity, permission denied: " + ref);
		}
		  
		String id = ref.getId();
	    if (id == null || "".equals(id)) {
	        throw new IllegalArgumentException("Cannot delete message, No id in provided reference: " + ref);
	    }
	    DiscussionTopic topic = forumManager.getTopicById(new Long(ref.getId()));
		if (null == topic) {
	        throw new IllegalArgumentException("Cannot delete topic, No topic in provided reference: " + ref);
	    }
		  
		DiscussionForum forum = forumManager.getForumById(topic.getBaseForum().getId());
	    if (forum == null) {
	    	  throw new IllegalArgumentException("Invalid entity for creation, no forum found");
	    }
	    	
  		String siteId = forumManager.getContextForForumById(forum.getId());
		  
		if (!uiPermissionsManager.isChangeSettings(topic, forum, siteId)) {
		    throw new IllegalArgumentException("Cannot delete topic, Insufficient Privilages");
	    }

		forumManager.deleteTopic(topic);
	}

	public List<?> getEntities(EntityReference ref, Search search) {
		
		String siteId = null;
		String forumId = null;
		boolean privateMessages = false;
		if (! search.isEmpty()) {
			Restriction locRes = search.getRestrictionByProperty(CollectionResolvable.SEARCH_LOCATION_REFERENCE);
            if (locRes != null) {
                ForumSearch fs = findLocationByReference(locRes.getStringValue());
                siteId = fs.siteId;
                forumId = fs.forumId;
                privateMessages = fs.isPrivate;
            }
		}		
		
		String userId = UserDirectoryService.getCurrentUser().getId();
		if (userId == null || "".equals(userId)) {
			return null;
		}

		List<DecoratedForumInfo> dForums = new ArrayList<DecoratedForumInfo>();


			if(privateMessages && siteId != null){
				
				DecoratedForumInfo dForum = new DecoratedForumInfo(0L, "Messages");

				Area area = getPrivateMessageManager().getPrivateMessageArea(siteId);

				if (area != null) {    
					List aggregateList = new ArrayList();
					PrivateForum pf = getPrivateMessageManager().initializePrivateMessageArea(area, aggregateList, userId, siteId);
					pf = getPrivateMessageManager().initializationHelper(pf, area, userId);
					List pvtTopics = pf.getTopics();
					Collections.sort(pvtTopics, PrivateTopicImpl.TITLE_COMPARATOR);   //changed to date comparator
					

					List topicsbyLocalization= new ArrayList();// only three folder supported, if need more, please modifify here

					String local_received=rb.getString("pvt_received");
					String local_sent = rb.getString("pvt_sent");
					String local_deleted= rb.getString("pvt_deleted");

					String current_NAV= rb.getString("pvt_message_nav");

					topicsbyLocalization.add(local_received);
					topicsbyLocalization.add(local_sent);
					topicsbyLocalization.add(local_deleted);

					int countForFolderNum = 0;// only three folder 
					Iterator iterator = pvtTopics.iterator(); 
					for (int indexlittlethanTHREE=0;indexlittlethanTHREE<3;indexlittlethanTHREE++)//Iterator iterator = pvtTopics.iterator(); iterator.hasNext();)//only three times
					{

						PrivateTopic topic = (PrivateTopic) iterator.next();

						if (topic != null) { 
							
						    String CurrentTopicTitle= topic.getTitle();//folder name

							/** filter topics by context and type*/                                                    
							if (topic.getTypeUuid() != null
									&& topic.getTypeUuid().equals(typeManager.getUserDefinedPrivateTopicType())
									&& topic.getContextId() != null && !topic.getContextId().equals(getPrivateMessageManager().getContextId())){
								continue;
							}       



							String typeUuid="";  // folder uuid
							if(getLanguage(CurrentTopicTitle).toString().equals(getLanguage(current_NAV).toString()))
							{
								typeUuid = getPrivateMessageTypeFromContext(topicsbyLocalization.get(countForFolderNum).toString());

							} else {
								typeUuid = getPrivateMessageTypeFromContext(topic.getTitle());
							}
							
							countForFolderNum++;

							int totalNoMessages = getPrivateMessageManager().findMessageCount(typeUuid, aggregateList);

							int totalUnreadMessages = getPrivateMessageManager().findUnreadMessageCount(typeUuid, aggregateList);

							DecoratedTopicInfo dTopicInfo = new DecoratedTopicInfo(
									topic.getId(), topic.getTitle(),totalUnreadMessages, totalNoMessages, typeUuid, 
									topic.getBaseForum().getId(), topic.getShortDescription(), "");
							dForum.addTopic(dTopicInfo);
						}

					}

					while(iterator.hasNext())//add more folder 
					{
						PrivateTopic topic = (PrivateTopic) iterator.next();
						
						if (topic != null) {

							/** filter topics by context and type*/                                                    
							if (topic.getTypeUuid() != null
									&& topic.getTypeUuid().equals(typeManager.getUserDefinedPrivateTopicType())
									&& topic.getContextId() != null && !topic.getContextId().equals(siteId)){
								continue;
							}       

							String typeUuid = getPrivateMessageTypeFromContext(topic.getTitle());          

							int totalNoMessages = getPrivateMessageManager().findMessageCount(typeUuid, aggregateList);
							int totalUnreadMessages = getPrivateMessageManager().findUnreadMessageCount(typeUuid,aggregateList);

							DecoratedTopicInfo dTopicInfo = new DecoratedTopicInfo(
									topic.getId(), topic.getTitle(),totalUnreadMessages, totalNoMessages, typeUuid, 
									topic.getBaseForum().getId(), topic.getShortDescription(), "");
							dForum.addTopic(dTopicInfo);
						}          

					}
				} 

				dForums.add(dForum);
				
			} else if ((siteId != null && !"".equals(siteId)) || (forumId != null && !"".equals(forumId))) {

				List<DiscussionForum> forums = new ArrayList<DiscussionForum>();
				if(forumId != null && !"".equals(forumId)){
					DiscussionForum forum = forumManager.getForumByIdWithTopics(new Long(forumId));
					siteId = forumManager.getContextForForumById(forum.getId());
					forums.add(forum);
				
				} else {
					forums = forumManager.getDiscussionForumsByContextId(siteId);
				}

				for (DiscussionForum forum : forums) {
					if(forum.getDraft().equals(Boolean.FALSE)){
						DecoratedForumInfo dForum = new DecoratedForumInfo(forum.getId(), forum.getTitle());

						List<DiscussionTopic> topics = forum.getTopics();
						int viewableTopics = 0;

						for (DiscussionTopic topic : topics) {
							if(topic.getDraft().equals(Boolean.FALSE)){

								if (forumManager.isInstructor(userId, siteId) || 
										getUiPermissionsManager().isRead(topic.getId(), topic.getDraft(), forum.getDraft(), userId, siteId))
								{
									int unreadMessages = 0;
									int totalMessages = 0;
									if (!topic.getModerated().booleanValue()
											|| (topic.getModerated().booleanValue() && 
													getUiPermissionsManager().isModeratePostings(topic.getId(), forum.getLocked(), forum.getDraft(), topic.getLocked(), topic.getDraft(), userId, siteId))){

										unreadMessages = getMessageManager().findUnreadMessageCountByTopicIdByUserId(topic.getId(), userId);										
									
									} else {	
										// b/c topic is moderated and user does not have mod perm, user may only
										// see approved msgs or pending/denied msgs authored by user
										unreadMessages = getMessageManager().findUnreadViewableMessageCountByTopicIdByUserId(topic.getId(), userId);
									}
									
									totalMessages = getMessageManager().findViewableMessageCountByTopicIdByUserId(topic.getId(), userId);
									topic.setBaseForum(forum);
									
									dForum.addTopic(
											new DecoratedTopicInfo(topic.getId(), topic.getTitle(), unreadMessages, totalMessages, "", 
													topic.getBaseForum().getId(), topic.getShortDescription(), ""));
									viewableTopics++;
								}						  
							}
						}
						
						// TODO this is a bit too simplistic but will do for now. better to be more restrictive than less at this point
						// "instructor" type users can view all forums. others may view the forum if they can view at least one topic within the forum
						if (forumManager.isInstructor(userId, siteId) || viewableTopics > 0) {
							dForums.add(dForum);
						}
					}
				}
			}
		

		return dForums;
	}
	
	@EntityCustomAction(action="forum",viewKey=EntityView.VIEW_LIST)
    public List<?> getForum(EntityView view, Map<String, Object> params) {
        String forumId = view.getPathSegment(2);
        if (forumId == null) {
        	forumId = (String) params.get("forumId");
            if (forumId == null) {
                throw new IllegalArgumentException("forumId must be set in order to get the forum info, set in params or in the URL /topic/forum/forumId");
            }
        }
        List<?> l = getEntities(new EntityReference(ENTITY_PREFIX, ""), 
                new Search(CollectionResolvable.SEARCH_LOCATION_REFERENCE,"/forum/" + forumId));
        return l;
    }
	
	@EntityCustomAction(action="site",viewKey=EntityView.VIEW_LIST)
    public List<?> getForumsInSite(EntityView view, Map<String, Object> params) {
        String siteId = view.getPathSegment(2);
        if (siteId == null) {
        	siteId = (String) params.get("site");
            if (siteId == null) {
                throw new IllegalArgumentException("site must be set in order to get the forum info, set in params or in the URL /topic/site/siteId");
            }
        }
        List<?> l = getEntities(new EntityReference(ENTITY_PREFIX, ""), 
                new Search(CollectionResolvable.SEARCH_LOCATION_REFERENCE,"/site/" + siteId));
        return l;
    }
	
	@EntityCustomAction(action="private",viewKey=EntityView.VIEW_LIST)
    public List<?> getPrivateTopicsInSite(EntityView view, Map<String, Object> params) {
        String siteId = view.getPathSegment(2);
        if (siteId == null) {
        	siteId = (String) params.get("private");
            if (siteId == null) {
                throw new IllegalArgumentException("siteId must be set in order to get the private topic info, set in params or in the URL /topic/private/siteId");
            }
        }
        List<?> l = getEntities(new EntityReference(ENTITY_PREFIX, ""), 
                new Search(CollectionResolvable.SEARCH_LOCATION_REFERENCE,"/private/" + siteId));
        return l;
    }
	
	/**
     * Find a site (and optionally group) by reference
     * @param locationReference
     * @return a Site and optional group
     * @throws IllegalArgumentException if they cannot be found for this ref
     */
    public ForumSearch findLocationByReference(String locationReference) {
    	ForumSearch holder = new ForumSearch(locationReference);
        if (locationReference.contains("/forum/")) {
            // group membership
        	EntityReference ref = new EntityReference(locationReference);
        	String forumId = ref.getId();
            holder.forumId = forumId;
        } else if (locationReference.contains("/site/")) {
            // site membership
            EntityReference ref = new EntityReference(locationReference);
            String siteId = ref.getId();            
            holder.siteId = siteId;
        } else if(locationReference.contains("/private/")){
        	EntityReference ref = new EntityReference(locationReference);
            String siteId = ref.getId();            
            holder.siteId = siteId;
            holder.isPrivate = true;
        }else{
            throw new IllegalArgumentException("Do not know how to handle this location reference ("+locationReference+"), only can handle forum, site and private references");
        }
        return holder;
    }
    

    
    public static class ForumSearch{
    	public String forumId;
    	public String siteId;
    	public boolean isPrivate = false;
    	public String locationReference;
        public ForumSearch(String locationReference) {
            this.locationReference = locationReference;
        }
    }
	
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.FORM, Formats.XML, Formats.JSON };
	}

	public String[] getHandledInputFormats() {
		return new String[] { Formats.XML, Formats.JSON, Formats.HTML };
	}

	public UIPermissionsManager getUiPermissionsManager() {
		return uiPermissionsManager;
	}

	public void setUiPermissionsManager(UIPermissionsManager uiPermissionsManager) {
		this.uiPermissionsManager = uiPermissionsManager;
	}

	public MessageForumsMessageManager getMessageManager() {
		return messageManager;
	}

	public void setMessageManager(MessageForumsMessageManager messageManager) {
		this.messageManager = messageManager;
	}

	public PrivateMessageManager getPrivateMessageManager() {
		return privateMessageManager;
	}


	public void setPrivateMessageManager(PrivateMessageManager privateMessageManager) {
		this.privateMessageManager = privateMessageManager;
	}
	private String getLanguage(String navName)
	{
		String Tmp= "";
		//getLocale( String userId )
		
		//( userId);//SessionManager.getCurrentSessionUserId() );

		//  List topicsbyLocalization= new ArrayList();// only three folder supported, if need more, please modifify here

		//
		//		  topicsbyLocalization.add(local_received);
		//		  topicsbyLocalization.add(local_sent);
		//		  topicsbyLocalization.add(local_deleted);

		

		if(navName.equals("Received")||navName.equals("Sent")||navName.equals("Deleted"))
		{
			Tmp = "en";
		}
		else if(navName.equals("Recibidos")||navName.equals("Enviados")||navName.equals("Borrados"))
		{

			Tmp ="es";

		}


		else//english language
		{		  
			Tmp="en";		  
		}


		return Tmp;	  
	}

	private String getPrivateMessageTypeFromContext(String navMode){    

		List<String> topicsbyLocalization= new ArrayList<String>();// only three folder supported, if need more, please modifify here


		String local_received=rb.getString("pvt_received");
		String local_sent = rb.getString("pvt_sent");
		String local_deleted= rb.getString("pvt_deleted");
		

		topicsbyLocalization.add(local_received);
		topicsbyLocalization.add(local_sent);
		topicsbyLocalization.add(local_deleted);

		//need to add more dictionary to support more language
		if (((String) topicsbyLocalization.get(0)).equalsIgnoreCase(navMode)||"Recibidos".equalsIgnoreCase(navMode)||"Received".equalsIgnoreCase(navMode)){
			return typeManager.getReceivedPrivateMessageType();
		}
		else if (((String) topicsbyLocalization.get(1)).equalsIgnoreCase(navMode)||"Enviados".equalsIgnoreCase(navMode)||"Sent".equalsIgnoreCase(navMode)){
			return typeManager.getSentPrivateMessageType();
		}
		else if (((String) topicsbyLocalization.get(2)).equalsIgnoreCase(navMode)||"Borrados".equalsIgnoreCase(navMode)||"Deleted".equalsIgnoreCase(navMode)){
			return typeManager.getDeletedPrivateMessageType(); 
		}
		else if (PVTMSG_MODE_DRAFT.equalsIgnoreCase(navMode)){
			return typeManager.getDraftPrivateMessageType();
		}
		else{
			return typeManager.getCustomTopicType(navMode);
		}    	  
	}

	public MessageForumsTypeManager getTypeManager() {
		return typeManager;
	}


	public void setTypeManager(MessageForumsTypeManager typeManager) {
		this.typeManager = typeManager;
	}
}
