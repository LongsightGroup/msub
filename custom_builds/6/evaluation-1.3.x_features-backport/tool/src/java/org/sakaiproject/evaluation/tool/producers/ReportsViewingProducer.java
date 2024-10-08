/**
 * $Id: ReportsViewingProducer.java 69470 2010-07-29 08:04:59Z lovemore.nalube@uct.ac.za $
 * $URL: https://source.sakaiproject.org/contrib/evaluation/branches/1.3.x/tool/src/java/org/sakaiproject/evaluation/tool/producers/ReportsViewingProducer.java $
 * ReportsViewingProducer.java - evaluation - Oct 05, 2006 9:23:47 PM - whumphri
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.producers;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.ReportingPermissions;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.utils.EvalResponseAggregatorUtil;
import org.sakaiproject.evaluation.tool.utils.RenderingUtils;
import org.sakaiproject.evaluation.tool.utils.RenderingUtils.AnswersMean;
import org.sakaiproject.evaluation.tool.viewparams.DownloadReportViewParams;
import org.sakaiproject.evaluation.tool.viewparams.ReportParameters;
import org.sakaiproject.evaluation.utils.ArrayUtils;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.DataTemplateItem;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.HierarchyNodeGroup;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.TemplateItemGroup;
import org.sakaiproject.util.Validator;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInitBlock;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * rendering the report results from an evaluation
 * 
 * @author Steven Githens
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Will Humphries (whumphri@vt.edu)
 */
public class ReportsViewingProducer implements ViewComponentProducer, ViewParamsReporter {
    private static Log log = LogFactory.getLog(ReportsViewingProducer.class);

    private static final String VIEWMODE_REGULAR = "viewmode_regular";
    private static final String VIEWMODE_ALLESSAYS = "viewmode_allessays";
    private static final String VIEWMODE_SELECTITEMS = "viewmode_selectitems";

    public static final String VIEW_ID = "report_view";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalResponseAggregatorUtil responseAggregator;
    public void setEvalResponseAggregatorUtil(EvalResponseAggregatorUtil bean) {
        this.responseAggregator = bean;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private EvalAuthoringService authoringService;
    public void setAuthoringService(EvalAuthoringService authoringService) {
        this.authoringService = authoringService;
    }

    private EvalSettings evalSettings;
    public void setEvalSettings(EvalSettings evalSettings) {
        this.evalSettings = evalSettings;
    }

    private ReportingPermissions reportingPermissions;
    public void setReportingPermissions(ReportingPermissions perms) {
        this.reportingPermissions = perms;
    }
    
    private NavBarRenderer navBarRenderer;
    public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
	}

    int totalCommentsCount = 0;
    int totalTextResponsesCount = 0;
    int displayNumber = 0;

    String currentViewMode = VIEWMODE_REGULAR;
    boolean collapseEssays = true;
    Long[] itemsToView;

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
        ReportParameters reportViewParams = (ReportParameters) viewparams;
        String currentUserId = commonLogic.getCurrentUserId();

        if (VIEWMODE_ALLESSAYS.equals(reportViewParams.viewmode)) {
            currentViewMode = VIEWMODE_ALLESSAYS;
            collapseEssays = false;
        }
        else if (VIEWMODE_SELECTITEMS.equals(reportViewParams.viewmode)) {
            currentViewMode = VIEWMODE_SELECTITEMS;
            collapseEssays = false;
            if (reportViewParams.items == null) {
                itemsToView = new Long[] {};
            }
            else {
                itemsToView = reportViewParams.items;
            }
        }
        else {
            currentViewMode = VIEWMODE_REGULAR;
        }

        if (! reportViewParams.external) {
        	navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());
        }

        UIMessage.make(tofill, "view-report-title", "viewreport.page.title");

        Long evaluationId = reportViewParams.evaluationId;
        if (evaluationId == null) {
            // invalid view params
            throw new IllegalArgumentException("Evaluation id is required to view report");
        } else {
            /*
             * We only need to show the choose groups breadcrumb if it's actually 
             * possible for us to view more than one group.
             */
            Set<String> viewableGroups = reportingPermissions.getResultsViewableEvalGroupIdsForCurrentUser(evaluationId);
            if (viewableGroups.isEmpty()) {
                UIMessage.make(tofill, "security-warning", "viewreport.not.allowed");
                return;
            } else if (viewableGroups.size() == 1) {
                // only one group to view
                reportViewParams.groupIds = viewableGroups.toArray(new String[] {});
            } else if (viewableGroups.size() > 1) {
                // user can choose other groups so give them a link
                UIInternalLink.make(tofill, "report-groups-title", UIMessage.make("reportgroups.page.title"), 
                        new ReportParameters(ReportChooseGroupsProducer.VIEW_ID, reportViewParams.evaluationId));
                if (reportViewParams.groupIds == null || reportViewParams.groupIds.length == 0) {
                    reportViewParams.groupIds = viewableGroups.toArray(new String[] {});
                }
            }

            EvalEvaluation evaluation = evaluationService.getEvaluationById(evaluationId);

            // do a permission check
            if (! reportingPermissions.canViewEvaluationResponses(evaluation, reportViewParams.groupIds)) {
                throw new SecurityException("Invalid user attempting to access reports page: " + currentUserId);
            }

            Long templateId = evaluation.getTemplate().getId();

            // Fetch most of all the data and metadata with the ultra TIDL object
            TemplateItemDataList tidl = responseAggregator.prepareTemplateItemDataStructure(evaluationId, reportViewParams.groupIds);

            List<EvalTemplateItem> allTemplateItems = tidl.getAllTemplateItems();

            if (! allTemplateItems.isEmpty()) {

                if (VIEWMODE_ALLESSAYS.equals(reportViewParams.viewmode)) {
                    currentViewMode = VIEWMODE_ALLESSAYS;
                }

                // Render Reporting links such as xls, pdf output, and options for viewing essays and stuff
                renderReportingOptionsTopLinks(tofill, evaluation, templateId, reportViewParams);

                // Evaluation Info
                UIOutput.make(tofill, "evaluationTitle", evaluation.getTitle());

                // The Groups we are viewing
                UIMessage.make(tofill, "selectedGroups", "viewreport.viewinggroups", 
                        new String[] { responseAggregator.getCommaSeparatedGroupNames(reportViewParams.groupIds) });

                int renderedItemCount = 0;
                // loop through the TIGs and handle each associated category
                for (TemplateItemGroup tig : tidl.getTemplateItemGroups()) {
                    // check if we should render any of these items at all
                    Boolean instructorViewAllResults = evaluation.getInstructorViewAllResults() == null ? Boolean.FALSE : (boolean) evaluation.getInstructorViewAllResults();
                    
                    if (! renderAnyBasedOnOptions(tig.getTemplateItems(), commonLogic.getEvalUserById( tig.associateId ), evaluation.getOwner(), instructorViewAllResults)) {
                        continue;
                    }
                    UIBranchContainer categorySectionBranch = UIBranchContainer.make(tofill, "categorySection:");
                    // handle printing the category header
                    if (EvalConstants.ITEM_CATEGORY_COURSE.equals(tig.associateType) && !((Boolean)evalSettings.get(EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY))) {
                        UIMessage.make(categorySectionBranch, "categoryHeader", "viewreport.itemlist.course");
                    } else if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(tig.associateType)) {
                        EvalUser user = commonLogic.getEvalUserById( tig.associateId );
                        UIMessage.make(categorySectionBranch, "categoryHeader", 
                                "viewreport.itemlist.instructor", new Object[] { user.displayName });
                    } else if (EvalConstants.ITEM_CATEGORY_ASSISTANT.equals(tig.associateType)) {
                        EvalUser user = commonLogic.getEvalUserById( tig.associateId );
                        UIMessage.make(categorySectionBranch, "categoryHeader", 
                                "viewreport.itemlist.ta", new Object[] { user.displayName });
                    }

                    // loop through the hierarchy node groups
                    for (HierarchyNodeGroup hng : tig.hierarchyNodeGroups) {
                        // check if we should render any of these items at all
                        if (! renderAnyBasedOnOptions(hng.templateItems)) {
                            continue;
                        }
                        // render a node title
                        if (hng.node != null) {
                            // Showing the section title is system configurable via the administrate view
                            Boolean showHierSectionTitle = (Boolean) evalSettings.get(EvalSettings.DISPLAY_HIERARCHY_HEADERS);
                            if (showHierSectionTitle) {
                                UIBranchContainer nodeTitleBranch = UIBranchContainer.make(categorySectionBranch, "itemrow:nodeSection");
                                UIOutput.make(nodeTitleBranch, "nodeTitle", hng.node.title);
                            }
                        }

                        List<DataTemplateItem> dtis = hng.getDataTemplateItems(true); // include block children
                        for (int i = 0; i < dtis.size(); i++) {
                            DataTemplateItem dti = dtis.get(i);
                            if (renderBasedOnOptions(dti.templateItem)) {
                                UIBranchContainer nodeItemsBranch = UIBranchContainer.make(categorySectionBranch, "itemrow:templateItem");
                                // add a style to each rendered item
                                String styleClass = renderedItemCount % 2 != 0 ? "reportItemOddLine" : "reportItemEvenLine"; // must match the existing CSS classes
                                nodeItemsBranch.decorate( new UIStyleDecorator(styleClass) );
                                // render the item
                                renderTemplateItemResults(nodeItemsBranch, dti, reportViewParams);
                                renderedItemCount++;
                            }
                        }
                    }

                }

                // show the comments/textresponses stuff
                String allComments = "";
                if (totalCommentsCount > 0) {
                    allComments = "allComments";
                }
                String allTextResponses = "";
                if (totalTextResponsesCount > 0) {
                    allTextResponses = "allTextResponses";
                }            
                // this fills in the javascript init call ()
                UIInitBlock.make(tofill, "initJavascript", "EvalSystem.initEvalReportView", 
                        new Object[] {allComments, allTextResponses} );

            }
        }

    }


    /**
     * Handles the rendering of the response answer results for a specific templateItem
     * 
     * @param tofill the parent container to render in
     * @param dti the wrapper for this templateItem
     */
    private void renderTemplateItemResults(UIBranchContainer tofill, DataTemplateItem dti, ReportParameters reportViewParams) {
        EvalTemplateItem templateItem = dti.templateItem;
        String templateItemType = TemplateItemUtils.getTemplateItemType(templateItem);

        // get the answers associated with this data template item
        List<EvalAnswer> itemAnswers = dti.getAnswers();

        if ( EvalConstants.ITEM_TYPE_SCALED.equals(templateItemType) 
                || EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(templateItemType) 
                || EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(templateItemType)
                || EvalConstants.ITEM_TYPE_BLOCK_CHILD.equals(templateItemType) ) {
            // scales/choices type item
            displayNumber++;

            UIBranchContainer scaled = UIBranchContainer.make(tofill, "scaledSurvey:");

            int responsesCount = itemAnswers.size();

            UIOutput.make(scaled, "itemNum", displayNumber+"");
            UIVerbatim.make(scaled, "itemText", templateItem.getItem().getItemText());
            UIMessage.make(scaled, "responsesCount", "viewreport.responses.count", new Object[] {responsesCount});

            int naCount = 0;
            if (! VIEWMODE_ALLESSAYS.equals(currentViewMode)) {
                // if we are in essay view mode then do not show the scale or the answers counts
                EvalScale scale = templateItem.getItem().getScale();
                String[] scaleOptions = scale.getOptions();
                String scaleLabels[] = new String[scaleOptions.length];

                int[] choicesCounts = TemplateItemDataList.getAnswerChoicesCounts(templateItemType, scaleOptions.length, itemAnswers);

                for (int x = 0; x < scaleLabels.length; x++) {
                    UIBranchContainer choicesBranch = UIBranchContainer.make(scaled, "choices:");
                    UIOutput.make(choicesBranch, "choiceText", scaleOptions[x]);
                    UIMessage.make(choicesBranch, "choiceCount", "viewreport.answers.percentage", 
                            new String[] { choicesCounts[x]+"", makePercentage(choicesCounts[x], responsesCount) });
                }

                if (templateItem.getUsesNA() != null && templateItem.getUsesNA()) {
                    naCount = choicesCounts[choicesCounts.length-1];
                    UIBranchContainer choicesBranch = UIBranchContainer.make(scaled, "choices:");
                    UIMessage.make(choicesBranch, "choiceText", "reporting.notapplicable.longlabel");
                    UIMessage.make(choicesBranch, "choiceCount", "viewreport.answers.percentage", 
                            new String[] { naCount+"", makePercentage(naCount, responsesCount) });
                }

                // http://www.caret.cam.ac.uk/jira/browse/CTL-1504
                AnswersMean answersMean = RenderingUtils.calculateMean(choicesCounts);
                Object[] params = new Object[] {answersMean.getAnswersCount()+"", answersMean.getMeanText()};
                UIMessage.make(scaled, "statistics", "viewreport.answers.mean", params);
            }

            if (dti.usesComments()) {
                // render the comments
                UIBranchContainer showCommentsBranch = UIBranchContainer.make(tofill, "showComments:");
                List<String> comments = dti.getComments();
                int commentCount = 0;
                for (String comment : comments) {
                    UIBranchContainer commentsBranch = UIBranchContainer.make(showCommentsBranch, "comments:");
                    if (commentCount % 2 != 0) {
                        commentsBranch.decorate(new UIStyleDecorator("itemsListOddLine")); // must match the existing CSS class
                    }
                    UIOutput.make(commentsBranch, "commentNum", (commentCount + 1)+"");
                    UIOutput.make(commentsBranch, "itemComment", comment);
                    commentCount++;
                }
                if (comments.size() <= 0) {
                    UIMessage.make(showCommentsBranch, "noComments", "viewreport.no.comments");
                }
                totalCommentsCount += comments.size();
            }            

        } else if (EvalConstants.ITEM_TYPE_TEXT.equals(templateItemType)) {
            // text/essay type items
            displayNumber++;
            UIBranchContainer essay = UIBranchContainer.make(tofill, "essayType:");
            UIOutput.make(essay, "itemNum", displayNumber + "");
            UIVerbatim.make(essay, "itemText", templateItem.getItem().getItemText());

            //         ReportParameters params = new ReportParameters(VIEW_ID, reportViewParams.evaluationId);
            //         params.viewmode = VIEWMODE_SELECTITEMS;
            //         params.items = new Long[] {templateItem.getId()};
            //         params.groupIds = reportViewParams.groupIds;
            //         UIInternalLink.make(essay, "essayResponse", UIMessage.make("viewreport.view.viewresponses"), params);

            // render the responses
            UIBranchContainer showResponsesBranch = UIBranchContainer.make(tofill, "showResponses:");
            int responsesCount = 0;
            int naCount = 0;
            for (EvalAnswer answer : itemAnswers) {
                if (answer.NA) {
                    naCount++;
                } else if (! EvalUtils.isBlank(answer.getText())) {
                    UIBranchContainer responsesBranch = UIBranchContainer.make(showResponsesBranch, "responses:");
                    if (responsesCount % 2 != 0) {
                        responsesBranch.decorate(new UIStyleDecorator("itemsListOddLine")); // must match the existing CSS class
                    }
                    UIOutput.make(responsesBranch, "responseNum", (responsesCount + 1)+"");
                    UIOutput.make(responsesBranch, "itemResponse", answer.getText());
                    responsesCount++;
                }
            }
            if (responsesCount == 0) {
                UIMessage.make(showResponsesBranch, "noResponse", "viewreport.no.responses");
            }
            totalTextResponsesCount += responsesCount;

            UIMessage.make(essay, "responsesCount", "viewreport.responses.count", new Object[] {responsesCount + naCount});

            if (templateItem.getUsesNA() != null && templateItem.getUsesNA()) {
                // show the number of NA responses
                UIBranchContainer naBranch = UIBranchContainer.make(essay, "showNA:");
                UIOutput.make(naBranch, "naCount", naCount+"");
            }

        } else if (EvalConstants.ITEM_TYPE_HEADER.equals(templateItemType)
                || EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(templateItemType)) {
            // header/textual bits
            UIBranchContainer textual = UIBranchContainer.make(tofill, "textualItem:");
            UIVerbatim.make(textual, "itemText", templateItem.getItem().getItemText());

        } else {
            log.warn("Skipped invalid item type ("+templateItemType+"): TI: " + templateItem.getId() );
        }
    }


    /**
     * @param value
     * @param total
     * @return a string version of the percentage number
     */
    private String makePercentage(int value, int total) {
        return (total > 0) ? String.format("%.0f", (float) (value * 100.0 / total)) : "0"; 
    }


    /**
     * Checks if any of the items in a set should be rendered based
     * on the passed in options for this view
     * 
     * @param templateItems a list of {@link EvalTemplateItem}
     * @return true if any of the items in this list should be rendered, false otherwise
     */
    private boolean renderAnyBasedOnOptions(List<EvalTemplateItem> templateItems) {
        boolean renderAny = false;
        for (EvalTemplateItem templateItem : templateItems) {
            if (renderBasedOnOptions(templateItem)) {
                renderAny = true;
                break;
            }
        }
        return renderAny;
    }
    
    private boolean renderAnyBasedOnOptions(List<EvalTemplateItem> templateItems, EvalUser associatedUser, String owner, Boolean instructorViewAllResults) {
        //TODO check for course item types.
        String currentUserId = commonLogic.getCurrentUserId();
        log.debug("ViewAll: "+instructorViewAllResults+", Current user: "+currentUserId+", Item user: "+associatedUser.userId+", Owner: "+owner);
                
        if (!"invalid:null".equals(associatedUser.userId)   // if this is a instructor question
          && !commonLogic.isUserAdmin(currentUserId)   // and currentUser is not admin
          && !currentUserId.equals(owner)   // and currentUser is not the creator of this evaluation
          && !instructorViewAllResults //  and the evaluation has been so configured...
          && !currentUserId.equals(associatedUser.userId)) {
            // don't show currentUser results for other instructors
            return false;
        } else {
            return renderAnyBasedOnOptions(templateItems);
        }
        
    }

    /**
     * Should we render this item based off the passed in parameters?
     * (ie. Should we only render certain template items)
     */
    private boolean renderBasedOnOptions(EvalTemplateItem templateItem) {
        boolean togo = false;
        if (VIEWMODE_SELECTITEMS.equals(currentViewMode)) {
            // If this item isn't in the list of items to view, don't render it.
            if (ArrayUtils.contains(itemsToView, templateItem.getId())) {
                togo = true;
            }
        }
        else if (VIEWMODE_ALLESSAYS.equals(currentViewMode)) {
            // show all items with comments or textual responses
            String templateItemType = TemplateItemUtils.getTemplateItemType(templateItem);
            if (EvalConstants.ITEM_TYPE_TEXT.equals(templateItemType)) {
                togo = true;
            } else if (templateItem.getUsesComment()) {
                togo = true;
            }
        }
        else if (VIEWMODE_REGULAR.equals(currentViewMode)) {
            togo = true;
        }
        else {
            // default to true
            togo = true;
        }
        return togo;
    }


    /**
     * Renders the reporting specific links like XLS and PDF download, etc.
     * 
     * @param tofill
     * @param evaluation
     * @param templateId
     * @param reportViewParams
     */
    private void renderReportingOptionsTopLinks(UIContainer tofill, EvalEvaluation evaluation, Long templateId, ReportParameters reportViewParams) {
        // Render the link to show "All Essays" or "All Questions"
        ReportParameters viewEssaysParams = (ReportParameters) reportViewParams.copyBase();
        if (VIEWMODE_ALLESSAYS.equals(currentViewMode)) {
            viewEssaysParams.viewmode = VIEWMODE_REGULAR; 
            UIInternalLink.make(tofill, "viewItemsFilterLink", UIMessage.make("viewreport.view.all"), 
                    viewEssaysParams);
        }
        else {
            viewEssaysParams.viewmode = VIEWMODE_ALLESSAYS;
            UIInternalLink.make(tofill, "viewItemsFilterLink",  UIMessage.make("viewreport.view.written"), 
                    viewEssaysParams);
        }

        /*
         * The Downloaded names should have a max length indicated by the
         * constant.  We run them through the validator to make sure they
         * are ok for display on OS filesystems. At the moment we are using
         * just the Evaluation title for the name of the downloaded file.
         */
        String evaltitle = evaluation.getTitle();
        if (evaltitle.length() > EvalToolConstants.EVAL_REPORTING_MAX_NAME_LENGTH) {
            evaltitle = evaltitle.substring(0, EvalToolConstants.EVAL_REPORTING_MAX_NAME_LENGTH);
            evaltitle = evaltitle.trim().replaceAll(" ", "_");
        }

        // FIXME don't use sakai classes directly (plus what the crap does this do anyway? -AZ)
        evaltitle = Validator.escapeZipEntry(evaltitle);

        Boolean allowCSVExport = (Boolean) evalSettings.get(EvalSettings.ENABLE_CSV_REPORT_EXPORT);
        if (allowCSVExport != null && allowCSVExport == true) {
            UIInternalLink.make(tofill, "csvResultsReport", UIMessage.make("viewreport.view.csv"), new DownloadReportViewParams(
                    "csvResultsReport", templateId, reportViewParams.evaluationId, reportViewParams.groupIds, evaltitle+".csv"));
        }

        Boolean allowXLSExport = (Boolean) evalSettings.get(EvalSettings.ENABLE_XLS_REPORT_EXPORT); 
        if (allowXLSExport != null && allowXLSExport == true) {
            UIInternalLink.make(tofill, "xlsResultsReport", UIMessage.make("viewreport.view.xls"), new DownloadReportViewParams(
                    "xlsResultsReport", templateId, reportViewParams.evaluationId, reportViewParams.groupIds, evaltitle+".xls"));
        }

        Boolean allowPDFExport = (Boolean) evalSettings.get(EvalSettings.ENABLE_PDF_REPORT_EXPORT);
        if (allowPDFExport != null && allowPDFExport == true) {
            UIInternalLink.make(tofill, "pdfResultsReport", UIMessage.make("viewreport.view.pdf"), new DownloadReportViewParams(
                    "pdfResultsReport", templateId, reportViewParams.evaluationId, reportViewParams.groupIds, evaltitle+".pdf"));
        }

        // FIXME should this be protected with an option?
        Boolean allowListOfEvalTakers = (Boolean) evalSettings.get(EvalSettings.ENABLE_LIST_OF_TAKERS_EXPORT);
        if (allowListOfEvalTakers != null && allowListOfEvalTakers == true) {
            UIInternalLink.make(tofill, "exportListOfEvaluationTakers", UIMessage.make("viewreport.view.listofevaluationtakers"), new DownloadReportViewParams(
                    "csvTakersReport", templateId, reportViewParams.evaluationId, reportViewParams.groupIds, evaltitle+"-takers.csv"));
        }
    }


    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new ReportParameters();
    }

}
