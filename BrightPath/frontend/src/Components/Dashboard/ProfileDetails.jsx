import React, { useState } from 'react';
import axios from 'axios';
import { useUser } from '../../context/UserContext';

const ProfileDetails = () => {
  const { user, updateUser } = useUser(); // Make sure `updateUser` is implemented in your context
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    username: user?.username || '',
    email: user?.email || '',
    password: '', // Password is not pre-filled for security
  });

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

const handleSave = async () => {
  const updatedData = {
    id: user.id,
    username: formData.username,
    email: formData.email,
  };

  if (formData.password.trim() !== "") {
    updatedData.password = formData.password;
  }

  try {
    await updateUser(updatedData);

    const updatedUser = { ...user, ...updatedData };
    localStorage.setItem("user", JSON.stringify(updatedUser));
    updateUser(updatedUser);

    setFormData({
      username: updatedUser.username,
      email: updatedUser.email,
      password: "",
    });

    setIsEditing(false);
  } catch (err) {
    console.error("Update failed in component:", err.message);
    alert("Failed to update. Check console for details.");
  }
};


const handlePhotoChange = async (e) => {
  const file = e.target.files[0];
  if (!file) return;

  const formData = new FormData();
  formData.append("profileImage", file);

  try {
    const response = await axios.put(
      `http://localhost:8081/api/users/${user.id}/profile-image`,
      formData,
      { headers: { "Content-Type": "multipart/form-data" } }
    );
    const updatedUser = { ...user, profileImage: response.data.profileImage };
    localStorage.setItem("user", JSON.stringify(updatedUser));
    updateUser(updatedUser);
  } catch (error) {
    console.error("Image upload failed:", error.response?.data || error.message);
    alert("Image upload failed.");
  }
};



  return (
    <div className="profile-details">
      <h2>Profile Details</h2>
      
      <div className="profile-section">
        <div className="profile-image-container">
  {user?.profileImage ? (
    <img 
      src={`http://localhost:8081/uploads/${user.profileImage}`} 
      alt="Profile"
      className="large-profile-image"
      onError={(e) => {
        e.target.onerror = null;
        e.target.src = '/img/default-profile.png';
      }}
    />
  ) : (
    <div className="profile-image-placeholder">
      <i className="fas fa-user-circle"></i>
    </div>
  )}

  {/* Hidden file input */}
  <input
    type="file"
    id="photo-upload"
    accept="image/*"
    style={{ display: 'none' }}
    onChange={handlePhotoChange}
  />
  <label htmlFor="photo-upload" className="change-photo-btn">
    <i className="fas fa-camera"></i> Change Photo
  </label>
</div>

        
        <div className="profile-info">
          <div className="info-row">
            <label>Username:</label>
            {isEditing ? (
              <input
                type="text"
                name="username"
                value={formData.username}
                onChange={handleInputChange}
              />
            ) : (
              <div className="info-value">{user?.username || 'N/A'}</div>
            )}
          </div>
          
          <div className="info-row">
            <label>Email:</label>
            {isEditing ? (
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleInputChange}
              />
            ) : (
              <div className="info-value">{user?.email || 'N/A'}</div>
            )}
          </div>
          
          <div className="info-row">
            <label>Password:</label>
            {isEditing ? (
              <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleInputChange}
              />
            ) : (
              <div className="info-value">
                ••••••••
                <button className="change-password-btn" onClick={() => setIsEditing(true)}>
                  Change Password
                </button>
              </div>
            )}
          </div>
          
          <div className="info-actions">
            {isEditing ? (
              <>
                <button className="save-profile-btn" onClick={handleSave}>
                  <i className="fas fa-save me-1"></i> Save
                </button>
                <button className="cancel-profile-btn" onClick={() => setIsEditing(false)}>
                  <i className="fas fa-times me-1"></i> Cancel
                </button>
              </>
            ) : (
              <button className="edit-profile-btn" onClick={() => setIsEditing(true)}>
                <i className="fas fa-edit me-1"></i> Edit Profile
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProfileDetails;
