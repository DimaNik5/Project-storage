import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fileApi } from '../../api/fileApi';

const CreateRepository = ({ userId }) => {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  if (!userId) {
    navigate('/');
    return null;
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!name.trim()) return;

    try {
      setLoading(true);
      const repo = await fileApi.createRepository(name, description, userId);
      navigate(`/repo/${repo.id}`);
    } catch (error) {
      alert('Error creating repository');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="create-repository">
      <h1>Create New Repository</h1>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Repository Name *</label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="my-awesome-project"
            required
          />
        </div>
        <div className="form-group">
          <label>Description</label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="A short description of your repository"
          />
        </div>
        <div className="form-actions">
          <button type="submit" disabled={loading}>
            {loading ? 'Creating...' : 'Create Repository'}
          </button>
          <button type="button" onClick={() => navigate('/')}>
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
};

export default CreateRepository;