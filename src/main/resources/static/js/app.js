/* ════════════════════════════════════════════════════════════════════
   SpendWise v2.0 – Frontend App Logic
   All API calls → Spring Boot on http://localhost:8080
   ════════════════════════════════════════════════════════════════════ */

'use strict';

// ── State ─────────────────────────────────────────────────────────────
let state = {
  subscriptions: [],
  settings: { budget_limit: '5000', home_currency: 'INR' },
  currencies: [],
  chart: null,
  currentSection: 'dashboard'
};

// ── Currency symbol map ────────────────────────────────────────────────
const CURRENCY_SYMBOLS = {
  INR:'₹', USD:'$', EUR:'€', GBP:'£', JPY:'¥', CNY:'¥',
  AUD:'A$', CAD:'CA$', CHF:'Fr', KRW:'₩', SGD:'S$', HKD:'HK$',
  MYR:'RM', THB:'฿', IDR:'Rp', PHP:'₱', BRL:'R$', MXN:'$',
  ZAR:'R', TRY:'₺', SEK:'kr', NOK:'kr', DKK:'kr', NZD:'NZ$',
  HUF:'Ft', CZK:'Kč', PLN:'zł', RON:'lei', BGN:'лв',
  ILS:'₪', ISK:'kr'
};

function sym(c) { return CURRENCY_SYMBOLS[c] || c + ' '; }

// ── Bootstrap ─────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', async () => {
  updateDateLabel();
  await loadSettings();
  await loadCurrencies();
  await refreshDashboard();
  await loadSubscriptions();
  closeSidebar();
});

// ── Navigation ────────────────────────────────────────────────────────
function navigate(section) {
  // Hide all sections
  document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
  document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
  document.querySelectorAll('.mob-nav-btn').forEach(b => b.classList.remove('active'));

  // Show target section
  document.getElementById('section-' + section).classList.add('active');
  const nb = document.getElementById('nav-' + section);
  const mb = document.getElementById('mob-' + section);
  if (nb) nb.classList.add('active');
  if (mb) mb.classList.add('active');

  state.currentSection = section;
  closeSidebar();

  // Lazy load section data
  if (section === 'converter') loadRates();
  if (section === 'settings') populateSettingsForm();
}

// ── Sidebar (mobile) ──────────────────────────────────────────────────
function toggleSidebar() {
  const sidebar = document.getElementById('sidebar');
  const overlay = document.getElementById('sidebarOverlay');
  sidebar.classList.toggle('open');
  overlay.classList.toggle('visible');
}

function closeSidebar() {
  document.getElementById('sidebar').classList.remove('open');
  document.getElementById('sidebarOverlay').classList.remove('visible');
}

document.getElementById('sidebarOverlay').addEventListener('click', closeSidebar);

// ── Date label ────────────────────────────────────────────────────────
function updateDateLabel() {
  const el = document.getElementById('dash-date');
  if (el) {
    const now = new Date();
    el.textContent = now.toLocaleDateString('en-IN', {
      weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
    });
  }
}

// ── API helpers ───────────────────────────────────────────────────────
async function api(method, path, body = null) {
  const opts = {
    method,
    headers: { 'Content-Type': 'application/json' }
  };
  if (body) opts.body = JSON.stringify(body);
  const res = await fetch(path, opts);
  if (!res.ok) {
    const err = await res.json().catch(() => ({ error: 'Request failed' }));
    throw new Error(err.error || 'Request failed');
  }
  return res.json();
}

// ── Settings ──────────────────────────────────────────────────────────
async function loadSettings() {
  try {
    state.settings = await api('GET', '/api/settings');
  } catch (e) {
    console.error('Settings load failed:', e);
  }
}

function populateSettingsForm() {
  const homeSel = document.getElementById('s-home-currency');
  const budgetInp = document.getElementById('s-budget');
  if (homeSel) homeSel.value = state.settings.home_currency || 'INR';
  if (budgetInp) budgetInp.value = state.settings.budget_limit || '5000';
}

async function saveHomeCurrency() {
  const val = document.getElementById('s-home-currency').value;
  try {
    await api('POST', '/api/settings', { key: 'home_currency', value: val });
    state.settings.home_currency = val;
    toast('Home currency saved to ' + val, 'success');
    await refreshDashboard();
    await loadSubscriptions();
  } catch (e) { toast(e.message, 'error'); }
}

async function saveBudget() {
  const val = document.getElementById('s-budget').value;
  if (!val || isNaN(val) || Number(val) <= 0) {
    toast('Enter a valid budget amount', 'error');
    return;
  }
  try {
    await api('POST', '/api/settings', { key: 'budget_limit', value: val });
    state.settings.budget_limit = val;
    toast('Budget limit saved: ' + sym(state.settings.home_currency) + Number(val).toLocaleString(), 'success');
    await refreshDashboard();
  } catch (e) { toast(e.message, 'error'); }
}

// ── Load currencies list ──────────────────────────────────────────────
async function loadCurrencies() {
  try {
    state.currencies = await api('GET', '/api/currency/list');
    populateCurrencyDropdowns();
  } catch (e) {
    console.error('Currency list failed:', e);
    state.currencies = ['INR','USD','EUR','GBP','JPY'];
    populateCurrencyDropdowns();
  }
}

const CURRENCY_NAMES = {
  INR:'Indian Rupee', USD:'US Dollar', EUR:'Euro', GBP:'British Pound',
  JPY:'Japanese Yen', CNY:'Chinese Yuan', AUD:'Australian Dollar',
  CAD:'Canadian Dollar', CHF:'Swiss Franc', KRW:'South Korean Won',
  SGD:'Singapore Dollar', HKD:'Hong Kong Dollar', MYR:'Malaysian Ringgit',
  THB:'Thai Baht', IDR:'Indonesian Rupiah', PHP:'Philippine Peso',
  BRL:'Brazilian Real', MXN:'Mexican Peso', ZAR:'South African Rand',
  TRY:'Turkish Lira', SEK:'Swedish Krona', NOK:'Norwegian Krone',
  DKK:'Danish Krone', NZD:'New Zealand Dollar', HUF:'Hungarian Forint',
  CZK:'Czech Koruna', PLN:'Polish Zloty', RON:'Romanian Leu',
  BGN:'Bulgarian Lev', ILS:'Israeli Shekel', ISK:'Icelandic Krona'
};

function populateCurrencyDropdowns() {
  const selects = ['f-currency','conv-from','conv-to','rates-base','s-home-currency'];
  const home = state.settings.home_currency || 'INR';

  selects.forEach(id => {
    const el = document.getElementById(id);
    if (!el) return;
    el.innerHTML = '';
    state.currencies.forEach(c => {
      const opt = document.createElement('option');
      opt.value = c;
      opt.textContent = c + (CURRENCY_NAMES[c] ? ' – ' + CURRENCY_NAMES[c] : '');
      el.appendChild(opt);
    });
  });

  // Set sensible defaults
  const fCur = document.getElementById('f-currency');
  if (fCur) fCur.value = home;

  const convFrom = document.getElementById('conv-from');
  if (convFrom) convFrom.value = 'USD';
  const convTo = document.getElementById('conv-to');
  if (convTo) convTo.value = home;
  const ratesBase = document.getElementById('rates-base');
  if (ratesBase) ratesBase.value = home;
  const sHome = document.getElementById('s-home-currency');
  if (sHome) sHome.value = home;
}

// ── Dashboard ─────────────────────────────────────────────────────────
async function refreshDashboard() {
  const home = state.settings.home_currency || 'INR';
  try {
    // Parallel fetch
    const [totalData, subs, upcoming, chartData] = await Promise.all([
      api('GET', `/api/subscriptions/total?homeCurrency=${home}`),
      api('GET', '/api/subscriptions'),
      api('GET', '/api/subscriptions/upcoming'),
      api('GET', `/api/subscriptions/chart?homeCurrency=${home}`)
    ]);

    state.subscriptions = subs;

    // Stat: total
    const totalEl = document.getElementById('stat-total');
    if (totalEl) {
      totalEl.textContent = sym(home) + Number(totalData.total).toLocaleString('en-IN', { minimumFractionDigits: 2 });
      totalEl.className = 'stat-value ' + (totalData.overBudget ? 'danger' : '');
    }

    // Stat: budget
    const budgetEl = document.getElementById('stat-budget');
    const budgetStatus = document.getElementById('stat-budget-status');
    if (budgetEl) {
      budgetEl.textContent = sym(home) + Number(totalData.budgetLimit).toLocaleString('en-IN');
      budgetEl.className = 'stat-value ' + (totalData.overBudget ? 'danger' : 'success');
    }
    if (budgetStatus) {
      budgetStatus.textContent = totalData.overBudget ? '⚠️ Over budget!' : '✅ Within budget';
      budgetStatus.className = 'stat-hint ' + (totalData.overBudget ? 'danger' : 'success');
    }

    // Stat: count
    const countEl = document.getElementById('stat-count');
    if (countEl) countEl.textContent = subs.length;

    // Stat: upcoming
    const upEl = document.getElementById('stat-upcoming');
    if (upEl) upEl.textContent = upcoming.length;

    // Currency hint
    const hintEl = document.getElementById('stat-currency-hint');
    if (hintEl) hintEl.textContent = 'monthly equivalent in ' + home;

    // Upcoming list
    renderUpcoming(upcoming, home);

    // Chart
    renderChart(chartData, home);

  } catch (e) {
    console.error('Dashboard refresh failed:', e);
    toast('Dashboard error: ' + e.message, 'error');
  }
}

function renderUpcoming(upcoming, home) {
  const el = document.getElementById('upcoming-list');
  if (!el) return;

  if (upcoming.length === 0) {
    el.innerHTML = `<div class="empty-state"><div class="empty-icon">🎉</div><p>No renewals in the next 7 days</p></div>`;
    return;
  }

  el.innerHTML = upcoming.map(s => `
    <div class="renewal-item">
      <div>
        <div class="renewal-name">${esc(s.name)}</div>
        <div class="renewal-amount">${sym(s.currency)}${s.amount.toFixed(2)} / ${s.billingCycle || 'Monthly'}</div>
      </div>
      <div class="renewal-days">
        ${s.daysUntilRenewal === 0 ? 'Today!' : s.daysUntilRenewal + ' day' + (s.daysUntilRenewal !== 1 ? 's' : '')}
      </div>
    </div>
  `).join('');
}

function renderChart(data, home) {
  const canvas = document.getElementById('spendingChart');
  const emptyEl = document.getElementById('chart-empty');
  if (!canvas) return;

  const labels = Object.keys(data);
  const values = Object.values(data);

  if (labels.length === 0) {
    canvas.style.display = 'none';
    if (emptyEl) emptyEl.style.display = 'block';
    return;
  }

  canvas.style.display = 'block';
  if (emptyEl) emptyEl.style.display = 'none';

  const COLORS = ['#7c3aed','#06b6d4','#10b981','#f59e0b','#ef4444','#8b5cf6','#14b8a6','#f97316'];

  if (state.chart) { state.chart.destroy(); state.chart = null; }

  state.chart = new Chart(canvas, {
    type: 'doughnut',
    data: {
      labels,
      datasets: [{
        data: values,
        backgroundColor: COLORS.slice(0, labels.length),
        borderWidth: 2,
        borderColor: '#1c2128'
      }]
    },
    options: {
      responsive: true,
      plugins: {
        legend: {
          position: 'bottom',
          labels: { color: '#94a3b8', font: { family: 'Inter', size: 12 }, padding: 12 }
        },
        tooltip: {
          callbacks: {
            label: ctx => ` ${sym(home)}${ctx.parsed.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`
          }
        }
      },
      cutout: '60%'
    }
  });
}

// ── Subscriptions ─────────────────────────────────────────────────────
async function loadSubscriptions() {
  try {
    const home = state.settings.home_currency || 'INR';
    const [subs, chartData] = await Promise.all([
      api('GET', '/api/subscriptions'),
      api('GET', `/api/subscriptions/chart?homeCurrency=${home}`)
    ]);
    state.subscriptions = subs;
    renderSubscriptionsTable(subs, home);
  } catch (e) {
    console.error('Load subscriptions failed:', e);
  }
}

function renderSubscriptionsTable(subs, home) {
  const tbody = document.getElementById('subs-tbody');
  const emptyEl = document.getElementById('subs-empty');
  const table = document.getElementById('subs-table');
  if (!tbody) return;

  if (subs.length === 0) {
    tbody.innerHTML = '';
    if (table) table.style.display = 'none';
    if (emptyEl) emptyEl.style.display = 'block';
    return;
  }

  if (table) table.style.display = 'table';
  if (emptyEl) emptyEl.style.display = 'none';

  tbody.innerHTML = subs.map(s => {
    const monthly = computeMonthly(s);
    const days = s.daysUntilRenewal;
    // -999 = no date set; negative = overdue; 0 = today
    const daysText = days === -999 || days == null ? '—' : days < 0 ? 'Overdue' : days === 0 ? 'Today' : days + 'd';
    const daysClass = days !== -999 && days != null && days <= 3 && days >= 0 ? 'danger' : days !== -999 && days != null && days <= 7 && days >= 0 ? 'warning' : '';
    const catClass = 'badge-' + (s.category || 'Others').toLowerCase();

    return `<tr>
      <td data-label="Service"><strong>${esc(s.name)}</strong></td>
      <td data-label="Amount">${sym(s.currency)}${s.amount.toFixed(2)}</td>
      <td data-label="Monthly (Home)">${sym(home)}${monthly.toFixed(2)}</td>
      <td data-label="Category"><span class="badge ${catClass}">${esc(s.category || 'Others')}</span></td>
      <td data-label="Renewal Date">${s.renewalDate || '—'}</td>
      <td data-label="Days Left" class="${daysClass}">${daysText}</td>
      <td data-label="Cycle">${s.billingCycle || 'Monthly'}</td>
      <td data-label="Action">
        <button class="btn btn-danger" style="padding:6px 12px;font-size:.8rem;"
                onclick="deleteSubscription(${s.id}, '${esc(s.name)}')">🗑</button>
      </td>
    </tr>`;
  }).join('');
}

// Monthly equivalent in home currency (client-side approximation for table display)
function computeMonthly(s) {
  let monthly = s.amount;
  if (s.billingCycle === 'Yearly')  monthly = s.amount / 12;
  if (s.billingCycle === 'Weekly')  monthly = s.amount * 4.33;
  // Note: home-currency conversion shown in stat-total (server-side accurate)
  return monthly;
}

// ── Add Subscription ──────────────────────────────────────────────────
function toggleAddForm() {
  const card = document.getElementById('add-form-card');
  if (!card) return;
  const isVisible = card.style.display !== 'none';
  card.style.display = isVisible ? 'none' : 'block';
  if (!isVisible) document.getElementById('f-name').focus();
}

async function addSubscription() {
  const name = document.getElementById('f-name').value.trim();
  const amount = parseFloat(document.getElementById('f-amount').value);
  const currency = document.getElementById('f-currency').value;
  const category = document.getElementById('f-category').value;
  const cycle = document.getElementById('f-cycle').value;
  const date = document.getElementById('f-date').value;
  const errEl = document.getElementById('form-error');

  // Client-side validation
  if (!name) { showFormError('Service name is required'); return; }
  if (!amount || amount <= 0) { showFormError('Enter a valid amount greater than 0'); return; }

  hideFormError();

  try {
    await api('POST', '/api/subscriptions', {
      name, amount, currency, category,
      renewalDate: date || null,
      billingCycle: cycle
    });

    toast('✅ ' + name + ' added successfully!', 'success');
    clearAddForm();
    toggleAddForm();
    await loadSubscriptions();
    await refreshDashboard();
  } catch (e) {
    showFormError(e.message);
  }
}

function showFormError(msg) {
  const el = document.getElementById('form-error');
  if (el) { el.textContent = '⚠️ ' + msg; el.style.display = 'block'; }
}

function hideFormError() {
  const el = document.getElementById('form-error');
  if (el) el.style.display = 'none';
}

function clearAddForm() {
  ['f-name','f-amount','f-date'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.value = '';
  });
  const fCur = document.getElementById('f-currency');
  if (fCur) fCur.value = state.settings.home_currency || 'INR';
}

// ── Delete Subscription ───────────────────────────────────────────────
async function deleteSubscription(id, name) {
  if (!confirm(`Delete "${name}"?`)) return;
  try {
    await api('DELETE', `/api/subscriptions/${id}`);
    toast(`🗑️ ${name} removed`, 'info');
    await loadSubscriptions();
    await refreshDashboard();
  } catch (e) {
    toast('Delete failed: ' + e.message, 'error');
  }
}

// ── Currency Converter ────────────────────────────────────────────────
function swapCurrencies() {
  const from = document.getElementById('conv-from');
  const to   = document.getElementById('conv-to');
  if (!from || !to) return;
  [from.value, to.value] = [to.value, from.value];
}

async function convertCurrency() {
  const from   = document.getElementById('conv-from').value;
  const to     = document.getElementById('conv-to').value;
  const amount = parseFloat(document.getElementById('conv-amount').value);

  if (!amount || amount <= 0) { toast('Enter a valid amount', 'error'); return; }

  try {
    const data = await api('GET', `/api/currency/convert?from=${from}&to=${to}&amount=${amount}`);
    const resultEl  = document.getElementById('conv-result');
    const amountEl  = document.getElementById('conv-result-amount');
    const rateEl    = document.getElementById('conv-result-rate');

    if (resultEl) resultEl.style.display = 'block';
    if (amountEl) amountEl.textContent = sym(to) + data.converted.toLocaleString('en-IN', { minimumFractionDigits: 2 });
    if (rateEl) {
      const rate = data.converted / amount;
      rateEl.textContent = `1 ${from} = ${sym(to)}${rate.toFixed(4)} ${to}`;
    }
  } catch (e) {
    toast('Conversion failed: ' + e.message, 'error');
  }
}

async function loadRates() {
  const baseSel = document.getElementById('rates-base');
  if (!baseSel) return;
  const base = baseSel.value || state.settings.home_currency || 'INR';

  const grid = document.getElementById('rates-grid');
  if (grid) grid.innerHTML = '<div class="spinner"></div>';

  try {
    const data = await api('GET', `/api/currency/rates?base=${base}`);
    const rates = data.rates;

    const ts = document.getElementById('rates-timestamp');
    if (ts) ts.textContent = 'Updated: ' + new Date().toLocaleTimeString();

    if (grid) {
      grid.innerHTML = Object.entries(rates)
        .filter(([c]) => c !== base)
        .sort(([a], [b]) => a.localeCompare(b))
        .map(([c, r]) => `
          <div class="rate-tile">
            <div class="rtc">${c}</div>
            <div class="rtv">${sym(c)}${r < 1 ? r.toFixed(4) : r.toFixed(2)}</div>
            <div class="rtc" style="font-size:.65rem;margin-top:2px;">per 1 ${base}</div>
          </div>
        `).join('');
    }
  } catch (e) {
    if (grid) grid.innerHTML = `<p class="text-muted text-sm">⚠️ Live rates unavailable. Check internet connection.</p>`;
  }
}

// ── Toast notification ────────────────────────────────────────────────
let toastTimer = null;

function toast(msg, type = 'info') {
  const el = document.getElementById('toast');
  if (!el) return;

  el.textContent = msg;
  el.className = 'show ' + type;

  if (toastTimer) clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { el.className = ''; }, 3200);
}

// ── Utility ───────────────────────────────────────────────────────────
function esc(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g,'&amp;')
    .replace(/</g,'&lt;')
    .replace(/>/g,'&gt;')
    .replace(/"/g,'&quot;');
}
