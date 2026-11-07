package com.campus.placement.config;

import com.campus.placement.domain.*;
import com.campus.placement.repository.*;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Inserts SYNTHETIC demo data on first start (app.seed=true and empty database). */
@Component
public class DataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private final CompanyRepository companies;
    private final StudentRepository students;
    private final OpportunityRepository opportunities;
    private final ApplicationRepository applications;
    private final boolean enabled;

    public DataSeeder(CompanyRepository companies, StudentRepository students, OpportunityRepository opportunities,
                      ApplicationRepository applications, @Value("${app.seed:true}") boolean enabled) {
        this.companies = companies;
        this.students = students;
        this.opportunities = opportunities;
        this.applications = applications;
        this.enabled = enabled;
    }

    @Override
    public void run(String... args) {
        if (!enabled || companies.count() > 0 || students.count() > 0) return;
        LocalDate today = LocalDate.now();

        Company nimbus = company("Nimbus Software", "IT Services", "Chennai", "https://nimbus.example.com", "Cloud and enterprise software company.");
        Company zenith = company("Zenith Analytics", "Data & AI", "Bengaluru", "https://zenith.example.com", "Analytics and machine learning products.");
        Company orbit = company("Orbit Systems", "Electronics", "Hyderabad", "https://orbit.example.com", "Embedded systems and IoT devices.");
        Company blueRiver = company("BlueRiver FinTech", "Finance", "Mumbai", "https://blueriver.example.com", "Digital payments platform.");
        Company vertex = company("Vertex Robotics", "Robotics", "Coimbatore", "https://vertex.example.com", "Industrial automation and robotics.");
        Company greenLeaf = company("GreenLeaf Energy", "Energy", "Trichy", "https://greenleaf.example.com", "Solar and smart-grid solutions.");

        Opportunity o1 = opp("Software Engineer Intern", OpportunityType.INTERNSHIP, nimbus, "Chennai", 25000.0, 7.0, 1, "CSE,IT", 5, today.plusDays(10), "Work on cloud microservices with a mentor team.");
        Opportunity o2 = opp("Associate Software Engineer", OpportunityType.PLACEMENT, nimbus, "Chennai", 6.5, 7.5, 0, "CSE,IT,ECE", 3, today.plusDays(20), "Full-time role, 2-year bond-free program.");
        Opportunity o3 = opp("Data Science Intern", OpportunityType.INTERNSHIP, zenith, "Remote", 30000.0, 8.0, 0, "CSE,IT", 2, today.plusDays(5), "Build ML pipelines and dashboards.");
        Opportunity o4 = opp("Machine Learning Engineer", OpportunityType.PLACEMENT, zenith, "Bengaluru", 10.0, 8.5, 0, "CSE,IT", 1, today.plusDays(30), "Applied ML role.");
        Opportunity o5 = opp("Embedded Firmware Intern", OpportunityType.INTERNSHIP, orbit, "Hyderabad", 20000.0, 6.5, 2, "ECE,EEE", 4, today.plusDays(12), "Firmware for IoT devices.");
        Opportunity o6 = opp("Hardware Design Engineer", OpportunityType.PLACEMENT, orbit, "Hyderabad", 5.5, 7.0, 1, "ECE,EEE", 2, today.plusDays(25), "PCB and hardware design.");
        Opportunity o7 = opp("Backend Developer Intern", OpportunityType.INTERNSHIP, blueRiver, "Mumbai", 28000.0, 7.5, 0, "ALL", 3, today.plusDays(3), "Payments API development.");
        Opportunity o8 = opp("Robotics Intern", OpportunityType.INTERNSHIP, vertex, "Coimbatore", 18000.0, 6.5, 2, "MECH,EEE,ECE", 3, today.plusDays(15), "Assist in robot cell commissioning.");
        Opportunity o9 = opp("Graduate Engineer Trainee", OpportunityType.PLACEMENT, vertex, "Coimbatore", 4.8, 6.5, 1, "MECH,EEE", 4, today.plusDays(40), "Trainee program in automation.");
        Opportunity o10 = opp("Solar Analyst Intern", OpportunityType.INTERNSHIP, greenLeaf, "Trichy", 15000.0, 6.0, 2, "ALL", 2, today.minusDays(2), "Deadline already passed (demo of expired state).");
        o10.setStatus(OpportunityStatus.OPEN);
        opportunities.saveAll(List.of(o1, o2, o3, o4, o5, o6, o7, o8, o9, o10));

        Student a = student("Aarav Kumar", "aarav.kumar@campus.demo", "CS2201", "CSE", 8.9, 0, 2026, "Java, React, SQL");
        Student b = student("Diya Sharma", "diya.sharma@campus.demo", "CS2202", "CSE", 9.2, 0, 2026, "Python, ML, Spring Boot");
        Student c = student("Karthik Raj", "karthik.raj@campus.demo", "IT2203", "IT", 7.6, 0, 2026, "Node.js, MySQL");
        Student d = student("Meera Nair", "meera.nair@campus.demo", "IT2204", "IT", 6.8, 1, 2026, "HTML, CSS, JavaScript");
        Student e = student("Rohan Das", "rohan.das@campus.demo", "EC2205", "ECE", 8.1, 0, 2026, "Embedded C, VHDL");
        Student f = student("Sneha Iyer", "sneha.iyer@campus.demo", "EC2206", "ECE", 7.2, 1, 2026, "Verilog, MATLAB");
        Student g = student("Vikram Singh", "vikram.singh@campus.demo", "EE2207", "EEE", 7.9, 0, 2026, "PLC, MATLAB");
        Student h = student("Anjali Menon", "anjali.menon@campus.demo", "EE2208", "EEE", 6.4, 2, 2026, "AutoCAD");
        Student i = student("Harish Babu", "harish.babu@campus.demo", "ME2209", "MECH", 7.1, 0, 2026, "SolidWorks, CAD");
        Student j = student("Pooja Reddy", "pooja.reddy@campus.demo", "ME2210", "MECH", 6.9, 1, 2026, "CATIA, Robotics basics");
        Student k = student("Imran Khan", "imran.khan@campus.demo", "CS2211", "CSE", 8.4, 0, 2026, "Go, Docker, Kubernetes");
        Student l = student("Lakshmi Priya", "lakshmi.priya@campus.demo", "CS2212", "CSE", 7.3, 0, 2026, "C++, DSA");
        students.saveAll(List.of(a, b, c, d, e, f, g, h, i, j, k, l));

        applications.saveAll(List.of(
                app(a, o1, ApplicationStatus.SHORTLISTED, "Strong profile"),
                app(a, o2, ApplicationStatus.INTERVIEW, "Technical round scheduled"),
                app(b, o3, ApplicationStatus.OFFERED, "Offer released"),
                app(b, o4, ApplicationStatus.INTERVIEW, null),
                app(c, o1, ApplicationStatus.APPLIED, null),
                app(c, o7, ApplicationStatus.APPLIED, null),
                app(e, o5, ApplicationStatus.OFFERED, "Selected after interview"),
                app(e, o6, ApplicationStatus.SHORTLISTED, null),
                app(f, o5, ApplicationStatus.REJECTED, "Skill mismatch"),
                app(g, o8, ApplicationStatus.INTERVIEW, null),
                app(g, o9, ApplicationStatus.OFFERED, "Placed - GET"),
                app(i, o9, ApplicationStatus.APPLIED, null),
                app(k, o2, ApplicationStatus.OFFERED, "Placed - Associate SE"),
                app(k, o3, ApplicationStatus.SHORTLISTED, null),
                app(l, o7, ApplicationStatus.WITHDRAWN, "Chose another opportunity")));
        log.info("Seeded demo data: 6 companies, 10 opportunities, 12 students. Admin login: tpo@campus.demo");
    }

    private Company company(String name, String industry, String location, String website, String desc) {
        Company c = new Company();
        c.setName(name); c.setIndustry(industry); c.setLocation(location); c.setWebsite(website); c.setDescription(desc);
        return companies.save(c);
    }

    private Opportunity opp(String title, OpportunityType type, Company company, String location, Double salary,
                            double minCgpa, int maxBacklogs, String depts, int openings, LocalDate deadline, String desc) {
        Opportunity o = new Opportunity();
        o.setTitle(title); o.setType(type); o.setCompany(company); o.setLocation(location); o.setSalary(salary);
        o.setMinCgpa(minCgpa); o.setMaxBacklogs(maxBacklogs); o.setAllowedDepartments(depts); o.setOpenings(openings);
        o.setDeadline(deadline); o.setDescription(desc);
        return o;
    }

    private Student student(String name, String email, String roll, String dept, double cgpa, int backlogs, int year, String skills) {
        Student s = new Student();
        s.setName(name); s.setEmail(email); s.setRollNo(roll); s.setDepartment(dept); s.setCgpa(cgpa);
        s.setBacklogs(backlogs); s.setGraduationYear(year); s.setSkills(skills);
        return s;
    }

    private Application app(Student s, Opportunity o, ApplicationStatus status, String remarks) {
        Application a = new Application();
        a.setStudent(s); a.setOpportunity(o); a.setStatus(status); a.setRemarks(remarks);
        return a;
    }
}
