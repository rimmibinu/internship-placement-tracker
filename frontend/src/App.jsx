import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './auth.jsx';
import Layout from './components/Layout.jsx';
import Login from './pages/Login.jsx';
import Dashboard from './pages/Dashboard.jsx';
import Opportunities from './pages/Opportunities.jsx';
import OpportunityDetail from './pages/OpportunityDetail.jsx';
import OpportunityForm from './pages/OpportunityForm.jsx';
import Companies from './pages/Companies.jsx';
import Students from './pages/Students.jsx';
import Applications from './pages/Applications.jsx';
import Profile from './pages/Profile.jsx';

function Protected({ children, adminOnly }) {
  const { user, isAdmin } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (adminOnly && !isAdmin) return <Navigate to="/" replace />;
  return children;
}

export default function App() {
  const { user } = useAuth();
  return (
    <Routes>
      <Route path="/login" element={user ? <Navigate to="/" replace /> : <Login />} />
      <Route element={<Protected><Layout /></Protected>}>
        <Route index element={<Dashboard />} />
        <Route path="opportunities" element={<Opportunities />} />
        <Route path="opportunities/new" element={<Protected adminOnly><OpportunityForm /></Protected>} />
        <Route path="opportunities/:id" element={<OpportunityDetail />} />
        <Route path="opportunities/:id/edit" element={<Protected adminOnly><OpportunityForm /></Protected>} />
        <Route path="applications" element={<Applications />} />
        <Route path="companies" element={<Protected adminOnly><Companies /></Protected>} />
        <Route path="students" element={<Protected adminOnly><Students /></Protected>} />
        <Route path="profile" element={<Profile />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
