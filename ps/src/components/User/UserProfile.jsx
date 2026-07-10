import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { fileApi } from '../../api/fileApi';

const UserProfile = ({ currentUser }) => {
  const { userId } = useParams();
  const [user, setUser] = useState(null);
  const [repositories, setRepositories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [editData, setEditData] = useState({ username: '', email: '' });

  useEffect(() => {
    loadUserData();
  }, [userId]);

  const loadUserData = async () => {
    try {
      setLoading(true);
      const userData = await fileApi.getUser(userId);
      setUser(userData);
      setEditData({ username: userData.username, email: userData.email });
      
      const repos = await fileApi.getUserRepositories(userId);
      setRepositories(repos);
    } catch (error) {
      console.error('Error loading user:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdate = async () => {
    try {
      const updated = await fileApi.updateUser(userId, editData.username, editData.email);
      setUser(updated);
      setEditing(false);
    } catch (error) {
      alert('Error updating profile');
    }
  };

  if (loading) return <div className="loading">Loading...</div>;
  if (!user) return <div className="error">User not found</div>;

  const isOwner = currentUser?.id === user.id;

  return (
    <div className="user-profile">
      <div className="profile-header">
        <h1>{user.username}</h1>
        {isOwner && (
          <button onClick={() => setEditing(!editing)}>
            {editing ? 'Cancel' : 'Edit Profile'}
          </button>
        )}
      </div>

      {editing ? (
        <div className="edit-form">
          <div className="form-group">
            <label>Username</label>
            <input
              type="text"
              value={editData.username}
              onChange={(e) => setEditData({ ...editData, username: e.target.value })}
            />
          </div>
          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              value={editData.email}
              onChange={(e) => setEditData({ ...editData, email: e.target.value })}
            />
          </div>
          <button onClick={handleUpdate}>Save</button>
        </div>
      ) : (
        <div className="profile-info">
          <p><strong>Email:</strong> {user.email}</p>
          <p><strong>Joined:</strong> {new Date(user.createdAt).toLocaleDateString()}</p>
        </div>
      )}

      <div className="user-repositories">
        <h2>Repositories ({repositories.length})</h2>
        <div className="repo-grid">
          {repositories.map(repo => (
            <Link to={`/repo/${repo.id}`} key={repo.id} className="repo-card">
              <h3>{repo.name}</h3>
              <p>{repo.description || 'No description'}</p>
              <div className="repo-meta">
                <span>{repo.fileCount} files</span>
                <span>{repo.isPublic ? 'Public' : 'Private'}</span>
              </div>
            </Link>
          ))}
        </div>
      </div>
    </div>
  );
};

export default UserProfile;