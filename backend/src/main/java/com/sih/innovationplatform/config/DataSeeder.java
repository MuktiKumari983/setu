package com.sih.innovationplatform.config;

import com.sih.innovationplatform.impact.ImpactEvidence;
import com.sih.innovationplatform.impact.ImpactEvidenceRepository;
import com.sih.innovationplatform.impact.ImpactRecord;
import com.sih.innovationplatform.impact.ImpactRecordRepository;
import com.sih.innovationplatform.industry.Industry;
import com.sih.innovationplatform.industry.IndustryRepository;
import com.sih.innovationplatform.match.Match;
import com.sih.innovationplatform.match.MatchRepository;
import com.sih.innovationplatform.notification.Notification;
import com.sih.innovationplatform.notification.NotificationRepository;
import com.sih.innovationplatform.problem.Problem;
import com.sih.innovationplatform.problem.ProblemCapability;
import com.sih.innovationplatform.problem.ProblemCapabilityRepository;
import com.sih.innovationplatform.problem.ProblemCategory;
import com.sih.innovationplatform.problem.ProblemCategoryRepository;
import com.sih.innovationplatform.problem.ProblemRepository;
import com.sih.innovationplatform.project.Document;
import com.sih.innovationplatform.project.DocumentRepository;
import com.sih.innovationplatform.project.FailureRecord;
import com.sih.innovationplatform.project.FailureRecordRepository;
import com.sih.innovationplatform.project.Milestone;
import com.sih.innovationplatform.project.MilestoneRepository;
import com.sih.innovationplatform.project.Project;
import com.sih.innovationplatform.project.ProjectComment;
import com.sih.innovationplatform.project.ProjectCommentRepository;
import com.sih.innovationplatform.project.ProjectMember;
import com.sih.innovationplatform.project.ProjectMemberRepository;
import com.sih.innovationplatform.project.ProjectRepository;
import com.sih.innovationplatform.project.ProjectStatusHistory;
import com.sih.innovationplatform.project.ProjectStatusHistoryRepository;
import com.sih.innovationplatform.project.Task;
import com.sih.innovationplatform.project.TaskRepository;
import com.sih.innovationplatform.team.StudentTeam;
import com.sih.innovationplatform.team.StudentTeamRepository;
import com.sih.innovationplatform.university.Department;
import com.sih.innovationplatform.university.DepartmentRepository;
import com.sih.innovationplatform.university.Faculty;
import com.sih.innovationplatform.university.FacultyRepository;
import com.sih.innovationplatform.university.University;
import com.sih.innovationplatform.university.UniversityRepository;
import com.sih.innovationplatform.user.Role;
import com.sih.innovationplatform.user.RoleRepository;
import com.sih.innovationplatform.user.User;
import com.sih.innovationplatform.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Seeds realistic demo data the first time an empty database is started.
 * Demo scenario centres on a rural waste-collection problem; includes two
 * failed pilots and three implemented projects with verified impact.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final LocalDate SEED_TARGET_DATE = LocalDate.of(2026, 1, 15);
    private static final LocalDateTime SEED_COMPLETED_AT = LocalDateTime.of(2026, 1, 15, 12, 0);
    private static final LocalDateTime SEED_REVIEWED_AT = LocalDateTime.of(2026, 1, 20, 10, 0);
    private static final String MENTOR_FACULTY_NAME = "Dr. Anjali Verma";

    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final ProblemCategoryRepository categoryRepo;
    private final UniversityRepository universityRepo;
    private final DepartmentRepository departmentRepo;
    private final FacultyRepository facultyRepo;
    private final StudentTeamRepository teamRepo;
    private final IndustryRepository industryRepo;
    private final ProblemRepository problemRepo;
    private final ProblemCapabilityRepository capabilityRepo;
    private final ProjectRepository projectRepo;
    private final ProjectMemberRepository memberRepo;
    private final TaskRepository taskRepo;
    private final MilestoneRepository milestoneRepo;
    private final ProjectCommentRepository commentRepo;
    private final DocumentRepository documentRepo;
    private final ProjectStatusHistoryRepository historyRepo;
    private final FailureRecordRepository failureRepo;
    private final ImpactRecordRepository impactRepo;
    private final ImpactEvidenceRepository evidenceRepo;
    private final MatchRepository matchRepo;
    private final NotificationRepository notificationRepo;
    private final PasswordEncoder passwordEncoder;
    private final Set<MatchKey> seededMatches = new HashSet<>();

    @Override
    @Transactional
    public void run(String... args) {
        seededMatches.clear();
        if (problemRepo.count() > 0 || roleRepo.count() > 0 || userRepo.count() > 0
                || categoryRepo.count() > 0 || universityRepo.count() > 0 || industryRepo.count() > 0
                || projectRepo.count() > 0 || matchRepo.count() > 0) {
            return;
        }
        log.info("Seeding demo data...");
        seedRoles();
        seedCategories();
        seedUniversities();
        seedUsers();
        seedProblems();
        seedIndustries();
        seedProjects();
        seedImpact();
        seedNotifications();
        log.info("Seeding complete.");
    }

    private User admin;
    private User government;
    private User citizen;
    private User student;
    private User facultyUser;
    private User universityAdmin;
    private User industryUser;

    private University iitd;
    private University iitm;
    private University nitt;
    private University anna;
    private University coep;

    private void seedRoles() {
        Map<String, String> roles = Map.of(
                "CITIZEN", "Citizen / community member",
                "STUDENT", "Student team member",
                "FACULTY", "Faculty / mentor",
                "UNIVERSITY_ADMIN", "University coordinator",
                "INDUSTRY", "Industry partner",
                "GOVERNMENT_ADMIN", "Government official",
                "PLATFORM_ADMIN", "Platform administrator");
        for (Map.Entry<String, String> e : roles.entrySet()) {
            roleRepo.save(new Role(e.getKey(), e.getValue()));
        }
    }

    private void seedUsers() {
        admin = user("Platform Admin", "admin@example.com", "PLATFORM_ADMIN");
        government = user("District Coordination Office", "government@example.com", "GOVERNMENT_ADMIN");
        citizen = user("Meena Ramesh", "citizen@example.com", "CITIZEN");
        student = user("Arjun Nair", "student@example.com", "STUDENT");
        facultyUser = user(MENTOR_FACULTY_NAME, "faculty@example.com", "FACULTY");
        universityAdmin = user("Prof. Suresh Iyer", "university@example.com", "UNIVERSITY_ADMIN");
        industryUser = user("Kavita Rao", "industry@example.com", "INDUSTRY");
        linkFacultyUserToUniversity(facultyUser);
    }

    void linkFacultyUserToUniversity(User facultyUser) {
        Faculty mentor = facultyRepo.findAll().stream()
                .filter(f -> MENTOR_FACULTY_NAME.equals(f.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Seeded faculty mentor '" + MENTOR_FACULTY_NAME + "' was not found"));
        facultyUser.setLinkedUniversityId(persistedUniversityId(mentor.getUniversity()));
        userRepo.save(facultyUser);
    }

    private Long persistedUniversityId(University university) {
        if (university == null || university.getId() == null) {
            return null;
        }
        return universityRepo.findById(university.getId())
                .map(University::getId)
                .orElseThrow(() -> new IllegalStateException(
                        "Cannot link a user to missing university id " + university.getId()));
    }

    private User user(String name, String email, String roleName) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode("password123"));
        u.setRole(roleRepo.findByName(roleName).orElseThrow());
        u.setStatus("ACTIVE");
        return userRepo.save(u);
    }

    private void seedCategories() {
        List<String[]> cats = List.of(
                new String[]{"Waste Management", "Collection, segregation and disposal of waste"},
                new String[]{"Water Quality", "Drinking water safety, contamination, monitoring"},
                new String[]{"Public Health", "Health services, outreach, disease prevention"},
                new String[]{"Education", "Schooling, literacy, digital learning"},
                new String[]{"Renewable Energy", "Solar, grid reliability, rural electrification"},
                new String[]{"Agriculture & Livelihood", "Farming support, market access, income"},
                new String[]{"Transportation", "Roads, mobility, rural connectivity"},
                new String[]{"Public Safety", "Street lighting, security, vulnerable groups"},
                new String[]{"Flood & Disaster", "Early warning, evacuation, monsoon risks"},
                new String[]{"Digital Inclusion", "Internet access and digital skills"},
                new String[]{"Housing & Civic Infrastructure", "Housing, drainage, civic amenities"});
        for (String[] c : cats) {
            categoryRepo.save(new ProblemCategory(c[0], c[1]));
        }
    }

    private ProblemCategory cat(String name) {
        return categoryRepo.findByName(name).orElse(null);
    }

    private void seedUniversities() {
        iitd = universityRepo.save(new University("Indian Institute of Technology Delhi",
                "New Delhi", "IoT research lab, Waste & Water Research Group, Environmental Engineering dept, "
                        + "previously built smart bin monitoring and water quality pilots"));
        iitm = universityRepo.save(new University("Indian Institute of Technology Madras",
                "Chennai", "Centre for Innovation, GIS & geospatial lab, data analytics research centre, "
                        + "flood modelling research, coastal resilience projects"));
        nitt = universityRepo.save(new University("NIT Tiruchirappalli",
                "Tiruchirappalli", "Embedded systems lab, IoT lab, rural development institute, "
                        + "previous solar micro-grid pilots in villages"));
        anna = universityRepo.save(new University("Anna University",
                "Chennai", "Department of Public Health Engineering, analytics centre, "
                        + "education technology research group, mobile app projects"));
        coep = universityRepo.save(new University("College of Engineering Pune",
                "Pune", "GIS & surveying lab, agri-tech incubator, "
                        + "farmer advisory platform built with partners"));

        departmentRepo.save(new Department(iitd, "Environmental Engineering", "Waste & water systems"));
        departmentRepo.save(new Department(iitd, "Computer Science", "AI, IoT, web systems"));
        departmentRepo.save(new Department(iitm, "Civil Engineering", "Hydrology, geospatial analysis"));
        departmentRepo.save(new Department(iitm, "Computer Science", "Data science, GIS"));
        departmentRepo.save(new Department(nitt, "Electrical Engineering", "Embedded, IoT"));
        departmentRepo.save(new Department(nitt, "Computer Applications", "Software development"));
        departmentRepo.save(new Department(anna, "Public Health", "Community health research"));
        departmentRepo.save(new Department(anna, "Education Technology", "Digital learning"));
        departmentRepo.save(new Department(coep, "Agricultural Engineering", "Agri tech, irrigation"));
        departmentRepo.save(new Department(coep, "Civil Engineering", "GIS, surveying"));

        faculty(MENTOR_FACULTY_NAME, iitd, "Environmental Engineering", "Professor",
                "Waste Management, IoT Sensors, GIS, Circular Economy",
                "Smart solid waste management systems",
                "Smart Bin pilot for NDMC Delhi", "anjali.verma@iitd.ac.in");
        faculty("Dr. R. Subramanian", iitm, "Civil Engineering", "Associate Professor",
                "GIS, Hydrological Modeling, Data Analytics, Flood Modelling",
                "Flood forecasting for coastal cities",
                "Adyar river water quality dashboard", "rsub@iitm.ac.in");
        faculty("Dr. Karthik Prasad", nitt, "Electrical Engineering", "Assistant Professor",
                "IoT, Embedded Systems, Sensors, Prototyping",
                "Low power sensors for rural monitoring",
                "Solar water pump telemetry in Trichy district", "karthik@nitt.edu");
        faculty("Dr. Lakshmi Menon", iitm, "Computer Science", "Professor",
                "Machine Learning, Data Analytics, Computer Vision",
                "Visual monitoring of civic infrastructure",
                "Smart bin image classification pilot", "lakshmi@cse.iitm.ac.in");
        faculty("Dr. Nisha Kulkarni", coep, "Agricultural Engineering", "Assistant Professor",
                "GIS, Agri Analytics, Irrigation, Mobile Apps",
                "Precision agriculture for small farms",
                "Farmer price advisory app for Pune belt", "nisha.k@coep.edu.in");
        faculty("Prof. Sharma Rao", anna, "Public Health", "Professor",
                "Public Health, Epidemiology, Mobile Health",
                "Community health monitoring",
                "Rural health worker app (TN fit)", "sharma.rao@annauniv.edu");
        faculty("Dr. Amit Panigrahi", nitt, "Computer Applications", "Assistant Professor",
                "Web Development, Java, Spring Boot, Data Analytics",
                "Digital platforms for public services",
                "Panchayat service portal", "amit@nitc.edu.in");
        faculty("Dr. Priya Iyer", anna, "Education Technology", "Associate Professor",
                "E-Learning, Content Design, Mobile Apps, Web Development",
                "Digital literacy in rural schools",
                "Offline learning app for government schools", "priya@annauniv.edu");
        faculty("Dr. Sanjay Bhosale", coep, "Civil Engineering", "Professor",
                "GIS, Remote Sensing, Surveying, Geospatial Analysis",
                "Geospatial mapping for rural infrastructure",
                "Village infrastructure GIS atlas", "sanjay.b@coep.edu.in");
        faculty("Dr. Eswar Reddy", iitm, "Civil Engineering", "Assistant Professor",
                "GIS, Hydrology, Early Warning Systems, Data Analytics",
                "River-level early warning systems",
                "Chennai flood early warning prototype", "eswar@iitm.ac.in");
    }

    private void faculty(String name, University uni, String deptName, String title,
                         String expertise, String researchArea, String prevProjects, String email) {
        Department dept = departmentRepo.findByUniversityId(uni.getId()).stream()
                .filter(d -> d.getName().equals(deptName)).findFirst().orElse(null);
        facultyRepo.save(new Faculty(uni, dept, name, title, expertise, researchArea, prevProjects, email));
    }

    private void seedProblems() {
        problem("Poor waste collection in rural community",
                "Waste collection is irregular and there is no tracking mechanism. Garbage is piling up at village collection points, affecting health of residents.",
                cat("Waste Management"), "Hosur, Tamil Nadu", 6200, "HIGH",
                "Regular tracked waste collection with measurable coverage",
                Problem.STATUS_VERIFIED, "Waste Management", "HIGH",
                "IoT, Data Analytics, GIS, Web Development",
                "waste, collection, tracking, village, garbage",
                "Classified as Waste Management from keywords waste/collection/garbage; tracking implies IoT + web dashboard.",
                0.94, null, 2500, 4);
        problem("Contaminated drinking water in flood-affected villages",
                "Groundwater and wells are contaminated after floods. Families have no safe drinking water source and testing is not available locally.",
                cat("Water Quality"), "Chennai outskirts", 12000, "HIGH",
                "Affordable water testing and contamination alerts",
                Problem.STATUS_VERIFIED, "Water Quality", "HIGH",
                "IoT, Sensors, Data Analytics, GIS, Web Development",
                "water, contamination, flood, drinking, testing",
                "Water Quality matched on water/contamination/drinking; sensors + alert dashboard.",
                0.91, null, 3000, 3);
        problem("Irregular bus service isolates villages from district hospital",
                "Three villages have no bus connectivity after 6 pm; patients cannot reach the district hospital at night.",
                cat("Transportation"), "Thanjavur, Tamil Nadu", 8400, "CRITICAL",
                "Reliable evening transport connectivity",
                Problem.STATUS_SUBMITTED, "Transportation", "CRITICAL",
                "IoT, Data Analytics, Algorithms, GIS, Mobile App",
                "bus, transport, hospital, connectivity, village",
                "Transportation matched on bus/transport/connectivity; critical flagged on night hospital access.",
                0.88, null, 0, 0);
        problem("Low solar power reliability in remote hamlets",
                "Solar micro-grids installed by the panchayat fail frequently and there is no monitoring or repair support.",
                cat("Renewable Energy"), "Kolar, Karnataka", 3800, "MEDIUM",
                "Reliable monitored solar power for hamlets",
                Problem.STATUS_VERIFIED, "Renewable Energy", "HIGH",
                "IoT, Embedded Systems, Electrical Engineering, Mobile App",
                "solar, power, grid, monitoring, hamlets",
                "Renewable Energy matched on solar/power/grid.",
                0.87, null, 0, 0);
        problem("School dropout rise after digital classes gap",
                "Students lost the habit of attending school during the digital-only period; dropout is rising in upper primary classes.",
                cat("Education"), "Viluppuram, Tamil Nadu", 2500, "MEDIUM",
                "Bring students back and track attendance",
                Problem.STATUS_SUBMITTED, "Education", "MEDIUM",
                "Web Development, Mobile App, Data Analytics, Content Design",
                "school, dropout, education, attendance, students",
                "Education matched on school/dropout/students.",
                0.85, null, 0, 0);
        problem("No early warning for seasonal flooding of villages",
                "Every monsoon the river overflows and villages near the bank flood without advance warning; people lose cattle and homes.",
                cat("Flood & Disaster"), "Chennai outskirts", 15000, "CRITICAL",
                "Village-level early warning system",
                Problem.STATUS_VERIFIED, "Flood & Disaster", "CRITICAL",
                "IoT, GIS, Data Analytics, Hydrological Modeling, Web Development",
                "flood, warning, rainfall, river, evacuation",
                "Flood & Disaster matched on flood/warning/rainfall/river.",
                0.93, null, 0, 0);
        problem("Farmers not getting fair market prices",
                "Farmers sell at local mandis without price transparency; middlemen capture most of the margin on perishable produce.",
                cat("Agriculture & Livelihood"), "Pune district", 18000, "MEDIUM",
                "Price transparency and direct market access",
                Problem.STATUS_VERIFIED, "Agriculture & Livelihood", "MEDIUM",
                "Data Analytics, Mobile App, IoT, GIS",
                "farmer, price, market, produce, income",
                "Agriculture matched on farmer/price/market/produce.",
                0.9, null, 0, 0);
        problem("Street lighting theft and outages in peri-urban areas",
                "Street lights in new layouts are often stolen or remain non-functional due to no monitoring.",
                cat("Public Safety"), "Coimbatore, Tamil Nadu", 7000, "MEDIUM",
                "Monitored, tamper-proof street lighting",
                Problem.STATUS_SUBMITTED, "Public Safety", "HIGH",
                "IoT, Computer Vision, Web Development, GIS",
                "street light, theft, safety, darkness",
                "Public Safety matched on street light/safety/theft; vision-based tamper alerts.",
                0.86, null, 0, 0);
        problem("Dumping of untreated effluent into river",
                "Units upstream discharge untreated effluent; downstream villages report skin and stomach problems.",
                cat("Water Quality"), "Varanasi outskirts", 22000, "HIGH",
                "Monitor effluent and enforce compliance",
                Problem.STATUS_VERIFIED, "Water Quality", "CRITICAL",
                "IoT, Sensors, Computer Vision, Data Analytics, GIS",
                "effluent, river, water, discharge, contamination",
                "Water Quality matched on water/discharge/contamination.",
                0.92, null, 0, 0);
        problem("No internet access blocks online services in villages",
                "Villages have no broadband and poor mobile signal; students and residents cannot access online services or learning.",
                cat("Digital Inclusion"), "Aurangabad, Maharashtra", 11000, "MEDIUM",
                "Community internet and digital literacy",
                Problem.STATUS_SUBMITTED, "Digital Inclusion", "MEDIUM",
                "Networking, Web Development, Mobile App, Content Design",
                "internet, broadband, digital, mobile, network",
                "Digital Inclusion matched on internet/broadband/digital/mobile.",
                0.87, null, 0, 0);
    }

    private void problem(String title, String desc, ProblemCategory category, String location,
                         int beneficiaries, String urgency, String outcome, String status,
                         String aiCategory, String aiUrgency, String skills, String keywords,
                         String explanation, Double confidence, Long similarId, int capFrom, int capCount) {
        Problem p = new Problem();
        p.setTitle(title);
        p.setDescription(desc);
        p.setCategory(category);
        p.setLocation(location);
        p.setEstimatedBeneficiaries(beneficiaries);
        p.setUrgency(urgency);
        p.setRequiredOutcome(outcome);
        p.setSubmitter(citizen);
        p.setStatus(status);
        p.setAiCategory(aiCategory);
        p.setAiUrgency(aiUrgency);
        p.setAiSkills(skills);
        p.setAiKeywords(keywords);
        p.setAiExplanation(explanation);
        p.setAiConfidence(confidence);
        p.setAiSimilarity(null);
        if (similarId != null) {
            p.setSimilarProblem(problemRepo.getReferenceById(similarId));
        }
        Problem saved = problemRepo.save(p);
        addCapabilities(saved, skills, category != null ? category.getName() : "General");
    }

    private void addCapabilities(Problem p, String skills, String category) {
        List<String> sk = java.util.Arrays.stream(skills.split(",")).map(String::trim).toList();
        String[][] templates = switch (category) {
            case "Waste Management" -> new String[][]{
                    {"Collection scheduling", "Backend,Data Analytics", "Scheduling and tracking waste pickup", "HIGH"},
                    {"Route optimization", "Algorithms,GIS", "Optimise collection routes", "HIGH"},
                    {"Waste segregation", "IoT,Computer Vision", "Smart segregation monitoring", "MEDIUM"},
                    {"Monitoring dashboard", "Web Development", "Live citizen dashboard", "HIGH"}};
            case "Water Quality" -> new String[][]{
                    {"Sensor deployment & telemetry", "IoT,Sensors", "Deploy water quality sensors", "HIGH"},
                    {"Data analytics & alerts", "Data Analytics", "Detect contamination and alert", "HIGH"},
                    {"Spatial monitoring", "GIS", "Map affected sources", "MEDIUM"},
                    {"Public dashboard", "Web Development", "Real-time quality publishing", "MEDIUM"}};
            case "Flood & Disaster" -> new String[][]{
                    {"Flood monitoring sensors", "IoT,Sensors", "River-level and rainfall sensing", "HIGH"},
                    {"Early warning system", "Data Analytics,Algorithms", "Prediction and alert thresholds", "CRITICAL"},
                    {"Evacuation mapping", "GIS", "Flood-risk village maps", "HIGH"},
                    {"Alert dashboard", "Web Development", "Alerts to residents/authorities", "HIGH"}};
            case "Renewable Energy" -> new String[][]{
                    {"Solar micro-grid", "Electrical Engineering", "Community solar design", "HIGH"},
                    {"Energy monitoring", "IoT,Embedded Systems", "Grid health telemetry", "HIGH"},
                    {"Revenue model", "Data Analytics", "Tariff and payment tracking", "MEDIUM"},
                    {"Mobile monitoring", "Mobile App", "Village energy dashboard", "MEDIUM"}};
            case "Agriculture & Livelihood" -> new String[][]{
                    {"Field data collection", "Mobile App,Data Analytics", "Produce and price data", "HIGH"},
                    {"Market price engine", "Data Analytics", "Real-time price comparisons", "HIGH"},
                    {"Irrigation advisory", "IoT,GIS", "Soil and weather guidance", "MEDIUM"},
                    {"Farmer dashboard", "Web Development", "Advisory dashboard", "MEDIUM"}};
            case "Transportation" -> new String[][]{
                    {"Fleet tracking", "IoT", "Vehicle tracking", "HIGH"},
                    {"Route analytics", "Data Analytics,Algorithms", "Utilization analysis", "HIGH"},
                    {"Route mapping", "GIS", "Optimal routes", "MEDIUM"},
                    {"Commuter app", "Web Development,Mobile App", "Real-time schedules", "MEDIUM"}};
            case "Public Safety" -> new String[][]{
                    {"Tamper monitoring", "IoT", "Sensor on light poles", "HIGH"},
                    {"Vision analytics", "Computer Vision", "Detect damage/theft", "MEDIUM"},
                    {"Repair workflow", "Web Development", "Ticketing and repair tracking", "MEDIUM"}};
            case "Education" -> new String[][]{
                    {"Content platform", "Web Development,Content Design", "Local-language learning", "HIGH"},
                    {"Attendance tracking", "Data Analytics", "Monitor participation", "MEDIUM"},
                    {"Digital literacy labs", "Networking", "Community internet access", "MEDIUM"},
                    {"Analytics dashboard", "Web Development,Data Analytics", "District education dashboard", "MEDIUM"}};
            case "Digital Inclusion" -> new String[][]{
                    {"Community connectivity", "Networking", "Shared broadband at panchayat", "HIGH"},
                    {"Digital literacy program", "Content Design", "Training residents", "MEDIUM"},
                    {"Service kiosks", "Web Development", "E-services at centers", "MEDIUM"}};
            default -> new String[][]{
                    {"Field assessment & data collection", "Data Analytics,Mobile App", "Collect baseline data", "HIGH"},
                    {"Monitoring application", "Web Development,Data Analytics", "Monitoring dashboard", "MEDIUM"},
                    {"Community engagement", "Content Design", "Inform the community", "MEDIUM"}};
        };
        for (String[] t : templates) {
            ProblemCapability pc = new ProblemCapability();
            pc.setProblem(p);
            pc.setCapability(t[0]);
            pc.setRequiredSkills(t[1]);
            pc.setDescription(t[2]);
            pc.setPriority(t[3]);
            capabilityRepo.save(pc);
        }
    }

    private void seedIndustries() {
        industryRepo.save(new Industry("AquaPure Systems", "Water & Environment",
                "Water treatment and monitoring products for municipal and rural bodies",
                "Sensors, IoT, Water Quality, Data Analytics", "LoRaWAN, Python, Cloud, Flow Meter",
                "Sensor kits, water testing labs", "Seed funding, joint grants",
                "Domain mentoring for water projects", "Water quality testing", "On-site installation and calibration",
                "Chennai"));
        industryRepo.save(new Industry("FarmLink Agritech", "Agriculture",
                "Market linkage and advisory services for smallholder farmers",
                "Agronomy, Mobile Apps, GIS, Data Analytics", "Flutter, Node.js, PostgreSQL",
                "Soil sensor kits, APMC market data", "Procurement hubs",
                "Farming advisory and field trials", "Soil lab testing", "Village-level deployment",
                "Pune"));
        industryRepo.save(new Industry("GeoSense Technologies", "Geospatial Services",
                "GIS mapping and remote sensing for disaster management",
                "GIS, Remote Sensing, Hydrological Modeling", "QGIS, ArcGIS, Sentinel Hub",
                "Drone survey kits", "Grant matching",
                "GIS technical mentoring", "Flood model calibration", "Field survey support",
                "Hyderabad"));
        industryRepo.save(new Industry("CivicSense Robotics", "Smart Infrastructure",
                "Robotics and IoT for municipal waste and street infrastructure",
                "Robotics, IoT, Computer Vision, GIS", "ROS, Edge AI, Arduino",
                "Sorting robots, sensor meshes", "Municipal contracts",
                "Hardware mentoring", "Prototype stress testing", "City-scale pilot rollouts",
                "New Delhi"));
        industryRepo.save(new Industry("VoltGrid Solutions", "Renewable Energy",
                "Solar microgrid management software and hardware",
                "Solar, Electrical Engineering, IoT", "Modbus, MQTT, Cloud",
                "Inverter telemetry kits", "Renewable subsidies",
                "Energy domain mentoring", "Grid simulation rigs", "Field installation",
                "Tiruchirappalli"));
        industryRepo.save(new Industry("MobilityNext", "Transportation",
                "Data-driven public transport planning and shuttle services",
                "Transport Planning, GIS, Data Analytics", "GTFS, Python, QGIS",
                "GPS loggers", "Route pilot funding",
                "Transport domain mentoring", "Route simulation", "Pilot bus routes",
                "Chennai"));
        industryRepo.save(new Industry("NetCivic", "Digital Inclusion",
                "Community broadband and digital literacy programs",
                "Networking, Mesh Networking, Digital Literacy", "WiFi mesh, CPEs",
                "Mesh routers", "Rural connectivity funds",
                "Digital literacy curricula", "Network throughput testing", "Community installation",
                "Ranchi"));
        log.info("Seeded {} industries", industryRepo.count());
    }

    private void seedProjects() {
        List<Problem> problems = problemRepo.findAll();
        StudentTeam green = team("GreenCoders", iitd, "Full-stack + IoT team",
                "Java, IoT, Data Analytics, React", "Spring Boot, React, Arduino, MySQL",
                "Smart cities, civic tech", "Full-time", "Smart bin ingress prototype");
        StudentTeam ruralTech = team("RuralTech Squad", nitt, "Embedded + mobile team",
                "Embedded C, Sensors, Android, Data Analytics", "ESP32, Android, Firebase",
                "Rural electrification, sensor networks", "Weekends", "Solar pump telemetry demo");
        StudentTeam aqua = team("AquaWatchers", iitm, "Water + GIS team",
                "Python, GIS, Sensors, Data Analytics", "QGIS, Python, LoRaWAN",
                "Water quality, flood resilience", "Full-time", "Adyar water quality dashboard");
        StudentTeam agro = team("Kisan Connect", coep, "Agri tech team",
                "Mobile Dev, Data Analytics, GIS, Java", "Flutter, Node.js, PostgreSQL",
                "Farmer livelihoods, market access", "Full-time", "Farmer advisory platform MVP");
        StudentTeam edu = team("LearnBridge", anna, "E-learning team",
                "Web Development, Java, React, Data Analytics", "React, Spring Boot, MySQL",
                "Rural education, digital literacy", "Part-time", "Offline learning app");

        green.setLeadId(student.getId());
        userRepo.save(student);
        teamRepo.save(green);

        // ---- implemented projects ----
        project(problems.get(1), iitm, aqua, f("Dr. R. Subramanian", iitm), ind("AquaPure Systems"),
                "IMPACT", "Water quality monitoring deployed in 4 villages");

        project(problems.get(6), coep, agro, f("Dr. Nisha Kulkarni", coep), ind("FarmLink Agritech"),
                "IMPACT", "Farmer price transparency platform live");

        project(problems.get(8), iitm, aqua, f("Dr. Lakshmi Menon", iitm), ind("AquaPure Systems"),
                "IMPACT", "Effluent monitoring with vision-based alerts");

        // ---- failed pilots (recovery demo) ----
        Project flood = project(problems.get(5), iitm, aqua, f("Dr. Eswar Reddy", iitm), ind("GeoSense Technologies"),
                Project.NEEDS_SUPPORT, "Flood early warning pilot stalled at GIS integration");
        Project waste = project(problems.get(0), iitd, green, f(MENTOR_FACULTY_NAME, iitd), ind("CivicSense Robotics"),
                Project.NEEDS_SUPPORT, "Waste collection pilot stalled at GIS route optimization");

        // ---- in progress ----
        project(problems.get(3), nitt, ruralTech, f("Dr. Karthik Prasad", nitt), ind("VoltGrid Solutions"),
                "IMPLEMENTATION", "Solar grid reliability platform at 72%");
        project(problems.get(2), anna, edu, f("Prof. Sharma Rao", anna), ind("MobilityNext"),
                "PILOT", "Evening bus connectivity pilot");
        project(problems.get(6), coep, agro, f("Dr. Nisha Kulkarni", coep), ind("FarmLink Agritech"),
                "PROTOTYPE", "Market price engine prototype");
        project(problems.get(4), anna, edu, f("Dr. Priya Iyer", anna), null,
                "TEAM_FORMED", "Attendance tracking pod");
        project(problems.get(9), nitt, ruralTech, f("Dr. Amit Panigrahi", nitt), ind("NetCivic"),
                "PROPOSAL", "Community connectivity proposal");
        project(problems.get(7), iitd, green, f(MENTOR_FACULTY_NAME, iitd), ind("CivicSense Robotics"),
                "PILOT", "Street light tamper pilot");

        markFailed(waste, "GIS expertise missing for route optimization");
        markFailed(flood, "GIS expertise missing for evacuation zone mapping");
    }

    private void markFailed(Project p, String reason) {
        User actor = government;
        p.setStatus(Project.NEEDS_SUPPORT);
        p.setFailureReason(reason);
        FailureRecord fr = new FailureRecord();
        fr.setProject(p);
        fr.setStage("PILOT");
        fr.setReason(reason);
        fr.setMissingCapability("GIS");
        fr.setReportedBy(actor);
        fr.setResolved(false);
        failureRepo.save(fr);
        history(p, Project.NEEDS_SUPPORT, "Failed at PILOT: " + reason, actor);
    }

    private StudentTeam team(String name, University uni, String desc, String skills,
                             String tech, String interests, String availability, String prev) {
        return teamRepo.save(new StudentTeam(uni, name, desc, skills, tech, interests, availability, prev));
    }

    private Faculty f(String name, University uni) {
        return facultyRepo.findAll().stream()
                .filter(x -> x.getName().equals(name)).findFirst().orElseThrow();
    }

    private Industry ind(String name) {
        return industryRepo.findAll().stream().filter(x -> x.getName().equals(name)).findFirst().orElse(null);
    }

    private Project project(Problem problem, University uni, StudentTeam team, Faculty mentor,
                            Industry industry, String status, String description) {
        Project p = new Project();
        p.setProblem(problem);
        p.setTitle(problem.getTitle());
        p.setDescription(description);
        p.setUniversity(uni);
        p.setTeam(team);
        p.setLeadMentor(mentor);
        p.setIndustry(industry);
        p.setStatus(status);
        int idx = Project.STAGES.indexOf(status);
        p.setProgressPercent(idx >= 0 ? List.of(0, 10, 20, 25, 35, 45, 55, 65, 75, 100).get(idx) : 0);
        p.setCreatedBy(admin);
        Project saved = projectRepo.save(p);
        member(saved, admin, "COORDINATOR");
        member(saved, student, "TEAM_LEAD");
        member(saved, citizen, "CITIZEN");

        long seedIndex = problem.getId();
        match(problem.getId(), "UNIVERSITY", uni.getId(), seedScore(seedIndex, "UNIVERSITY"),
                "Listed departments and research areas cover the required capabilities",
                List.of("Matched skills", "Research alignment"));
        match(problem.getId(), "TEAM", team.getId(), seedScore(seedIndex, "TEAM"),
                "Team skills include the required stack",
                List.of("Java", "IoT", "Data Analytics"));
        if (mentor != null) {
            match(problem.getId(), "MENTOR", mentor.getId(), seedScore(seedIndex, "MENTOR"),
                    "Expertise overlaps the problem domain",
                    List.of("Domain expertise", "Research area"));
        }
        if (industry != null) {
            match(problem.getId(), "INDUSTRY", industry.getId(), seedScore(seedIndex, "INDUSTRY"),
                    "Hardware and deployment capabilities available",
                    List.of("IoT hardware", "Deployment"));
        }

        // lifecycle/history
        history(saved, Project.STAGES.get(0), "Challenge submitted", citizen);
        if (idx >= 1) history(saved, Project.STAGES.get(1), "Verified by district office", government);
        if (idx >= 2) history(saved, Project.STAGES.get(2), "Automated matching completed", admin);
        if (idx >= 3) history(saved, Project.STAGES.get(3), "Student team formed", universityAdmin);
        if (idx >= 4) history(saved, Project.STAGES.get(4), "Proposal accepted", government);
        if (idx >= 5) history(saved, Project.STAGES.get(5), "Prototype milestone complete", student);
        if (idx >= 6) history(saved, Project.STAGES.get(6), "Expert review passed", facultyUser);
        if (idx >= 7) history(saved, Project.STAGES.get(7), "Pilot started in villages", universityAdmin);
        if (idx >= 8) history(saved, Project.STAGES.get(8), "Implementation underway", universityAdmin);
        if (idx >= 9) history(saved, Project.STAGES.get(9), "Impact recorded", citizen);

        milestone(saved, "Discovery & scoping", "Field visits and baseline data", idx >= 1 ? "COMPLETED" : "PENDING");
        milestone(saved, "Prototype", "Usable prototype of solution", idx >= 5 ? "COMPLETED" : (idx >= 4 ? "IN_PROGRESS" : "PENDING"));
        milestone(saved, "Pilot", "Pilot in target villages", idx >= 7 ? "COMPLETED" : (idx >= 6 ? "IN_PROGRESS" : "PENDING"));
        milestone(saved, "Implementation", "Full rollout and training", idx >= 9 ? "COMPLETED" : (idx >= 8 ? "IN_PROGRESS" : "PENDING"));

        task(saved, "Collect baseline field data", "Survey at least 50 households", "DONE");
        task(saved, "Build monitoring dashboard", "Live KPIs per village", idx >= 5 ? "IN_PROGRESS" : "OPEN");
        task(saved, "Integrate sensor telemetry", "Connect devices to dashboard", idx >= 7 ? "DONE" : "OPEN");
        if (mentor != null) {
            comment(saved, facultyUser, "MENTOR", "Guidance: keep the data schema simple; focus on village-level view.");
        }
        if (industry != null) {
            comment(saved, industryUser, "INDUSTRY", "We can provide hardware kits and deploy with the team on site.");
        }
        comment(saved, citizen, "GENERAL", "Community is very supportive; local panchayat will coordinate.");
        document(saved, "Baseline survey report", "/files/baseline-survey.pdf");

        problem.setStatus(idx >= 2 ? Problem.STATUS_MATCHED : (idx >= 1 ? Problem.STATUS_VERIFIED : Problem.STATUS_SUBMITTED));
        if (idx >= 2) {
            problem.setStatus(Problem.STATUS_MATCHED);
        }
        return saved;
    }

    private void member(Project p, User u, String role) {
        if (memberRepo.findByProjectId(p.getId()).stream().anyMatch(m -> m.getUser().getId().equals(u.getId()))) {
            return;
        }
        ProjectMember m = new ProjectMember();
        m.setProject(p);
        m.setUser(u);
        m.setRoleInProject(role);
        memberRepo.save(m);
    }

    private void history(Project p, String status, String note, User actor) {
        ProjectStatusHistory h = new ProjectStatusHistory();
        h.setProject(p);
        h.setStatus(status);
        h.setNote(note);
        h.setChangedBy(actor);
        historyRepo.save(h);
    }

    private void milestone(Project p, String title, String desc, String status) {
        Milestone m = new Milestone();
        m.setProject(p);
        m.setTitle(title);
        m.setDescription(desc);
        m.setStatus(status);
        m.setTargetDate(SEED_TARGET_DATE.plusDays(30));
        if ("COMPLETED".equals(status)) {
            m.setCompletedAt(SEED_COMPLETED_AT);
        }
        milestoneRepo.save(m);
    }

    private void task(Project p, String title, String desc, String status) {
        Task t = new Task();
        t.setProject(p);
        t.setTitle(title);
        t.setDescription(desc);
        t.setStatus(status);
        t.setAssignedTo(student);
        taskRepo.save(t);
    }

    private void comment(Project p, User author, String type, String content) {
        ProjectComment c = new ProjectComment();
        c.setProject(p);
        c.setAuthor(author);
        c.setCommentType(type);
        c.setContent(content);
        commentRepo.save(c);
    }

    private void document(Project p, String name, String url) {
        Document d = new Document();
        d.setProject(p);
        d.setName(name);
        d.setUrl(url);
        d.setUploader(student);
        documentRepo.save(d);
    }

    static double seedScore(long seed, String type) {
        int base = switch (type) {
            case "UNIVERSITY" -> 90;
            case "TEAM" -> 91;
            case "MENTOR" -> 88;
            case "INDUSTRY" -> 87;
            default -> 85;
        };
        return Math.min(0.99, (base + (int) (seed % 5)) / 100.0);
    }

    void match(Long problemId, String type, Long entityId, double score, String explanation, List<String> factors) {
        MatchKey key = new MatchKey(problemId, type, entityId);
        if (!seededMatches.add(key)) {
            return;
        }
        if (matchRepo.findByProblemIdAndEntityTypeAndEntityId(problemId, type, entityId).isPresent()) {
            return;
        }
        Match m = new Match();
        m.setProblem(problemRepo.getReferenceById(problemId));
        m.setEntityType(type);
        m.setEntityId(entityId);
        m.setScore(Math.round(score * 100.0) / 100.0);
        m.setExplanation(explanation);
        m.setContributingFactors(String.join(" | ", factors));
        m.setStatus("PROPOSED");
        matchRepo.save(m);
    }

    private record MatchKey(Long problemId, String entityType, Long entityId) {
    }

    private void seedImpact() {
        List<Project> implemented = projectRepo.findAll().stream()
                .filter(p -> p.getStatus().equals("IMPACT")).toList();
        for (int i = 0; i < implemented.size(); i++) {
            Project p = implemented.get(i);
            ImpactRecord r = new ImpactRecord();
            r.setProject(p);
            r.setBeneficiaries(i == 0 ? 12000L : (i == 1 ? 18000L : 22000L));
            r.setLocationsCovered(4);
            r.setCostSavings(i == 0 ? 360000.0 : (i == 1 ? 150000.0 : 200000.0));
            r.setProblemReduction("Coverage improved from 20% to 85%; complaint turnaround halved");
            r.setDurationMonths(9);
            r.setImplementationPercent(100);
            r.setDomainMetrics("4 villages, 6 monitoring points, SLA 94%");
            r.setSubmittedBy(student);
            r.setVerificationStatus(i == 0 ? ImpactRecord.PENDING : ImpactRecord.VERIFIED);
            ImpactRecord saved = impactRepo.save(r);

            ImpactEvidence e1 = new ImpactEvidence();
            e1.setImpactRecord(saved);
            e1.setEvidenceType(i == 0 ? "DOCUMENT" : "MEASUREMENT");
            e1.setDescription(i == 0 ? "Implementation report and village photos"
                    : "Before/after measurements with panchayat verification");
            e1.setFileUrl("/files/impact-evidence-" + (i + 1) + ".pdf");
            e1.setStatus(i == 0 ? "PENDING" : "VERIFIED");
            e1.setSubmittedBy(student);
            if (i != 0) {
                e1.setReviewedBy(admin);
                e1.setReviewedAt(SEED_REVIEWED_AT);
            }
            evidenceRepo.save(e1);
        }
    }

    private void seedNotifications() {
        notify(admin, "3 new challenges awaiting verification", "PROBLEM", "/challenges");
        notify(government, "2 projects need support after pilot failure", "FAILURE", "/projects");
        notify(citizen, "Your waste collection challenge is being worked on", "PROJECT", "/projects");
        notify(student, "New team matched to water quality challenge", "MATCH", "/matching");
        notify(industryUser, "New mentorship request pending approval", "MATCH", "/matching");
        notify(universityAdmin, "1 impact record awaiting review", "IMPACT", "/impact");
    }

    private void notify(User u, String message, String type, String link) {
        Notification n = new Notification();
        n.setUser(u);
        n.setMessage(message);
        n.setType(type);
        n.setLink(link);
        notificationRepo.save(n);
    }
}