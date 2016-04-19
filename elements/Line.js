import React, {
    Component,
    PropTypes,
    ReactNativeBaseComponent
} from 'react-native';
import Path from './Path';

let propType = PropTypes.oneOfType([PropTypes.string, PropTypes.number]);

class Line extends Component{
    static displayName = 'Line';
    static propTypes = {
        x1: propType,
        x2: propType,
        y1: propType,
        y2: propType,
        strokeLinecap: PropTypes.oneOf(['butt', 'square', 'round']),
        strokeCap: PropTypes.oneOf(['butt', 'square', 'round'])
    };


    _convertPath = (props = this.props) => {
        return `M${props.x1},${props.y1}L${props.x2},${props.y2}Z`;
    };

    render() {
        return <Path
            {...this.props}
            ref="shape"
            d={this._convertPath()}
        />;
    }
}

export default Line;
