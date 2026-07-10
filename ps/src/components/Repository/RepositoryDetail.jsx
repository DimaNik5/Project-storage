import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { fileApi } from '../../api/fileApi';
import FileList from '../File/FileList';
import CommentSection from '../Comment/CommentSection';
import './Repository.css';

const RepositoryDetail = ({ userId }) => {
  const { repoId } = useParams();
  const [repo, setRepo] = useState(null);
  const [canWrite, setCanWrite] = useState(false);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('files');

  useEffect(() => {
    loadRepoData();
  }, [repoId]);

  const loadRepoData = async () => {
    try {
      setLoading(true);
      const repoData = await fileApi.getRepository(repoId);
      setRepo(repoData);
      
      if (userId) {
        const permData = await fileApi.checkWritePermission(repoId, userId);
        setCanWrite(permData.canWrite);
      }
    } catch (error) {
      console.error('Error loading repository:', error);
      alert('Repository not found or access denied');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="loading">Loading...</div>;
  if (!repo) return <div className="error">Repository not found</div>;

  return (
    <div className="repository-detail">
      <div className="repo-info-header">
        <h1>{repo.name}</h1>
        <p className="repo-description">{repo.description || 'No description'}</p>
        <div className="repo-stats">
          <span>Owner: {repo.ownerName}</span>
          <span>Files: {repo.fileCount}</span>
          <span>Collaborators: {repo.collaboratorCount}</span>
        </div>
      </div>

      <div className="tabs">
        <button 
          className={activeTab === 'files' ? 'active' : ''} 
          onClick={() => setActiveTab('files')}
        >
          Files
        </button>
        <button 
          className={activeTab === 'comments' ? 'active' : ''} 
          onClick={() => setActiveTab('comments')}
        >
          Comments
        </button>
      </div>

      <div className="tab-content">
        {activeTab === 'files' && (
          <FileList 
            repoId={repoId} 
            userId={userId} 
            canWrite={canWrite} 
          />
        )}
        {activeTab === 'comments' && (
          <CommentSection 
            repoId={repoId} 
            userId={userId} 
          />
        )}
      </div>
    </div>
  );
};

export default RepositoryDetail;