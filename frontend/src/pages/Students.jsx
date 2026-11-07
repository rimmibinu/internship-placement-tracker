import { useState } from 'react';
import { api } from '../api.js';
import { useToast } from '../components/Toast.jsx';
import { DEPARTMENTS, Empty, Field, Loading, useFetch } from '../components/ui.jsx';

const EMPTY = { name: '', email: '', rollNo: '', department: 'CSE', cgpa: '', backlogs: '0', graduationYear: String(new Date().getFullYear() + 1), skills: '' };

function validate(f) {
  const e = {};
  if (!f.name.trim()) e.name = 'Name is required';
  if (!/^\S+@\S+\.\S+$/.test(f.email)) e.email = 'Enter a valid email';
  if (!f.rollNo.trim()) e.rollNo = 'Roll number is required';
  if (f.cgpa === '' || Number(f.cgpa) < 0 || Number(f.cgpa) > 10) e.cgpa = 'CGPA must be between 0 and 10';
  if (f.backlogs === '' || Number(f.backlogs) < 0 || !Number.isInteger(Number(f.backlogs))) e.backlogs = 'Enter 0 or a positive whole number';
  if (!(Number(f.graduationYear) >= 2020 && Number(f.graduationYear) <= 2040)) e.graduationYear = 'Year must be between 2020 and 2040';
  return e;
}

export default function Students() {
  const toast = useToast();
  const [q, setQ] = useState('');
  const [dept, setDept] = useState('');
  const [form, setForm] = useState(null);
  const [editId, setEditId] = useState(null);
  const [errors, setErrors] = useState({});
  const { data, error, loading, reload } = useFetch(() => api('/students', { params: { q, department: dept } }), [q, dept]);

  function openForm(s) {
    setErrors({}); setEditId(s ? s.id : null);
    setForm(s ? { name: s.name, email: s.email, rollNo: s.rollNo, department: s.department, cgpa: String(s.cgpa), backlogs: String(s.backlogs), graduationYear: String(s.graduationYear), skills: s.skills || '' } : { ...EMPTY });
  }

  async function save(e) {
    e.preventDefault();
    const v = validate(form); setErrors(v);
    if (Object.keys(v).length) return;
    const body = { ...form, cgpa: Number(form.cgpa), backlogs: Number(form.backlogs), graduationYear: Number(form.graduationYear) };
    try {
      await api(editId ? `/students/${editId}` : '/students', { method: editId ? 'PUT' : 'POST', body });
      toast(editId ? 'Student updated' : 'Student added'); setForm(null); reload();
    } catch (err) { setErrors(err.errors || {}); toast(err.message, 'error'); }
  }

  async function remove(s) {
    if (!window.confirm(`Delete ${s.name}?`)) return;
    try { await api(`/students/${s.id}`, { method: 'DELETE' }); toast('Student deleted'); reload(); }
    catch (err) { toast(err.message, 'error'); }
  }

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  return (
    <>
      <div className="page-head"><h1>Students</h1><button className="btn btn-primary" onClick={() => openForm(null)}>+ Add student</button></div>
      <p className="hint">Use synthetic or authorised demo data only.</p>
      {form && (
        <form className="card form" onSubmit={save} noValidate>
          <h2>{editId ? 'Edit student' : 'New student'}</h2>
          <div className="form-grid">
            <Field label="Name *" error={errors.name}><input value={form.name} onChange={set('name')} /></Field>
            <Field label="Email *" error={errors.email}><input type="email" value={form.email} onChange={set('email')} /></Field>
            <Field label="Roll number *" error={errors.rollNo}><input value={form.rollNo} onChange={set('rollNo')} /></Field>
            <Field label="Department *" error={errors.department}>
              <select value={form.department} onChange={set('department')}>{DEPARTMENTS.map((d) => <option key={d}>{d}</option>)}</select>
            </Field>
            <Field label="CGPA *" error={errors.cgpa}><input type="number" min="0" max="10" step="0.01" value={form.cgpa} onChange={set('cgpa')} /></Field>
            <Field label="Active backlogs *" error={errors.backlogs}><input type="number" min="0" value={form.backlogs} onChange={set('backlogs')} /></Field>
            <Field label="Graduation year *" error={errors.graduationYear}><input type="number" value={form.graduationYear} onChange={set('graduationYear')} /></Field>
            <Field label="Skills" error={errors.skills}><input value={form.skills} onChange={set('skills')} placeholder="Java, React, SQL" /></Field>
          </div>
          <div className="actions"><button className="btn btn-primary">Save</button><button type="button" className="btn" onClick={() => setForm(null)}>Cancel</button></div>
        </form>
      )}
      <div className="card filters">
        <input placeholder="Search name, roll no or email…" value={q} onChange={(e) => setQ(e.target.value)} />
        <select value={dept} onChange={(e) => setDept(e.target.value)}><option value="">All departments</option>{DEPARTMENTS.map((d) => <option key={d}>{d}</option>)}</select>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      {loading ? <Loading /> : data.length === 0 ? <Empty text="No students found." /> : (
        <div className="card table-wrap"><table className="table">
          <thead><tr><th>Name</th><th>Roll no</th><th>Dept</th><th className="num">CGPA</th><th className="num">Backlogs</th><th>Skills</th><th></th></tr></thead>
          <tbody>{data.map((s) => (
            <tr key={s.id}><td><b>{s.name}</b><div className="hint">{s.email}</div></td><td>{s.rollNo}</td><td>{s.department}</td>
              <td className="num">{s.cgpa}</td><td className="num">{s.backlogs}</td><td>{s.skills || '—'}</td>
              <td className="actions"><button className="btn btn-sm" onClick={() => openForm(s)}>Edit</button>
                <button className="btn btn-sm btn-danger" onClick={() => remove(s)}>Delete</button></td></tr>
          ))}</tbody>
        </table></div>
      )}
    </>
  );
}
