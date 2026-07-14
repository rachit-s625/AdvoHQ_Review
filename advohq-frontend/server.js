const express = require('express');
const path = require('path');
const helmet = require('helmet');
const compression = require('compression');

const chat = require('./api/chat');
const sendOtp = require('./api/send-otp');
const verifyOtp = require('./api/verify-otp');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(helmet({
  contentSecurityPolicy: {
    directives: {
      defaultSrc: ["'self'"],
      // cdnjs serves Font Awesome's CSS; unpkg serves html-docx-js
      styleSrc: ["'self'", "'unsafe-inline'", "https://fonts.googleapis.com", "https://cdnjs.cloudflare.com"],
      fontSrc: ["'self'", "https://fonts.gstatic.com", "https://cdnjs.cloudflare.com"],
      // The editor loads pdf.js and mammoth from cdnjs and html-docx-js from
      // unpkg; blocking them broke PDF/DOCX rendering with a silent failure.
      scriptSrc: ["'self'", "'unsafe-inline'", "https://cdnjs.cloudflare.com", "https://unpkg.com"],
      // The app's pages wire up buttons with inline onclick="..." attributes
      // throughout; Helmet's default CSP blocks those via script-src-attr
      // 'none' regardless of script-src, which silently breaks every button.
      scriptSrcAttr: ["'unsafe-inline'"],
      // blob: covers image thumbnails from URL.createObjectURL in the editor
      imgSrc: ["'self'", "data:", "blob:"],
      // login.html/create-account.html call the Java backend directly via
      // fetch(); connect-src 'self' alone silently blocks all of those.
      // pdf.js fetches its cross-origin worker script via XHR (cdnjs) and
      // then runs it as a blob worker.
      connectSrc: ["'self'", "https://advohq-backend.onrender.com", "https://cdnjs.cloudflare.com"],
      workerSrc: ["'self'", "blob:", "https://cdnjs.cloudflare.com"],
    },
  },
  referrerPolicy: { policy: 'strict-origin-when-cross-origin' },
}));

app.use(compression());
app.use(express.json());

// ── API routes (proxied Vercel-style handlers) ──
app.post('/api/chat', chat);
app.post('/api/send-otp', sendOtp);
app.post('/api/verify-otp', verifyOtp);

// ── ALLOWED ROUTES (for safe redirects) ──
const ALLOWED_REDIRECTS = new Set([
  '/',
  '/blog',
  '/contact',
  '/create-account',
  '/editor',
  '/home',
  '/login',
  '/privacy',
  '/schedule',
  '/settings',
  '/signup',
  '/terms'
]);

// ── CLEAN URLS ──
// /schedule.html → redirect to /schedule (internal links keep the .html
// suffix; this is what actually strips it from the address bar, matching
// the old Vercel cleanUrls behavior).
app.get(/^(.+)\.html$/, (req, res, next) => {
  const clean = req.params[0] === '/index' ? '/' : req.params[0];
  const qs = req.url.slice(req.path.length);
  const isSafeQuery = qs === '' || qs.startsWith('?');

  if (ALLOWED_REDIRECTS.has(clean) && isSafeQuery) {
    res.redirect(301, clean + qs);
  } else {
    res.redirect(301, '/');
  }
});

// ── CACHE HEADERS ──
// Pages (extensionless clean URLs, .html, or /) and our own .js (no build
// step or cache-busted filenames, so a long-cached copy would never see
// updates): no-cache, always fresh. Everything else (fonts, images, etc):
// cache for 1 year.
app.use((req, res, next) => {
  const ext = path.extname(req.path);
  if (ext === '' || ext === '.html' || ext === '.js') {
    res.setHeader('Cache-Control', 'no-cache, no-store, must-revalidate');
    res.setHeader('Pragma', 'no-cache');
  } else {
    res.setHeader('Cache-Control', 'public, max-age=31536000, immutable');
  }
  next();
});

// ── STATIC FILES + CLEAN URLS ──
// /terms → terms.html, /privacy → privacy.html, etc.
app.use(express.static(__dirname, {
  index: 'index.html',
  extensions: ['html'],
}));

// ── 404 ──
app.use((req, res) => {
  res.status(404).send('Page not found');
});

app.listen(PORT, () => {
  console.log(`AdvoHQ frontend running at http://localhost:${PORT}`);
});
