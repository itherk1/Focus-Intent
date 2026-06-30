const { Resvg } = require('@resvg/resvg-js');
const fs = require('fs');

const svgIcon = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" width="512" height="512">
  <rect width="512" height="512" fill="#004D40" />
  <g transform="translate(100, 100) scale(13)">
    <path fill="#FFFFFF" d="M6,2v6h0.01L6,8.01 10,12l-4,4 0.01,0.01H6V22h12v-5.99h-0.01L18,16l-4,-4 4,-3.99 -0.01,-0.01H18V2H6zM16,16.5V20H8v-3.5l4,-4 4,4zM12,11l-4,-4V4h8v3.5l-4,4z" />
  </g>
</svg>`;

const svgFeature = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 500" width="1024" height="500">
  <defs>
    <linearGradient id="grad1" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" style="stop-color:#004D40;stop-opacity:1" />
      <stop offset="100%" style="stop-color:#00251a;stop-opacity:1" />
    </linearGradient>
  </defs>
  <rect width="1024" height="500" fill="url(#grad1)" />
  
  <circle cx="250" cy="250" r="300" fill="#005A4C" opacity="0.8"/>
  <circle cx="900" cy="600" r="400" fill="#00332a" opacity="0.6" />

  <g transform="translate(100, 125) scale(10)">
    <path fill="#FFFFFF" d="M6,2v6h0.01L6,8.01 10,12l-4,4 0.01,0.01H6V22h12v-5.99h-0.01L18,16l-4,-4 4,-3.99 -0.01,-0.01H18V2H6zM16,16.5V20H8v-3.5l4,-4 4,4zM12,11l-4,-4V4h8v3.5l-4,4z" />
  </g>

  <text x="380" y="240" fill="#FFFFFF" font-family="sans-serif" font-weight="900" font-size="72">Focus Intent</text>
  <text x="380" y="310" fill="#80cbc4" font-family="sans-serif" font-weight="300" font-size="32">Stop Doomscrolling. Reclaim Your Time.</text>
</svg>`;

const resvgIcon = new Resvg(svgIcon, { fitTo: { mode: 'width', value: 512 } });
fs.writeFileSync('play_store_icon.png', resvgIcon.render().asPng());

const resvgFeature = new Resvg(svgFeature, { fitTo: { mode: 'width', value: 1024 } });
fs.writeFileSync('feature_graphic.png', resvgFeature.render().asPng());

console.log('Generated PNGs with Hourglass');
