package com.campus.placement.repository;

import com.campus.placement.domain.Student;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmailIgnoreCase(String email);
    Optional<Student> findByRollNoIgnoreCase(String rollNo);

    @Query("select s from Student s where (lower(s.name) like lower(concat('%', :q, '%')) "
            + "or lower(s.rollNo) like lower(concat('%', :q, '%')) "
            + "or lower(s.email) like lower(concat('%', :q, '%'))) "
            + "and (:dept = '' or s.department = :dept) order by s.name")
    List<Student> search(@Param("q") String q, @Param("dept") String dept);
}
