import React, {PropTypes, Component} from 'react';
import './Path'; // must import Path first, don`t know why. without this will throw an `Super expression must either be null or a function, not undefined`
import extractProps from '../lib/extract/extractProps';
import {RenderableAttributes} from '../lib/attributes';
import {requireNativeComponent} from 'react-native';
import mergeContext from '../lib/mergeContext';
import {rectProps, pathProps, fillProps, strokeProps} from '../lib/props';

class Rect extends Component{
    static displayName = 'Rect';
    static propTypes = {
        ...pathProps,
        ...rectProps
    };

    static contextTypes = {
        ...fillProps,
        ...strokeProps,
        ...rectProps,
        isInGroup: PropTypes.bool
    };

    render() {
        let props = mergeContext(this.props, this.context);

        return <RNSVGRect
            {...extractProps({
                ...props,
                x: null,
                y: null
            })}
            x={props.x.toString()}
            y={props.y.toString()}
            width={props.width.toString()}
            height={props.height.toString()}
            rx={props.rx ? props.rx.toString() : '0'}
            ry={props.ry ? props.ry.toString() : '0'}
        />;
    }
}

const RNSVGRect = requireNativeComponent('RNSVGRect', null);

export default Rect;
