const pg = require('C:\\Users\\akage\\node_modules\\pg');

const c = new pg.Client({
  host: 'next2view-dev-pg.postgres.database.azure.com',
  user: 'next2view_admin',
  database: 'next2view_dev',
  password: process.env.PGPASSWORD,
  port: 5432,
  ssl: { rejectUnauthorized: false }
});

(async () => {
  try {
    await c.connect();
    const r = await c.query("SELECT email, role, mfa_enabled FROM app_user ORDER BY role, email");
    console.table(r.rows);
  } catch (e) {
    console.error('Error:', e.message);
  } finally {
    await c.end();
  }
})();
