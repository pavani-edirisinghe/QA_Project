import React, { createContext, useState, useEffect, useContext } from 'react';
import axios from 'axios';

const UserContext = createContext(null);

// Custom hook to consume the context
export const useUser = () => {
  const context = useContext(UserContext);
  if (!context) {
    throw new Error('useUser must be used within a UserProvider');
  }
  return context;
};

// Context provider component
export const UserProvider = ({ children }) => {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch (e) {
        console.error("Error parsing user data:", e);
        localStorage.removeItem('user');
      }
    }
  }, []);

  const login = (userData) => {
    setUser(userData);
    localStorage.setItem('user', JSON.stringify(userData));
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('user');
    localStorage.removeItem('authToken');
  };

const updateUser = async (updatedData) => {
  if (!user || !user.id) {
    console.error("User ID not available for update.");
    return;
  }

  try {
    const response = await axios.put(
      `http://localhost:8081/api/users/${user.id}`,
      updatedData
    );

    // Update user state and local storage
    setUser(response.data);
    localStorage.setItem("user", JSON.stringify(response.data));
  } catch (error) {
    console.error("Update failed", error.response?.data || error.message);
    throw error;
  }
};


  return (
    <UserContext.Provider value={{ user, login, logout, updateUser }}>
      {children}
    </UserContext.Provider>
  );
};
