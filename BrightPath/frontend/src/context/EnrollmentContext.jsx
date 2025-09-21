import React, { createContext, useState, useContext, useEffect } from 'react';

const EnrollmentContext = createContext();

export const EnrollmentProvider = ({ children }) => {
  const [enrollments, setEnrollments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchEnrollments = async (userId) => {
    try {
      setLoading(true);
      const response = await fetch(`http://localhost:8081/api/enrollments/user/${userId}`);
      
      if (!response.ok) {
        throw new Error('Failed to fetch enrollments');
      }
      
      const data = await response.json();
      setEnrollments(data);
      setLoading(false);
    } catch (err) {
      setError(err.message);
      setLoading(false);
    }
  };

  const enrollInCourse = async (userId, courseId) => {
    try {
      const response = await fetch(`http://localhost:8081/api/enrollments/${userId}/${courseId}`, {
        method: 'POST',
      });
      
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Enrollment failed');
      }
      
      const data = await response.json();
      setEnrollments(prev => [...prev, data]);
      return data;
    } catch (err) {
      throw new Error(err.message || 'Enrollment failed');
    }
  };

  const isEnrolled = (courseId) => {
    return enrollments.some(e => e.course.id === courseId);
  };

  return (
    < EnrollmentContext.Provider value={{ 
      enrollments, 
      loading, 
      error, 
      enrollInCourse,
      isEnrolled,
      refreshEnrollments: fetchEnrollments
    }}>
      {children}
    </EnrollmentContext.Provider>
  );
};

export const useEnrollment = () => useContext(EnrollmentContext);
