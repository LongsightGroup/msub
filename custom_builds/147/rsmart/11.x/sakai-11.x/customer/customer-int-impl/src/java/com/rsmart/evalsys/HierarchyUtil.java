package com.rsmart.evalsys;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.hierarchy.HierarchyService;
import org.sakaiproject.hierarchy.model.HierarchyNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 9/12/11
 * Time: 9:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class HierarchyUtil {
    private static final Log log = LogFactory.getLog(HierarchyUtil.class);

    private String rootNodeName = "evaluationHierarchyId";
    private String rootNodeTitle = "Root";
    private HierarchyNode rootNode;
    protected HierarchyService hierarchyService;
    private HashMap hierarchyNodes = new HashMap();
    public String permBeEvaluated = "provider.be.evaluated";
    public String permTakeEvaluation = "provider.take.evaluation";
    public String permAssignEvaluation = "provider.assign.eval";
    public String permAssistantRole = "section.role.ta";

    public Map getHierarchyNodes(){
        return hierarchyNodes;
    }

    public Map reloadHierarchyNodes() {
        Set<HierarchyNode> nodes = hierarchyService.getChildNodes(rootNode.id, false);
        for (HierarchyNode node : nodes) {
            hierarchyNodes.put(node.title, node);
        }
        return hierarchyNodes;
    }

    public HierarchyNode getNodeByEid(String eid) {
         return (HierarchyNode)hierarchyNodes.get(eid);
     }

    public void init()
    {
    	log.info("init");
        rootNode = hierarchyService.getRootNode(rootNodeName);

        if (rootNode == null) {
            rootNode = hierarchyService.createHierarchy(rootNodeName);
            hierarchyService.saveNodeMetaData(rootNode.id, rootNodeTitle, rootNodeTitle, null);
        }

        reloadHierarchyNodes();

    }

    public void setRootNodeName(String rootNodeName) {
        this.rootNodeName = rootNodeName;
    }

    public void setRootNode(HierarchyNode rootNode) {
        this.rootNode = rootNode;
    }

    public void setHierarchyService(HierarchyService hierarchyService) {
        this.hierarchyService = hierarchyService;
    }

    public String getRootNodeName() {
        return rootNodeName;
    }

    public HierarchyNode getRootNode() {
        return rootNode;
    }

    public String getPermBeEvaluated() {
        return permBeEvaluated;
    }

    public void setPermBeEvaluated(String permBeEvaluated) {
        this.permBeEvaluated = permBeEvaluated;
    }

    public String getPermTakeEvaluation() {
        return permTakeEvaluation;
    }

    public void setPermTakeEvaluation(String permTakeEvaluation) {
        this.permTakeEvaluation = permTakeEvaluation;
    }

    public String getPermAssignEvaluation() {
        return permAssignEvaluation;
    }

    public void setPermAssignEvaluation(String permAssignEvaluation) {
        this.permAssignEvaluation = permAssignEvaluation;
    }

    public String getPermAssistantRole() {
        return permAssistantRole;
    }

    public void setPermAssistantRole(String permAssistantRole) {
        this.permAssistantRole = permAssistantRole;
    }

    public void setRootNodeTitle(String rootNodeTitle) {
        this.rootNodeTitle = rootNodeTitle;
    }
}
