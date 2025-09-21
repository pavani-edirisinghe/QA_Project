package com.brightpath.backend.repository;

import com.brightpath.backend.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByUserId(Long userId);

    // Add this method for duplicate check
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}