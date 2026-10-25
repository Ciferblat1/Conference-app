
import { Routes, Route } from 'react-router';

import './App.css'

import DirectionsPage from './pages/DirectionsPage'
import RegisterPage from './pages/RegisterPage';
import LoginPage from './pages/LoginPage';
import RequireAuth from './components/RequireAuth';

function App() {

  return (
    <Routes>
      <Route path="/register" element={<RegisterPage/>}/>
      <Route path="/login" element={<LoginPage/>}/>
      <Route element={<RequireAuth/>}>
        <Route path="/" element={<DirectionsPage/>}/>
      </Route>
    </Routes>
  )
}

export default App
