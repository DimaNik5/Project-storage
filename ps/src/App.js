import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import RepositoryList from './components/Repository/RepositoryList';
import RepositoryDetail from './components/Repository/RepositoryDetail';
import CreateRepository from './components/Repository/CreateRepository';
import UserProfile from './components/User/UserProfile';
import './App.css';

const App = () => {
  const [currentUser, setCurrentUser] = useState(() => {
    // Загружаем пользователя из localStorage при старте
    const saved = localStorage.getItem('currentUser');
    return saved ? JSON.parse(saved) : null;
  });
  
  const [showLogin, setShowLogin] = useState(false);
  const [loginData, setLoginData] = useState({ username: '', email: '' });
  const [loginError, setLoginError] = useState('');
  const [logging, setLogging] = useState(false);

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoginError('');
    
    if (!loginData.username.trim() || !loginData.email.trim()) {
      setLoginError('Please fill in all fields');
      return;
    }

    try {
      setLogging(true);
      const { fileApi } = await import('./api/fileApi');
      
      // Создаем нового пользователя или входим существующим
      const user = await fileApi.createOrLoginUser(
        loginData.username, 
        loginData.email
      );
      
      setCurrentUser(user);
      localStorage.setItem('currentUser', JSON.stringify(user));
      setShowLogin(false);
      setLoginData({ username: '', email: '' });
      
    } catch (error) {
      console.error('Login error:', error);
      const errorMsg = error.response?.data?.error || 'Error logging in. Please try again.';
      setLoginError(errorMsg);
    } finally {
      setLogging(false);
    }
  };

  const handleLogout = () => {
    setCurrentUser(null);
    localStorage.removeItem('currentUser');
  };

  return (
    <Router>
      <div className="app">
        <header className="app-header">
          <div className="header-left">
            <Link to="/" className="logo">Project Storage</Link>
          </div>
          <nav className="header-nav">
            {currentUser ? (
              <div className="user-nav">
                <Link to="/" className="nav-link">Repositories</Link>
                <Link to={`/profile/${currentUser.id}`} className="nav-link">
                  {currentUser.username}
                </Link>
                <button onClick={handleLogout} className="logout-btn">
                  Logout
                </button>
              </div>
            ) : (
              <button onClick={() => setShowLogin(true)} className="login-btn">
                Sign In
              </button>
            )}
          </nav>
        </header>

        {showLogin && !currentUser && (
          <div className="modal-overlay" onClick={() => setShowLogin(false)}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
              <div className="modal-header">
                <h2>Sign in to Project Storage</h2>
                <button className="close-btn" onClick={() => setShowLogin(false)}>✕</button>
              </div>
              
              <form onSubmit={handleLogin}>
                {loginError && (
                  <div className="error-message">{loginError}</div>
                )}
                
                <div className="form-group">
                  <label htmlFor="username">Username</label>
                  <input
                    id="username"
                    type="text"
                    placeholder="Choose a username"
                    value={loginData.username}
                    onChange={(e) => setLoginData({ ...loginData, username: e.target.value })}
                    required
                  />
                </div>
                
                <div className="form-group">
                  <label htmlFor="email">Email</label>
                  <input
                    id="email"
                    type="email"
                    placeholder="your@email.com"
                    value={loginData.email}
                    onChange={(e) => setLoginData({ ...loginData, email: e.target.value })}
                    required
                  />
                </div>
                
                <button 
                  type="submit" 
                  className="submit-btn"
                  disabled={logging}
                >
                  {logging ? 'Signing in...' : 'Sign in'}
                </button>
                
                <p className="modal-note">
                  New users will be automatically registered
                </p>
              </form>
            </div>
          </div>
        )}

        <main className="main-content">
          <Routes>
            <Route 
              path="/" 
              element={<RepositoryList userId={currentUser?.id} />} 
            />
            <Route 
              path="/repo/:repoId" 
              element={<RepositoryDetail userId={currentUser?.id} />} 
            />
            <Route 
              path="/create" 
              element={<CreateRepository userId={currentUser?.id} />} 
            />
            <Route 
              path="/profile/:userId" 
              element={<UserProfile currentUser={currentUser} />} 
            />
          </Routes>
        </main>
      </div>
    </Router>
  );
};

export default App;