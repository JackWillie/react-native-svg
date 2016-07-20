import React, {PropTypes} from 'react';
import createReactNativeComponentClass from 'react/lib/createReactNativeComponentClass';
import Defs from './Defs';
import extractProps from '../lib/extract/extractProps';
import extractText from '../lib/extract/extractText';
import mergeContext from '../lib/mergeContext';
import {numberProp, textProps, fillProps, strokeProps, pathProps} from '../lib/props';
import {TextAttributes} from '../lib/attributes';
import Shape from './Shape';

class Text extends Shape {
    static displayName = 'Text';

    static propTypes = {
        dx: numberProp,
        dy: numberProp,
        ...textProps,
        ...pathProps
    };

    static defaultProps = {
        dx: 0,
        dy: 0
    };

    setNativeProps = (...args) => {
        this.root.setNativeProps(...args);
    };

    render() {
        let props = mergeContext(this.props, this.context);

        let x = 0;
        if (props.x) {
            x = props.dx ? +props.x + (+props.dx) : +props.x;
        }
        let y = 0;
        if (props.y) {
            y = props.dy ? +props.y + (+props.dy) : +props.y;
        }

        return (
            <RNSVGText
                ref={ele => this.root = ele}
                {...extractProps({...props, x, y})}
                {...extractText(props)}
            />
        );
    }
}

const RNSVGText = createReactNativeComponentClass({
    validAttributes: TextAttributes,
    uiViewClassName: 'RNSVGText'
});

export default Text;
