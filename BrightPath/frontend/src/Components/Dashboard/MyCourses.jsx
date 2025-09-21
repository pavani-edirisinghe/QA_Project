import React, { useEffect } from 'react';
import { useEnrollment } from '../../context/EnrollmentContext';
import { useUser } from '../../context/UserContext';
import { useNavigate } from 'react-router-dom';

const MyCourses = () => {
  const { user } = useUser();
  const { enrollments, loading, error, refreshEnrollments } = useEnrollment();
  const navigate = useNavigate();
  
  // Extract just the courses from enrollments
  const myCourses = enrollments.map(enrollment => enrollment.course);

  useEffect(() => {
    if (user && user.id) {
      refreshEnrollments(user.id);
    }
  }, [user]);

  if (loading) {
    return (
      <div className="my-courses">
        <h2>My Enrolled Courses</h2>
        <div className="text-center py-4">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
          <p>Loading your courses...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="my-courses">
        <h2>My Enrolled Courses</h2>
        <div className="alert alert-danger">
          Error loading courses: {error}
        </div>
      </div>
    );
  }

  return (
    <div className="my-courses">
      <h2>My Enrolled Courses</h2>
      
      {myCourses.length === 0 ? (
        <div className="empty-courses">
          <div className="empty-icon">
            <i className="fas fa-book-open"></i>
          </div>
          <h3>You haven't enrolled in any courses yet</h3>
          <p>Browse available courses and start your learning journey today!</p>
          <button 
            className="browse-btn"
            onClick={() => navigate('/courses')}
          >
            Browse Courses
          </button>
        </div>
      ) : (
        <div className="course-grid">
          {myCourses.map(course => (
            <div className="course-card" key={course.id}>
              <div className="course-thumb">
                {course.imageUrl ? (
                  <img 
  src={`http://localhost:8081${course.imageUrl}`} 
  alt={course.name} 
  
/>
                ) : (
                  <div className="course-placeholder">
                    <i className="fas fa-book"></i>
                  </div>
                )}
                <div className="course-status">
                  <span>Enrolled</span>
                </div>
              </div>
              <div className="course-details">
                <div className="course-content">
                  <h4>{course.name}</h4>
                  <p>{course.description}</p>
                </div>
                <div className="course-actions">
                   <button className="continue-btn" onClick={() => navigate(`/course/${course.id}`)}>
                    <i className="fas fa-play"></i> Continue
                   </button>
                   <button className="resources-btn">
                    <i className="fas fa-download"></i> Resources
                   </button>
                </div>

              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default MyCourses;