/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/
package org.theospi.portfolio.migration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.metaobj.shared.mgt.AgentManager;
import org.sakaiproject.metaobj.shared.mgt.IdManager;
import org.sakaiproject.metaobj.shared.mgt.MetaobjEntityManager;
import org.sakaiproject.metaobj.shared.mgt.StructuredArtifactDefinitionManager;
import org.sakaiproject.metaobj.shared.model.Agent;
import org.sakaiproject.metaobj.shared.model.ElementBean;
import org.sakaiproject.metaobj.shared.model.Id;
import org.sakaiproject.metaobj.shared.model.IdentifiableObject;
import org.sakaiproject.metaobj.shared.model.StructuredArtifactDefinitionBean;
import org.sakaiproject.metaobj.utils.xml.SchemaFactory;
import org.sakaiproject.metaobj.utils.xml.SchemaNode;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.theospi.portfolio.guidance.mgt.GuidanceManager;
import org.theospi.portfolio.guidance.model.Guidance;
import org.theospi.portfolio.help.model.Glossary;
import org.theospi.portfolio.help.model.GlossaryEntry;
import org.theospi.portfolio.matrix.DefaultScaffoldingBean;
import org.theospi.portfolio.matrix.MatrixFunctionConstants;
import org.theospi.portfolio.matrix.MatrixManager;
import org.theospi.portfolio.matrix.model.Attachment;
import org.theospi.portfolio.matrix.model.Cell;
import org.theospi.portfolio.matrix.model.Criterion;
import org.theospi.portfolio.matrix.model.Level;
import org.theospi.portfolio.matrix.model.Matrix;
import org.theospi.portfolio.matrix.model.Scaffolding;
import org.theospi.portfolio.matrix.model.ScaffoldingCell;
import org.theospi.portfolio.matrix.model.WizardPageDefinition;
import org.theospi.portfolio.matrix.model.WizardPageForm;
import org.theospi.portfolio.presentation.PresentationManager;
import org.theospi.portfolio.presentation.model.Presentation;
import org.theospi.portfolio.presentation.model.PresentationComment;
import org.theospi.portfolio.presentation.model.PresentationItem;
import org.theospi.portfolio.presentation.model.PresentationItemDefinition;
import org.theospi.portfolio.presentation.model.PresentationLog;
import org.theospi.portfolio.presentation.model.PresentationTemplate;
import org.theospi.portfolio.presentation.model.TemplateFileRef;
import org.theospi.portfolio.presentation.model.impl.HibernatePresentationProperties;
import org.theospi.portfolio.review.mgt.ReviewManager;
import org.theospi.portfolio.review.model.Review;
import org.theospi.portfolio.security.Authorization;
import org.theospi.portfolio.security.AuthorizationFacade;
import org.theospi.portfolio.security.impl.AllowAllSecurityAdvisor;
import org.theospi.portfolio.shared.model.ItemDefinitionMimeType;
import org.theospi.portfolio.style.model.Style;
import org.theospi.portfolio.workflow.mgt.WorkflowManager;



/**
 *
 */
public class OspMigrationJob implements Job {
   // logger
   protected final transient Log logger = LogFactory.getLog(getClass());

   // class members
   private static final String FORM_ID_FEEDBACK                        = "feedback_form";                          // did not exist in osp 2.0 so no data to migrate
   private static final String FORM_ID_EVALUATION                      = "evaluation_form";
   private static final String FORM_ID_REFLECTION_WITH_2_EXPECTATIONS  = "reflection_with_2_expectations_form";
   private static final String FORM_ID_REFLECTION_WITH_3_EXPECTATIONS  = "reflection_with_3_expectations_form";
   private static final String FORM_TYPE                               = "form";
   private static final String MIGRATED_FOLDER                         = "migratedMatrixForms";
   private static final String MIGRATED_FOLDER_PATH                    = "/" + MIGRATED_FOLDER + "/";

   // sakai services (injected via the spring framework's IoC)
   private AgentManager                        agentManager;
   private AuthorizationFacade                 authzManager;
   private ContentHostingService               contentHosting;
   private Glossary                            glossaryManager;
   private GuidanceManager                     guidanceManager;
   private IdManager                           idManager;
   private MatrixManager                       matrixManager;
   private PresentationManager                 presentationManager;
   private ReviewManager                       reviewManager;
   private SecurityService                     securityService;
   private SiteService                         siteService;
   private StructuredArtifactDefinitionManager structuredArtifactDefinitionManager;
   private WorkflowManager                     workflowManager;

   // data members injected via the spring framework's IoC.
   private Map<String, String>    tableMap;             // key: osp 2.5 table name, value: osp 2.0 table name
   private List<String>           authzToolFunctions;   //
   private List<FormWrapper>      matrixForms;          // information about each matrix form

   // data members
   private DataSource             dataSource;
   private DefaultScaffoldingBean defaultScaffoldingBean;
   private Statement              stmt;
   private Map<Object, Object>    userUniquenessMap;
   private Id                     expectation2FormId;   // form with 2 expectations: intellectual growth field, 2 (evidence field & reflection field)
   private Id                     expectation3FormId;   // form with 3 expectations: intellectual growth field, 3 (evidence field & reflection field)
   private Map<String, Id>        reflectionFormIdMap;  // wsu has a different reflection form for each level
                                                        // level "GE 101/301" & level "GE 303" use 2 expectationForm  (1st and 2nd columns)
                                                        // level "GE 401"                      use 3 expectationForm  (3rd         column )
   private Id                     feedbackFormId;       // not used in osp 2.0, so nothing to migrate
   private Id                     evaluationFormId;     // reviewer's comments on the student's reflections
   private Map<String, String>    matrixIdMap;          // matrices are recreated from scratch, so we need to keep track of the old osp 2.0 ids and the new 2.5 ids




   /**
    * default constructor.
    */
   public OspMigrationJob() {
      tableMap            = new HashMap<String, String>();
      authzToolFunctions  = new ArrayList<String>();
      matrixForms         = new ArrayList<FormWrapper>();
      matrixIdMap         = new HashMap<String, String>();   // matrices are recreated from scratch, so we need to keep track of the old osp 2.0 ids and the new 2.5 ids
      reflectionFormIdMap = new HashMap<String, Id>();
   }

   public void init() {
      evaluationFormId  = idManager.getId(FORM_ID_EVALUATION);
                            // level label, form id
      reflectionFormIdMap.put("GE 101/301", idManager.getId(FORM_ID_REFLECTION_WITH_2_EXPECTATIONS));   // the 1st column      uses 2 expectations
      reflectionFormIdMap.put("GE 303"    , idManager.getId(FORM_ID_REFLECTION_WITH_2_EXPECTATIONS));   // the 2nd column also uses 2 expectations
      reflectionFormIdMap.put("GE 401"    , idManager.getId(FORM_ID_REFLECTION_WITH_3_EXPECTATIONS));   // the 3rd column      uses 3 expectations
   }

   /**
    * migrates osp 2.0 content (authorizations, forms, matrices, presentations, glossary) to osp 2.2.
    * <br/><br/>
    * @param jobExecutionContext   quartz job execution context.
    * <br/><br/>
    * @throws JobExecutionException   if the job fails to run.
    */
   public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
      logger.info("osp 2.0 -> osp 2.2 migration quartz job started");
      Date start = new Date();
      Date end   = new Date();

      Connection connection = null;
      try {
         boolean isDeveloper = ServerConfigurationService.getBoolean("osp.migration.developer", false);

         connection = SqlService.borrowConnection(); // getDataSource().getConnection();

         if (isDeveloper)
            developerClearAllTables(connection);

         userUniquenessMap = new HashMap<Object, Object>();

         // create the new sakai 2.5 forms that will used in matrices
         createMatrixForms();

         runAuthzMigration               (connection, isDeveloper);
         runGlossaryMigration            (connection, isDeveloper);
         runMatrixMigration              (connection, isDeveloper);
         runPresentationTemplateMigration(connection, isDeveloper);
         runPresentationMigration        (connection, isDeveloper);
         runPresentationItemFix          (connection, isDeveloper);

         userUniquenessMap = null;

      } catch (SQLException e) {
         logger.error("osp 2.0 -> osp 2.2 migration quartz job error.", e);
         throw new JobExecutionException(e);
      } finally {
         if (connection != null) {
            try {
               SqlService.returnConnection(connection); //connection.close();
            }
            catch (Exception e) {
               // can't do anything with this.
            }
         }
         end = new Date();
      }
      logger.info("osp 2.0 -> osp 2.2 migration quartz job ran for " + (end.getTime() - start.getTime()) + " ms.");
      logger.info("osp 2.0 -> osp 2.2 migration quartz job finished.");
   }

   private Id saveForm(String owner, String name, byte[] fileContent, String formType) {
      getSecurityService().pushAdvisor(new AllowAllSecurityAdvisor());

      Session sakaiSession = SessionManager.getCurrentSession();
      String userId = sakaiSession.getUserId();
      sakaiSession.setUserId(owner);
      sakaiSession.setUserEid(owner);

      String description = "";
      String folder = "/user/" + owner;
      String type = "application/x-osp";

      try {
         ContentCollectionEdit groupCollection = getContentHosting().addCollection(folder);
         groupCollection.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, owner);
         getContentHosting().commitCollection(groupCollection);
      }
      catch (IdUsedException e) {
         // ignore... it is already there.
      }
      catch (Exception e) {
         throw new RuntimeException(e);
      }

      folder = "/user/" + owner + MIGRATED_FOLDER_PATH;

      try {
         ContentCollectionEdit groupCollection = getContentHosting().addCollection(folder);
         groupCollection.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, MIGRATED_FOLDER);
         groupCollection.getPropertiesEdit().addProperty(ResourceProperties.PROP_DESCRIPTION, "Folder for Migrated Matrix Forms");
         getContentHosting().commitCollection(groupCollection);
      }
      catch (IdUsedException e) {
         // ignore... it is already there.
      }
      catch (Exception e) {
         throw new RuntimeException(e);
      }

      try {
         ResourcePropertiesEdit resourceProperties = getContentHosting().newResourceProperties();
         resourceProperties.addProperty (ResourceProperties.PROP_DISPLAY_NAME, name);
         resourceProperties.addProperty (ResourceProperties.PROP_DESCRIPTION, description);
         resourceProperties.addProperty(ResourceProperties.PROP_CONTENT_ENCODING, "UTF-8");
         resourceProperties.addProperty(ResourceProperties.PROP_STRUCTOBJ_TYPE, formType);
         resourceProperties.addProperty(ContentHostingService.PROP_ALTERNATE_REFERENCE, MetaobjEntityManager.METAOBJ_ENTITY_PREFIX);

         ContentResource resource = getContentHosting().addResource(name, folder, 0, type,
               fileContent, resourceProperties, NotificationService.NOTI_NONE);
         return idManager.getId(getContentHosting().getUuid(resource.getId()));
      }
      catch (Exception e) {
         throw new RuntimeException(e);
      } finally {
         getSecurityService().popAdvisor();
         sakaiSession.setUserEid(userId);
         sakaiSession.setUserId(userId);
      }
   }

   private Id createFeedbackForm(String owner, String title, String comment) {
      String formId = FORM_ID_FEEDBACK;
      ElementBean feedbackForm = setupForm(formId);
      feedbackForm.put("comment", comment.replace('\u0000', ' '));
      byte[] xml = feedbackForm.toXmlString().getBytes();
      return saveForm(owner, title, xml, formId);
   }

/* private Id createEvaluationForm(String owner, String title, String grade, String comment) {
      String formId = FORM_ID_EVALUATION;
      ElementBean evalForm = setupForm(formId);
      evalForm.put("grade", grade);
      if(comment == null)
         comment = "";
      evalForm.put("comment", comment.replace('\u0000', ' '));
      byte[] xml = evalForm.toXmlString().getBytes();
      return saveForm(owner, title, xml, formId);
   }
*/
   /**
    * @return the id of the instance of the reflection form created using the growth, connect, and evidence data.
    * <br/><br/>
    * @param formId     id of the reflection form which has 2 expectations.
    * @param owner      eid of the user who created the data.
    * @param title      title of the form instance.
    * @param growth     osp 2.0 reflection intellectual growth statement.
    * @param connect1   osp 2.0 reflection connect text.
    * @param evidence1  osp 2.0 reflection evidence.
    * @param connect2   osp 2.0 reflection connect text.
    * @param evidence2  osp 2.0 reflection evidence.
    * @param connect3   osp 2.0 reflection connect text.
    * @param evidence3  osp 2.0 reflection evidence.
    */
   private Id createReflectionForm(Id formId, String owner, String title, String growth, String connect1, String evidence1, String connect2, String evidence2, String connect3, String evidence3) {
      if (growth    == null) growth    = "";
      if (connect1  == null) connect1  = "";
      if (connect2  == null) connect2  = "";
      if (connect3  == null) connect3  = "";
      if (evidence1 == null) evidence1 = "";
      if (evidence2 == null) evidence2 = "";
      if (evidence3 == null) evidence3 = "";

      ElementBean reflectionForm = setupForm(formId.toString());

      // the osp 2.0 reflection fields map to the sakai 2.5 reflection form in the following way:
      reflectionForm.put("goals"      , growth   .replace('\u0000', ' '));
      reflectionForm.put("evidence"   , evidence1.replace('\u0000', ' '));
      reflectionForm.put("reflection" , connect1 .replace('\u0000', ' '));
      reflectionForm.put("evidence2"  , evidence2.replace('\u0000', ' '));
      reflectionForm.put("reflection2", connect2 .replace('\u0000', ' '));
      if (formId.getValue() == FORM_ID_REFLECTION_WITH_3_EXPECTATIONS)
      {
         reflectionForm.put("evidence3"  , evidence3.replace('\u0000', ' '));
         reflectionForm.put("reflection3", connect3 .replace('\u0000', ' '));
      }
      byte[] xml = reflectionForm.toXmlString().getBytes();

      return saveForm(owner, title, xml, formId.getValue());
   }


/* private Id createExpectationForm(String owner, String title, String evidence, String connect) {
      String formId = FORM_ID_EXPECTATION;
      ElementBean feedbackForm = setupForm(formId);
      if(evidence == null)
         evidence = "";
      if(connect == null)
         connect = "";
      feedbackForm.put("evidence", evidence.replace('\u0000', ' '));
      feedbackForm.put("connect" , connect.replace('\u0000', ' '));
      byte[] xml = feedbackForm.toXmlString().getBytes();
      return saveForm(owner, title, xml, formId);
   }
*/
   /**
    * @return an instance of the form specified by the formId.
    * <br/><br/>
    * @param formId   the id of the form to be retrieved.
    */
   private ElementBean setupForm(String formId) {
      StructuredArtifactDefinitionBean bean          = structuredArtifactDefinitionManager.loadHome(idManager.getId(formId));
      SchemaFactory                    schemaFactory = SchemaFactory.getInstance();
      SchemaNode                       schema        = schemaFactory.getSchema(new ByteArrayInputStream(bean.getSchema())).getChild(bean.getDocumentRoot());
      ElementBean                      form          = new ElementBean(bean.getDocumentRoot(), schema);

      return form;
   }

   private void developerClearAllTables(Connection con) throws JobExecutionException
   {
      logger.info("developerClearAllTables()");
      String sql = "";
      try {
         stmt = con.createStatement();
         Statement innerstmt = con.createStatement();

         sql = "SET FOREIGN_KEY_CHECKS=0";
         stmt.executeUpdate(sql);

         sql = "DELETE FROM content_resource WHERE RESOURCE_ID LIKE '%" + MIGRATED_FOLDER + "%'";
         stmt.executeUpdate(sql);

         sql = "TRUNCATE content_resource_lock";
         stmt.executeUpdate(sql);

         sql = "show tables like 'osp_%'";
         ResultSet rs = stmt.executeQuery(sql);
         try {
            while (rs.next()) {
               String tableName = rs.getString(1);
               sql = "TRUNCATE " + tableName;
               if(!tableName.endsWith("_BKP"))
                  innerstmt.executeUpdate(sql);
            }
         }
         finally {
            try {
               innerstmt.close();
               rs.close();
            } catch (Exception e) {
            }
         }


      } catch (Exception e) {
         logger.error("error truncating data with this sql: " + sql);
         logger.error("", e);
         throw new JobExecutionException(e);
     } finally {
        logger.info("Quartz task finished: developerClearAllTables()");
        try {
            sql = "SET FOREIGN_KEY_CHECKS=1";
            stmt.executeUpdate(sql);
            stmt.close();
         } catch (Exception e) {
         }
     }
   }

   /**
    * creates the matrix forms that exist in sakai 2.5 but which did not exist in osp 2.0.
    */
   protected void createMatrixForms() {
      logger.info("Quartz task started: createMatrixForms()");

      // switch user to 'admin'
      getSecurityService().pushAdvisor(new AllowAllSecurityAdvisor());
      org.sakaiproject.tool.api.Session sakaiSession = SessionManager.getCurrentSession();
      String userId = sakaiSession.getUserId();
      sakaiSession.setUserId("admin");
      sakaiSession.setUserEid("admin");

      List<StructuredArtifactDefinitionBean> forms = new ArrayList<StructuredArtifactDefinitionBean>();
      try {
         for (FormWrapper matrixFormWrapper : matrixForms)
            forms.add(processDefinedForm(matrixFormWrapper));

         for (StructuredArtifactDefinitionBean formDefinition : forms)
            structuredArtifactDefinitionManager.save(formDefinition);
      } finally {
         // revert back to the original user
         getSecurityService().popAdvisor();
         sakaiSession.setUserEid(userId);
         sakaiSession.setUserId(userId);
      }
      logger.info("Quartz task finished: createMatrixForms()");
   }

   /**
    * @return a new sakai 2.5 form definition from the values specified in the given form wrapper.
    * <br/><br/>
    * @param wrapper   contains the values used to create the sakai 2.5 form definition.
    */
   protected StructuredArtifactDefinitionBean processDefinedForm(FormWrapper wrapper) {
      // see if the form definition already exists
      StructuredArtifactDefinitionBean form = structuredArtifactDefinitionManager.loadHome(getIdManager().getId(wrapper.getIdValue()));

      // if it doesn't, then create a new one
      if (form == null) {
         form = new StructuredArtifactDefinitionBean();
         form.setCreated(new Date());
         form.setNewId(getIdManager().getId(wrapper.getIdValue()));
      }

      // have the form definition use the information (id, schema, etc.) in the wrapper (specified in the components.xml file)
      updateForm(wrapper, form);

      return form;
   }

   /**
    * creates a new form specified by the form wrapper.
    * however, if the specified form already exists in sakai, as evidenced by the id attribute of the form  parameter not being null,
    * then the form is simply updated with the data in the wrapper.
    * @return the newly created form.
    * <br/><br/>
    * @param wrapper   contains new sakai 2.5 information needed to create a form definition.
    * @param form      form definition bean
    */
   protected StructuredArtifactDefinitionBean updateForm(FormWrapper wrapper, StructuredArtifactDefinitionBean form) {
      if (wrapper.getIdValue() ==  null)
         throw new RuntimeException("The form wrapper id is null you cheaky little monkey.");

      form.setSchema      (loadResource(wrapper.getXsdFileLocation()).toByteArray());
      form.setModified    (new Date());
      form.setDescription (wrapper.getDescription());
      form.setGlobalState (StructuredArtifactDefinitionBean.STATE_PUBLISHED);
      form.setSiteState   (StructuredArtifactDefinitionBean.STATE_UNPUBLISHED);
      form.setDocumentRoot(wrapper.getDocumentRoot());
      form.setInstruction (wrapper.getInstruction());
      form.setExternalType(wrapper.getExternalType());
      form.setSiteId      (null);
      form.setOwner       (getAgentManager().getAgent("admin"));

      return form;
   }

   protected ByteArrayOutputStream loadResource(String name) {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      InputStream is = getClass().getResourceAsStream(name);

      try {
         int c = is.read();
         while (c != -1) {
            bos.write(c);
            c = is.read();
         }
         bos.flush();
      }
      catch (IOException e) {
         throw new RuntimeException(e);
      } finally {
         try {
            is.close();
         }
         catch (IOException e) {
            // can't do anything now..
         }
      }
      return bos;
   }

   /**
    * <br/><br/>
    * @param con           connection to the database.
    * @param isDeveloper   ?
    * <br/><br/>
    * @throws JobExecutionException   if the permissions can not be migrated.
    */
   protected void runAuthzMigration(Connection con, boolean isDeveloper) throws JobExecutionException {
      logger.info("osp 2.0 -> osp 2.2 migration quartz sub-task started: runAuthzMigration()");

      String tableName = getOldTableName("osp_authz_simple");
      String sql       = "select * from " + tableName;

      try {
         stmt = con.createStatement();
         ResultSet rs = stmt.executeQuery(sql);
         try {
            while (rs.next()) {
               //String id = rs.getString("id");
               String qual     = rs.getString("qualifier_id");
               String agentStr = rs.getString("agent_id");
               String func     = rs.getString("function_name");

               // transformations on the authz stuff that needs to change from a tool_id to a site_id
               try {
                  if (authzToolFunctions.contains(func)) {
                     ToolConfiguration toolConfig = siteService.findTool(qual);
                     if (toolConfig != null) {
                        qual = toolConfig.getContext();
                     }
                  }
                  if (func.equalsIgnoreCase(MatrixFunctionConstants.REVIEW_MATRIX)) {
                     func = MatrixFunctionConstants.EVALUATE_MATRIX;
                  }

                  Agent agent = agentManager.getAgent(agentStr);

                  // there needs to be an agent or else the authorization is invalid
                  if(agent == null)
                     logger.error("OSP Migration error: agent was null: " + agentStr);
                  else if(agent.getId() == null)
                     logger.error("OSP Migration error: agent id was null: " + agentStr);
                  else if(agent.getId().getValue() == null)
                     logger.error("OSP Migration error: agent id value was null: " + agentStr);
                  else if(qual == null)
                     logger.error("OSP Migration error: qualifier was null: " + qual);
                  else
                     authzManager.createAuthorization(agent, func, idManager.getId(qual));
               } catch(Exception e) {
                  if(!isDeveloper)
                     throw e;
               }
            }
         } finally {
            rs.close();
         }

         //This will create new authorizations for the review and view functions
         sql = "select distinct ss.site_id, role_name, '" + MatrixFunctionConstants.USE_SCAFFOLDING + "' as func " +
               "From SAKAI_SITE_TOOL st JOIN SAKAI_SITE ss ON st.site_id = ss.site_id "+
               "JOIN SAKAI_REALM r ON r.realm_id = CONCAT('/site/', ss.site_id) " +
               "JOIN SAKAI_REALM_RL_FN rf ON r.REALM_KEY = rf.REALM_KEY " +
               " JOIN SAKAI_REALM_ROLE rr ON rf.ROLE_KEY = rr.ROLE_KEY " +
               "where st.registration = 'osp.matrix' " +
               "and role_name in ('access', 'member', 'student') " +
               "union " +
               "select distinct ss.site_id, role_name, '" + MatrixFunctionConstants.REVIEW_MATRIX + "' as func " +
               "From SAKAI_SITE_TOOL st JOIN SAKAI_SITE ss ON st.site_id = ss.site_id " +
               "JOIN SAKAI_REALM r ON r.realm_id = CONCAT('/site/', ss.site_id) " +
               "JOIN SAKAI_REALM_RL_FN rf ON r.REALM_KEY = rf.REALM_KEY " +
               " JOIN SAKAI_REALM_ROLE rr ON rf.ROLE_KEY = rr.ROLE_KEY " +
               "where st.registration = 'osp.matrix' " +
               "and role_name in ('maintain', 'project owner', 'instructor')";

         stmt = con.createStatement();
         rs = stmt.executeQuery(sql);
         try {
            while (rs.next()) {
             //String id     = rs.getString("id");
               String siteId = rs.getString("site_id");
               String role   = rs.getString("role_name");
               String func   = rs.getString("func");
               String agent  = "/site/" + siteId + "/" + role;
               try {
                  // the agent has already been verified as they are coming from the db
                  authzManager.createAuthorization(agentManager.getAgent(agent), func, idManager.getId(siteId));
               } catch(Exception e) {
                  if(!isDeveloper)
                     throw e;
               }
            }
         } finally {
            rs.close();
         }

      } catch (Exception e) {
         logger.error("error selecting data with this sql: " + sql);
         logger.error("", e);
         throw new JobExecutionException(e);
      } finally {
         try {
             stmt.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
      }
      logger.info("osp 2.0 -> osp 2.2 migration quartz sub-task fininshed: runAuthzMigration()");
   }

   protected void runGlossaryMigration(Connection con, boolean isDeveloper) throws JobExecutionException {
      logger.info("Quartz task started: runGlossaryMigration()");
      String tableName = getOldTableName("osp_help_glossary");
      String tableName2 = getOldTableName("osp_help_glossary_desc");
      String sql = "select g.id, g.term, g.description, g.worksite_id, " +
            "gd.long_description from " + tableName + " g, " + tableName2 +
            " gd where g.id = gd.entry_id";

      try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            try {
               while (rs.next()) {
                  String id = rs.getString("id");
                  String term = rs.getString("term");
                  String desc = rs.getString("description");
                  String longDesc = rs.getString("long_description");
                  String site_id = rs.getString("worksite_id");

                  Id theId = idManager.getId(id);

                  GlossaryEntry entry = new GlossaryEntry(term, desc);
                  entry.setId(theId);
                  entry.setWorksiteId(site_id);
                  entry.getLongDescriptionObject().setEntryId(theId);
                  entry.getLongDescriptionObject().setLongDescription(longDesc);
                  glossaryManager.addEntry(entry);

               }
           } finally {
               rs.close();
           }
        } catch (Exception e) {
            logger.error("error selecting data with this sql: " + sql);
            logger.error("", e);
            throw new JobExecutionException(e);
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
                // ignore
            }
        }
        logger.info("Quartz task fininshed: runGlossaryMigration()");
   }

   /**
    * migrates matrices
    * <br/><br/>
    * @param con           jdbc connection.
    * @param isDeveloper   whether this is run in development mode or release mode.
    */
   protected void runMatrixMigration(Connection con, boolean isDeveloper) throws JobExecutionException {
      logger.info("Quartz task started: runMatrixMigration()");

      String tableName = getOldTableName("osp_scaffolding"),
      tableName2 = null,
      tableName3 = null;
      String sql = "select * from " + tableName;

      Statement matrixInnerStmt = null, innerStmt = null;
      try {
//       List<String> additionalForms = new ArrayList<String>();

         matrixInnerStmt = con.createStatement();
         innerStmt       = con.createStatement();
         stmt            = con.createStatement();
         ResultSet    rs = stmt.executeQuery(sql);

         try {
            // convert the matrix scaffoldings
            while (rs.next()) {
               String  id             = rs.getString ("id");
               String  owner          = rs.getString ("ownerid");
               String  title          = rs.getString ("title");
               String  description    = rs.getString ("description");
               String  documentRoot   = rs.getString ("documentroot");
               String  privacyxsdid   = rs.getString ("privacyxsdid");
               String  worksite       = rs.getString ("worksiteId");
               boolean published      = rs.getBoolean("published");
               String  publishedBy    = rs.getString ("publishedBy");
               Date    publishedDate  = rs.getDate   ("publishedDate");

               String columnLabel     = defaultScaffoldingBean.getColumnLabel();
               String rowLabel        = defaultScaffoldingBean.getRowLabel();
               // joneric: fixed to get the code to compile
               Scaffolding defaultScaffolding = defaultScaffoldingBean.createDefaultScaffolding();
               String readyColor      = defaultScaffolding.getReadyColor();
               String pendingColor    = defaultScaffolding.getPendingColor();
               String completedColor  = defaultScaffolding.getCompletedColor();
               String lockColor       = defaultScaffolding.getLockedColor();
               int    workflowOption  = Scaffolding.HORIZONTAL_PROGRESSION;            //rs.getInt("workflowOption");
               String exposed_page_id = "";                                            //rs.getString("exposed_page_id");
               String style_id        = "";                                            //rs.getString("style_id");

               Scaffolding scaffolding = new Scaffolding();
               Id sid = idManager.getId(id);
               scaffolding.setId(null);
               scaffolding.setNewId(sid);
               Agent scaffAgent = agentManager.getAgent(owner);

               if(scaffAgent == null) {
                  logger.error("OSP Migration Error: The scaffolding owner agent couldn't be found: " + owner);
                  continue;
               } else if(scaffAgent.getId() == null) {
                  logger.error("OSP Migration Error: The scaffolding owner agent id couldn't be found: " + owner);
                  continue;
               } else if(scaffAgent.getId().getValue() == null) {
                  logger.error("OSP Migration Error: The scaffolding owner agent id value couldn't be found: " + owner);
                  continue;
               }


               scaffolding.setOwner(scaffAgent);
               scaffolding.setTitle(title);
               scaffolding.setDescription(description);
               scaffolding.setWorksiteId(idManager.getId(worksite));
               scaffolding.setPublished(published);
               if(publishedBy != null && agentManager.getAgent(publishedBy) != null  && agentManager.getAgent(publishedBy).getId() != null)
                  scaffolding.setPublishedBy(agentManager.getAgent(publishedBy));
               scaffolding.setColumnLabel(columnLabel);
               scaffolding.setPublishedDate(publishedDate);
               scaffolding.setRowLabel(rowLabel);

               scaffolding.setReadyColor(readyColor);
               scaffolding.setPendingColor(pendingColor);
               scaffolding.setCompletedColor(completedColor);
               scaffolding.setLockedColor(lockColor);
               scaffolding.setWorkflowOption(workflowOption);
               scaffolding.setExposedPageId(exposed_page_id);
               Style style = null; //new Style();
               //style.setId(idManager.getId(style_id));
               scaffolding.setStyle(style);



               // convert the criteria
               // create new sakai 2.5 Criteria objects which have the same id as their counterparts in osp 2.0
               tableName = getOldTableName("osp_scaffolding_criteria");
               tableName2 = getOldTableName("osp_matrix_label");
               sql = "select * from " + tableName + " join " + tableName2 + " on ELT=ID where parent_id='" + id + "' order by seq_num";
               ResultSet rss = innerStmt.executeQuery(sql);

               Map<String, Criterion> criteriaMap = new HashMap<String, Criterion>();

               try {
                  while (rss.next()) {
                     int sequenceNumber = rss.getInt("seq_num");
                     Id lid = idManager.getId(rss.getString("elt"));
                     String color = rss.getString("color");
                     String textColor = null;
                     String ldescription = rss.getString("description");

                     Criterion criterion = new Criterion();

                     criterion.setId(null);
                     criterion.setNewId(lid);
                     criterion.setColor(color);
                     criterion.setTextColor(textColor);
                     criterion.setScaffolding(scaffolding);
                     criterion.setDescription(ldescription);
                     criterion.setSequenceNumber(sequenceNumber);

                     scaffolding.add(criterion);

                     criteriaMap.put(lid.getValue(), criterion);
                  }
               } finally {
                  rss.close();
               }


               // convert the levels
               // create new sakai 2.5 Level objects which have the same id as their counterparts in osp 2.0
               tableName = getOldTableName("osp_scaffolding_levels");
               tableName2 = getOldTableName("osp_matrix_label");
               sql = "select * from " + tableName + " join " + tableName2 + " on ELT=ID where scaffolding_id='" + id + "' order by seq_num";
               rss = innerStmt.executeQuery(sql);
               Map<String, Level> levelMap = new HashMap<String, Level>();

               try {
                  while (rss.next()) {
                     int sequenceNumber = rss.getInt("seq_num");
                     Id lid = idManager.getId(rss.getString("elt"));
                     String color = rss.getString("color");
                     String textColor = null;
                     String ldescription = rss.getString("description");

                     Level level = new Level();

                     level.setId(null);
                     level.setNewId(lid);
                     level.setColor(color);
                     level.setTextColor(textColor);
                     level.setScaffolding(scaffolding);
                     level.setDescription(ldescription);
                     level.setSequenceNumber(sequenceNumber);

                     scaffolding.add(level);

                     levelMap.put(lid.getValue(), level);
                  }
               } finally {
                  rss.close();
               }


               // convert the scaffolding cells
               tableName = getOldTableName("osp_scaffolding_cell");
               sql = "select * from " + tableName + " where scaffolding_id='" + id + "' ";
               rss = innerStmt.executeQuery(sql);

               Map<String, ScaffoldingCell> scaffoldingCellMap        = new HashMap<String, ScaffoldingCell>();
               Map<String, String         > scaffoldingCellExpheadMap = new HashMap<String, String         >();

               try {
                  while (rss.next()) {
                     Id     cid                = idManager.getId(rss.getString("id"));
                     String criterionStr       = rss.getString("rootcriterion_id");
                     String levelStr           = rss.getString("level_id");
                     String expectationheader  = rss.getString("expectationheader");
                     String initialStatus      = rss.getString("initialstatus");
                     String gradablereflection = rss.getString("gradablereflection");

                     Level           level            = levelMap.get(levelStr);
                     Criterion       criterion        = criteriaMap.get(criterionStr);
                     String          crdescription    = criterion.getDescription();
                     String          lvdescription    = level.getDescription();
                     Id              reflectionFormId = reflectionFormIdMap.get(lvdescription);
                     ScaffoldingCell cell             = new ScaffoldingCell();

                     cell.setId(null);
                     cell.setNewId(cid);
                     cell.setInitialStatus(initialStatus);
                     cell.setLevel(level);
                     cell.setRootCriterion(criterion);
                     cell.setScaffolding(scaffolding);

                     WizardPageDefinition page = cell.getWizardPageDefinition();
                     page.setNewId(idManager.createId());
                     page.setSiteId(worksite);
                     page.setTitle((criterion.getDescription() != null ? criterion.getDescription() : "") + " - " + (level.getDescription() != null ? level.getDescription() : ""));

                     cell.setEvaluationDevice    (evaluationFormId);
                     cell.setEvaluationDeviceType(FORM_TYPE);
                     cell.setReflectionDevice    (reflectionFormId);
                     cell.setReflectionDeviceType(FORM_TYPE);
                     cell.setReviewDevice        (feedbackFormId);
                     cell.setReviewDeviceType    (FORM_TYPE);
//                   cell.setAdditionalForms     (additionalForms);

                     // this needs to be after setting the forms
                     page.setEvalWorkflows(new HashSet(getWorkflowManager().createEvalWorkflows(page)));

                     scaffolding.add(cell);
                     scaffoldingCellMap       .put(cid.getValue(), cell);
                     scaffoldingCellExpheadMap.put(cid.getValue(), expectationheader);
                     List<Authorization> scellAuthzs = authzManager.getAuthorizations(null, MatrixFunctionConstants.EVALUATE_MATRIX, cid);

                     for(Authorization a : scellAuthzs) {
                        authzManager.createAuthorization(a.getAgent(), a.getFunction(), page.getNewId());
                        authzManager.deleteAuthorization(a.getAgent(), a.getFunction(), a.getQualifier());
                     }
                  }
               } finally {
                  rss.close();
               }

               // save the scaffolding!
               Id scaffId = (Id)matrixManager.save(scaffolding);
               scaffolding = matrixManager.getScaffolding(scaffId);


               // migrate the expectations into the guidance.instruction of the cell
               tableName  = getOldTableName("osp_scaffolding_cell");
               tableName2 = getOldTableName("osp_expectation");
               tableName3 = getOldTableName("osp_matrix_label");
               sql = "select SCAFFOLDING_CELL_ID, " + tableName3 + ".ID, DESCRIPTION " +
               " FROM " + tableName +
               " JOIN " + tableName2 + " ON " + tableName + ".ID=SCAFFOLDING_CELL_ID " +
               " JOIN " + tableName3 + " ON ELT=" + tableName3 + ".ID " +
               " where scaffolding_id='" + id + "' ORDER BY SCAFFOLDING_CELL_ID, SEQ_NUM";

               rss = innerStmt.executeQuery(sql);
               String lastScaffoldingCellId = "",
                      guidanceText          = null,
                      scaffoldingCellId     = null;

               try {
                  while (rss.next()) {
                     scaffoldingCellId = rss.getString("SCAFFOLDING_CELL_ID");

                     if(!scaffoldingCellId.equals(lastScaffoldingCellId)) {
                        if(guidanceText != null) {
                           ScaffoldingCell scell = scaffoldingCellMap.get(lastScaffoldingCellId);
                           Guidance        guide = guidanceManager.createNew("", worksite, scell.getWizardPageDefinition().getId(), MatrixFunctionConstants.VIEW_SCAFFOLDING_GUIDANCE, MatrixFunctionConstants.EDIT_SCAFFOLDING_GUIDANCE);
                           guidanceText += "</ul>";
                           guide.getInstruction().setText(guidanceText);
                           scell.setGuidance(guide);
                           matrixManager.storeScaffoldingCell(scell);
                        }
                        lastScaffoldingCellId = scaffoldingCellId;
                        //starts a new cell
                        String expHeader = (String)scaffoldingCellExpheadMap.get(scaffoldingCellId);
                        guidanceText = expHeader + "\n<ul>";
                     }
                     if(guidanceText.length() > 0)
                        guidanceText += "\n<br />";
                     guidanceText += "<li>" + rss.getString("DESCRIPTION") + "</li>";
                  }

                  if(guidanceText != null) {
                     ScaffoldingCell scell = (ScaffoldingCell)scaffoldingCellMap.get(scaffoldingCellId);
                     Guidance guide = guidanceManager.createNew("", worksite, scell.getWizardPageDefinition().getId(), MatrixFunctionConstants.VIEW_SCAFFOLDING_GUIDANCE, MatrixFunctionConstants.EDIT_SCAFFOLDING_GUIDANCE);
                     guidanceText += "</ul>";
                     guide.getInstruction().setText(guidanceText);
                     scell.setGuidance(guide);
                     matrixManager.storeScaffoldingCell(scell);
                  }
               } finally {
                  rss.close();
               }




// joneric - this is the custom stuff
//           where we migrate the matrices for a particular school
//           every school will have different matrices/forms

               // convert the user matrices
               tableName  = getOldTableName("osp_matrix_tool");
               tableName2 = getOldTableName("osp_matrix");
               tableName3 = getOldTableName("osp_matrix_cell");
               sql = "select " + tableName3 + ".id, matrix_id, owner, status, reflection_id, scaffolding_cell_id " +
                     " from " + tableName + " join " + tableName2 +
                     " on matrixtool_id=" + tableName + ".id " +
                     " join " + tableName3 + " on matrix_id=" + tableName2 + ".id " +
                     " where scaffolding_id='" + id + "' order by owner";

               logger.debug("get matrix list: " + sql);
               rss = innerStmt.executeQuery(sql);

               tableName  = getOldTableName("osp_reflection");
               tableName2 = getOldTableName("osp_reflection_item");
               tableName3 = getOldTableName("osp_reviewer_item");

               String  lastOwner        = "";
               String  lastMatrixId     = "";
               Matrix  matrix           = null;
               String  oldMatrixId      = null;
               boolean badCell          = false;
               boolean badMatrix        = false;
               int     intelGrowthIndex = 1;

               try {
                  while (rss.next()) {
                     oldMatrixId                = rss.getString("matrix_id");
                     String mcidStr             = rss.getString("id");
                     Id     mcid                = idManager.getId(mcidStr);
                     String mowner              = rss.getString("owner");
                     String status              = rss.getString("status");
                     String scaffolding_cell_id = rss.getString("scaffolding_cell_id");

                     if(!mowner.equals(lastOwner)) {
                        if(matrix != null && !badCell) {
                           if(matrix.getOwner() == null)
                              logger.error("OSP Migration Error: The matrix owner agent couldn't be found: " + lastOwner);
                           else if(matrix.getOwner().getId() == null)
                              logger.error("OSP Migration Error: The matrix owner agent id couldn't be found: " + lastOwner);
                           else if(matrix.getOwner().getId().getValue() == null)
                              logger.error("OSP Migration Error: The matrix owner agent id value couldn't be found: " + lastOwner);
                           else {
                              Object newMatrix = matrixManager.save(matrix);
                              matrixIdMap.put(lastMatrixId, matrix.getId().getValue());
                           }
                        }

                        lastOwner    = mowner;
                        lastMatrixId = oldMatrixId;
                        matrix       = new Matrix();

                        matrix.setOwner(agentManager.getAgent(mowner));
                        matrix.setScaffolding(scaffolding);

                        badMatrix = matrix.getOwner() == null || matrix.getOwner().getId() == null || matrix.getOwner().getId().getValue() == null;
                        badCell   = false;
                     }
                     badCell = scaffolding_cell_id == null || badMatrix;

                     if(!badCell) {
                        ScaffoldingCell sCell = (ScaffoldingCell)scaffoldingCellMap.get(scaffolding_cell_id);

                        // convert the matrix cell file attachments (non form files, but rather uploaded files like word docs, image files, text docs, etc)
                        boolean isReady = status.equals(MatrixFunctionConstants.READY_STATUS);
                        Cell cell = new Cell();
                        cell.setNewId(mcid);
                        cell.getWizardPage().setNewId(idManager.createId());
                        cell.getWizardPage().setOwner(matrix.getOwner());
                        cell.setScaffoldingCell(sCell);
                        cell.setStatus(status);

                        Set<Attachment> attachments = new HashSet<Attachment>();
                        String cellAttachmentTable = getOldTableName("osp_cell_attachment");
                        sql = "select * from " + cellAttachmentTable + " where cell_id='" + mcidStr + "'";
                        ResultSet rsCellFiles = matrixInnerStmt.executeQuery(sql);
                        try {
                           while(rsCellFiles.next()) {
                              String     attid    = rsCellFiles.getString("id");
                              String     artifact = rsCellFiles.getString("artifactId");
                              Attachment att      = new Attachment();
                              att.setNewId(idManager.getId(attid));
                              att.setArtifactId(idManager.getId(artifact));
                              att.setWizardPage(cell.getWizardPage());
                              attachments.add(att);
                              if(!isReady)
                                 contentHosting.lockObject(artifact, cell.getWizardPage().getNewId().getValue(), "cell atts locked on submit", true);
                           }
                           cell.setAttachments(attachments);
                        } finally {
                           rsCellFiles.close();
                        }

                        // custom wsu code:
                        // get the intellectual growth statement from osp 2.0.
                        tableName = getOldTableName("osp_reflection");
                        sql = "select id, growthstatement from " + tableName + " where cell_id='" + mcidStr + "'";
                        ResultSet intelGrowthRS = matrixInnerStmt.executeQuery(sql);
                        try {
                           if (intelGrowthRS.next()) {
                              String reflection_id = intelGrowthRS.getString(1);
                              String growth        = intelGrowthRS.getString(2);

                              // wsu matrix cells have either 2 expectations or three, depending on the level (ie, column) of the cell.
                              sql = "select connecttext, evidence from " + tableName2 + " where reflection_id='" + reflection_id + "' order by seq_num";
                              ResultSet reflectionRS = matrixInnerStmt.executeQuery(sql);
                              Set<WizardPageForm> pageForms = new HashSet<WizardPageForm>();
                              String[] connect  = new String[3];
                              String[] evidence = new String[3];
                              try {
                                 int i=0;
                                 while(reflectionRS.next() && i<3) {
                                    connect [i] = reflectionRS.getString("CONNECTTEXT");
                                    evidence[i] = reflectionRS.getString("EVIDENCE");
                                    i++;
                                 }

                                 // save the reflection
                                 String  maxtrixOwnerId   = matrix.getOwner().getId().getValue();
                                 Integer anId             = incUser(maxtrixOwnerId);
                                         title            = "Reflection " + scaffolding.getTitle() + " " + sCell.getRootCriterion().getDescription() + " " + sCell.getLevel().getDescription() + " - " + anId.toString();
                                 Id      formId           = reflectionFormIdMap.get(sCell.getLevel().getDescription());
                                 Id      reflectionFormId = createReflectionForm(formId, maxtrixOwnerId, title, growth, connect[0], evidence[0], connect[1], evidence[1], connect[2], evidence[2]);

                                 Review review = reviewManager.createNew("", worksite);
                                 review.setDeviceId(formId.getValue());                            // form id
                                 review.setParent  (cell.getWizardPage().getNewId().getValue());   // wizard page
                                 review.setType    (Review.REFLECTION_TYPE);                       // constant
                                 review.setReviewContent(reflectionFormId);
                                 getReviewManager().saveReview(review);

                                 if(!isReady)
                                    contentHosting.lockObject(reflectionFormId.getValue(), review.getId().getValue(), "reflection submitted", true);

                              } finally {
                                 reflectionRS.close();
                              }
                           }
                        } finally {
                           intelGrowthRS.close();
                        }

                        //
/*                      sql = "SELECT ID, REVIEWER_ID, COMMENTS, GRADE, STATUS, CREATED, MODIFIED FROM " + tableName3 + " WHERE CELL_ID='" + mcidStr + "'";
                        ResultSet evalRS = matrixInnerStmt.executeQuery(sql);
                        try {
                           while(evalRS.next()) {
                              String riid        = evalRS.getString("ID");
                              String reviewer_id = evalRS.getString("REVIEWER_ID");
                              String comment     = evalRS.getString("COMMENTS");
                              String grade       = evalRS.getString("GRADE");
                              String ri_status   = evalRS.getString("STATUS");
                              String ri_created  = evalRS.getString("CREATED");
                              String ri_modified = evalRS.getString("MODIFIED");

                              // save the Reviews
                              //Skip if the reviewer is null
                              if (reviewer_id != null && !reviewer_id.equalsIgnoreCase("")) {
                                 Integer anId = incUser(reviewer_id);
                                 Id evaluationForm = createEvaluationForm(reviewer_id,
                                                     "Review "+ scaffolding.getTitle() +
                                                     " " + sCell.getRootCriterion().getDescription() +
                                                     " " + sCell.getLevel().getDescription() +
                                                     " - " + anId.toString(),
                                                     grade, comment);

                                 Review review = reviewManager.createNew("", worksite);
                                 review.setDeviceId(evaluationFormId.getValue());// form idvalue
                                 Id pageId = resolveId(cell.getWizardPage());
                                 review.setParent(pageId.getValue());// wizard page
                                 review.setType(Review.EVALUATION_TYPE);//contant
                                 review.setReviewContent(evaluationForm);
                                 review = getReviewManager().saveReview(review);

                                 contentHosting.lockObject(evaluationForm.getValue(), review.getId().getValue(), "evaluation is once off", true);
                              }
                           }
                        } finally {
                           evalRS.close();
                        }
                        */
                        matrix.add(cell);
                     }
                  }  // end while(rss.next()) -- looping through each matrix
               } finally {
                  rss.close();
               }

               if(matrix != null && !badCell) {
                  if(matrix.getOwner() == null)
                     logger.error("OSP Migration Error: The matrix owner agent couldn't be found: " + lastOwner);
                  else if(matrix.getOwner().getId() == null)
                     logger.error("OSP Migration Error: The matrix owner agent id couldn't be found: " + lastOwner);
                  else if(matrix.getOwner().getId().getValue() == null)
                     logger.error("OSP Migration Error: The matrix owner agent id value couldn't be found: " + lastOwner);
                  else {
                     Object newMatrix = matrixManager.save(matrix);
                     matrixIdMap.put(lastMatrixId, matrix.getId().getValue());
                  }
               }
            }  // end while(rs.next()) -- looping through each scaffolding
         } finally {
            rs.close();
         }
      } catch (Exception e) {
         logger.error("error selecting data with this sql: " + sql);
         logger.error("", e);
         throw new JobExecutionException(e);
      } finally {
         try {
            stmt.close();
            innerStmt.close();
            matrixInnerStmt.close();
         } catch (Exception e) {
             // ignore
         }
      }
      for(String oldMatrixId : matrixIdMap.keySet())
         logger.debug("old matrix id: " + oldMatrixId + "     new matrix id: " + matrixIdMap.get(oldMatrixId));

      logger.info("Quartz task fininshed: runMatrixMigration()");
   }

   protected void runPresentationTemplateMigration(Connection con, boolean isDeveloper) throws JobExecutionException {
      logger.info("Quartz task started: runPresentationTemplateMigration()");
      String templateTableName = getOldTableName("osp_presentation_template");
      String sql = "select * from " + templateTableName;

      try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            try {
               while (rs.next()) {
                  String id = rs.getString("id");
                  String name = rs.getString("name");
                  String desc = rs.getString("description");
                  boolean includeHeaderAndFooter = rs.getBoolean("includeHeaderAndFooter");
                  //boolean includeComments = rs.getBoolean("includeComments");
                  boolean published = rs.getBoolean("published");
                  String owner = rs.getString("owner_id");
                  String renderer = rs.getString("renderer");
                  String markup = rs.getString("markup");
                  String propertyPage = rs.getString("propertyPage");
                  String documentRoot = rs.getString("documentRoot");
                  Date created = rs.getDate("created");
                  Date modified = rs.getDate("modified");
                  String siteId = rs.getString("site_id");
                  Id tid = idManager.getId(id);

                  PresentationTemplate template = new PresentationTemplate();
                  template.setId(null);
                  template.setNewId(tid);
                  template.setName(name);
                  template.setDescription(desc);
                  template.setIncludeHeaderAndFooter(includeHeaderAndFooter);
                  template.setPublished(published);
                  if (agentManager.getAgent(owner) == null || agentManager.getAgent(owner).getId() == null) {
                     template.setOwner(agentManager.getAgent("admin"));
                  } else {
                     template.setOwner(agentManager.getAgent(owner));
                  }
                  template.setRenderer(idManager.getId(renderer));
                  template.setMarkup(markup);
                  template.setPropertyPage(idManager.getId(propertyPage));
                  template.setDocumentRoot(documentRoot);
                  template.setCreated(created);
                  template.setModified(modified);
                  template.setSiteId(siteId);

                  Set<PresentationItemDefinition> itemDefs = createTemplateItemDefs(con, template);
                  template.setItems(itemDefs);

                  Set<TemplateFileRef> fileRefs = createTemplateFileRefs(con, template);
                  template.setFiles(fileRefs);


                  if(template.getOwner() == null)
                     logger.error("OSP Migration Error: The template owner agent couldn't be found: " + owner);
                  else if(template.getOwner().getId() == null)
                     logger.error("OSP Migration Error: The template owner agent id couldn't be found: " + owner);
                  else if(template.getOwner().getId().getValue() == null)
                     logger.error("OSP Migration Error: The template owner agent id value couldn't be found: " + owner);
                  else
                     presentationManager.storeTemplate(template, false, false);

               }
           } finally {
               rs.close();
           }
        } catch (Exception e) {
            logger.error("error selecting data with this sql: " + sql);
            logger.error("", e);
            throw new JobExecutionException(e);
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
                // ignore
            }
        }
        logger.info("Quartz task fininshed: runPresentationTemplateMigration()");
   }

   protected void runPresentationMigration(Connection con, boolean isDeveloper) throws JobExecutionException {
      logger.info("Quartz task started: runPresentationMigration()");
      String templateTableName = getOldTableName("osp_presentation");
      String sql = "select * from " + templateTableName;

      try {
         stmt = con.createStatement();
         ResultSet rs = stmt.executeQuery(sql);
         try {
            while (rs.next()) {
               String id = rs.getString("id");
               String owner = rs.getString("owner_id");
               String templateId = rs.getString("template_id");
               String name = rs.getString("name");
               String desc = rs.getString("description");
               boolean isDefault = rs.getBoolean("isDefault");
               boolean isPublic = rs.getBoolean("isPublic");

               Date expiresOn = rs.getDate("expiresOn");
               Date created = rs.getDate("created");
               Date modified = rs.getDate("modified");
               //Blob properties = rs.getBlob("properties");
               String toolId = rs.getString("tool_id");
               Id pid = idManager.getId(id);

               Presentation presentation = new Presentation();
               presentation.setNewObject(true);
               presentation.setId(null);
               presentation.setNewId(pid);
               presentation.setName(name);
               presentation.setDescription(desc);
               presentation.setIsDefault(isDefault);
               presentation.setIsPublic(isPublic);
               presentation.setOwner(agentManager.getAgent(owner));
               presentation.setExpiresOn(expiresOn);
               presentation.setCreated(created);
               presentation.setModified(modified);
               presentation.setToolId(toolId);

               if(presentation.getOwner() == null) {
                  logger.error("OSP Migration Error: The presentation owner agent couldn't be found: " + owner);
                  continue;
               } else if(presentation.getOwner().getId() == null) {
                  logger.error("OSP Migration Error: The presentation owner agent id couldn't be found: " + owner);
                  continue;
               } else if(presentation.getOwner().getId().getValue() == null) {
                  logger.error("OSP Migration Error: The presentation owner agent id value couldn't be found: " + owner);
                  continue;
               }

               String siteId;
               try {
                  siteId = siteService.findTool(toolId).getContext();
               } catch (NullPointerException npe) {
                  logger.warn("Quartz task warning: runPresentationMigration().  Can't find context for toolId: " + toolId);
                  siteId = "dataMigrationError";
               }

               presentation.setSiteId(siteId);

               try
               {
                  HibernatePresentationProperties hpp = new HibernatePresentationProperties();
                  String[] names = {"properties"};
                  Object props = hpp.nullSafeGet(rs, names, null);
                  presentation.setProperties((ElementBean)props);
                  presentation.setPresentationType(Presentation.TEMPLATE_TYPE);
                  PresentationTemplate template = presentationManager.getPresentationTemplate(idManager.getId(templateId));
                  presentation.setTemplate(template);
               }
               catch (Throwable ex)
               {
                  logger.error("Error migrating presentation outline");
                  ex.printStackTrace();
               }

                //TODO template no longer has comments, replace line below
               //presentation.setAllowComments(template.isIncludeComments());

               Set<PresentationItem> items = createPresentationItems(con, presentation);
               presentation.setItems(items);

               presentationManager.storePresentation(presentation, false, false);

               createPresentationComments(con, presentation);
               createPresentationLogs(con, presentation);
            }
         } finally {
            rs.close();
         }
      } catch (Exception e) {
         logger.error("error selecting data with this sql: " + sql);
         logger.error("", e);
         throw new JobExecutionException(e);
      } finally {
         try {stmt.close();} catch (Exception e) {/* ignore */}
      }
      logger.info("Quartz task fininshed: runPresentationMigration()");
   }

   // fix presentation items that point to an old matrix in their artifact_id column
   protected void runPresentationItemFix(Connection con, boolean isDeveloper) throws JobExecutionException {
      logger.info("Quartz task started: runPresentationItemFix()");

      String    tableName = "osp_presentation_item";
      String    sql       = "select presentation_id, artifact_id, item_definition_id from osp_presentation_item";
      Statement statement = null;

      // loop through the osp presentation items and see which ones are pointing to a matrix
      try {
         statement = con.createStatement();
         ResultSet rs = statement.executeQuery(sql);
         try {
            while (rs.next()) {
               String presentationId   = rs.getString("presentation_id"  );
               String artifactId       = rs.getString("artifact_id"       );
               String itemDefinitionId = rs.getString("item_definition_id");
               String newMatrixId      = matrixIdMap.get(artifactId);

               // is the artifact id pointing to the old matrix id
               if (newMatrixId != null) {
                  PreparedStatement statement2 = null;
                  String            sql2       = "update osp_presentation_item set artifact_id = ? where presentation_id = ? and artifact_id = ? and item_definition_id = ?";
                  try {
                     statement2 = con.prepareStatement(sql2);
                     statement2.setString(1, newMatrixId     );
                     statement2.setString(2, presentationId  );
                     statement2.setString(3, artifactId      );
                     statement2.setString(4, itemDefinitionId);
                     statement2.executeUpdate();
                  } catch (Exception ex) {
                     logger.error("error updating the osp_presentation_item table to fix old matrix artifact_ids: " + sql2, ex);
                     throw new JobExecutionException(ex);
                  } finally {
                     if (statement2 != null)
                        statement2.close();
                  }
               }
            }
         } finally {
            rs.close();
         }
      } catch (Exception e) {
         logger.error("error selecting data with this sql: " + sql, e);
         throw new JobExecutionException(e);
      } finally {
         try {
            statement.close();
         } catch (Exception e) {
            e.printStackTrace();
            // nothing can be done here
         }
      }
   }

   protected Set<PresentationItemDefinition> createTemplateItemDefs(Connection con, PresentationTemplate template) throws JobExecutionException {
      Set<PresentationItemDefinition> itemDefs = new HashSet<PresentationItemDefinition>();
      String itemDefTableName = getOldTableName("osp_presentation_item_def");
      String sql = "select * from " + itemDefTableName + " where template_id = '" + resolveId(template).getValue() + "'";
      Statement innerstmt = null;
      try {

         innerstmt = con.createStatement();
         ResultSet rs2 = innerstmt.executeQuery(sql);
         try {
            while (rs2.next()) {

               String id = rs2.getString("id");
               String name = rs2.getString("name");
               String title = rs2.getString("title");
               String description = rs2.getString("description");
               boolean allowMultiple = rs2.getBoolean("allowMultiple");
               String type = rs2.getString("type");
               String externalType = rs2.getString("external_type");
               int seq = rs2.getInt("sequence_no");
               //String templateId = rs2.getString("template_id");

               Id defid = idManager.getId(id);
               PresentationItemDefinition pid = new PresentationItemDefinition();
               pid.setId(null);
               pid.setNewId(defid);
               pid.setName(name);
               pid.setTitle(title);
               pid.setDescription(description);
               pid.setAllowMultiple(allowMultiple);
               pid.setType(type);
               pid.setExternalType(externalType);
               pid.setSequence(seq);
               pid.setPresentationTemplate(template);
               Set mimeTypes = createTemplateItemDefMimeTypes(con, pid);
               pid.setMimeTypes(mimeTypes);

               itemDefs.add(pid);
            }

         } finally {
            rs2.close();
         }
      } catch (Exception e) {
         logger.error("error selecting data with this sql: " + sql);
         logger.error("", e);
         throw new JobExecutionException(e);
      } finally {
         try {
            innerstmt.close();
         } catch (Exception e) {
             // ignore
         }
      }
      return itemDefs;
   }

   protected Set<TemplateFileRef> createTemplateFileRefs(Connection con, PresentationTemplate template) throws JobExecutionException {
      Set<TemplateFileRef> fileRefs = new HashSet<TemplateFileRef>();
      String fileRefTableName = getOldTableName("osp_template_file_ref");
      String sql = "select * from " + fileRefTableName + " where template_id = '" + resolveId(template).getValue() + "'";
      Statement innerstmt = null;
      try {

         innerstmt = con.createStatement();
         ResultSet rs2 = innerstmt.executeQuery(sql);
         try {
            while (rs2.next()) {

               String id = rs2.getString("id");
               String fileId = rs2.getString("file_id");
               String fileTypeId = rs2.getString("file_type");
               String usage = rs2.getString("usage_desc");
               //String templateId = rs2.getString("template_id");

               Id refid = idManager.getId(id);
               TemplateFileRef tfr = new TemplateFileRef();
               tfr.setId(null);
               tfr.setNewId(refid);
               tfr.setFileId(fileId);
               tfr.setFileType(fileTypeId);
               tfr.setUsage(usage);

               tfr.setPresentationTemplate(template);

               fileRefs.add(tfr);
            }

         } finally {
            rs2.close();
         }
      } catch (Exception e) {
         logger.error("error selecting data with this sql: " + sql);
         logger.error("", e);
         throw new JobExecutionException(e);
      } finally {
         try {
            innerstmt.close();
         } catch (Exception e) {
             // ignore
         }
      }
      return fileRefs;
   }

   protected Set createTemplateItemDefMimeTypes(Connection con, PresentationItemDefinition itemDef) throws JobExecutionException {
      Set mimeTypes = new HashSet();
      String itemDefMimeTypeTableName = getOldTableName("osp_pres_itemdef_mimetype");
      String sql = "select * from " + itemDefMimeTypeTableName + " where item_def_id = '" + resolveId(itemDef).getValue() + "'";
      Statement innerstmt = null;
      try {

         innerstmt = con.createStatement();
         ResultSet rs2 = innerstmt.executeQuery(sql);
         try {
            while (rs2.next()) {

               String primary = rs2.getString("primaryMimeType");
               String secondary = rs2.getString("secondaryMimeType");

               ItemDefinitionMimeType mimeType = new ItemDefinitionMimeType(primary, secondary);

               mimeTypes.add(mimeType);
            }

         } finally {
            rs2.close();
         }
      } catch (Exception e) {
         logger.error("error selecting data with this sql: " + sql);
         logger.error("", e);
         throw new JobExecutionException(e);
      } finally {
         try {
            innerstmt.close();
         } catch (Exception e) {
         }
      }
      return mimeTypes;
   }

   protected Set<PresentationItem> createPresentationItems(Connection con, Presentation presentation) throws JobExecutionException {
      Set<PresentationItem> items         = new HashSet<PresentationItem>();
      String                itemTableName = getOldTableName("osp_presentation_item");
      String                sql           = "select * from " + itemTableName + " where presentation_id = '" + resolveId(presentation).getValue() + "'";
      Statement             innerstmt     = null;

      try {

         innerstmt = con.createStatement();
         ResultSet rs2 = innerstmt.executeQuery(sql);
         try {
            while (rs2.next()) {

               String artifactId = rs2.getString("artifact_id");
               String itemDef = rs2.getString("item_definition_id");

               PresentationItem item = new PresentationItem();
               item.setArtifactId(idManager.getId(artifactId));
               item.setDefinition(presentationManager.getPresentationItemDefinition(idManager.getId(itemDef)));

               items.add(item);
            }

         } finally {
            rs2.close();
         }
      } catch (Exception e) {
         logger.error("error selecting data with this sql: " + sql);
         logger.error("", e);
         throw new JobExecutionException(e);
      } finally {
         try {
            innerstmt.close();
         } catch (Exception e) {
             // ignore
         }
      }
      return items;
   }

   protected void createPresentationComments(Connection con, Presentation presentation) throws JobExecutionException {
      String commentTableName = getOldTableName("osp_presentation_comment");
      String sql = "select * from " + commentTableName + " where presentation_id = '" + resolveId(presentation).getValue() + "'";
      Statement innerstmt = null;
      try {

         innerstmt = con.createStatement();
         ResultSet rs2 = innerstmt.executeQuery(sql);
         try {
            while (rs2.next()) {

               String id = rs2.getString("id");
               String title = rs2.getString("title");
               String commentText = rs2.getString("commentText");
               String owner = rs2.getString("creator_id");
               byte visibility = rs2.getByte("visibility");
               Date created = rs2.getDate("created");

               Id cid = idManager.getId(id);

               Agent creator = agentManager.getAgent(idManager.getId(owner));
               if (creator != null && creator.getId() != null) {
                  PresentationComment comment = new PresentationComment();
                  comment.setId(null);
                  comment.setNewId(cid);
                  comment.setTitle(title);
                  comment.setComment(commentText);
                  comment.setCreator(creator);
                  comment.setPresentation(presentation);
                  comment.setVisibility(visibility);
                  comment.setCreated(created);
                  presentationManager.createComment(comment, false, false);
               }
            }
         } finally {
            rs2.close();
         }
      } catch (Exception e) {
         logger.error("error selecting data with this sql: " + sql);
         logger.error("", e);
         throw new JobExecutionException(e);
      } finally {
         try {
            innerstmt.close();
         } catch (Exception e) {
         }
      }
   }

   protected void createPresentationLogs(Connection con, Presentation presentation) throws JobExecutionException {
      String logTableName = getOldTableName("osp_presentation_log");
      String sql = "select * from " + logTableName + " where presentation_id = '" + resolveId(presentation).getValue()  +"'";
      Statement innerstmt = null;
      try {

         innerstmt = con.createStatement();
         ResultSet rs2 = innerstmt.executeQuery(sql);
         try {
            while (rs2.next()) {

               String id = rs2.getString("id");
               String viewer = rs2.getString("viewer_id");
               Date viewed = rs2.getDate("view_date");

               Id lid = idManager.getId(id);

               //discard any views from users that don't exist anymore
               Agent viewerAgent = agentManager.getAgent(idManager.getId(viewer));
               if (viewerAgent != null && viewerAgent.getId() != null) {
                  PresentationLog log = new PresentationLog();
                  log.setId(null);
                  log.setNewId(lid);
                  log.setViewer(viewerAgent);
                  log.setViewDate(viewed);
                  log.setPresentation(presentation);
                  presentationManager.storePresentationLog(log);
               }
            }
         } finally {
            rs2.close();
         }
      } catch (Exception e) {
         logger.error("error selecting data with this sql: " + sql);
         logger.error("", e);
         throw new JobExecutionException(e);
      } finally {
         try {
            innerstmt.close();
         } catch (Exception e) {
         }
      }
   }

   private Integer incUser(String userId)
   {
      Integer accessTimes = (Integer)userUniquenessMap.get(userId);

      if(accessTimes == null)
         accessTimes = new Integer(1);
      else
         accessTimes = new Integer(accessTimes.intValue() + 1);
      userUniquenessMap.put(userId, accessTimes);
      return accessTimes;
   }

   private Id resolveId(IdentifiableObject obj) {
      if (obj.getId() == null)
         return obj.getNewId();
      return obj.getId();
   }

   protected String getOldTableName(String tableName)
   {
      return (String)getTableMap().get(tableName);
   }

   public DataSource getDataSource() {
      return dataSource;
   }

   public void setDataSource(DataSource dataSource) {
      this.dataSource = dataSource;
   }

   public IdManager getIdManager() {
      return idManager;
   }

   public void setIdManager(IdManager idManager) {
      this.idManager = idManager;
   }

   public AgentManager getAgentManager() {
      return agentManager;
   }

   public void setAgentManager(AgentManager agentManager) {
      this.agentManager = agentManager;
   }

   public AuthorizationFacade getAuthzManager() {
      return authzManager;
   }

   public void setAuthzManager(AuthorizationFacade authzManager) {
      this.authzManager = authzManager;
   }

   public Map<String, String> getTableMap() {
      return tableMap;
   }

   public void setTableMap(Map<String, String> tableMap) {
      this.tableMap = tableMap;
   }

   public Glossary getGlossaryManager() {
      return glossaryManager;
   }

   public void setGlossaryManager(Glossary glossaryManager) {
      this.glossaryManager = glossaryManager;
   }

   public MatrixManager getMatrixManager() {
      return matrixManager;
   }

   public void setMatrixManager(
         MatrixManager matrixManager) {
      this.matrixManager = matrixManager;
   }

   public PresentationManager getPresentationManager() {
      return presentationManager;
   }

   public void setPresentationManager(
         PresentationManager presentationManager) {
      this.presentationManager = presentationManager;
   }

   public StructuredArtifactDefinitionManager getStructuredArtifactDefinitionManager() {
      return structuredArtifactDefinitionManager;
   }

   public void setStructuredArtifactDefinitionManager(
         StructuredArtifactDefinitionManager structuredArtifactDefinitionManager) {
      this.structuredArtifactDefinitionManager = structuredArtifactDefinitionManager;
   }

   public SiteService getSiteService() {
      return siteService;
   }

   public void setSiteService(SiteService siteService) {
      this.siteService = siteService;
   }

   public List<String> getAuthzToolFunctions() {
      return authzToolFunctions;
   }

   public void setAuthzToolFunctions(List<String> authzToolFunctions) {
      this.authzToolFunctions = authzToolFunctions;
   }

   public List getMatrixForms() {
      return matrixForms;
   }

   public void setMatrixForms(List matrixForms) {
      this.matrixForms = matrixForms;
   }

   public SecurityService getSecurityService() {
      return securityService;
   }

   public void setSecurityService(SecurityService securityService) {
      this.securityService = securityService;
   }

   public ContentHostingService getContentHosting() {
      return contentHosting;
   }

   public void setContentHosting(ContentHostingService contentHosting) {
      this.contentHosting = contentHosting;
   }

   public GuidanceManager getGuidanceManager() {
      return guidanceManager;
   }

   public void setGuidanceManager(GuidanceManager guidanceManager) {
      this.guidanceManager = guidanceManager;
   }

   public ReviewManager getReviewManager() {
      return reviewManager;
   }

   public void setReviewManager(ReviewManager reviewManager) {
      this.reviewManager = reviewManager;
   }

   public DefaultScaffoldingBean getDefaultScaffoldingBean() {
      return defaultScaffoldingBean;
   }

   public void setDefaultScaffoldingBean(
         DefaultScaffoldingBean defaultScaffoldingBean) {
      this.defaultScaffoldingBean = defaultScaffoldingBean;
   }

   /**
    * @return Returns the workflowManager.
    */
   public WorkflowManager getWorkflowManager() {
      return workflowManager;
   }

   /**
    * @param workflowManager The workflowManager to set.
    */
   public void setWorkflowManager(WorkflowManager workflowManager) {
      this.workflowManager = workflowManager;
   }
}

