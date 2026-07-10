import React, { useState, useEffect } from 'react';
import { fileApi } from '../../api/fileApi';
import FileUpload from './FileUpload';
import './File.css';

const FileList = ({ repoId, userId, canWrite }) => {
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadFiles();
  }, [repoId]);

  const loadFiles = async () => {
    try {
      setLoading(true);
      const filesData = await fileApi.getRepositoryFiles(repoId);
      setFiles(filesData);
    } catch (error) {
      console.error('Error loading files:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = async (fileId, fileName) => {
    try {
      await fileApi.downloadFile(fileId, fileName);
    } catch (error) {
      alert('Error downloading file');
    }
  };

  const handleDownloadAll = async () => {
    try {
      await fileApi.downloadRepositoryAsZip(repoId);
    } catch (error) {
      alert('Error downloading files');
    }
  };

  const handleDelete = async (fileId) => {
    if (!window.confirm('Delete this file?')) return;
    
    try {
      await fileApi.deleteFile(fileId, userId);
      loadFiles();
    } catch (error) {
      alert('Error deleting file');
    }
  };

  const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  if (loading) return <div className="loading">Loading files...</div>;

  return (
    <div className="file-list">
      <div className="file-actions">
        <button onClick={handleDownloadAll} disabled={files.length === 0}>
          Download All (ZIP)
        </button>
      </div>

      {canWrite && (
        <FileUpload repoId={repoId} userId={userId} onUpload={loadFiles} />
      )}

      {files.length === 0 ? (
        <p className="no-files">No files in this repository</p>
      ) : (
        <div className="files-table">
          <div className="file-header">
            <span>Name</span>
            <span>Type</span>
            <span>Size</span>
            <span>Actions</span>
          </div>
          {files.map(file => (
            <div key={file.id} className="file-row">
              <span>{file.originalFileName}</span>
              <span>{file.fileType}</span>
              <span>{formatFileSize(file.fileSize)}</span>
              <span className="file-actions">
                <button onClick={() => handleDownload(file.id, file.originalFileName)}>
                  Download
                </button>
                {canWrite && (
                  <button onClick={() => handleDelete(file.id)} className="delete-btn">
                    Delete
                  </button>
                )}
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default FileList;