import { useState } from 'react';
import { adminAPI, auditAPI } from '../../services/api';

export default function AdminDashboard() {
  // Deposit form
  const [depositWalletId, setDepositWalletId] = useState('');
  const [depositAmount, setDepositAmount] = useState('');
  const [depositLoading, setDepositLoading] = useState(false);
  const [depositMessage, setDepositMessage] = useState('');
  const [depositError, setDepositError] = useState('');

  // Audit lookup
  const [auditUserId, setAuditUserId] = useState('');
  const [auditTxnId, setAuditTxnId] = useState('');
  const [auditResults, setAuditResults] = useState<unknown[]>([]);
  const [auditLoading, setAuditLoading] = useState(false);
  const [auditError, setAuditError] = useState('');

  const handleDeposit = async () => {
    setDepositMessage('');
    setDepositError('');

    if (!depositWalletId || !depositAmount) {
      setDepositError('Wallet ID and amount are required.');
      return;
    }

    setDepositLoading(true);
    try {
      const res = await adminAPI.deposit(depositWalletId, parseFloat(depositAmount));
      setDepositMessage(res.data.message || 'Deposit successful.');
      setDepositWalletId('');
      setDepositAmount('');
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosErr = err as { response?: { data?: { message?: string } } };
        setDepositError(axiosErr.response?.data?.message || 'Deposit failed.');
      } else {
        setDepositError('Unable to process deposit.');
      }
    } finally {
      setDepositLoading(false);
    }
  };

  const handleAuditByUser = async () => {
    if (!auditUserId) return;
    setAuditError('');
    setAuditLoading(true);
    try {
      const res = await auditAPI.getByUser(auditUserId);
      setAuditResults(res.data);
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosErr = err as { response?: { data?: { message?: string } } };
        setAuditError(axiosErr.response?.data?.message || 'Failed to fetch audit logs.');
      } else {
        setAuditError('Failed to fetch audit logs.');
      }
    } finally {
      setAuditLoading(false);
    }
  };

  const handleAuditByTxn = async () => {
    if (!auditTxnId) return;
    setAuditError('');
    setAuditLoading(true);
    try {
      const res = await auditAPI.getByTransaction(auditTxnId);
      setAuditResults(res.data);
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosErr = err as { response?: { data?: { message?: string } } };
        setAuditError(axiosErr.response?.data?.message || 'Failed to fetch audit logs.');
      } else {
        setAuditError('Failed to fetch audit logs.');
      }
    } finally {
      setAuditLoading(false);
    }
  };

  return (
    <>
      <div className="page-header">
        <h1 className="page-header__title">Admin Control Panel</h1>
        <p className="page-header__subtitle">
          Manage wallets, deposits, and audit trails
        </p>
      </div>

      {/* Admin Deposit Section */}
      <div className="card" style={{ marginBottom: '1.5rem' }}>
        <h3 style={{ marginBottom: '1rem', fontWeight: 700, fontSize: '1rem' }}>
          💰 Manual Deposit
        </h3>

        {depositError && <div className="alert alert--error">⚠ {depositError}</div>}
        {depositMessage && <div className="alert alert--success">✓ {depositMessage}</div>}

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr auto', gap: '0.75rem', alignItems: 'end' }}>
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label className="form-label" htmlFor="deposit-wallet-id">Wallet ID</label>
            <input
              id="deposit-wallet-id"
              className="form-input"
              type="text"
              placeholder="Paste wallet UUID"
              value={depositWalletId}
              onChange={(e) => setDepositWalletId(e.target.value)}
            />
          </div>
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label className="form-label" htmlFor="deposit-amount">Amount</label>
            <input
              id="deposit-amount"
              className="form-input"
              type="number"
              step="0.01"
              min="0.01"
              placeholder="0.00"
              value={depositAmount}
              onChange={(e) => setDepositAmount(e.target.value)}
            />
          </div>
          <button
            id="confirm-deposit"
            className="btn btn--primary"
            onClick={handleDeposit}
            disabled={depositLoading}
          >
            {depositLoading ? <span className="spinner" /> : 'Deposit'}
          </button>
        </div>
      </div>

      {/* Audit Logs Section */}
      <div className="card">
        <h3 style={{ marginBottom: '1rem', fontWeight: 700, fontSize: '1rem' }}>
          📋 Audit Log Lookup
        </h3>

        {auditError && <div className="alert alert--error">⚠ {auditError}</div>}

        <div style={{ display: 'grid', gridTemplateColumns: '1fr auto', gap: '0.75rem', alignItems: 'end', marginBottom: '1rem' }}>
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label className="form-label" htmlFor="audit-user-id">By User ID</label>
            <input
              id="audit-user-id"
              className="form-input"
              type="text"
              placeholder="User UUID"
              value={auditUserId}
              onChange={(e) => setAuditUserId(e.target.value)}
            />
          </div>
          <button
            id="audit-by-user-btn"
            className="btn btn--ghost"
            onClick={handleAuditByUser}
            disabled={auditLoading || !auditUserId}
          >
            Search
          </button>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr auto', gap: '0.75rem', alignItems: 'end', marginBottom: '1.5rem' }}>
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label className="form-label" htmlFor="audit-txn-id">By Transaction ID</label>
            <input
              id="audit-txn-id"
              className="form-input"
              type="text"
              placeholder="Transaction UUID"
              value={auditTxnId}
              onChange={(e) => setAuditTxnId(e.target.value)}
            />
          </div>
          <button
            id="audit-by-txn-btn"
            className="btn btn--ghost"
            onClick={handleAuditByTxn}
            disabled={auditLoading || !auditTxnId}
          >
            Search
          </button>
        </div>

        {/* Results Table */}
        {auditLoading ? (
          <div className="loading-state">
            <div className="spinner spinner--lg" />
            <span>Searching audit logs...</span>
          </div>
        ) : auditResults.length > 0 ? (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Action</th>
                  <th>Entity Type</th>
                  <th>Details</th>
                  <th>Timestamp</th>
                </tr>
              </thead>
              <tbody>
                {auditResults.map((log: unknown, i: number) => {
                  const entry = log as {
                    id?: string;
                    action?: string;
                    entityType?: string;
                    details?: string;
                    createdAt?: string;
                  };
                  return (
                    <tr key={entry.id || i}>
                      <td>
                        <span className="badge badge--info">{entry.action}</span>
                      </td>
                      <td>{entry.entityType}</td>
                      <td style={{ maxWidth: '300px', wordBreak: 'break-all', fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                        {entry.details || '—'}
                      </td>
                      <td style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                        {entry.createdAt ? new Date(entry.createdAt).toLocaleString() : '—'}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="empty-state">
            <div className="empty-state__icon">📋</div>
            <p className="empty-state__text">Search by User ID or Transaction ID to view audit logs</p>
          </div>
        )}
      </div>
    </>
  );
}
