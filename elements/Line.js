import React, {PropTypes, Component} from 'react';
import extractProps from '../lib/extract/extractProps';
import {RenderableAttributes} from '../lib/attributes';
import {requireNativeComponent} from 'react-native';
import mergeContext from '../lib/mergeContext';
import {lineProps, pathProps, fillProps, strokeProps} from '../lib/props';

class Line extends Component{
    static displayName = 'Line';
    static propTypes = {
        ...pathProps,
        ...lineProps
    };

    static contextTypes = {
        ...fillProps,
        ...strokeProps,
        ...lineProps,
        isInGroup: PropTypes.bool
    };

    render() {
        let props = mergeContext(this.props, this.context);
        return <RNSVGLine
            {...extractProps(props)}
            x1={props.x1.toString()}
            y1={props.y1.toString()}
            x2={props.x2.toString()}
            y2={props.y2.toString()}
        />;
    }
}

const RNSVGLine = requireNativeComponent('RNSVGLine', null);
export default Line;
