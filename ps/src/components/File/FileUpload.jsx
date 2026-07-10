import React, { useState, useRef } from 'react';
import { fileApi } from '../../api/fileApi';

const FileUpload = ({ repoId, userId, onUpload }) => {
  const [uploading, setUploading] = useState(false);
  const [commitMessage, setCommitMessage] = useState('');
  const fileInputRef = useRef(null);

  const handleUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    try {
      setUploading(true);
      await fileApi.uploadFile(repoId, file, userId, commitMessage);
      setCommitMessage('');
      fileInputRef.current.value = '';
      onUpload();
      alert('File uploaded successfully!');
    } catch (error) {
      alert('Error uploading file');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="file-upload">
      <h3>Upload File</h3>
      <input
        type="text"
        placeholder="Commit message (optional)"
        value={commitMessage}
        onChange={(e) => setCommitMessage(e.target.value)}
      />
      <input
        type="file"
        ref={fileInputRef}
        onChange={handleUpload}
        disabled={uploading}
      />
      {uploading && <span>Uploading...</span>}
    </div>
  );
};

export default FileUpload;