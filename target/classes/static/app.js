// ─── API Helper ──────────────────────────────────────────
async function api(path, opts = {}) {
  try {
    const res = await fetch(path, { ...opts, headers: { 'Content-Type': 'application/json', ...opts.headers } });
    if (res.status === 401) {
      if (path === '/api/auth/me') return null;
      showLogin(); return null;
    }
    if (path.includes('/export')) return res;
    const text = await res.text();
    return text ? JSON.parse(text) : null;
  } catch (e) { console.error('API error:', path, e); return null; }
}

let currentUser = null;
let currentPage = 'dashboard';

async function init() {
  const data = await api('/api/auth/me');
  if (data && data.name) { currentUser = data; showApp(); }
  else showLogin();
}

// ─── Login Page ──────────────────────────────────────────
function showLogin() {
  currentUser = null;
  document.getElementById('app').innerHTML = `
    <div class="min-h-screen bg-gradient-to-br from-indigo-500 via-purple-500 to-pink-500 flex items-center justify-center p-4">
      <div class="w-full max-w-md">
        <div class="text-center mb-8">
          <div class="inline-flex items-center justify-center w-16 h-16 bg-white rounded-2xl shadow-lg mb-4 text-3xl">📚</div>
          <h1 class="text-3xl font-bold text-white">Homework Alert</h1>
          <p class="text-white/80 mt-1">Google Sheets compatible homework manager</p>
        </div>
        <div class="bg-white rounded-2xl shadow-2xl p-8">
          <div class="flex mb-6 border-b">
            <button onclick="showTab('login')" id="tabLogin" class="flex-1 pb-2 text-sm font-medium border-b-2 border-indigo-600 text-indigo-600">Sign In</button>
            <button onclick="showTab('register')" id="tabRegister" class="flex-1 pb-2 text-sm font-medium border-b-2 border-transparent text-gray-400">Register</button>
          </div>
          <div id="authError" class="hidden mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded-lg text-sm"></div>
          <div id="authSuccess" class="hidden mb-4 p-3 bg-emerald-50 border border-emerald-200 text-emerald-700 rounded-lg text-sm"></div>
          <form id="loginForm" onsubmit="doLogin(event)" class="space-y-4">
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
              <input name="username" type="email" required class="w-full px-4 py-2.5 border rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none" placeholder="you@school.com"></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Password</label>
              <input name="password" type="password" required class="w-full px-4 py-2.5 border rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none"></div>
            <button type="submit" class="w-full py-2.5 bg-indigo-600 text-white rounded-lg font-medium hover:bg-indigo-700">Sign In</button>
          </form>
          <form id="registerForm" onsubmit="doRegister(event)" class="space-y-4 hidden">
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Role</label>
              <select name="role" onchange="toggleRegFields(this.value)" class="w-full px-4 py-2.5 border rounded-lg">
                <option value="STUDENT">Student</option><option value="TEACHER">Teacher</option><option value="PARENT">Parent</option>
              </select></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Full Name</label>
              <input name="name" type="text" required class="w-full px-4 py-2.5 border rounded-lg"></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
              <input name="email" type="email" required class="w-full px-4 py-2.5 border rounded-lg"></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Password</label>
              <input name="password" type="password" required minlength="6" class="w-full px-4 py-2.5 border rounded-lg"></div>
            <div id="regClass"><label class="block text-sm font-medium text-gray-700 mb-1">Class</label>
              <input name="className" type="text" class="w-full px-4 py-2.5 border rounded-lg" placeholder="e.g. Class 8-A"></div>
            <div id="regStudent" class="hidden"><label class="block text-sm font-medium text-gray-700 mb-1">Child's Email</label>
              <input name="studentEmail" type="email" class="w-full px-4 py-2.5 border rounded-lg" placeholder="child@school.com"></div>
            <button type="submit" class="w-full py-2.5 bg-indigo-600 text-white rounded-lg font-medium hover:bg-indigo-700">Create Account</button>
          </form>
          <div class="mt-6 pt-6 border-t">
            <p class="text-xs text-gray-400 mb-3 text-center">Demo accounts (password: password123)</p>
            <div class="grid gap-2 text-sm">
              <button onclick="fillDemo('admin@school.com')" class="text-left px-3 py-2 rounded-lg border hover:bg-gray-50">👑 <b>Admin</b> <span class="text-gray-400">admin@school.com</span></button>
              <button onclick="fillDemo('math.teacher@school.com')" class="text-left px-3 py-2 rounded-lg border hover:bg-gray-50">👩‍🏫 Math Teacher <span class="text-gray-400">math.teacher@school.com</span></button>
              <button onclick="fillDemo('english.teacher@school.com')" class="text-left px-3 py-2 rounded-lg border hover:bg-gray-50">👩‍🏫 English Teacher <span class="text-gray-400">english.teacher@school.com</span></button>
              <button onclick="fillDemo('aarav@school.com')" class="text-left px-3 py-2 rounded-lg border hover:bg-gray-50">🧑‍🎓 Student (8-A) <span class="text-gray-400">aarav@school.com</span></button>
              <button onclick="fillDemo('rohan@school.com')" class="text-left px-3 py-2 rounded-lg border hover:bg-gray-50">🧑‍🎓 Student (9-A) <span class="text-gray-400">rohan@school.com</span></button>
              <button onclick="fillDemo('parent.patel@email.com')" class="text-left px-3 py-2 rounded-lg border hover:bg-gray-50">👨‍👩‍👦 Parent (8-A) <span class="text-gray-400">parent.patel@email.com</span></button>
              <button onclick="fillDemo('parent.mehta@email.com')" class="text-left px-3 py-2 rounded-lg border hover:bg-gray-50">👨‍👩‍👦 Parent (9-A) <span class="text-gray-400">parent.mehta@email.com</span></button>
            </div>
          </div>
        </div>
      </div>
    </div>`;
}

function showTab(t) {
  document.getElementById('loginForm').classList.toggle('hidden', t !== 'login');
  document.getElementById('registerForm').classList.toggle('hidden', t !== 'register');
  document.getElementById('tabLogin').className = 'flex-1 pb-2 text-sm font-medium border-b-2 ' + (t === 'login' ? 'border-indigo-600 text-indigo-600' : 'border-transparent text-gray-400');
  document.getElementById('tabRegister').className = 'flex-1 pb-2 text-sm font-medium border-b-2 ' + (t === 'register' ? 'border-indigo-600 text-indigo-600' : 'border-transparent text-gray-400');
}
function toggleRegFields(r) {
  document.getElementById('regClass').classList.toggle('hidden', r !== 'STUDENT');
  document.getElementById('regStudent').classList.toggle('hidden', r !== 'PARENT');
}
function fillDemo(e) { document.getElementById('loginForm').username.value = e; document.getElementById('loginForm').password.value = 'password123'; }

async function doLogin(e) {
  e.preventDefault(); const f = e.target;
  const res = await fetch('/api/auth/login', { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: `username=${encodeURIComponent(f.username.value)}&password=${encodeURIComponent(f.password.value)}` });
  if (res.ok) { init(); } else {
    document.getElementById('authError').textContent = 'Invalid email or password';
    document.getElementById('authError').classList.remove('hidden');
  }
}
async function doRegister(e) {
  e.preventDefault(); const f = e.target;
  const body = { name: f.name.value, email: f.email.value, password: f.password.value, role: f.role.value };
  if (f.className?.value) body.className = f.className.value;
  if (f.studentEmail?.value) body.studentEmail = f.studentEmail.value;
  const data = await api('/api/auth/register', { method: 'POST', body: JSON.stringify(body) });
  if (data?.success) {
    document.getElementById('authSuccess').textContent = 'Account created! Please sign in.';
    document.getElementById('authSuccess').classList.remove('hidden'); showTab('login');
  } else {
    document.getElementById('authError').textContent = data?.error || 'Registration failed';
    document.getElementById('authError').classList.remove('hidden');
  }
}

// ─── App Shell ───────────────────────────────────────────
const roleColors = { ADMIN: 'bg-rose-700', TEACHER: 'bg-indigo-700', STUDENT: 'bg-emerald-700', PARENT: 'bg-amber-700' };
const roleNav = {
  ADMIN:   [['dashboard','🏠 Dashboard'],['subjects','📖 All Subjects'],['homework','📋 All Homework'],['notifications','🔔 Notifications']],
  TEACHER: [['dashboard','🏠 Dashboard'],['subjects','📖 My Subjects'],['homework','📋 My Homework'],['notifications','🔔 Notifications']],
  STUDENT: [['dashboard','🏠 Dashboard'],['homework','📋 My Homework'],['notifications','🔔 Notifications']],
  PARENT:  [['dashboard','🏠 Dashboard'],['homework',"📋 Child's Homework"],['notifications','🔔 Notifications']],
};

function showApp() {
  const nav = roleNav[currentUser.role] || [];
  const roleBadge = currentUser.role === 'ADMIN' ? '👑 Master Admin' : currentUser.role;
  document.getElementById('app').innerHTML = `
    <div class="flex min-h-screen">
      <aside class="hidden lg:block w-64 ${roleColors[currentUser.role]} text-white fixed h-full z-40">
        <div class="p-6">
          <h1 class="text-xl font-bold mb-8">📚 Homework Alert</h1>
          <div class="mb-8 p-3 bg-white/10 rounded-lg">
            <p class="font-semibold">${esc(currentUser.name)}</p>
            <span class="inline-block mt-1 px-2 py-0.5 rounded text-xs font-medium bg-white/20">${roleBadge}</span>
          </div>
          <nav id="navLinks" class="space-y-1">
            ${nav.map(([p,l]) => `<a href="#" onclick="navigate('${p}');return false" data-page="${p}" class="flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors hover:bg-white/10">${l}</a>`).join('')}
          </nav>
        </div>
        <div class="absolute bottom-0 left-0 right-0 p-6">
          <button onclick="doLogout()" class="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-white/10 w-full text-left">🚪 Logout</button>
        </div>
      </aside>
      <main class="lg:ml-64 flex-1 p-4 lg:p-6"><div id="pageContent"></div></main>
    </div>`;
  navigate(currentPage);
}

function navigate(page) {
  currentPage = page;
  document.querySelectorAll('#navLinks a').forEach(a => {
    a.classList.toggle('bg-white/20', a.dataset.page === page);
    a.classList.toggle('font-semibold', a.dataset.page === page);
  });
  if (page === 'dashboard') loadDashboard();
  else if (page === 'subjects') loadSubjects();
  else if (page === 'homework') loadHomework();
  else if (page === 'notifications') loadNotifications();
}

async function doLogout() { await fetch('/api/auth/logout', { method: 'POST' }); showLogin(); }
function esc(s) { const d = document.createElement('div'); d.textContent = s; return d.innerHTML; }
function statCard(icon, label, value, color) {
  const bg = {indigo:'bg-indigo-50',emerald:'bg-emerald-50',amber:'bg-amber-50',red:'bg-red-50',rose:'bg-rose-50'}[color]||'bg-gray-50';
  return `<div class="bg-white rounded-xl p-5 shadow-sm border"><div class="flex items-center gap-3">
    <div class="p-2.5 rounded-lg ${bg} text-xl">${icon}</div>
    <div><p class="text-2xl font-bold">${value}</p><p class="text-sm text-gray-500">${label}</p></div></div></div>`;
}
const isAdminOrTeacher = () => currentUser.role === 'ADMIN' || currentUser.role === 'TEACHER';

// ─── Dashboard ───────────────────────────────────────────
async function loadDashboard() {
  const data = await api('/api/dashboard'); if (!data) return;
  const el = document.getElementById('pageContent');

  if (currentUser.role === 'ADMIN') {
    el.innerHTML = `
      <h1 class="text-2xl font-bold text-gray-800 mb-6">👑 Admin Dashboard</h1>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        ${statCard('📖','All Subjects',data.totalSubjects,'rose')}
        ${statCard('📋','All Homework',data.totalHomework,'indigo')}
        ${statCard('👩‍🏫','Teachers',data.totalTeachers,'emerald')}
        ${statCard('⏰','Due Today',data.dueToday,'amber')}
      </div>
      <div class="bg-white rounded-xl shadow-sm border p-5">
        <div class="flex items-center justify-between mb-4">
          <h2 class="font-semibold text-gray-800">Recent Homework (All Teachers)</h2>
          <a href="/api/homework/export" class="text-sm text-indigo-600 hover:underline">📊 Export All CSV</a>
        </div>
        ${(data.recentHomework||[]).length ? data.recentHomework.map(hw => `
          <div class="flex items-center justify-between p-3 bg-gray-50 rounded-lg mb-2">
            <div><p class="font-medium text-gray-800">${esc(hw.title)}</p>
              <p class="text-sm text-gray-500">${esc(hw.subjectName)} · ${esc(hw.bookName)}, Pages ${hw.pageRange}
              <span class="text-xs text-gray-400 ml-2">by ${esc(hw.teacherName)}</span></p></div>
            <span class="text-sm text-gray-400">Due: ${hw.dueDate}</span>
          </div>`).join('') : '<p class="text-gray-400 text-sm">No homework yet</p>'}
      </div>`;
  } else if (currentUser.role === 'TEACHER') {
    el.innerHTML = `
      <h1 class="text-2xl font-bold text-gray-800 mb-6">Welcome, ${esc(currentUser.name)}</h1>
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
        ${statCard('📖','My Subjects',data.totalSubjects,'indigo')}
        ${statCard('📋','My Homework',data.totalHomework,'emerald')}
        ${statCard('⏰','Due Today',data.dueToday,'amber')}
      </div>
      <div class="bg-white rounded-xl shadow-sm border p-5">
        <div class="flex items-center justify-between mb-4">
          <h2 class="font-semibold text-gray-800">My Recent Homework</h2>
          <a href="/api/homework/export" class="text-sm text-indigo-600 hover:underline">📊 Export CSV</a>
        </div>
        ${(data.recentHomework||[]).length ? data.recentHomework.map(hw => `
          <div class="flex items-center justify-between p-3 bg-gray-50 rounded-lg mb-2">
            <div><p class="font-medium text-gray-800">${esc(hw.title)}</p>
              <p class="text-sm text-gray-500">${esc(hw.subjectName)} · ${esc(hw.bookName)}, Pages ${hw.pageRange}</p></div>
            <span class="text-sm text-gray-400">Due: ${hw.dueDate}</span>
          </div>`).join('') : '<p class="text-gray-400 text-sm">No homework yet</p>'}
      </div>`;
  } else if (currentUser.role === 'STUDENT') {
    el.innerHTML = `<h1 class="text-2xl font-bold text-gray-800 mb-6">Welcome, ${esc(currentUser.name)}</h1>
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">${statCard('⏰','Pending',data.totalPending,'amber')}${statCard('✅','Completed',data.totalCompleted,'emerald')}${statCard('⚠️','Due Today',data.dueToday,'red')}</div>`;
  } else {
    el.innerHTML = `<h1 class="text-2xl font-bold text-gray-800 mb-6">Welcome, ${esc(currentUser.name)}</h1>
      ${data.childName ? `<div class="bg-white rounded-xl shadow-sm border p-5"><h2 class="font-semibold text-gray-800 mb-4">👨‍👩‍👦 ${esc(data.childName)} ${data.childClass ? `<span class="text-sm font-normal text-gray-400">(${esc(data.childClass)})</span>` : ''}</h2>
        <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div class="bg-amber-50 rounded-lg p-4"><p class="text-2xl font-bold text-amber-700">${data.childPending}</p><p class="text-sm text-amber-600">Pending</p></div>
          <div class="bg-emerald-50 rounded-lg p-4"><p class="text-2xl font-bold text-emerald-700">${data.childCompleted}</p><p class="text-sm text-emerald-600">Completed</p></div>
          <div class="bg-red-50 rounded-lg p-4"><p class="text-2xl font-bold text-red-700">${data.childDueToday}</p><p class="text-sm text-red-600">Due Today</p></div>
        </div></div>` : '<p class="text-gray-500">No linked student.</p>'}`;
  }
}

// ─── Subjects ────────────────────────────────────────────
async function loadSubjects() {
  const subjects = await api('/api/subjects'); if (!subjects) return;
  const isAdmin = currentUser.role === 'ADMIN';

  document.getElementById('pageContent').innerHTML = `
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-800">${isAdmin ? 'All Subjects' : 'My Subjects'}</h1>
      ${isAdminOrTeacher() ? '<button onclick="showCreateSubject()" class="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700">➕ Add Subject</button>' : ''}
    </div>
    <div id="subjectMsg"></div>
    ${subjects.length === 0 ? '<div class="bg-white rounded-xl shadow-sm border p-8 text-center"><p class="text-4xl mb-3">📖</p><p class="text-gray-500">No subjects found.</p></div>' : ''}
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      ${subjects.map(s => `<div class="bg-white rounded-xl shadow-sm border p-5 hover:shadow-md">
        <div class="flex items-start justify-between">
          <div>
            <h3 class="font-semibold text-gray-800 text-lg">${esc(s.name)}</h3>
            <p class="text-sm text-gray-500 mt-1">${esc(s.className)}</p>
            ${isAdmin ? `<p class="text-xs text-gray-400 mt-1">👩‍🏫 ${esc(s.teacherName)}</p>` : ''}
          </div>
          ${isAdminOrTeacher() ? `<button onclick="deleteSubject(${s.id})" class="text-gray-400 hover:text-red-500 text-sm">🗑️</button>` : ''}
        </div>
      </div>`).join('')}
    </div>
    <div id="subjectModal" class="hidden fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"></div>`;
}

async function showCreateSubject() {
  let teacherOptions = '';
  if (currentUser.role === 'ADMIN') {
    const teachers = await api('/api/teachers');
    teacherOptions = `<div><label class="block text-sm font-medium text-gray-700 mb-1">Assign to Teacher</label>
      <select name="teacherEmail" class="w-full px-4 py-2.5 border rounded-lg">
        ${(teachers||[]).map(t => `<option value="${esc(t.email)}">${esc(t.name)} (${esc(t.email)})</option>`).join('')}
      </select></div>`;
  }
  document.getElementById('subjectModal').classList.remove('hidden');
  document.getElementById('subjectModal').innerHTML = `
    <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md p-6">
      <div class="flex items-center justify-between mb-4"><h2 class="text-lg font-semibold">Create Subject</h2>
        <button onclick="document.getElementById('subjectModal').classList.add('hidden')" class="text-gray-400 text-xl">&times;</button></div>
      <form onsubmit="createSubject(event)" class="space-y-4">
        <div><label class="block text-sm font-medium text-gray-700 mb-1">Subject Name</label>
          <input name="name" required class="w-full px-4 py-2.5 border rounded-lg" placeholder="e.g. Mathematics"></div>
        <div><label class="block text-sm font-medium text-gray-700 mb-1">Class</label>
          <input name="className" required class="w-full px-4 py-2.5 border rounded-lg" placeholder="e.g. Class 8-A"></div>
        ${teacherOptions}
        <button type="submit" class="w-full py-2.5 bg-indigo-600 text-white rounded-lg font-medium hover:bg-indigo-700">Create</button>
      </form>
    </div>`;
}

async function createSubject(e) {
  e.preventDefault(); const f = e.target;
  const body = { name: f.name.value, className: f.className.value };
  if (f.teacherEmail) body.teacherEmail = f.teacherEmail.value;
  await api('/api/subjects', { method: 'POST', body: JSON.stringify(body) });
  loadSubjects();
}

async function deleteSubject(id) {
  if (!confirm('Delete this subject?')) return;
  await api(`/api/subjects/${id}`, { method: 'DELETE' });
  loadSubjects();
}

// ─── Homework ────────────────────────────────────────────
async function loadHomework() {
  const [homework, subjects] = await Promise.all([api('/api/homework'), api('/api/subjects')]);
  if (!homework) return;
  const subjectNames = [...new Set((subjects||[]).map(s => s.name))];
  const today = new Date().toISOString().split('T')[0];
  const isAdmin = currentUser.role === 'ADMIN';

  document.getElementById('pageContent').innerHTML = `
    <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 mb-6">
      <h1 class="text-2xl font-bold text-gray-800">${isAdmin ? 'All Homework' : 'Homework'}</h1>
      <div class="flex items-center gap-2">
        <select id="hwFilter" onchange="filterHomework()" class="px-3 py-2 border rounded-lg text-sm">
          <option value="">All Subjects</option>
          ${subjectNames.map(s => `<option value="${esc(s)}">${esc(s)}</option>`).join('')}
        </select>
        ${isAdminOrTeacher() ? `
          <a href="/api/homework/export" class="px-3 py-2 border rounded-lg text-sm text-gray-600 hover:bg-gray-50">📊 CSV</a>
          <button onclick="showCreateHomework()" class="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700">➕ Assign</button>` : ''}
      </div>
    </div>
    ${currentUser.role === 'PARENT' ? '<div class="bg-blue-50 border border-blue-200 rounded-lg p-3 text-sm text-blue-700 mb-4">📊 View-only access — contact teacher for changes.</div>' : ''}
    <div id="hwList">${renderHomeworkList(homework, today)}</div>
    <div id="hwModal" class="hidden fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"></div>`;
  window._subjects = subjects;
}

function renderHomeworkList(homework, today) {
  if (!homework.length) return '<div class="bg-white rounded-xl shadow-sm border p-8 text-center"><p class="text-4xl mb-3">📋</p><p class="text-gray-500">No homework found.</p></div>';
  const isAdmin = currentUser.role === 'ADMIN';
  return homework.map(hw => {
    const overdue = hw.status !== 'completed' && hw.dueDate < today;
    return `<div class="bg-white rounded-xl shadow-sm border p-5 mb-3 hover:shadow-md ${overdue ? 'border-l-4 border-l-red-400' : ''}">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div class="flex-1">
          <div class="flex items-center gap-2 flex-wrap">
            <h3 class="font-semibold text-gray-800">${esc(hw.title)}</h3>
            <span class="px-2 py-0.5 bg-indigo-100 text-indigo-700 rounded text-xs font-medium">${esc(hw.subjectName)}</span>
            ${hw.className ? `<span class="px-2 py-0.5 bg-gray-100 text-gray-500 rounded text-xs">${esc(hw.className)}</span>` : ''}
            ${isAdmin && hw.teacherName ? `<span class="px-2 py-0.5 bg-purple-100 text-purple-600 rounded text-xs">👩‍🏫 ${esc(hw.teacherName)}</span>` : ''}
          </div>
          ${hw.description ? `<p class="text-sm text-gray-500 mt-1">${esc(hw.description)}</p>` : ''}
          <div class="flex flex-wrap items-center gap-4 mt-2 text-sm text-gray-500">
            <span>📖 ${esc(hw.bookName)}, Pages ${hw.pageRange}</span>
            <span>⏰ Due: ${hw.dueDate}</span>
          </div>
        </div>
        <div class="flex items-center gap-2">
          ${currentUser.role === 'STUDENT' ? `
            <button onclick="toggleStatus(${hw.id},'${hw.status}')" class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm font-medium ${hw.status === 'completed' ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700'}">
              ${hw.status === 'completed' ? '✅ Completed' : '⏰ Mark Done'}</button>` : ''}
          ${currentUser.role === 'PARENT' ? `
            <span class="px-3 py-1.5 rounded-lg text-sm font-medium ${hw.status === 'completed' ? 'bg-emerald-100 text-emerald-700' : overdue ? 'bg-red-100 text-red-700' : 'bg-amber-100 text-amber-700'}">
              ${hw.status === 'completed' ? '✅ Completed' : overdue ? '❌ Overdue' : '⏰ Pending'}</span>` : ''}
          ${isAdminOrTeacher() ? `
            ${hw.totalTracked > 0 ? `<span class="text-sm text-gray-500">${hw.completedCount}/${hw.totalTracked} done</span>` : ''}
            <button onclick="showEditHomework(${hw.id})" class="p-1.5 text-gray-400 hover:text-indigo-500" title="Edit">✏️</button>
            <button onclick="deleteHomework(${hw.id})" class="p-1.5 text-gray-400 hover:text-red-500" title="Delete">🗑️</button>` : ''}
        </div>
      </div>
    </div>`;
  }).join('');
}

async function filterHomework() {
  const subject = document.getElementById('hwFilter').value;
  const homework = await api('/api/homework' + (subject ? '?subject=' + encodeURIComponent(subject) : ''));
  document.getElementById('hwList').innerHTML = renderHomeworkList(homework || [], new Date().toISOString().split('T')[0]);
}

async function toggleStatus(id, current) {
  await api(`/api/homework/${id}/status`, { method: 'POST', body: JSON.stringify({ status: current === 'completed' ? 'pending' : 'completed' }) });
  loadHomework();
}

async function deleteHomework(id) {
  if (!confirm('Delete this homework?')) return;
  await api(`/api/homework/${id}`, { method: 'DELETE' });
  loadHomework();
}

async function showCreateHomework() {
  const subjects = window._subjects || [];
  let teacherField = '';
  if (currentUser.role === 'ADMIN') {
    const teachers = await api('/api/teachers');
    teacherField = `<div><label class="block text-sm font-medium text-gray-700 mb-1">Assign as Teacher</label>
      <select name="teacherEmail" class="w-full px-4 py-2.5 border rounded-lg">
        ${(teachers||[]).map(t => `<option value="${esc(t.email)}">${esc(t.name)}</option>`).join('')}
      </select></div>`;
  }
  document.getElementById('hwModal').classList.remove('hidden');
  document.getElementById('hwModal').innerHTML = `
    <div class="bg-white rounded-2xl shadow-2xl w-full max-w-lg p-6 max-h-[90vh] overflow-y-auto">
      <div class="flex items-center justify-between mb-4"><h2 class="text-lg font-semibold">Assign Homework</h2>
        <button onclick="document.getElementById('hwModal').classList.add('hidden')" class="text-gray-400 text-xl">&times;</button></div>
      <div id="hwFormError" class="hidden mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded-lg text-sm"></div>
      <form onsubmit="createHomework(event)" class="space-y-4">
        <div><label class="block text-sm font-medium text-gray-700 mb-1">Subject</label>
          <select name="subjectName" required class="w-full px-4 py-2.5 border rounded-lg" onchange="var o=this.options[this.selectedIndex]; document.querySelector('[name=className]').value=o.dataset.cls||'';">
            <option value="">Select</option>
            ${subjects.map(s => `<option value="${esc(s.name)}" data-cls="${esc(s.className)}">${esc(s.name)} (${esc(s.className)})</option>`).join('')}
          </select></div>
        <input type="hidden" name="className" value="">
        ${teacherField}
        <div><label class="block text-sm font-medium text-gray-700 mb-1">Title</label>
          <input name="title" required class="w-full px-4 py-2.5 border rounded-lg" placeholder="e.g. Chapter 5 Exercises"></div>
        <div><label class="block text-sm font-medium text-gray-700 mb-1">Description</label>
          <textarea name="description" rows="2" class="w-full px-4 py-2.5 border rounded-lg"></textarea></div>
        <div><label class="block text-sm font-medium text-gray-700 mb-1">Book Name</label>
          <input name="bookName" required class="w-full px-4 py-2.5 border rounded-lg" placeholder="e.g. NCERT Mathematics"></div>
        <div class="grid grid-cols-2 gap-4">
          <div><label class="block text-sm font-medium text-gray-700 mb-1">From Page</label>
            <input name="pageFrom" type="number" min="1" required class="w-full px-4 py-2.5 border rounded-lg"></div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">To Page</label>
            <input name="pageTo" type="number" min="0" class="w-full px-4 py-2.5 border rounded-lg"></div>
        </div>
        <div><label class="block text-sm font-medium text-gray-700 mb-1">Due Date</label>
          <input name="dueDate" type="date" required class="w-full px-4 py-2.5 border rounded-lg"></div>
        <button type="submit" class="w-full py-2.5 bg-indigo-600 text-white rounded-lg font-medium hover:bg-indigo-700">Assign Homework</button>
      </form>
    </div>`;
}

async function createHomework(e) {
  e.preventDefault(); const f = e.target;
  const body = { subjectName: f.subjectName.value, className: f.className.value, title: f.title.value,
    description: f.description.value, bookName: f.bookName.value,
    pageFrom: parseInt(f.pageFrom.value), pageTo: f.pageTo.value ? parseInt(f.pageTo.value) : 0, dueDate: f.dueDate.value };
  if (f.teacherEmail) body.teacherEmail = f.teacherEmail.value;
  const res = await api('/api/homework', { method: 'POST', body: JSON.stringify(body) });
  if (res?.error) {
    document.getElementById('hwFormError').textContent = res.error;
    document.getElementById('hwFormError').classList.remove('hidden');
  } else { loadHomework(); }
}

async function showEditHomework(id) {
  const allHw = await api('/api/homework');
  const hw = (allHw||[]).find(h => h.id === id); if (!hw) return;
  document.getElementById('hwModal').classList.remove('hidden');
  document.getElementById('hwModal').innerHTML = `
    <div class="bg-white rounded-2xl shadow-2xl w-full max-w-lg p-6 max-h-[90vh] overflow-y-auto">
      <div class="flex items-center justify-between mb-4"><h2 class="text-lg font-semibold">Edit Homework</h2>
        <button onclick="document.getElementById('hwModal').classList.add('hidden')" class="text-gray-400 text-xl">&times;</button></div>
      <form onsubmit="editHomework(event,${id})" class="space-y-4">
        <div><label class="block text-sm font-medium text-gray-700 mb-1">Title</label>
          <input name="title" required value="${esc(hw.title)}" class="w-full px-4 py-2.5 border rounded-lg"></div>
        <div><label class="block text-sm font-medium text-gray-700 mb-1">Description</label>
          <textarea name="description" rows="2" class="w-full px-4 py-2.5 border rounded-lg">${esc(hw.description||'')}</textarea></div>
        <div><label class="block text-sm font-medium text-gray-700 mb-1">Book Name</label>
          <input name="bookName" required value="${esc(hw.bookName)}" class="w-full px-4 py-2.5 border rounded-lg"></div>
        <div class="grid grid-cols-2 gap-4">
          <div><label class="block text-sm font-medium text-gray-700 mb-1">From Page</label>
            <input name="pageFrom" type="number" min="1" required value="${hw.pageFrom}" class="w-full px-4 py-2.5 border rounded-lg"></div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">To Page</label>
            <input name="pageTo" type="number" min="0" value="${hw.pageTo}" class="w-full px-4 py-2.5 border rounded-lg"></div>
        </div>
        <div><label class="block text-sm font-medium text-gray-700 mb-1">Due Date</label>
          <input name="dueDate" type="date" required value="${hw.dueDate}" class="w-full px-4 py-2.5 border rounded-lg"></div>
        <button type="submit" class="w-full py-2.5 bg-indigo-600 text-white rounded-lg font-medium hover:bg-indigo-700">Save Changes</button>
      </form>
    </div>`;
}

async function editHomework(e, id) {
  e.preventDefault(); const f = e.target;
  await api(`/api/homework/${id}`, { method: 'PUT', body: JSON.stringify({
    title: f.title.value, description: f.description.value, bookName: f.bookName.value,
    pageFrom: parseInt(f.pageFrom.value), pageTo: f.pageTo.value ? parseInt(f.pageTo.value) : 0, dueDate: f.dueDate.value
  })});
  loadHomework();
}

// ─── Notifications ───────────────────────────────────────
async function loadNotifications() {
  const data = await api('/api/notifications'); if (!data) return;
  document.getElementById('pageContent').innerHTML = `
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-800">Notifications</h1>
      ${data.unreadCount > 0 ? '<button onclick="markAllRead()" class="text-sm text-indigo-600 hover:underline">Mark all read</button>' : ''}
    </div>
    ${data.notifications.length === 0 ? '<div class="bg-white rounded-xl shadow-sm border p-8 text-center"><p class="text-4xl mb-3">🔔</p><p class="text-gray-500">No notifications yet.</p></div>' :
      data.notifications.map(n => `
        <div class="bg-white rounded-xl shadow-sm border p-4 mb-2 ${!n.read ? 'border-l-4 border-l-indigo-400' : ''}">
          <p class="font-medium text-gray-800">${esc(n.title)}</p>
          <p class="text-sm text-gray-500 mt-0.5">${esc(n.message)}</p>
          <p class="text-xs text-gray-400 mt-1">${new Date(n.createdAt).toLocaleString()}</p>
        </div>`).join('')}`;
}
async function markAllRead() { await api('/api/notifications/read-all', { method: 'POST' }); loadNotifications(); }

// ─── Boot ────────────────────────────────────────────────
init();
