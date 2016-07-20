import React, {PropTypes} from 'react';
import './Path'; // must import Path first, don`t know why. without this will throw an `Super expression must either be null or a function, not undefined`
import createReactNativeComponentClass from 'react/lib/createReactNativeComponentClass';
import mergeContext from '../lib/mergeContext';
import {rectProps, pathProps, fillProps, strokeProps, numberProp} from '../lib/props';
import {RectAttributes} from '../lib/attributes';
import Shape from './Shape';

class Rect extends Shape {
    static displayName = 'Rect';

    static propTypes = {
        ...pathProps,
        ...rectProps
    };

    static defaultProps = {
        x: 0,
        y: 0,
        width: 0,
        height: 0,
        rx: 0,
        ry: 0
    };

    setNativeProps = (...args) => {
        this.root.setNativeProps(...args);
    };

    render() {
        let props = mergeContext(this.props, this.context);

        return <RNSVGRect
            ref={ele => this.root = ele}
            {...this.extractProps({
                ...props,
                x: null,
                y: null
            })}
            x={props.x.toString()}
            y={props.y.toString()}
            width={props.width.toString()}
            height={props.height.toString()}
            rx={props.rx.toString()}
            ry={props.ry.toString()}
        />;
    }
}

const RNSVGRect = createReactNativeComponentClass({
    validAttributes: RectAttributes,
    uiViewClassName: 'RNSVGRect'
});

export default Rect;
