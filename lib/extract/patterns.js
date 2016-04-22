import rgba from '../rgba';
let patterns = {};
let patternReg = /^url\(#(\w+?)\)$/;

import {LinearGradientGenerator} from '../../elements/LinearGradient';
import {RadialGradientGenerator} from '../../elements/RadialGradient';


function isGradient(obj) {
    return obj instanceof LinearGradientGenerator || obj instanceof RadialGradientGenerator;
}

function set(id, pattern) {
    patterns[id] = pattern;
}

function remove(id) {
    delete patterns[id];
}

export {
    set,
    remove
}

export default function(patternSting, opacity, dimensions, svgId) {
    if (isGradient(patternSting)) {
        return patternSting;
    }

    if (isNaN(opacity)) {
        opacity = 1;
    }

    // 尝试匹配 "url(#pattern)"
    let matched = patternSting.match(patternReg);

    if (matched) {
        let patternName = `${matched[1]}:${svgId}`;
        let pattern = patterns[patternName];

        if (pattern) {
            if (pattern.length === 2) {
                return pattern(dimensions, opacity);
            } else {
                return pattern(opacity);
            }
        }
        return null;
    }

    return rgba(patternSting, opacity).rgbaString();
}
