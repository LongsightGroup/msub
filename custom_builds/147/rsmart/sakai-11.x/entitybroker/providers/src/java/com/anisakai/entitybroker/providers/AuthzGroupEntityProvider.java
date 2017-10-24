package com.anisakai.entitybroker.providers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.*;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;

import com.anisakai.entitybroker.providers.model.EntityAuthzGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 9/9/13
 * Time: 4:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class AuthzGroupEntityProvider extends AbstractAsahiEntityProvider implements CoreEntityProvider,
        Outputable, Resolvable, Describeable, ActionsExecutable, CollectionResolvable, Updateable {

    private static Log log = LogFactory.getLog(AuthzGroupEntityProvider.class);

    private AuthzGroupService authzGroupService;

    public static final String ENTITY_PREFIX = "authz";

    public List<?> getEntities(EntityReference ref, Search search) {
        if (!isSuperUser() && !isEntityInternalRequest(ref)) {
            throw new SecurityException("You must be logged in as an admin user to retreive authz group data.");
        }
        List<EntityAuthzGroup> authzGroups = new ArrayList<EntityAuthzGroup>();
        if (search.getRestrictionByProperty("id") != null){

            String id = search.getRestrictionByProperty("id").getStringValue();

            EntityAuthzGroup authzGroupEntity = getAuthzEntity(getAuthzGroup(id));

            if (authzGroupEntity != null){
                authzGroups.add(authzGroupEntity);
            }
        } else {
            List<AuthzGroup> authzGroupsFound = authzGroupService.getAuthzGroups("!", null);

            if (authzGroupsFound != null && !authzGroupsFound.isEmpty()){
                Pattern templatePattern = Pattern.compile("!.*");
                for (AuthzGroup authzGroupTemplate : authzGroupsFound){
                    Matcher templateMatcher = templatePattern.matcher(authzGroupTemplate.getId());
                    if (templateMatcher.matches()){
                        authzGroups.add(getAuthzEntity(authzGroupTemplate));
                    }
                }
            }
        }
        return authzGroups;
    }

    public boolean entityExists(String id) {
        if (id == null || "".equals(id)){
            return false;
        }

        if(id.contains("site-")){
            id = id.replace("site-", "/site/");
        }

        AuthzGroup realm = getAuthzGroup(id);
        if (realm != null){
            return true;
        }
        return false;
    }

    public String[] getHandledOutputFormats() {
        return new String[] {Formats.XML, Formats.HTML, Formats.JSON};
    }

    public Object getEntity(EntityReference ref) {
        if (!isSuperUser() && !isEntityInternalRequest(ref)) {
            throw new SecurityException("You must be logged in as an admin user to retreive authz group data.");
        }
        if (ref == null || ref.getId() == null || getAuthzGroup(ref.getId()) == null) {
            return new EntityAuthzGroup();
        }
        return getAuthzEntity(getAuthzGroup(ref.getId()));
    }

    public String getLocalEntityPrefix() {
        return ENTITY_PREFIX;
    }

    public void setAuthzGroupService(AuthzGroupService authzGroupService){
        this.authzGroupService = authzGroupService;
    }

    private EntityAuthzGroup getAuthzEntity(AuthzGroup authzGroup) {
        if (authzGroup != null){
            return new EntityAuthzGroup(authzGroup);
        }
        return null;
    }

    private AuthzGroup getAuthzGroup(String id) {
        if (id != null && !"".equals(id)){
            try {
                return authzGroupService.getAuthzGroup(id);
            } catch (GroupNotDefinedException e) {
                log.error("AuthzGroupEntityProvider GroupNotDefinedException for id: " + id, e);
            }
        }
        return null;
    }

    @Override
    public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        if (!isSuperUser() && !isEntityInternalRequest(ref)) {
            throw new SecurityException("You must be logged in as an admin user to update authz group data.");
        }
        String authzGroupId = ref.getId();
        if (authzGroupId == null || "".equals(authzGroupId)) {
            throw new IllegalArgumentException("Missing authz group id for ref: " + ref.toString());
        }

        try {
            AuthzGroup authzGroup = authzGroupService.getAuthzGroup(authzGroupId);
            if (entity == null) {
                throw new IllegalArgumentException("Cannot update null entity");
            }
            if (entity instanceof AuthzGroup) {
                AuthzGroup entityAuthzGroup = (AuthzGroup) entity;
                if (entityAuthzGroup != null && authzGroup.getId().equals(entityAuthzGroup.getId())) {
                    authzGroupService.save(entityAuthzGroup);
                }
            }
        } catch (GroupNotDefinedException e) {
            log.error("Unable to find AuthzGroup with id:" + authzGroupId, e);
        } catch (AuthzPermissionException e) {
            log.error("You do not have permission to update authz group with id:" + authzGroupId, e);
        }
    }
}
