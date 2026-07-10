import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { fileApi } from '../../api/fileApi';
import './Repository.css';

const RepositoryList = ({ userId }) => {
  const [publicRepos, setPublicRepos] = useState([]);
  const [userRepos, setUserRepos] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadRepositories();
  }, [userId]);

  const loadRepositories = async () => {
    try {
      setLoading(true);
      const [publicData, userData] = await Promise.all([
        fileApi.getPublicRepositories(),
        userId ? fileApi.getUserRepositories(userId) : Promise.resolve([])
      ]);
      setPublicRepos(publicData);
      setUserRepos(userData);
    } catch (error) {
      console.error('Error loading repositories:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="loading">Loading...</div>;
  }

  return (
    <div className="repository-list">
      <div className="repository-header">
        <h1>Repositories</h1>
        {userId && (
          <button onClick={() => navigate('/create')}>
            New Repository
          </button>
        )}
      </div>

      {userId && userRepos.length > 0 && (
        <section>
          <h2>Your Repositories</h2>
          <div className="repo-grid">
            {userRepos.map(repo => (
              <Link to={`/repo/${repo.id}`} key={repo.id} className="repo-card">
                <h3>{repo.name}</h3>
                <p>{repo.description || 'No description'}</p>
                <div className="repo-meta">
                  <span>{repo.fileCount} files</span>
                  <span>{repo.collaboratorCount} collaborators</span>
                  <span>{repo.isPublic ? 'Public' : 'Private'}</span>
                </div>
              </Link>
            ))}
          </div>
        </section>
      )}

      <section>
        <h2>Public Repositories</h2>
        {publicRepos.length === 0 ? (
          <p className="no-repos">No public repositories yet</p>
        ) : (
          <div className="repo-grid">
            {publicRepos.map(repo => (
              <Link to={`/repo/${repo.id}`} key={repo.id} className="repo-card">
                <h3>{repo.name}</h3>
                <p>{repo.description || 'No description'}</p>
                <div className="repo-meta">
                  <span>{repo.ownerName}</span>
                  <span>{repo.fileCount} files</span>
                </div>
              </Link>
            ))}
          </div>
        )}
      </section>
    </div>
  );
};

export default RepositoryList;