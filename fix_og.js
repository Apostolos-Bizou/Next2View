const fs = require('fs');
let c = fs.readFileSync('C:/Users/akage/Next2View/frontend/index.html', 'utf8');
c = c.replace(/\r\n/g, '\n');
c = c.replace(/og-image\.png/g, 'og-image.svg');
c = c.replace(/twitter:image.*og-image\.png/g, 'twitter:image" content="https://www.next2view.com/og-image.svg');
fs.writeFileSync('C:/Users/akage/Next2View/frontend/index.html', c, 'utf8');
console.log('Updated to SVG:', c.includes('og-image.svg'));
