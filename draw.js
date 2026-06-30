const fs = require('fs');
const { createCanvas } = require('canvas');

// Play Store Icon (512x512)
const iconCanvas = createCanvas(512, 512);
const icx = iconCanvas.getContext('2d');

icx.fillStyle = '#004D40';
icx.fillRect(0, 0, 512, 512);

// Draw the shield/target
icx.fillStyle = '#FFFFFF';
icx.translate(256, 256);
icx.scale(10, 10);
// Draw simple pause or shield shape
// Instead of complex SVG, let's draw two bold vertical bars to represent "pause" and breaking the scroll
icx.fillRect(-10, -12, 6, 24);
icx.fillRect(4, -12, 6, 24);

const outIcon = fs.createWriteStream('./play_store_icon.png');
const streamIcon = iconCanvas.createPNGStream();
streamIcon.pipe(outIcon);

// Feature Graphic (1024x500)
const featCanvas = createCanvas(1024, 500);
const fcx = featCanvas.getContext('2d');

const grad = fcx.createLinearGradient(0, 0, 1024, 500);
grad.addColorStop(0, '#004D40');
grad.addColorStop(1, '#00251a');
fcx.fillStyle = grad;
fcx.fillRect(0, 0, 1024, 500);

// Circles
fcx.beginPath();
fcx.arc(250, 250, 300, 0, 2 * Math.PI);
fcx.fillStyle = 'rgba(0, 90, 76, 0.8)';
fcx.fill();

fcx.beginPath();
fcx.arc(900, 600, 400, 0, 2 * Math.PI);
fcx.fillStyle = 'rgba(0, 51, 42, 0.6)';
fcx.fill();

// Draw pause symbol
fcx.fillStyle = '#FFFFFF';
fcx.save();
fcx.translate(200, 250);
fcx.scale(8, 8);
fcx.fillRect(-10, -12, 6, 24);
fcx.fillRect(4, -12, 6, 24);
fcx.restore();

fcx.fillStyle = '#FFFFFF';
fcx.font = 'bold 72px sans-serif';
fcx.fillText('Focus Intent', 380, 230);

fcx.fillStyle = '#80cbc4';
fcx.font = '36px sans-serif';
fcx.fillText('Stop Doomscrolling. Reclaim Your Time.', 380, 300);

const outFeat = fs.createWriteStream('./feature_graphic.png');
const streamFeat = featCanvas.createPNGStream();
streamFeat.pipe(outFeat);
outFeat.on('finish', () => console.log('Successfully generated PNGs in /app/release'));
