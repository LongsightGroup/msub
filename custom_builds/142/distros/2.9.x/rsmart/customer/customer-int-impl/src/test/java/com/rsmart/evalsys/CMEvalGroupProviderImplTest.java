package com.rsmart.evalsys;

import com.github.javafaker.Faker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 1/21/13
 * Time: 1:29 PM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(MockitoJUnitRunner.class)
public class CMEvalGroupProviderImplTest {
    CMEvalGroupProviderImpl cmEvalGroupProvider ;

    @Mock
    UserDirectoryService userDirectoryService;

    @Mock
    CourseManagementService courseManagementService;

    @Mock
    SqlService sqlService;

    String[] groupIds;
    String[] sectionEids;
    Section[] sections;

    Faker faker = new Faker();

    String groupDelimiter = "@@@";
    String[] instructors;
    String[] students;
    String[] teachingAssistants;
    String[] admins;

    String propertyName = faker.name();

    Map users;

    private String permBeEvaluated = "provider.be.evaluated";
    private String permTakeEvaluation = "provider.take.evaluation";
    private String permAssignEvaluation = "provider.assign.eval";
    private String permRoleTA = "provider.role.ta";

    @Before
   	public void setUp() throws Exception {
        cmEvalGroupProvider = new CMEvalGroupProviderImpl();
        cmEvalGroupProvider.setCmTeachingAssistantRole("TA");
        cmEvalGroupProvider.setGroupDelimiter(groupDelimiter);
        cmEvalGroupProvider.setUseInstructorUserPropertyInTitle(true);
        cmEvalGroupProvider.setInstructorUserPropertyName(propertyName);
        cmEvalGroupProvider.setCourseManagementService(courseManagementService);
        cmEvalGroupProvider.setUds(userDirectoryService);
        cmEvalGroupProvider.setSqlService(sqlService);

        users = new HashMap();

        instructors = new String[4];
        for (int i = 0; i < 4; i++) {
            instructors[i] = faker.name();
            addUser(instructors[i]);
        }


        teachingAssistants = new String[4];
        for (int i = 0; i < 4; i++) {
            teachingAssistants[i] = faker.name();
            addUser(teachingAssistants[i]);

        }
        admins = new String[4];
        for (int i = 0; i < 4; i++) {

            admins[i] = faker.name();
            addUser(admins[i]);
        }
        students = new String[100];
        for (int i = 0; i < 100; i++) {
            students[i] = faker.name();
            addUser(students[i]);
        }

        sectionEids = new String[4];
        for (int i=0;i<4;i++) {
            sectionEids[i] = faker.phoneNumber();
        }

        sections = new Section[4];

        for (int i= 0;i<4;i++) {
            sections[i] = Mockito.mock(Section.class);
            when(sections[i].getEid()).thenReturn(sectionEids[i]);
            when(sections[i].getTitle()).thenReturn(faker.sentence());
            when(sections[i].getCourseOfferingEid()).thenReturn(faker.name());
            when(sections[i].getDescription()).thenReturn(faker.name());


            if (i == 0) {
                Set instructorsSet = new HashSet();
                instructorsSet.add(instructors[0]);
                instructorsSet.add(instructors[1]);
                EnrollmentSet enrollmentSet = getEnrollmentSet(instructorsSet);
                when(sections[i].getEnrollmentSet()).thenReturn(enrollmentSet);
            }

            if (i == 1) {
                Set instructorsSet = new HashSet();
                instructorsSet.add(instructors[2]);
                EnrollmentSet enrollmentSet = getEnrollmentSet(instructorsSet);
                when(sections[i].getEnrollmentSet()).thenReturn(enrollmentSet);
            }

            if (i == 2) {
                Set instructorsSet = new HashSet();
                instructorsSet.add(instructors[3]);
                EnrollmentSet enrollmentSet = getEnrollmentSet(instructorsSet);
                when(sections[i].getEnrollmentSet()).thenReturn(enrollmentSet);
            }

            if (i > 2) {
                EnrollmentSet enrollmentSet = getEnrollmentSet(new HashSet());
                when(sections[i].getEnrollmentSet()).thenReturn(enrollmentSet);
            }


        }



        groupIds = new String[4];

        // 2 instructors and 2 ta's
        groupIds[0] = sectionEids[0] + groupDelimiter + instructors[0] ;
        groupIds[0] = sectionEids[0] + groupDelimiter + instructors[1] ;
        groupIds[0] = sectionEids[0] + groupDelimiter + teachingAssistants[0] ;
        groupIds[0] = sectionEids[0] + groupDelimiter + teachingAssistants[1] ;


        // 1 instructor and 1 ta
        groupIds[1] = sectionEids[1] + groupDelimiter + instructors[2] ;
        groupIds[1] = sectionEids[1] + groupDelimiter + teachingAssistants[2] ;


        // 1 instructor and no tas
        groupIds[2] = sectionEids[2] + groupDelimiter + instructors[3] ;

        // no instructor and 1 tas
        groupIds[3] = sectionEids[3] + groupDelimiter + teachingAssistants[3] ;




        for (int i= 0;i<4;i++) {
            when(courseManagementService.isSectionDefined(sectionEids[i])).thenReturn(true);
            when(courseManagementService.getSection(sectionEids[i])).thenReturn(sections[i]);
        }



        when(courseManagementService.findInstructingSections(instructors[0])).thenReturn(new HashSet<Section>(Arrays.asList(sections[0])));




    }

    private EnrollmentSet getEnrollmentSet(Set instructorsSet) {
        EnrollmentSet es = Mockito.mock(EnrollmentSet.class);
        when(es.getOfficialInstructors()).thenReturn(instructorsSet);
        when(es.getDescription()).thenReturn(faker.sentence());
        when(es.getEid()).thenReturn(faker.name());
        when(es.getTitle()).thenReturn(faker.name());
        return es;
    }

    private void addUser(String id) {
        User user = Mockito.mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getEid()).thenReturn(id);
        when(user.getLastName()).thenReturn(faker.lastName());
        when(user.getFirstName()).thenReturn(faker.firstName());
        when(user.getDisplayId()).thenReturn(id);
        when(user.getEmail()).thenReturn(faker.lastName() + "@" + faker.lastName() + ".com");
        when(user.getType()).thenReturn("registered");
        when(user.getModifiedDate()).thenReturn(new Date());
        when(user.getCreatedDate()).thenReturn(new Date());
        ResourceProperties properties = Mockito.mock(ResourceProperties.class);
        when(properties.get(propertyName)).thenReturn(faker.phoneNumber());
        when(user.getProperties()).thenReturn(properties);

        try {
            when(userDirectoryService.getUser(id)).thenReturn(user);
            when(userDirectoryService.getUserByEid(id)).thenReturn(user);
            when(userDirectoryService.getUserEid(id)).thenReturn(id);
            when(userDirectoryService.getUserId(id)).thenReturn(id);
        } catch (UserNotDefinedException e) {
            e.printStackTrace();
        }

        users.put(id, user);
    }

    @Test
    public void getUserIdsForEvalGroupsTest(){


    }

    @Test
    public void countUserIdsForEvalGroups(){

    }

    @Test
    public void getEvalGroupsForUser(){
        List<EvalGroup> evalGroups = cmEvalGroupProvider.getEvalGroupsForUser(instructors[0],permBeEvaluated);


    }

    @Test
    public void countEvalGroupsForUser(){
        assertTrue(cmEvalGroupProvider.countEvalGroupsForUser(instructors[0], permBeEvaluated) == 1);
    }

    @Test
    public void getGroupByGroupId(){

        for (int i=0;i<4;i++){
            EvalGroup evalGroup = cmEvalGroupProvider.getGroupByGroupId(groupIds[i]);
            assertNotNull(evalGroup);
            assertNotNull(evalGroup.title);
            assertNotNull(evalGroup.evalGroupId);
            assertNotNull(evalGroup.type);
            System.out.println(evalGroup.title);
            if (i == 0 || i == 1 || i == 3 ) {
                assertTrue(evalGroup.title.contains("(TA)"));
            } else {
                assertFalse(evalGroup.title.contains("(TA)"));
            }
        }

    }

    @Test
    public void isUserAllowedInGroup(){


    }
}
