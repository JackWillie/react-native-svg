import extractColor from './extractColor';

const urlIdPattern = /^url\(#(.+?)\)$/;

export default function extractBrush(colorOrBrush) {
  if (!colorOrBrush || colorOrBrush === 'none') {
    return null;
  }

  if (colorOrBrush === 'currentColor') {
    return [2];
  }
  try {
    const matched = typeof colorOrBrush === 'string' && colorOrBrush.match(urlIdPattern);
    // brush
    if (matched) {
      return [1, matched[1]];
    } else {
      // solid color
      const color = extractColor(colorOrBrush);
      const r = color[0];
      const g = color[1];
      const b = color[2];
      const a = color[3];
      return [0, r, g, b, a === undefined ? 1 : a];
    }
  } catch (err) {
    console.warn(`"${colorOrBrush}" is not a valid color or brush`);
    return null;
  }
}
