/**
 * Format potential percentage props
 *
 * convert somet props like those
 *   width="50%"
 *   height="500"
 *
 * to
 *   {
 *      width: {
 *          percentage: true,
 *          value: 0.5
 *      },
 *      height: {
 *          percentage: false,
 *          value: 500
 *      }
 *   }
 *
 *
 */
import _ from 'lodash';
import percentToFloat from './percentToFloat';

function percentageTransform(value) {
    if (typeof value === 'number') {
        return {
            percentage: false,
            value
        };
    }

    let float = percentToFloat(value);

    return {
        percentage: float !== +value,
        value: float
    };
}

export default function (props, list) {
    return _.reduce(list, (prev, name) => {
        if (!_.isNil(props[name])) {
            prev[name] = percentageTransform(props[name]);
        }

        return prev;
    }, {});
}
