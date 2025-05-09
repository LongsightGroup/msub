/*Copyright (C) Reazon Systems, Inc.  All rights reserved.*/
/*
 * ERROR CODE DEFINITION
 * ---------------------
 * RZN9834953
 * 		Cannot authenticate with iRubric
 * RZN9832413
 * 		Invalid returned value for getallgrade purpose
 * RZN9834745
 * 		Invalid returned xToken from iRubric
 * RZN8345123
 * 		Error viewing the iRubric by student role
 * RZN9862813
 * 		Error when attaching an iRubric
 * RZN3534953
 * 		Invalid returned value from iRubricRZN3534953
 * RZN9831153
 * 		Empty Purpose parameter value
 */
package com.reazon.tool.irubricpatch.ui;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.portal.util.URLUtils;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.LetterGradePercentMapping;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.Authz;
import org.sakaiproject.tool.gradebook.facades.ContextManagement;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * A servlet to integrate with iRubric system
 * 
 * @author CD
 * 
 */
public class IRubricServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1720367954299139347L;

	private static Log LOG = LogFactory.getLog(IRubricServlet.class);

	private static final String XTOKEN = "xtoken";
	private static final String CMD = "p";
	private static final String CMD_VIEW = "v";
	private static final String CMD_ATTACH = "a";
	private static final String CMD_GRADE = "g";
	private static final String CMD_GRADE_ALL = "ga";
	private static final String CMD_GET_ATTACHED_RUBRIC = "getattachedrubric";
	private static final String CMD_GET_GRADE = "gg";
	private static final String CMD_GET_GRADES_BY_GRADEBOOK = "gag";
	private static final String CMD_GET_GRADES_BY_ROS = "gas";
	private static final String FIELD = "fieldToUpdate";
	private static final String ERR_IRUBRIC_UNAVAILABLE = "Sorry, cannot contact iRubric at this time. Please try again in a few minutes. <BR><BR>Should the problems persists, contact your system administrator.";

	// iRubric bean class name
	private static final String IRUBRIC_BEAN = "com.reazon.tool.irubricpatch.ui.IrubricBean";
	private IrubricBean iRubricBean;
	private Authz authzService;
	private Authn authnService;
	private ContextManagement contextMgm;

	/**
	 * Authenticate with IRubric system
	 * 
	 * @param postData
	 * @param printWriter
	 *            TODO
	 * @param cmd
	 *            TODO
	 * @return TODO
	 */
	private boolean doiRubricAuthentication(String postData,
			PrintWriter printWriter, String cmd) {

		boolean isInfoEnabled = LOG.isInfoEnabled();
		boolean isAuthenticated = false;

		HttpURLConnection connection = null;
		DataOutputStream dout = null;

		if (isInfoEnabled) {
			LOG.info("Init request URL: " + iRubricBean.getInitReqURL());
		}

		try {
			if (isInfoEnabled) {
				LOG.info("Start connecting to iRubric system");
			}
			// connect to iRubric server
			connection = Helper.createHttpURLConnection(iRubricBean
					.getInitReqURL(), iRubricBean.getTimeout());
		} catch (IOException ex) {
			LOG.error("Cannot request to init to iRubric system.", ex);
			return false;
		}

		try {
			if (isInfoEnabled) {
				LOG.info("Posting data to iRubric system");
			}

			dout = new DataOutputStream(connection.getOutputStream());
			dout.writeBytes(postData);
			dout.flush();
			dout.close();

			if (isInfoEnabled) {
				LOG.info("Obtain return data from iRubric system");
			}
			// obtain the security token from iRubric server
			String result = Helper.getResponseData(connection);
			iRubricBean.setXtoken(result);

			connection.disconnect();
			isAuthenticated = true;
		} catch (IOException e) {
			LOG.error(ERR_IRUBRIC_UNAVAILABLE, e);
			iRubricBean.renderErrorMessageByCmd(printWriter, cmd,
					ERR_IRUBRIC_UNAVAILABLE);
		} catch (Exception e) {
			LOG.error("Cannot authenticate with iRubric.", e);
			iRubricBean.dumpErrorMessage(printWriter, cmd, "RZN9834953");
		} finally {
			dout = null;
			connection = null;
		}

		return isAuthenticated;
	}

	/*
	 * Initialize for servlet (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		WebApplicationContext context = WebApplicationContextUtils
				.getWebApplicationContext(this.getServletContext());
		iRubricBean = (IrubricBean) context.getBean(IRUBRIC_BEAN);

		authzService = (Authz) context
				.getBean("org_sakaiproject_tool_gradebook_facades_Authz");
		authnService = (Authn) context
				.getBean("org_sakaiproject_tool_gradebook_facades_Authn");
		contextMgm = (ContextManagement) context
				.getBean("org_sakaiproject_tool_gradebook_facades_ContextManagement");
	}

	/**
	 * doGet method
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * doPost method
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		PrintWriter writer = response.getWriter();

		String cmd = "";
		try {
			cmd = request.getParameter(CMD).toLowerCase();
		} catch (NullPointerException ex) {
			if (LOG.isErrorEnabled()) {
				LOG.error("Error while getting purpose name.", ex);
			}
			iRubricBean
					.renderErrorMessageByCmd(writer, cmd,
							"Error while getting purpose name. Please contact your system administrator.");
		}

		if (cmd.equals("")) {
			return;
		}

		if (!cmd.equals(CMD_GET_ATTACHED_RUBRIC)) {
			authnService.setAuthnContext(request);
			String gradebookUid = contextMgm.getGradebookUid(request);
			if (!isAuthorized(cmd, gradebookUid)) {
				StringBuilder path = new StringBuilder(request.getContextPath());
				path.append("/noRole.jsp");
				response.sendRedirect(path.toString());
				return;
			}
		}

		// get the PURPOSE name
		if (cmd.equals(CMD_GET_ATTACHED_RUBRIC)) {
			String xToken = request.getParameter(XTOKEN);
			String responseData = getData(iRubricBean, xToken, null, cmd);

			LOG.info("The attached rubric data: " + responseData);

			if (responseData != null) {
				switch (responseData.charAt(0)) {
				case 'A':
					// get the attached rubric data
					String sTemp = responseData.trim().substring(1);
					String[] datas = sTemp.split("\\|");
					if (datas.length > 0) {
						Long gradebookItemId = Long.parseLong(datas[0]);
						String iRubricId = datas[1];
						String iRubricTitle = datas[2];
						try {
							iRubricBean.updateAssignmetByRubric(
									gradebookItemId, iRubricId, iRubricTitle);
							if (LOG.isInfoEnabled())
								LOG
										.info("Update gradebook item is successful.");
						} catch (Exception e) {
							LOG
									.error(
											"Error while updating gradebook item.",
											e);
						}
					}
					break;
				default:
					LOG.error("The reponse data is in invalid format.");
					break;
				}
			} else {
				LOG.error("Error while getting data from iRubric Server.");
			}
		} else {

			String dataPacket = null;
			// build data packet to send to iRubric system

			try {
				dataPacket = iRubricBean.buildPostData(request, cmd);
				LOG.info("Data trasnfering - " + dataPacket);
			} catch (Exception ex) {
				LOG.error("Constructing data packet", ex);
				iRubricBean
						.renderErrorMessageByCmd(
								writer,
								cmd,
								"Error while constructing data packet. Please contact your system administrator.");
			}

			// authenticate with iRubric system
			if (doiRubricAuthentication(dataPacket, writer, cmd)) {
				// authenticating completed
				String xToken = iRubricBean.getXtoken();

				switch (xToken.charAt(0)) {
				case 'T':
					String gradebookUid = contextMgm.getGradebookUid(request);
					Gradebook currentGradebook = iRubricBean.getGradebookManager().getGradebook(gradebookUid);
					if (cmd.equals(CMD_ATTACH) || cmd.equals(CMD_VIEW)
							|| cmd.equals(CMD_GRADE)) {
						String redirecLink = URLUtils.addParameter(iRubricBean
								.getIrubricRedirectUrl(), XTOKEN, xToken);
						response.sendRedirect(redirecLink);

						LOG.info("iRubric - " + redirecLink);
						
					} else if (cmd.equals(CMD_GRADE_ALL)) {
						String redirecLink = URLUtils.addParameter(iRubricBean
								.getIrubricRedirectUrl(), XTOKEN, xToken);
						response.setContentType("text/plain");
						writer.print(redirecLink);		
						
					} else if (cmd.equals(CMD_GET_GRADE)) {

						String updatedControl = request.getParameter(FIELD);

						String sGrade = getData(iRubricBean, xToken, writer,
								cmd);
						LOG.info("grade array: - " + sGrade);
						processOneGrade(iRubricBean, writer, sGrade,
								updatedControl, currentGradebook);

					} else if (cmd.equals(CMD_GET_GRADES_BY_GRADEBOOK)
							|| cmd.equals(CMD_GET_GRADES_BY_ROS)) {

						String sGrade = getData(iRubricBean, xToken, writer,
								cmd);

						LOG.info("iRubric - Parsing scores ...");
						
						
						processAllGrades(iRubricBean, writer, sGrade, cmd, currentGradebook);
					}
					break;
				case 'E':
					iRubricBean.dumpErrorMessage(writer, cmd, "RZN9834745");
					LOG
							.debug("Error generating security token on iRubric system.");
					break;
				default:

					break;
				}
			}

			writer.close();
		}
	}

	/**
	 * @param iRubricBean
	 * @param secToken
	 * @param printWriter
	 * @param cmd
	 *            TODO
	 * @return
	 */
	private String getData(IrubricBean iRubricBean, String secToken,
			PrintWriter printWriter, String cmd) {

		String dataPacket = null;

		HttpURLConnection connection = null;
		try {
			// get grade from iRubric server
			connection = Helper.createHttpURLGetConnection(URLUtils
					.addParameter(iRubricBean.getIrubricRedirectUrl(), XTOKEN,
							secToken), iRubricBean.getTimeout());

			dataPacket = Helper.getResponseData(connection);

			connection.disconnect();
		} catch (IOException ex) {
			dataPacket = null;
			LOG.error(ex);
			if (printWriter != null) {
				iRubricBean
						.renderErrorMessageByCmd(
								printWriter,
								cmd,
								"iRubric Server is not Available.  Please let the system administrator know should the problem persist.");
			}
		} catch (Exception ex) {
			dataPacket = null;
			LOG.error(ex);
			if (printWriter != null) {
				iRubricBean
						.renderErrorMessageByCmd(
								printWriter,
								cmd,
								"The reponse data is in invalid format.  Please let the system administrator know should the problem persist.");
			}
		} finally {
			// dispose the connection
			if (connection != null) {
				connection = null;
			}
		}

		return dataPacket;
	}

	/**
	 * @param iRubricBean
	 * @param printWriter
	 * @param sGrade
	 * @param fieldName
	 */
	private void processOneGrade(IrubricBean iRubricBean,
			PrintWriter printWriter, String sGrade, String fieldName, Gradebook gradebook) {
		if (sGrade != null) {
			switch (sGrade.charAt(0)) {
			case 'R':
				
				String newScore = sGrade.substring(1).trim();
				LOG.info("New Score from FC:  " + newScore);
				if (gradebook != null){ 
					int gradeEntry = gradebook.getGrade_type();
					if (gradeEntry == GradebookService.GRADE_TYPE_LETTER){
						LetterGradePercentMapping lgpm = iRubricBean.getGradebookManager()
																	.getLetterGradePercentMapping(gradebook);
						Double pointsPossible = iRubricBean.getPointsPossible();
						newScore = iRubricBean.getGradebookManager()
												.convertPointToLetterGrade(lgpm, pointsPossible, Double.parseDouble(newScore));
						LOG.info("New Score from FC is converted  " + newScore);
					}
				}
				// call updateScoreTextbox JavaScript to update score field on
				// UI
				printWriter
						.print("<html><body onload=\"window.parent.updateScoreTextbox('"
								+ fieldName
								+ "','"
								+ newScore
								+ "');\"></body></html>");
				break;
			case 'N':
				printWriter
						.print("<html><body onload=\"window.parent.ungradedConfirm('"
								+ fieldName + "');\"></body></html>");
				break;
			case 'E':
				printWriter
						.print("<html><body onload=\"window.parent.unAttachediRubric();\"></body></html>");
				break;
			default:
				printWriter
						.print(iRubricBean
								.renderJSErrorBox("Invalid returned value from iRubric system.  Please let the system administrator know should the problem persist."));
				LOG.debug("Returned value - " + sGrade);
				break;
			}
		}
	}

	/**
	 * @param iRubricBean
	 * @param printWriter
	 * @param sGrade
	 * @param cmd
	 */
	private void processAllGrades(IrubricBean iRubricBean,
			PrintWriter printWriter, String sGrade, String cmd, Gradebook gradebook) {
		if (sGrade != null) {
			switch (sGrade.charAt(0)) {
			case 'A':
				// All scores were transferred correctly
				String strScoreStream = sGrade.substring(1).trim();
				LOG.info("score Stream: " + strScoreStream);
				if (gradebook != null){ 
					int gradeEntry = gradebook.getGrade_type();
					if (gradeEntry == GradebookService.GRADE_TYPE_LETTER){
						LetterGradePercentMapping lgpm = iRubricBean.getGradebookManager()
																	.getLetterGradePercentMapping(gradebook);
						StringBuilder scoresBuilder = new StringBuilder();
						String[] records = strScoreStream.split("\\|");
						int length = records.length;
						if (length > 0) {
							Double pointsPossible = iRubricBean.getPointsPossible();
							for(int i = 0; i< length; i ++){
								Double score = Double.parseDouble(records[i].split("\\,")[1].trim());
								LOG.info("before converted score " + score);
								
								// for getallgrade by student, get pointsPossible for iRubricServer response
								if (cmd.equals(CMD_GET_GRADES_BY_ROS)) {
									pointsPossible = Double.parseDouble(records[i].split("\\,")[2].trim());
									//pointsPossible = new Double(100);
								}
								String letterScore = iRubricBean.getGradebookManager()
																.convertPointToLetterGrade(lgpm, pointsPossible,score);
								LOG.info("after converted score " + letterScore);
								String record = records[i].split("\\,")[0].trim() + "," + letterScore;
								if(i<length -1)
									scoresBuilder.append(record).append("|");
								else
									scoresBuilder.append(record);
							}
							strScoreStream = scoresBuilder.toString();
							LOG.info("score Stream is converted: " + strScoreStream);
						}
					}
				}
				printWriter
						.print("<html><body onload=\"window.parent.getAllScores('"
								+ strScoreStream + "');\"></body></html>");
				break;

			case 'N':
				// No student has been graded yet.
				printWriter
						.print("<html><body onload=\"window.parent.alertNoScore();\"></body></html>");
				break;
			case 'E':
				if (cmd.equals(CMD_GET_GRADES_BY_GRADEBOOK)) {
					printWriter
							.print("<html><body onload=\"window.parent.unAttachediRubric();\"></body></html>");
					break;
				}
			default:
				printWriter
						.print(iRubricBean
								.renderJSErrorBox("Invalid returned value from iRubric system.  Please let the system administrator know should the problem persist."));
				LOG.info("Returned value: " + sGrade);
				break;
			}
		}
	}

	/**
	 * @param cmd
	 * @param gradebookId
	 * @return
	 */
	private boolean isAuthorized(String cmd, String gradebookUid) {

		if (gradebookUid != null) {
			if (cmd.equals(CMD_GET_ATTACHED_RUBRIC)) {
				return true;
			}

			if (cmd.equals(CMD_VIEW)) {
				if (authzService.isUserAbleToViewOwnGrades(gradebookUid)
						|| authzService.isUserAbleToGrade(gradebookUid)) {
					return true;
				}
			} else if (cmd.equals(CMD_ATTACH)) {
				if (authzService.isUserAbleToEditAssessments(gradebookUid)) {
					return true;
				}
			} else if (cmd.equals(CMD_GET_GRADE) || cmd.equals(CMD_GRADE)
					|| cmd.equals(CMD_GET_GRADES_BY_ROS)
					|| cmd.equals(CMD_GET_GRADES_BY_GRADEBOOK) || cmd.equals(CMD_GRADE_ALL)) {
				if (authzService.isUserAbleToGrade(gradebookUid)) {
					return true;
				}
			}
		}
		return false;
	}
}
