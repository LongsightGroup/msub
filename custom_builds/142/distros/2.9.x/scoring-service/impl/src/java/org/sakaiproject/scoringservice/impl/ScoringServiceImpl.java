package org.sakaiproject.scoringservice.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.scoringservice.api.ScoringAgent;
import org.sakaiproject.scoringservice.api.ScoringComponent;
import org.sakaiproject.scoringservice.api.ScoringService;
import org.springframework.core.OrderComparator;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 5/21/13
 * Time: 1:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScoringServiceImpl implements ScoringService {
    private Map<String, ScoringAgent> scoringAgentMap = new HashMap();
    private ArrayList sortedScoringAgents = new ArrayList();
    private ScoringAgent defaultScoringAgent;

    private static final Log log = LogFactory.getLog(ScoringServiceImpl.class);

    public void init(){
    }

    public void register(ScoringAgent agent, boolean isDefault) {
        if (agent == null) {
            throw new RuntimeException("can't register a null agent");
        }
        if (agent.getAgentId() == null) {
            throw new RuntimeException("the scoring agentId is null");
        }
        scoringAgentMap.put(agent.getAgentId(), agent);
        sortedScoringAgents = new ArrayList(scoringAgentMap.values());
        Collections.sort(sortedScoringAgents, new OrderComparator());

        if (isDefault) {
            defaultScoringAgent = agent;
        }
    }

    @Override
    public void register(ScoringAgent agent) {
        if (agent == null) {
            throw new RuntimeException("can't register a null agent");
        }
        if (agent.getAgentId() == null) {
            throw new RuntimeException("the scoring agentId is null");
        }
        scoringAgentMap.put(agent.getAgentId(), agent);
        sortedScoringAgents = new ArrayList(scoringAgentMap.values());
        Collections.sort(sortedScoringAgents, new OrderComparator());
    }

    @Override
    public String getScoreLaunchUrl(String agentId, String gradebookUid, String gradebookItemId, String studentId) {
        ScoringAgent scoringAgent = getAgentById(agentId);
        return scoringAgent.getScoreLaunchUrl(gradebookUid, gradebookItemId, studentId);
    }

    @Override
    public String getScoringComponentLaunchUrl(String agentId, String gradebookUid, String gradebookItemId) {
        ScoringAgent scoringAgent = getAgentById(agentId);
        return scoringAgent.getScoringComponentLaunchUrl(gradebookUid, gradebookItemId);
    }

    @Override
    public String getScore(String agentId, String gradebookUid, String gradebookItemId, String studentId) {
        ScoringAgent scoringAgent = getAgentById(agentId);
        return scoringAgent.getScore(gradebookUid, gradebookItemId, studentId);

    }

    @Override
    public ScoringComponent getScoringComponent(String agentId, String gradebookUid, String gradebookItemId) {
        ScoringAgent scoringAgent = getAgentById(agentId);
        if (!scoringAgent.hasScoringComponent())  {
            return null;
        }
        return scoringAgent.getScoringComponent(gradebookUid, gradebookItemId);
    }

    public ScoringAgent getAgentById(String agentId) {
        ScoringAgent scoringAgent = scoringAgentMap.get(agentId);
        if (scoringAgent == null) {
            throw new RuntimeException("can't locate an agent with a null agentId");
        }
        return scoringAgent;
    }

    @Override
    public boolean hasScoringComponent(String agentId, String gradebookUid, String gradebookItemId) {
        ScoringAgent scoringAgent = getAgentById(agentId);
        return scoringAgent.hasScoringComponent();
    }

    public boolean isScoringAgentEnabled(String agentId, String gradebookUid, String gradebookItemId){
        ScoringAgent scoringAgent = getAgentById(agentId);
        return scoringAgent.isEnabled( gradebookUid,  gradebookItemId);
    }

    public ScoringAgent getDefaultScoringAgent() {
        return defaultScoringAgent;
    }

    @Override
    public List<ScoringAgent> getScoringAgents() {
        return sortedScoringAgents;
    }
}
