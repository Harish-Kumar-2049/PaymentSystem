import { useState, useEffect } from 'react';
import { walletAPI, transactionAPI } from '../../services/api';
import { useAuth } from '../../context/AuthContext';

interface Wallet {
  id: string;
  balance: number;
  currency: string;
  status: string;
  createdAt: string;
}

interface TransferForm {
  sourceWalletId: string;
  targetWalletId: string;
  amount: string;
}

export default function UserDashboard() {
  const { user } = useAuth();
  const [wallets, setWallets] = useState<Wallet[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Transfer modal
  const [showTransfer, setShowTransfer] = useState(false);
  const [transferForm, setTransferForm] = useState<TransferForm>({
    sourceWalletId: '',
    targetWalletId: '',
    amount: '',
  });
  const [transferLoading, setTransferLoading] = useState(false);
  const [transferError, setTransferError] = useState('');
  const [transferSuccess, setTransferSuccess] = useState('');

  // Create wallet
  const [showCreateWallet, setShowCreateWallet] = useState(false);
  const [newCurrency, setNewCurrency] = useState('INR');
  const [createLoading, setCreateLoading] = useState(false);

  const fetchWallets = async () => {
    try {
      setLoading(true);
      const res = await walletAPI.getMyWallets();
      setWallets(res.data);
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'message' in err) {
        setError((err as { message: string }).message);
      } else {
        setError('Failed to load wallets.');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWallets();
  }, []);

  const handleCreateWallet = async () => {
    setCreateLoading(true);
    try {
      await walletAPI.create(newCurrency);
      setShowCreateWallet(false);
      setNewCurrency('INR');
      fetchWallets();
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosErr = err as { response?: { data?: { message?: string } } };
        setError(axiosErr.response?.data?.message || 'Failed to create wallet.');
      } else {
        setError('Failed to create wallet.');
      }
    } finally {
      setCreateLoading(false);
    }
  };

  const handleTransfer = async () => {
    setTransferError('');
    setTransferSuccess('');

    if (!transferForm.sourceWalletId || !transferForm.targetWalletId || !transferForm.amount) {
      setTransferError('All fields are required.');
      return;
    }

    if (transferForm.sourceWalletId === transferForm.targetWalletId) {
      setTransferError('Source and target wallets must be different.');
      return;
    }

    setTransferLoading(true);
    try {
      const idempotencyKey = crypto.randomUUID();
      await transactionAPI.transfer({
        sourceWalletId: transferForm.sourceWalletId,
        targetWalletId: transferForm.targetWalletId,
        amount: parseFloat(transferForm.amount),
        idempotencyKey,
      });
      setTransferSuccess('Transfer completed successfully!');
      setTransferForm({ sourceWalletId: '', targetWalletId: '', amount: '' });
      fetchWallets();
      setTimeout(() => {
        setShowTransfer(false);
        setTransferSuccess('');
      }, 1500);
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosErr = err as { response?: { data?: { message?: string } } };
        setTransferError(axiosErr.response?.data?.message || 'Transfer failed.');
      } else {
        setTransferError('Transfer failed. Check your connection.');
      }
    } finally {
      setTransferLoading(false);
    }
  };

  const formatCurrency = (amount: number, currency: string) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: currency || 'INR',
      minimumFractionDigits: 2,
    }).format(amount);
  };

  const totalBalance = wallets.reduce((sum, w) => sum + w.balance, 0);

  return (
    <>
      <div className="page-header">
        <h1 className="page-header__title">Dashboard</h1>
        <p className="page-header__subtitle">
          Welcome back, {user?.email}
        </p>
      </div>

      {error && <div className="alert alert--error">⚠ {error}</div>}

      {/* Stats */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-card__icon stat-card__icon--primary">💰</div>
          <div className="stat-card__label">Total Balance</div>
          <div className="stat-card__value">{formatCurrency(totalBalance, 'INR')}</div>
        </div>
        <div className="stat-card">
          <div className="stat-card__icon stat-card__icon--success">👛</div>
          <div className="stat-card__label">Active Wallets</div>
          <div className="stat-card__value">{wallets.filter(w => w.status === 'ACTIVE').length}</div>
        </div>
      </div>

      {/* Actions */}
      <div style={{ display: 'flex', gap: '0.75rem', marginBottom: '1.5rem' }}>
        <button
          id="create-wallet-btn"
          className="btn btn--primary"
          onClick={() => setShowCreateWallet(true)}
        >
          + New Wallet
        </button>
        <button
          id="transfer-btn"
          className="btn btn--ghost"
          onClick={() => setShowTransfer(true)}
          disabled={wallets.length === 0}
        >
          Send Money
        </button>
      </div>

      {/* Wallet Grid */}
      {loading ? (
        <div className="loading-state">
          <div className="spinner spinner--lg" />
          <span>Loading wallets...</span>
        </div>
      ) : wallets.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state__icon">👛</div>
          <p className="empty-state__text">No wallets yet. Create your first wallet to get started!</p>
        </div>
      ) : (
        <div className="wallet-grid">
          {wallets.map((wallet) => (
            <div className="wallet-card" key={wallet.id}>
              <span className="wallet-card__currency">{wallet.currency}</span>
              <div className="wallet-card__balance">
                {formatCurrency(wallet.balance, wallet.currency)}
              </div>
              <div className="wallet-card__id">ID: {wallet.id}</div>
              <span className={`wallet-card__status wallet-card__status--${wallet.status.toLowerCase()}`}>
                {wallet.status}
              </span>
            </div>
          ))}
        </div>
      )}

      {/* Create Wallet Modal */}
      {showCreateWallet && (
        <div className="modal-overlay" onClick={() => setShowCreateWallet(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal__header">
              <h2 className="modal__title">Create New Wallet</h2>
              <button className="modal__close" onClick={() => setShowCreateWallet(false)}>✕</button>
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="wallet-currency">Currency</label>
              <select
                id="wallet-currency"
                className="form-select"
                value={newCurrency}
                onChange={(e) => setNewCurrency(e.target.value)}
              >
                <option value="INR">INR – Indian Rupee</option>
                <option value="USD">USD – US Dollar</option>
                <option value="EUR">EUR – Euro</option>
                <option value="GBP">GBP – British Pound</option>
              </select>
            </div>
            <button
              id="confirm-create-wallet"
              className="btn btn--primary btn--full"
              onClick={handleCreateWallet}
              disabled={createLoading}
            >
              {createLoading ? <span className="spinner" /> : 'Create Wallet'}
            </button>
          </div>
        </div>
      )}

      {/* Transfer Modal */}
      {showTransfer && (
        <div className="modal-overlay" onClick={() => setShowTransfer(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal__header">
              <h2 className="modal__title">Send Money</h2>
              <button className="modal__close" onClick={() => { setShowTransfer(false); setTransferError(''); setTransferSuccess(''); }}>✕</button>
            </div>

            {transferError && <div className="alert alert--error">⚠ {transferError}</div>}
            {transferSuccess && <div className="alert alert--success">✓ {transferSuccess}</div>}

            <div className="form-group">
              <label className="form-label" htmlFor="transfer-source">From Wallet</label>
              <select
                id="transfer-source"
                className="form-select"
                value={transferForm.sourceWalletId}
                onChange={(e) => setTransferForm({ ...transferForm, sourceWalletId: e.target.value })}
              >
                <option value="">Select source wallet</option>
                {wallets.filter(w => w.status === 'ACTIVE').map(w => (
                  <option key={w.id} value={w.id}>
                    {w.currency} – {formatCurrency(w.balance, w.currency)}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="transfer-target">To Wallet ID</label>
              <input
                id="transfer-target"
                className="form-input"
                type="text"
                placeholder="Paste recipient wallet ID"
                value={transferForm.targetWalletId}
                onChange={(e) => setTransferForm({ ...transferForm, targetWalletId: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="transfer-amount">Amount</label>
              <input
                id="transfer-amount"
                className="form-input"
                type="number"
                step="0.01"
                min="0.01"
                placeholder="0.00"
                value={transferForm.amount}
                onChange={(e) => setTransferForm({ ...transferForm, amount: e.target.value })}
              />
            </div>

            <button
              id="confirm-transfer"
              className="btn btn--primary btn--full"
              onClick={handleTransfer}
              disabled={transferLoading}
            >
              {transferLoading ? <span className="spinner" /> : 'Send'}
            </button>
          </div>
        </div>
      )}
    </>
  );
}
