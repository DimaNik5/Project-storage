import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const fileApi = {
  createOrLoginUser: async (username, email) => {
    try {
      const response = await api.post('/users', { 
        username: username, 
        email: email 
      });
      return response.data;
    } catch (error) {
      console.error('Create/Login error:', error);
      throw error;
    }
  },

  getUser: async (userId) => {
    const response = await api.get(`/users/${userId}`);
    return response.data;
  },

  getUserByUsername: async (username) => {
    const response = await api.get(`/users/by-username/${username}`);
    return response.data;
  },

  getAllUsers: async () => {
    const response = await api.get('/users');
    return response.data;
  },

  updateUser: async (userId, username, email) => {
    const params = new URLSearchParams();
    if (username) params.append('username', username);
    if (email) params.append('email', email);
    
    const response = await api.put(`/users/${userId}`, params, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    });
    return response.data;
  },

  deleteUser: async (userId) => {
    const response = await api.delete(`/users/${userId}`);
    return response.data;
  },

  getPublicRepositories: async () => {
    const response = await api.get('/repositories/public');
    return response.data;
  },

  getUserRepositories: async (userId) => {
    const response = await api.get(`/repositories/user/${userId}`);
    return response.data;
  },

  getRepository: async (repoId) => {
    const response = await api.get(`/repositories/${repoId}`);
    return response.data;
  },

  checkWritePermission: async (repoId, userId) => {
    const response = await api.get(`/repositories/${repoId}/permissions/${userId}`);
    return response.data;
  },

  createRepository: async (name, description, userId) => {
    const params = new URLSearchParams();
    params.append('name', name);
    params.append('description', description);
    params.append('userId', userId);
    
    const response = await api.post('/repositories', params, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    });
    return response.data;
  },

  addCollaborator: async (repoId, userId, canWrite, requestedBy) => {
    const params = new URLSearchParams();
    params.append('userId', userId);
    params.append('canWrite', canWrite);
    params.append('requestedBy', requestedBy);
    
    const response = await api.post(`/repositories/${repoId}/collaborators`, params, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    });
    return response.data;
  },

  getRepositoryFiles: async (repoId) => {
    const response = await api.get(`/files/repository/${repoId}`);
    return response.data;
  },

  uploadFile: async (repoId, file, userId, commitMessage) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('userId', userId);
    if (commitMessage) {
      formData.append('commitMessage', commitMessage);
    }
    
    const response = await api.post(`/files/upload/${repoId}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  downloadFile: async (fileId, fileName) => {
    const response = await api.get(`/files/download/${fileId}`, {
      responseType: 'blob',
    });
    
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', fileName);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },

  downloadRepositoryAsZip: async (repoId) => {
    const response = await api.get(`/files/download/repository/${repoId}`, {
      responseType: 'blob',
    });
    
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `repository-${repoId}.zip`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },

  deleteFile: async (fileId, userId) => {
    await api.delete(`/files/${fileId}`, {
      params: { userId: userId }
    });
  },

  getRepositoryComments: async (repoId) => {
    const response = await api.get(`/comments/repository/${repoId}`);
    return response.data;
},

getComment: async (commentId) => {
    const response = await api.get(`/comments/${commentId}`);
    return response.data;
},

addComment: async (repoId, userId, content) => {
    const params = new URLSearchParams();
    params.append('userId', userId);
    params.append('content', content);
    
    const response = await api.post(`/comments/repository/${repoId}`, params, {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    });
    return response.data;
},

updateComment: async (commentId, userId, content) => {
    const params = new URLSearchParams();
    params.append('userId', userId);
    params.append('content', content);
    
    const response = await api.put(`/comments/${commentId}`, params, {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    });
    return response.data;
},

deleteComment: async (commentId, userId) => {
    const response = await api.delete(`/comments/${commentId}`, {
        params: { userId: userId }
    });
    return response.data;
},

getUserComments: async (userId) => {
    const response = await api.get(`/comments/user/${userId}`);
    return response.data;
},

getCommentCount: async (repoId) => {
    const response = await api.get(`/comments/count/${repoId}`);
    return response.data;
},
};