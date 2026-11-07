import { useState } from 'react';
import { api } from '../api.js';
import { useToast } from '../components/Toast.jsx';
import { Empty, Field, Loading, useFetch } from '../components/ui.jsx';

const EMPTY = { name: '', industry: '', website: '', location: '', description: '' };

export default function Companies() {
  const toast = useToast();
  const [q, setQ] = useState('');
  const [form, setForm] = useState(null); // null = hidden, object = editing/creating
  const [editId, setEditId] = useState(null);
  const [errors, setErrors] = useState({});
  const { data, error, loading, reload } = useFetch(() => api('/companies', { params: { q } }), [q]);

  function openForm(c) {
    setErrors({});
    setEditId(c ? c.id : null);
    setForm(c ? { name: c.name, industry: c.industry || '', website: c.website || '', location: c.location || '', description: c.description || '' } : { ...EMPTY });
  }

  async function save(e) {
    e.preventDefault();
    if (!form.name.trim()) { setErrors({ name: 'Company name is required' }); return; }
    if (form.website && !/^https?:\/\/\S+$/.test(form.website)) { setErrors({ website: 'Website must start with http:// or https://' }); return; }
    try {
      await api(editId ? `/companies/${editId}` : '/companies', { method: editId ? 'PUT' : 'POST', body: form });
      toast(editId ? 'Company updated' : 'Company added');
      setForm(null); reload();
    } catch (err) { setErrors(err.errors || {}); toast(err.message, 'error'); }
  }

  async function remove(c) {
    if (!window.confirm(`Delete ${c.name}?`)) return;
    try { await api(`/companies/${c.id}`, { method: 'DELETE' }); toast('Company deleted'); reload(); }
    catch (err) { toast(err.message, 'error'); }
  }

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  return (
    <>
      <div className="page-head"><h1>Companies</h1><button className="btn btn-primary" onClick={() => openForm(null)}>+ Add company</button></div>
      {form && (
        <form className="card form" onSubmit={save} noValidate>
          <h2>{editId ? 'Edit company' : 'New company'}</h2>
          <div className="form-grid">
            <Field label="Name *" error={errors.name}><input value={form.name} onChange={set('name')} maxLength={120} /></Field>
            <Field label="Industry" error={errors.industry}><input value={form.industry} onChange={set('industry')} /></Field>
            <Field label="Website" error={errors.website}><input value={form.website} onChange={set('website')} placeholder="https://" /></Field>
            <Field label="Location" error={errors.location}><input value={form.location} onChange={set('location')} /></Field>
          </div>
          <Field label="Description" error={errors.description}><textarea rows={3} value={form.description} onChange={set('description')} maxLength={1000} /></Field>
          <div className="actions"><button className="btn btn-primary">Save</button><button type="button" className="btn" onClick={() => setForm(null)}>Cancel</button></div>
        </form>
      )}
      <div className="card filters"><input placeholder="Search by name or industry…" value={q} onChange={(e) => setQ(e.target.value)} /></div>
      {error && <div className="alert alert-error">{error}</div>}
      {loading ? <Loading /> : data.length === 0 ? <Empty text="No companies found." /> : (
        <div className="card table-wrap"><table className="table">
          <thead><tr><th>Name</th><th>Industry</th><th>Location</th><th>Website</th><th></th></tr></thead>
          <tbody>{data.map((c) => (
            <tr key={c.id}><td><b>{c.name}</b></td><td>{c.industry || '—'}</td><td>{c.location || '—'}</td>
              <td>{c.website ? <a href={c.website} target="_blank" rel="noreferrer">{c.website.replace(/^https?:\/\//, '')}</a> : '—'}</td>
              <td className="actions"><button className="btn btn-sm" onClick={() => openForm(c)}>Edit</button>
                <button className="btn btn-sm btn-danger" onClick={() => remove(c)}>Delete</button></td></tr>
          ))}</tbody>
        </table></div>
      )}
    </>
  );
}
