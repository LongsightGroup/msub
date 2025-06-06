/**
 * Copyright (c) 2008-2010 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.profile2.tool.pages;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.profile2.exception.ProfileNotDefinedException;
import org.sakaiproject.profile2.exception.ProfilePreferencesNotDefinedException;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.model.SocialNetworkingInfo;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.tool.components.NotifyingAjaxLazyLoadPanel;
import org.sakaiproject.profile2.tool.components.ProfileImageRenderer;
import org.sakaiproject.profile2.tool.models.FriendAction;
import org.sakaiproject.profile2.tool.pages.panels.ChangeProfilePictureUpload;
import org.sakaiproject.profile2.tool.pages.panels.ChangeProfilePictureUrl;
import org.sakaiproject.profile2.tool.pages.panels.FriendsFeed;
import org.sakaiproject.profile2.tool.pages.panels.GalleryFeed;
import org.sakaiproject.profile2.tool.pages.panels.KudosPanel;
import org.sakaiproject.profile2.tool.pages.panels.MyBusinessDisplay;
import org.sakaiproject.profile2.tool.pages.panels.MyContactDisplay;
import org.sakaiproject.profile2.tool.pages.panels.MyInfoDisplay;
import org.sakaiproject.profile2.tool.pages.panels.MyInterestsDisplay;
import org.sakaiproject.profile2.tool.pages.panels.MySocialNetworkingDisplay;
import org.sakaiproject.profile2.tool.pages.panels.MyStaffDisplay;
import org.sakaiproject.profile2.tool.pages.panels.MyStatusPanel;
import org.sakaiproject.profile2.tool.pages.panels.MyStudentDisplay;
import org.sakaiproject.profile2.tool.pages.windows.AddFriend;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;


public class MyProfile extends BasePage {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(MyProfile.class);
	

	/**
	 * Default constructor if viewing own
	 */
	public MyProfile()   {
		log.debug("MyProfile()");

		//get user for this profile and render it
		String userUuid = sakaiProxy.getCurrentUserId();
		renderMyProfile(userUuid);
	}
	
	/**
	 * This constructor is called if we are viewing someone elses but in edit mode.
	 * This will only be called if we were a superuser editing someone else's profile.
	 * An additional catch is also in place.
	 * @param userUuid		uuid of other user 
	 */
	public MyProfile(final String userUuid)   {
		log.debug("MyProfile(" + userUuid +")");
		
		//double check only super users
		if(!sakaiProxy.isSuperUser()) {
			log.error("MyProfile: user " + sakaiProxy.getCurrentUserId() + " attempted to access MyProfile for " + userUuid + ". Redirecting...");
			throw new RestartResponseException(new MyProfile());
		}
		//render for given user
		renderMyProfile(userUuid);
	}
	
	/**
	 * Does the actual rendering of the page
	 * @param userUuid
	 */
	private void renderMyProfile(final String userUuid) {
		
		//don't do this for super users viewing other people's profiles as otherwise there is no way back to own profile
		if(!sakaiProxy.isSuperUserAndProxiedToUser(userUuid)) {
			disableLink(myProfileLink);
		}
		
		//add the feedback panel for any error messages
		FeedbackPanel feedbackPanel = new FeedbackPanel("feedbackPanel");
		add(feedbackPanel);
		feedbackPanel.setVisible(false); //hide by default
		
		//get the prefs record, or a default if none exists yet
		final ProfilePreferences prefs = preferencesLogic.getPreferencesRecordForUser(userUuid);
		
		//if null, throw exception
		if(prefs == null) {
			throw new ProfilePreferencesNotDefinedException("Couldn't create default preferences record for " + userUuid);
		}
		
		//get SakaiPerson for this user
		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userUuid);
		//if null, create one 
		if(sakaiPerson == null) {
			log.warn("No SakaiPerson for " + userUuid + ". Creating one.");
			sakaiPerson = sakaiProxy.createSakaiPerson(userUuid);
			//if its still null, throw exception
			if(sakaiPerson == null) {
				throw new ProfileNotDefinedException("Couldn't create a SakaiPerson for " + userUuid);
			}
			//post create event
			sakaiProxy.postEvent(ProfileConstants.EVENT_PROFILE_NEW, userUuid, true);
		} 
		
		//post view event
		sakaiProxy.postEvent(ProfileConstants.EVENT_PROFILE_VIEW_OWN, "/profile/"+userUuid, false);

		//get some values from SakaiPerson or SakaiProxy if empty
		//SakaiPerson returns NULL strings if value is not set, not blank ones
	
		//these must come from Account to keep it all in sync
		//we *could* get a User object here and get the values.
		String userDisplayName = sakaiProxy.getUserDisplayName(userUuid);
		/*
		String userFirstName = sakaiProxy.getUserFirstName(userId);
		String userLastName = sakaiProxy.getUserLastName(userId);
		*/

		String userEmail = sakaiProxy.getUserEmail(userUuid);
		
		//create instance of the UserProfile class
		//we then pass the userProfile in the constructor to the child panels
		UserProfile userProfile = new UserProfile();
				
		//get rest of values from SakaiPerson and setup UserProfile
		userProfile.setUserUuid(userUuid);
		
		userProfile.setNickname(sakaiPerson.getNickname());
		userProfile.setDateOfBirth(sakaiPerson.getDateOfBirth());
		userProfile.setDisplayName(userDisplayName);
		//userProfile.setFirstName(userFirstName);
		//userProfile.setLastName(userLastName);
		//userProfile.setMiddleName(sakaiPerson.getInitials());
		
		userProfile.setEmail(userEmail);
		userProfile.setHomepage(sakaiPerson.getLabeledURI());
		userProfile.setHomephone(sakaiPerson.getHomePhone());
		userProfile.setWorkphone(sakaiPerson.getTelephoneNumber());
		userProfile.setMobilephone(sakaiPerson.getMobile());
		userProfile.setFacsimile(sakaiPerson.getFacsimileTelephoneNumber());
		
		userProfile.setDepartment(sakaiPerson.getOrganizationalUnit());
		userProfile.setPosition(sakaiPerson.getTitle());
		userProfile.setSchool(sakaiPerson.getCampus());
		userProfile.setRoom(sakaiPerson.getRoomNumber());
		
		userProfile.setCourse(sakaiPerson.getEducationCourse());
		userProfile.setSubjects(sakaiPerson.getEducationSubjects());
		
		userProfile.setStaffProfile(sakaiPerson.getStaffProfile());
		userProfile.setAcademicProfileUrl(sakaiPerson.getAcademicProfileUrl());
		userProfile.setUniversityProfileUrl(sakaiPerson.getUniversityProfileUrl());
		userProfile.setPublications(sakaiPerson.getPublications());
		
		// business fields
		userProfile.setBusinessBiography(sakaiPerson.getBusinessBiography());
		userProfile.setCompanyProfiles(profileLogic.getCompanyProfiles(userUuid));
		
		userProfile.setFavouriteBooks(sakaiPerson.getFavouriteBooks());
		userProfile.setFavouriteTvShows(sakaiPerson.getFavouriteTvShows());
		userProfile.setFavouriteMovies(sakaiPerson.getFavouriteMovies());
		userProfile.setFavouriteQuotes(sakaiPerson.getFavouriteQuotes());
		userProfile.setPersonalSummary(sakaiPerson.getNotes());
		
		// social networking fields
		SocialNetworkingInfo socialInfo = profileLogic.getSocialNetworkingInfo(userProfile.getUserUuid());
		if(socialInfo == null){
			socialInfo = new SocialNetworkingInfo();
		}
		userProfile.setSocialInfo(socialInfo);
		
		//PRFL-97 workaround. SakaiPerson table needs to be upgraded so locked is not null, but this handles it if not upgraded.
		if(sakaiPerson.getLocked() == null) {
			userProfile.setLocked(false);
			this.setLocked(false);
		} else {
			this.setLocked(sakaiPerson.getLocked());
			userProfile.setLocked(this.isLocked());
		}
		
	
		//what type of picture changing method do we use?
		int profilePictureType = sakaiProxy.getProfilePictureType();
		
		//change picture panel (upload or url depending on property)
		final Panel changePicture;
		
		//render appropriate panel with appropriate constructor ie if superUser etc
		if(profilePictureType == ProfileConstants.PICTURE_SETTING_UPLOAD) {
			
			if(sakaiProxy.isSuperUserAndProxiedToUser(userUuid)){
				changePicture = new ChangeProfilePictureUpload("changePicture", userUuid);
			} else {
				changePicture = new ChangeProfilePictureUpload("changePicture");
			}
		} else if (profilePictureType == ProfileConstants.PICTURE_SETTING_URL) {
			if(sakaiProxy.isSuperUserAndProxiedToUser(userUuid)){
				changePicture = new ChangeProfilePictureUrl("changePicture", userUuid);
			} else {
				changePicture = new ChangeProfilePictureUrl("changePicture");
			}
		} else if (profilePictureType == ProfileConstants.PICTURE_SETTING_OFFICIAL) {
			//cannot edit anything if using official images
			changePicture = new EmptyPanel("changePicture");
		} else {	
			//no valid option for changing picture was returned from the Profile2 API.
			log.error("Invalid picture type returned: " + profilePictureType);
			changePicture = new EmptyPanel("changePicture");
		}
		changePicture.setOutputMarkupPlaceholderTag(true);
		changePicture.setVisible(false);
		add(changePicture);
		
		//add the current picture
		add(new ProfileImageRenderer("photo", userUuid, prefs));
		
		//change profile image button
		AjaxLink<Void> changePictureLink = new AjaxLink<Void>("changePictureLink") {
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target) {
				
				//show the panel
				changePicture.setVisible(true);
				target.addComponent(changePicture);
				
				//resize iframe to fit it
				target.appendJavascript("resizeFrame('grow');");
			}
						
		};
		changePictureLink.add(new Label("changePictureLabel", new ResourceModel("link.change.profile.picture")));
		
		//is picture changing disabled? (property, or locked)
		if((!sakaiProxy.isProfilePictureChangeEnabled() || userProfile.isLocked()) && !sakaiProxy.isSuperUser()) {
			changePictureLink.setEnabled(false);
			changePictureLink.setVisible(false);
		}
		
		//if using official images, is the user allowed to select an alternate?
		//or have they specified the official image in their preferences.
		if(sakaiProxy.isOfficialImageEnabledGlobally() && (!sakaiProxy.isUsingOfficialImageButAlternateSelectionEnabled() || prefs.isUseOfficialImage())) {
			changePictureLink.setEnabled(false);
			changePictureLink.setVisible(false);
		}
		
		
		add(changePictureLink);
		
		
		/* SIDELINKS */
		WebMarkupContainer sideLinks = new WebMarkupContainer("sideLinks");
		int visibleSideLinksCount = 0;
		
		//ADMIN: ADD AS CONNECTION
		if(sakaiProxy.isSuperUserAndProxiedToUser(userUuid)) {
			
			//init
			boolean friend = false;
			boolean friendRequestToThisPerson = false;
			boolean friendRequestFromThisPerson = false;
			String currentUserUuid = sakaiProxy.getCurrentUserId();
			String nickname = userProfile.getNickname();
			if(StringUtils.isBlank(nickname)) {
				nickname="";
			}

			//setup model to store the actions in the modal windows
			final FriendAction friendActionModel = new FriendAction();

			//setup friend status
			friend = connectionsLogic.isUserXFriendOfUserY(userUuid, currentUserUuid);
			if(!friend) {
				friendRequestToThisPerson = connectionsLogic.isFriendRequestPending(currentUserUuid, userUuid);
			}
			if(!friend && !friendRequestToThisPerson) {
				friendRequestFromThisPerson = connectionsLogic.isFriendRequestPending(userUuid, currentUserUuid);
			}
			
			WebMarkupContainer addFriendContainer = new WebMarkupContainer("addFriendContainer");
			final ModalWindow addFriendWindow = new ModalWindow("addFriendWindow");

			//link
			final AjaxLink<Void> addFriendLink = new AjaxLink<Void>("addFriendLink") {
				private static final long serialVersionUID = 1L;
				public void onClick(AjaxRequestTarget target) {
	    			addFriendWindow.show(target);
				}
			};
			final Label addFriendLabel = new Label("addFriendLabel");
			addFriendLink.add(addFriendLabel);
			addFriendContainer.add(addFriendLink);
			
			//setup link/label and windows
			if(friend) {
				addFriendLabel.setDefaultModel(new ResourceModel("text.friend.confirmed"));
	    		addFriendLink.add(new AttributeModifier("class", true, new Model<String>("instruction icon connection-confirmed")));
				addFriendLink.setEnabled(false);
			} else if (friendRequestToThisPerson) {
				addFriendLabel.setDefaultModel(new ResourceModel("text.friend.requested"));
	    		addFriendLink.add(new AttributeModifier("class", true, new Model<String>("instruction icon connection-request")));
				addFriendLink.setEnabled(false);
			} else if (friendRequestFromThisPerson) {
				//TODO (confirm pending friend request link)
				//could be done by setting the content off the addFriendWindow.
				//will need to rename some links to make more generic and set the onClick and setContent in here for link and window
				addFriendLabel.setDefaultModel(new ResourceModel("text.friend.pending"));
	    		addFriendLink.add(new AttributeModifier("class", true, new Model<String>("instruction icon connection-request")));
				addFriendLink.setEnabled(false);
			}  else {
				addFriendLabel.setDefaultModel(new StringResourceModel("link.friend.add.name", null, new Object[]{ nickname } ));
	    		addFriendLink.add(new AttributeModifier("class", true, new Model<String>("icon connection-add")));
				addFriendWindow.setContent(new AddFriend(addFriendWindow.getContentId(), addFriendWindow, friendActionModel, currentUserUuid, userUuid)); 
			}
			
			sideLinks.add(addFriendContainer);
		
			//ADD FRIEND MODAL WINDOW HANDLER 
			addFriendWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
				private static final long serialVersionUID = 1L;
	
				public void onClose(AjaxRequestTarget target){
	            	if(friendActionModel.isRequested()) { 
	            		//friend was successfully requested, update label and link
	            		addFriendLabel.setDefaultModel(new ResourceModel("text.friend.requested"));
	            		addFriendLink.add(new AttributeModifier("class", true, new Model<String>("instruction")));
	            		addFriendLink.setEnabled(false);
	            		target.addComponent(addFriendLink);
	            	}
	            }
	        });
			
			add(addFriendWindow);
		
			visibleSideLinksCount++;
			
			
			//ADMIN: LOCK/UNLOCK A PROFILE
			WebMarkupContainer lockProfileContainer = new WebMarkupContainer("lockProfileContainer");
			final Label lockProfileLabel = new Label("lockProfileLabel");
			
			final AjaxLink<Void> lockProfileLink = new AjaxLink<Void>("lockProfileLink") {
				private static final long serialVersionUID = 1L;

				public void onClick(AjaxRequestTarget target) {
					//toggle it to be opposite of what it currently is, update labels and icons
					boolean locked = isLocked();
	    			if(sakaiProxy.toggleProfileLocked(userUuid, !locked)) {
	    				setLocked(!locked);
	    				log.info("MyProfile(): SuperUser toggled lock status of profile for " + userUuid + " to " + !locked);
	    				lockProfileLabel.setDefaultModel(new ResourceModel("link.profile.locked." + isLocked()));
	    				add(new AttributeModifier("title", true, new ResourceModel("text.profile.locked." + isLocked())));
	    				if(isLocked()){
	    					add(new AttributeModifier("class", true, new Model<String>("icon locked")));
	    				} else {
	    					add(new AttributeModifier("class", true, new Model<String>("icon unlocked")));
	    				}
	    				target.addComponent(this);
	    			}
				}
			};
			
			//set init icon for locked
			if(isLocked()){
				lockProfileLink.add(new AttributeModifier("class", true, new Model<String>("icon locked")));
			} else {
				lockProfileLink.add(new AttributeModifier("class", true, new Model<String>("icon unlocked")));
			}
			
			lockProfileLink.add(lockProfileLabel);
					
			//setup link/label and windows with special property based on locked status
			lockProfileLabel.setDefaultModel(new ResourceModel("link.profile.locked." + isLocked()));
			lockProfileLink.add(new AttributeModifier("title", true, new ResourceModel("text.profile.locked." + isLocked())));
			
			lockProfileContainer.add(lockProfileLink);
			
			sideLinks.add(lockProfileContainer);
			
			visibleSideLinksCount++;

			
		} else {
			//blank components
			WebMarkupContainer addFriendContainer = new WebMarkupContainer("addFriendContainer");
			addFriendContainer.add(new AjaxLink("addFriendLink") {
				public void onClick(AjaxRequestTarget target) {}
			}).add(new Label("addFriendLabel"));
			sideLinks.add(addFriendContainer);
			add(new WebMarkupContainer("addFriendWindow"));
			
			WebMarkupContainer lockProfileContainer = new WebMarkupContainer("lockProfileContainer");
			lockProfileContainer.add(new AjaxLink("lockProfileLink") {
				public void onClick(AjaxRequestTarget target) {}
			}).add(new Label("lockProfileLabel"));
			sideLinks.add(lockProfileContainer);
		}
		
		
		//hide entire list if no links to show
		if(visibleSideLinksCount == 0) {
			sideLinks.setVisible(false);
		}
		
		add(sideLinks);
		
		
		//status panel
		Panel myStatusPanel = new MyStatusPanel("myStatusPanel", userProfile);
		add(myStatusPanel);
		
		if (sakaiProxy.isProfileFieldsEnabled()) {
			//info panel - load the display version by default
			Panel myInfoDisplay = new MyInfoDisplay("myInfo", userProfile);
			myInfoDisplay.setOutputMarkupId(true);
			add(myInfoDisplay);
			
			//contact panel - load the display version by default
			Panel myContactDisplay = new MyContactDisplay("myContact", userProfile);
			myContactDisplay.setOutputMarkupId(true);
			add(myContactDisplay);
			
			//university staff panel - load the display version by default
			Panel myStaffDisplay = new MyStaffDisplay("myStaff", userProfile);
			myStaffDisplay.setOutputMarkupId(true);
			add(myStaffDisplay);
			
			//business panel - load the display version by default
			Panel myBusinessDisplay;
			if (sakaiProxy.isBusinessProfileEnabled()) {
				myBusinessDisplay = new MyBusinessDisplay("myBusiness", userProfile);
				myBusinessDisplay.setOutputMarkupId(true);
			} else {
				myBusinessDisplay = new EmptyPanel("myBusiness");
			}
			add(myBusinessDisplay);
			
			//student panel
			Panel myStudentDisplay = new MyStudentDisplay("myStudent", userProfile);
			myStudentDisplay.setOutputMarkupId(true);
			add(myStudentDisplay);
			
			//social networking panel
			Panel mySocialNetworkingDisplay = new MySocialNetworkingDisplay("mySocialNetworking", userProfile);
			mySocialNetworkingDisplay.setOutputMarkupId(true);
			add(mySocialNetworkingDisplay);
			
			//interests panel - load the display version by default
			Panel myInterestsDisplay = new MyInterestsDisplay("myInterests", userProfile);
			myInterestsDisplay.setOutputMarkupId(true);
			add(myInterestsDisplay);
		} else {
			//PRFL-699 disable profile fields
			add(new EmptyPanel("myInfo"));
			add(new EmptyPanel("myContact"));
			add(new EmptyPanel("myStaff"));
			add(new EmptyPanel("myBusiness"));
			add(new EmptyPanel("myStudent"));
			add(new EmptyPanel("mySocialNetworking"));
			add(new EmptyPanel("myInterests"));
		}
		
		//kudos panel
		add(new AjaxLazyLoadPanel("myKudos"){
			private static final long serialVersionUID = 1L;

			@Override
			public Component getLazyLoadComponent(String markupId) {
				if(prefs.isShowKudos()){
										
					int score = kudosLogic.getKudos(userUuid);
					if(score > 0) {
						return new KudosPanel(markupId, userUuid, userUuid, score);
					}
				} 
				return new EmptyPanel(markupId);
			}
		});
		
		
		//friends feed panel for self - lazy loaded
		add(new NotifyingAjaxLazyLoadPanel("friendsFeed") {
			private static final long serialVersionUID = 1L;

			@Override
            public Component getLazyLoadComponent(String markupId) {
            	return new FriendsFeed(markupId, userUuid, userUuid);
            }

			@Override
			public void renderHead(IHeaderResponse response) {
				response.renderOnDomReadyJavascript("resizeFrame('grow');");
			}
        });
        	
        	
		//gallery feed panel
		add(new NotifyingAjaxLazyLoadPanel("galleryFeed") {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getLazyLoadComponent(String markupId) {
				if (sakaiProxy.isProfileGalleryEnabledGlobally() && prefs.isShowGalleryFeed()) {
					return new GalleryFeed(markupId, userUuid, userUuid)
							.setOutputMarkupId(true);
				} else {
					return new EmptyPanel(markupId);
				}
			}

			@Override
			public void renderHead(IHeaderResponse response) {
				response.renderOnDomReadyJavascript("resizeFrame('grow');");
			}
			
		});
		
		
	}
	
	
	private boolean locked;
	public boolean isLocked() {
		return locked;
	}
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	
}
